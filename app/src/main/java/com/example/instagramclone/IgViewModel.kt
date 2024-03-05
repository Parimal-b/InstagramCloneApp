package com.example.instagramclone

import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.instagramclone.data.Event
import com.example.instagramclone.data.PostData
import com.example.instagramclone.data.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject

const val USERS = "users"
const val POSTS = "posts"

@HiltViewModel
class IgViewModel @Inject constructor(
    val auth: FirebaseAuth,
    val db: FirebaseFirestore,
    val storage: FirebaseStorage
) : ViewModel() {
    val signedIn = mutableStateOf(false)
    val inProgress = mutableStateOf(false)
    val userData = mutableStateOf<UserData?>(null)

    val refreshPostsProgress = mutableStateOf(false)
    val posts = mutableStateOf<List<PostData>>(listOf())

    init {
        //auth.signOut()
        val currentUser = auth.currentUser
        signedIn.value = currentUser != null
        currentUser?.uid?.let { uid ->
            getUserData(uid)
        }
    }

    val popUpNotification = mutableStateOf<Event<String>?>(null)
    fun onSignUp(
        userName: String,
        email: String,
        pass: String
    ) {
        if (userName.isEmpty() or email.isEmpty() or pass.isEmpty()) {
            handleException(customMessage = "Please Enter all the fields")
            return
        }
        inProgress.value = true
        db.collection(USERS).whereEqualTo("userName", userName).get()
            .addOnSuccessListener { documents ->
                if (documents.size() > 0) {
                    handleException(customMessage = "UserName already exists")
                    inProgress.value = false
                } else {
                    auth.createUserWithEmailAndPassword(email, pass)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                signedIn.value = true
                                createOrUpdateProfile(userName = userName)
                            } else {
                                handleException(task.exception, "Signup Failed")
                            }
                            inProgress.value = false
                        }
                }
            }
            .addOnFailureListener {

            }
    }

    fun onLogin(email: String, pass: String) {
        if (email.isEmpty() or pass.isEmpty()) {
            handleException(customMessage = "Please enter all the details")
            return
        }
        inProgress.value = false
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    signedIn.value = true
                    inProgress.value = false
                    auth.currentUser?.uid?.let { uid ->
                        //handleException(customMessage = "Login Successful")
                        getUserData(uid)
                    }
                } else {
                    handleException(task.exception, "Login Failed")
                    inProgress.value = false
                }
            }
            .addOnFailureListener { exc ->
                handleException(exc, "Login Failed")
                inProgress.value = false
            }
    }

    private fun createOrUpdateProfile(
        name: String? = null,
        userName: String? = null,
        bio: String? = null,
        imageUrl: String? = null
    ) {
        val uid = auth.currentUser?.uid
        val userData = UserData(
            userId = uid,
            name = name ?: userData.value?.name,
            userName = userName ?: userData.value?.userName,
            bio = bio ?: userData.value?.bio,
            imageUrl = imageUrl ?: userData.value?.imageUrl,
            following = userData.value?.following
        )

        uid?.let {
            inProgress.value = true
            db.collection(USERS).document(uid).get().addOnSuccessListener {
                if (it.exists()) {
                    it.reference.update(userData.toMap())
                        .addOnSuccessListener {
                            this.userData.value = userData
                            inProgress.value = false
                        }
                        .addOnFailureListener {
                            handleException(it, "Cannot update user")
                            inProgress.value = false
                        }
                } else {
                    db.collection(USERS).document(uid).set(userData)
                    getUserData(uid)
                    inProgress.value = false
                }
            }
                .addOnFailureListener { exc ->
                    handleException(exc, "Cannot create a user")
                    inProgress.value = false
                }
        }
    }

    private fun getUserData(uid: String) {
        inProgress.value = true
        db.collection(USERS).document(uid).get()
            .addOnSuccessListener {
                val user = it.toObject<UserData>()
                userData.value = user
                inProgress.value = false
                refreshPosts()
                //popUpNotification.value = Event("UserData retrieved successfully")
            }
            .addOnFailureListener { exc ->
                handleException(exc, "Cannot Retrieve UserData")
            }
    }

    fun handleException(exception: Exception? = null, customMessage: String = "") {
        exception?.printStackTrace()
        val errorMsg = exception?.localizedMessage ?: ""
        val message = if (customMessage.isEmpty()) errorMsg else "$customMessage: $errorMsg"
        popUpNotification.value = Event(message)
    }

    fun updateProfileData(name: String, userName: String, bio: String) {
        createOrUpdateProfile(name, userName, bio)
    }

    private fun uploadImage(uri: Uri, onSuccess: (Uri) -> Unit) {
        inProgress.value = false
        val storageRef = storage.reference
        val uuid = UUID.randomUUID()
        val imageRef = storageRef.child("images/$uuid")

        val uploadTask = imageRef.putFile(uri)

        uploadTask
            .addOnSuccessListener {
                val result = it.metadata?.reference?.downloadUrl
                result?.addOnSuccessListener(onSuccess)
            }
            .addOnFailureListener { exc ->

                handleException(exc)
                inProgress.value = false

            }
    }

    fun uploadProfileImage(uri: Uri) {
        uploadImage(uri) {
            createOrUpdateProfile(imageUrl = it.toString())
        }
    }

    fun onLogout() {
        auth.signOut()
        signedIn.value = false
        userData.value = null
        popUpNotification.value = Event("Logged Out")
    }

    fun onNewPost(
        uri: Uri,
        description: String,
        onPostSuccess: () -> Unit
    ) {
        uploadImage(uri) {
            onCreatePost(it, description, onPostSuccess)
        }
    }

    private fun onCreatePost(imageUri: Uri, description: String, onPostSuccess: () -> Unit) {
        inProgress.value = true
        val currentUid = auth.currentUser?.uid
        val currentUsername = userData.value?.userName
        val currentUserImage = userData.value?.imageUrl

        if (currentUid != null) {
            val postUuid = UUID.randomUUID().toString()
            val post = PostData(
                postId = postUuid,
                userId = currentUid,
                userName = currentUsername,
                userImage = currentUserImage,
                postImage = imageUri.toString(),
                postDescription = description,
                time = System.currentTimeMillis()
            )

            db.collection(POSTS).document(postUuid).set(post)
                .addOnSuccessListener {
                    popUpNotification.value = Event("Post Successfully created")
                    inProgress.value = false
                    refreshPosts()
                    onPostSuccess.invoke()
                }
                .addOnFailureListener { exc ->
                    handleException(exc, "Unable to create a post")
                    inProgress.value = false
                }
        } else {
            handleException(customMessage = "Error: Username unavailable. Unable to create the post")
            onLogout()
            inProgress.value = false
        }
    }

    private fun refreshPosts(){
        val currentUid = auth.currentUser?.uid
        if (currentUid != null){
            refreshPostsProgress.value = true
            db.collection(POSTS).whereEqualTo("userId", currentUid).get()
                .addOnSuccessListener { documents ->
                    convertPosts(documents, posts)
                    refreshPostsProgress.value = false
                }
                .addOnFailureListener { exc->
                    handleException(exc, "Cannot fetch the posts")
                    refreshPostsProgress.value = false
                }

        }else{
            handleException(customMessage = "Error: User unavailable. Unable to refresh Posts")
            onLogout()
        }
    }

    private fun convertPosts(documents: QuerySnapshot, outState: MutableState<List<PostData>>){
        val newPosts = mutableListOf<PostData>()
        documents.forEach { doc ->
            val post = doc.toObject<PostData>()
            newPosts.add(post)
        }
        val sortedPosts = newPosts.sortedByDescending { it.time }
        outState.value = sortedPosts
    }
}
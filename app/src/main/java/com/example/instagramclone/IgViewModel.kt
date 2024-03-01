package com.example.instagramclone

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.instagramclone.data.Event
import com.example.instagramclone.data.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

const val USERS = "users"
@HiltViewModel
class IgViewModel @Inject constructor(
    val auth: FirebaseAuth,
    val db: FirebaseFirestore,
    val storage: FirebaseStorage
): ViewModel() {
    val signedIn = mutableStateOf(false)
    val inProgress = mutableStateOf(false)
    val userData = mutableStateOf<UserData?>(null)

    init {
        //auth.signOut()
        val currentUser = auth.currentUser
        signedIn.value = currentUser != null
        currentUser?.uid?.let {uid->
            getUserData(uid)
        }
    }

    val popUpNotification = mutableStateOf<Event<String>?>(null)
    fun onSignUp(
        userName: String,
        email: String,
        pass: String
    ){
        if (userName.isEmpty() or email.isEmpty() or pass.isEmpty()){
            handleException(customMessage = "Please Enter all the fields")
            return
        }
        inProgress.value = true
        db.collection(USERS).whereEqualTo("userName", userName).get()
            .addOnSuccessListener { documents ->
                if (documents.size()>0){
                    handleException(customMessage = "UserName already exists")
                    inProgress.value = false
                }else{
                    auth.createUserWithEmailAndPassword(email,pass)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                signedIn.value = true
                                createOrUpdateProfile(userName = userName)
                            }else{
                                handleException(task.exception, "Signup Failed")
                            }
                            inProgress.value = false
                        }
                }
            }
            .addOnFailureListener {

            }
    }

    fun onLogin(email: String, pass:String){
        if (email.isEmpty() or pass.isEmpty()){
            handleException(customMessage = "Please enter all the details")
            return
        }
        inProgress.value = false
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task->
                if (task.isSuccessful){
                    signedIn.value = true
                    inProgress.value = false
                    auth.currentUser?.uid?.let { uid->
                        //handleException(customMessage = "Login Successful")
                        getUserData(uid)
                    }
                }else{
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
        imageUrl: String ?= null
    ){
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
            inProgress.value =true
            db.collection(USERS).document(uid).get().addOnSuccessListener {
                if (it.exists()){
                    it.reference.update(userData.toMap())
                        .addOnSuccessListener {
                            this.userData.value = userData
                            inProgress.value = false
                        }
                        .addOnFailureListener {
                            handleException(it,"Cannot update user")
                            inProgress.value = false
                        }
                }else{
                    db.collection(USERS).document(uid).set(userData)
                    getUserData(uid)
                    inProgress.value = false
                }
            }
                .addOnFailureListener { exc->
                    handleException(exc, "Cannot create a user")
                    inProgress.value = false
                }
        }
    }

    private fun getUserData(uid: String){
        inProgress.value = true
        db.collection(USERS).document(uid).get()
            .addOnSuccessListener {
                val user = it.toObject<UserData>()
                userData.value = user
                inProgress.value = false
                //popUpNotification.value = Event("UserData retrieved successfully")
            }
            .addOnFailureListener { exc->
                handleException(exc, "Cannot Retrieve UserData")
            }
    }

    fun handleException(exception: Exception?=null, customMessage:String= ""){
        exception?.printStackTrace()
        val errorMsg = exception?.localizedMessage ?: ""
        val message = if(customMessage.isEmpty()) errorMsg else "$customMessage: $errorMsg"
        popUpNotification.value = Event(message)
    }
}
package com.example.instagramclone

import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.instagramclone.data.ChatData
import com.example.instagramclone.data.ChatUser
import com.example.instagramclone.data.CommentData
import com.example.instagramclone.data.Event
import com.example.instagramclone.data.Message
import com.example.instagramclone.data.PostData
import com.example.instagramclone.data.Status
import com.example.instagramclone.data.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.auth.User
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject

const val USERS = "users"
const val POSTS = "posts"
const val COMMENTS = "comments"
const val CHATS = "chat"
const val MESSAGES = "messages"
const val STATUS = "status"

@HiltViewModel
class IgViewModel @Inject constructor(
    val auth: FirebaseAuth,
    val db: FirebaseFirestore,
    val storage: FirebaseStorage
) : ViewModel() {
    val signedIn = mutableStateOf(false)
    val inProgress = mutableStateOf(false)
    val userData = mutableStateOf<UserData?>(null)

    val userProfile = mutableStateOf<UserData?>(null)

    val userRecommendation = mutableStateOf<MutableList<UserData>>(mutableListOf())

    val refreshPostsProgress = mutableStateOf(false)
    val posts = mutableStateOf<List<PostData>>(listOf())
    val userPosts = mutableStateOf<List<PostData>>(listOf())

    val searchedPosts = mutableStateOf<List<PostData>>(listOf())
    val searchOnlyPeople = mutableStateOf<List<UserData>>(listOf())
    val searchedPeopleByPost = mutableStateOf<List<PostData>>(listOf())
    val searchedPostsByUser = mutableStateOf<List<PostData>>(listOf())
    val searchedPeople = mutableStateOf<List<PostData>>(listOf())
    val searchedPostsProgress = mutableStateOf(false)

    val postsFeed = mutableStateOf<List<PostData>>(listOf())
    val postsFeedProgress = mutableStateOf(false)

    val followingUserData = mutableStateOf<MutableList<UserData>>(mutableListOf())

    val comments = mutableStateOf<List<CommentData>>(listOf())
    val commentProgress = mutableStateOf(false)

    val followers = mutableStateOf(0)
    val userFollowers = mutableStateOf(0)

    val sortedUsersList = mutableStateOf<List<UserData>>(listOf())

    //Chat functionality
    val chats = mutableStateOf<List<ChatData>>(listOf())
    val inProgressChats = mutableStateOf(false)

    val chatMessages = mutableStateOf<List<Message>>(listOf())
    val inProgressChatMessages = mutableStateOf(false)
    var currentChatMessagesListener: ListenerRegistration? = null

    val status = mutableStateOf<List<Status>>(listOf())
    val inProgressStatus = mutableStateOf(false)

    val chatId = mutableStateOf("")


    init {
        //auth.signOut()
        val currentUser = auth.currentUser
        signedIn.value = currentUser != null
        currentUser?.uid?.let { uid ->
            getUserData(uid)

        }
    }

    fun refreshData(){
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
                handleException(it, it.message.toString())
                inProgress.value = false
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

    fun getUserData(uid: String) {
        inProgress.value = true
        db.collection(USERS).document(uid).get()
            .addOnSuccessListener {
                val user = it.toObject<UserData>()
                userData.value = user
                inProgress.value = false
                refreshPosts()
                getPersonalizedFeed()
                user?.userId?.let { it1 -> getFollowers(it1) }
                populateChats()
                getUserRecommendations()
                populateStatuses()
                //popUpNotification.value = Event("UserData retrieved successfully")
            }
            .addOnFailureListener { exc ->
                handleException(exc, "Cannot Retrieve UserData")
            }
    }

    fun getUserProfile(uid: String) {
        inProgress.value = true
        db.collection(USERS).document(uid).get()
            .addOnSuccessListener {
                val user = it.toObject<UserData>()
                userProfile.value = user
                inProgress.value = false
                getUserPosts(uid)
                user?.userId?.let { it1 -> getUserFollowers(it1) }

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
            updatePostUserImageData(it.toString())
        }
    }

    private fun updatePostUserImageData(imageUrl: String) {
        val currentUid = auth.currentUser?.uid
        db.collection(POSTS).whereEqualTo("userId", currentUid).get()
            .addOnSuccessListener {
                val posts = mutableStateOf<List<PostData>>(arrayListOf())
                convertPosts(it, posts)
                val refs = arrayListOf<DocumentReference>()
                for (post in posts.value) {
                    post.postId?.let { id ->
                        refs.add(db.collection(POSTS).document(id))
                    }
                }

                if (refs.isNotEmpty()) {
                    db.runBatch { batch ->
                        for (ref in refs) {
                            batch.update(ref, "userImage", imageUrl)
                        }
                    }
                        .addOnSuccessListener { refreshPosts() }
                }
            }

    }

    fun onLogout() {
        auth.signOut()
        signedIn.value = false
        userData.value = null
        userProfile.value = null
        popUpNotification.value = Event("Logged Out")
        searchedPosts.value = listOf()
        userPosts.value = listOf()
        searchedPeople.value = listOf()
        postsFeed.value = listOf()
        comments.value = listOf()
        chats.value = listOf()
        userRecommendation.value = mutableListOf()
    }

    fun onNewPost(uri: Uri, description: String, userId: String, onPostSuccess: () -> Unit) {
        uploadImage(uri) {
            onCreatePost(it, description, userId, onPostSuccess)
        }
    }

    private fun onCreatePost(
        imageUri: Uri,
        description: String,
        userId: String,
        onPostSuccess: () -> Unit
    ) {
        inProgress.value = true
        val currentUid = auth.currentUser?.uid
        val currentUsername = userData.value?.userName
        val currentUserImage = userData.value?.imageUrl

        if (currentUid != null) {
            val postUuid = UUID.randomUUID().toString()

            val fillerWords = listOf("the", "be", "to", "is", "of", "and", "or", "a", "in", "it")
            var searchTerms = description
                .split(" ", ".", ",", "?", "!", "#")
                .map { it.lowercase() }
                .filter { it.isNotEmpty() and !fillerWords.contains(it) }

            val searchPeople = userId.split(" ", ".", ",", "?", "!", "#")
                .map { it.lowercase() }
                .filter { it.isNotEmpty() and !fillerWords.contains(it) }

            searchTerms = searchTerms + searchPeople

            val post = PostData(
                postId = postUuid,
                userId = currentUid,
                userName = currentUsername,
                userImage = currentUserImage,
                postImage = imageUri.toString(),
                postDescription = description,
                time = System.currentTimeMillis(),
                likes = listOf<String>(),
                searchTerms = searchTerms,
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

    private fun refreshPosts() {
        val currentUid = auth.currentUser?.uid
        if (currentUid != null) {
            refreshPostsProgress.value = true
            db.collection(POSTS).whereEqualTo("userId", currentUid).get()
                .addOnSuccessListener { documents ->
                    convertPosts(documents, posts)
                    refreshPostsProgress.value = false
                }
                .addOnFailureListener { exc ->
                    handleException(exc, "Cannot fetch the posts")
                    refreshPostsProgress.value = false
                }

        } else {
            handleException(customMessage = "Error: User unavailable. Unable to refresh Posts")
            onLogout()
        }
    }

    private fun getUserPosts(userId: String) {
        refreshPostsProgress.value = true
        db.collection(POSTS).whereEqualTo("userId", userId).get()
            .addOnSuccessListener { documents ->
                convertPosts(documents, userPosts)
                refreshPostsProgress.value = false
            }
            .addOnFailureListener { exc ->
                handleException(exc, "Cannot fetch the posts")
                refreshPostsProgress.value = false
            }

    }

    private fun convertPosts(documents: QuerySnapshot, outState: MutableState<List<PostData>>) {
        val newPosts = mutableListOf<PostData>()
        documents.forEach { doc ->
            val post = doc.toObject<PostData>()
            newPosts.add(post)
        }
        val sortedPosts = newPosts.sortedByDescending { it.time }
        outState.value = sortedPosts
    }

    private fun convertPeople(documents: QuerySnapshot, outState: MutableState<List<UserData>>) {
        val newPeople = mutableListOf<UserData>()
        documents.forEach { doc ->
            val post = doc.toObject<UserData>()
            newPeople.add(post)
        }
        val sortedPosts = newPeople.sortedByDescending { it.userName }
        outState.value = sortedPosts
    }

    fun searchPosts(searchTerm: String) {
        if (searchTerm.isNotEmpty()) {
            searchedPostsProgress.value = true
            db.collection(POSTS)
                .whereArrayContains("searchTerms", searchTerm.trim().lowercase())
                .get()
                .addOnSuccessListener {
                    convertPosts(it, searchedPosts)
                    convertPosts(it, searchedPeopleByPost)
                    searchedPostsProgress.value = false
                }
                .addOnFailureListener { exc ->
                    handleException(exc, "Cannot search posts")
                    searchedPostsProgress.value = false
                }

            db.collection(POSTS)
                .whereEqualTo("userName", searchTerm.trim().lowercase())
                .get()
                .addOnSuccessListener {
                    convertPosts(it, searchedPeople)
                    convertPosts(it, searchedPostsByUser)
                    searchedPostsProgress.value = false
                }
                .addOnFailureListener { exc ->
                    handleException(exc, "Cannot search posts")
                    searchedPostsProgress.value = false
                }
        }
    }

    //Search People
    fun searchPeople(searchTerm: String) {
        if (searchTerm.isNotEmpty()) {
            searchedPostsProgress.value = true
            db.collection(USERS)
                .whereEqualTo("userName", searchTerm)
                .get()
                .addOnSuccessListener {
                    convertPeople(it, searchOnlyPeople)
                    searchedPostsProgress.value = false
                }
                .addOnFailureListener { exc ->
                    handleException(exc, "Cannot search posts")
                    searchedPostsProgress.value = false
                }
        }
    }

    fun onFollowClick(userId: String) {
        auth.currentUser?.uid?.let { currentUser ->
            val following = arrayListOf<String>()
            userData.value?.following?.let {
                following.addAll(it)
            }
            if (following.contains(userId)) {
                following.remove(userId)
            } else {
                following.add(userId)
            }
            db.collection(USERS).document(currentUser).update("following", following)
                .addOnSuccessListener {
                    getUserData(currentUser)
                }
        }
    }

    private fun getPersonalizedFeed() {

        val following = userData.value?.following
        if (!following.isNullOrEmpty()) {
            postsFeedProgress.value = true
            db.collection(POSTS).whereIn("userId", following).get()
                .addOnSuccessListener {
                    convertPosts(documents = it, outState = postsFeed)
                    if (postsFeed.value.isEmpty()) {
                        getGeneralFeed()
                    } else {
                        postsFeedProgress.value = false
                    }
                }
                .addOnFailureListener { exc ->
                    handleException(exc, "Cannot get personalized feed")
                    postsFeedProgress.value = false
                }
        } else {
            getGeneralFeed()
        }
    }


    fun getGeneralFeed() {
        postsFeedProgress.value = true
        val currentTime = System.currentTimeMillis()
        val difference = 500 * 60 * 60 * 1000
        db.collection(POSTS)
            .whereGreaterThan("time", currentTime - difference)
            .get()

            .addOnSuccessListener {
                convertPosts(
                    documents = it,
                    outState = postsFeed
                )
                postsFeedProgress.value = false
            }
            .addOnFailureListener { exc ->
                handleException(exc, "Cannot get Feed")
                postsFeedProgress.value = false
            }
    }

    fun onLikePost(postData: PostData) {
        auth.currentUser?.uid?.let { userId ->
            postData.likes?.let { likes ->
                val newLikes = arrayListOf<String>()
                if (likes.contains(userId)) {
                    newLikes.addAll(likes.filter { userId != it })
                } else {
                    newLikes.addAll(likes)
                    newLikes.add(userId)
                }
                postData.postId?.let { postId ->
                    db.collection(POSTS).document(postId).update("likes", newLikes)
                        .addOnSuccessListener {
                            postData.likes = newLikes
                        }
                        .addOnFailureListener {
                            handleException(it, "Unable to like the post")
                        }
                }
            }

        }
    }

    fun createComment(postId: String, text: String) {
        userData.value?.userName.let { username ->
            val commentId = UUID.randomUUID().toString()
            val comment = CommentData(
                commentId = commentId,
                postId = postId,
                userName = username,
                text = text,
                timeStamp = System.currentTimeMillis()
            )

            db.collection(COMMENTS).document(commentId).set(comment)
                .addOnSuccessListener {
                    getComments(postId)
                }
                .addOnFailureListener { exc ->
                    handleException(exc, "Cannot create comment")
                }
        }
    }

    fun getComments(postId: String?) {
        commentProgress.value = true
        db.collection(COMMENTS).whereEqualTo("postId", postId).get()
            .addOnSuccessListener { documents ->
                val newComments = mutableListOf<CommentData>()
                documents.forEach { doc ->
                    val comment = doc.toObject<CommentData>()
                    newComments.add(comment)
                }
                val sortedComments = newComments.sortedByDescending { it.timeStamp }
                comments.value = sortedComments
                commentProgress.value = false
            }
            .addOnFailureListener { exc ->
                handleException(exc, "Cannot retrieve comments")
                commentProgress.value = false
            }
    }

    private fun getFollowers(uid: String) {
        db.collection(USERS).whereArrayContains("following", uid ?: "").get()
            .addOnSuccessListener { documents ->
                val users = mutableListOf<UserData>()
                followers.value = documents.size()
                documents.forEach { doc ->
                    val user = doc.toObject<UserData>()
                    users.add(user)
                }
                val sortedUsers = users.sortedByDescending { it.userName }
                sortedUsersList.value = sortedUsers

            }
            .addOnFailureListener { exc ->
                handleException(exc, "Not able to retreive followers")
            }
    }

    fun getCurrentFollowers(uid: String) {
        getFollowers(uid)
    }

    private fun getUserFollowers(uid: String) {
        db.collection(USERS).whereArrayContains("following", uid ?: "").get()
            .addOnSuccessListener { documents ->
                val users = mutableListOf<UserData>()
                userFollowers.value = documents.size()
                documents.forEach { doc ->
                    val user = doc.toObject<UserData>()
                    users.add(user)
                }
                val sortedUsers = users.sortedByDescending { it.userName }
                sortedUsersList.value = sortedUsers

            }
            .addOnFailureListener { exc ->
                handleException(exc, "Not able to retreive followers")
            }
    }

    fun getFollowingData(uid: List<String>): List<UserData> {
        // Initialize with default values or consider using a default constructor
        followingUserData.value = mutableListOf()
        for (item in uid) {
            db.collection(USERS).document(item).get()
                .addOnSuccessListener { documentSnapshot ->
                    val user = documentSnapshot.toObject<UserData>()
                    if (user != null) {
                        followingUserData.value.add(user)

                    }
                }

            // Return the initialized userData

        }
        return followingUserData.value
    }


    //Chat functions
    fun onAddChat(userName: String, onSuccess: () -> Unit) {
        if (userName.isEmpty()) {
            handleException(customMessage = "Field should not be empty")
        } else {
            db.collection(CHATS)
                .where(
                    Filter.or(
                        Filter.and(
                            Filter.equalTo("user1.userName", userName),
                            Filter.equalTo("user2.userName", userData.value?.userName)
                        ),

                        Filter.and(
                            Filter.equalTo("user1.userName", userData.value?.userName),
                            Filter.equalTo("user2.userName", userName)
                        )
                    )
                )
                .get()
                .addOnSuccessListener {
                    if (it.isEmpty) {
                        db.collection(USERS).whereEqualTo("userName", userName)
                            .get()
                            .addOnSuccessListener { querySnapshot ->
                                if (querySnapshot.isEmpty) {
                                    handleException(customMessage = "Cannot retrieve the user $userName")
                                } else {
                                    val chatPartner = querySnapshot.toObjects<UserData>()[0]
                                    val id = db.collection(CHATS).document().id
                                    val chat = ChatData(
                                        id,
                                        ChatUser(
                                            userData.value?.userId,
                                            userData.value?.userName,
                                            userData.value?.imageUrl
                                        ),

                                        ChatUser(
                                            chatPartner.userId,
                                            chatPartner.userName,
                                            chatPartner.imageUrl
                                        )
                                    )
                                    db.collection(CHATS).document(id).set(chat)
                                        .addOnSuccessListener {
                                            chatId.value = id
                                            onSuccess() // Execute the onSuccess callback
                                        }
                                        .addOnFailureListener { handleException(it) }
                                }
                            }
                            .addOnFailureListener { handleException(it) }
                    } else {
                        handleException(customMessage = "Chat already exists")
                    }
                }
                .addOnFailureListener { handleException(it) }
        }
    }


    private fun populateChats() {
        inProgressChats.value = true
        db.collection(CHATS).where(
            Filter.or(
                Filter.equalTo("user1.userId", userData.value?.userId),
                Filter.equalTo("user2.userId", userData.value?.userId)
            )
        )
            .addSnapshotListener { value, error ->
                if (error != null) {
                    handleException(error)
                }
                if (value != null) {
                    chats.value = value.documents.mapNotNull { it.toObject<ChatData>() }
                    inProgressChats.value = false
                }
            }
    }

    fun onSendReply(chatId: String, message: String) {
        val time = Calendar.getInstance().time.toString()
        val message = Message(
            userData.value?.userId, message, time
        )

        db.collection(CHATS)
            .document(chatId)
            .collection(MESSAGES)
            .document()
            .set(message)
    }

    fun populateChat(chatId: String) {
        inProgressChatMessages.value = true
        currentChatMessagesListener = db.collection(CHATS)
            .document(chatId)
            .collection(MESSAGES)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    handleException(error)
                }
                if (value != null) {
                    chatMessages.value = value.documents
                        .mapNotNull { it.toObject<Message>() }
                        .sortedBy { it.timestamp }

                }
                inProgressChatMessages.value = false
            }
    }

    fun depopulateChat() {
        chatMessages.value = listOf()
        currentChatMessagesListener = null
    }

    fun getChatId(currentUserId: String, userId: String) {
        chatId.value = "" // Assuming chatId is a LiveData or similar variable
        db.collection(CHATS)
            .whereEqualTo("user1.userId", currentUserId)
            .whereEqualTo("user2.userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    // If documents are found, update chatId with the ID of the first document
                    chatId.value = documents.documents[0].id
                } else {
                    // If no matching document is found, try the reverse query
                    db.collection(CHATS)
                        .whereEqualTo("user1.userId", userId)
                        .whereEqualTo("user2.userId", currentUserId)
                        .get()
                        .addOnSuccessListener { reverseDocuments ->
                            if (!reverseDocuments.isEmpty) {
                                // If documents are found in the reverse query, update chatId
                                chatId.value = reverseDocuments.documents[0].id
                            } else {
                                // If no matching document is found in both queries
                                println("No chat found between users: $currentUserId and $userId")
                            }
                        }
                        .addOnFailureListener { exception ->
                            // Handle failures in the reverse query
                            handleException(exception)
                        }
                }
            }
            .addOnFailureListener { exception ->
                // Handle failures in the initial query
                handleException(exception)
            }
    }


    fun getUserRecommendations() {
        db.collection(USERS)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    handleException(error, "Not able to retrieve user recommendations")
                    return@addSnapshotListener
                }

                val currentUserFollowing = userData.value?.following ?: emptyList()
                userRecommendation.value.clear()

                value?.forEach { doc ->
                    val user = doc.toObject<UserData>()
                    if (!currentUserFollowing.contains(user.userId) && userData.value?.userId != user.userId) {
                        userRecommendation.value.add(user)
                    }
                }
            }
    }


    private fun createStatus(imageUrl: String){
        val newStatus = Status(
            ChatUser(
                userData.value?.userId,
                userData.value?.userName,
                userData.value?.imageUrl
            ),
            imageUrl,
            System.currentTimeMillis()
        )

        db.collection(STATUS).document().set(newStatus)
    }

    fun uploadStatus(imageUri: Uri){
        uploadImage(imageUri){
            createStatus(imageUrl = it.toString())
        }
    }

    private fun populateStatuses(){
        inProgressStatus.value = true
        val milliTimeDelta = 24L * 60 * 60 * 1000
        val cutoff = System.currentTimeMillis() - milliTimeDelta

        db.collection(USERS)
            .addSnapshotListener { value, error ->
                if (error != null){
                    handleException(error)
                }else{
                    val currentConnections = arrayListOf(userData.value?.userId)
                    val users = value?.toObjects<UserData>()

                    users?.forEach { user ->
                        if (userData.value?.following?.contains(user.userId) == true){
                            if (user.userId == userData.value?.userId){
                                currentConnections.add(user.userId)
                            }else{
                                currentConnections.add(user.userId)
                            }
                        }

                    }
                    db.collection(STATUS)
                        .whereGreaterThan("timestamp", cutoff)
                        .whereIn("user.userId", currentConnections)
                        .addSnapshotListener { value, error ->
                            if (error != null){
                                handleException(error)
                            }
                            if(value != null){
                                status.value = value.toObjects()
                            }
                            inProgressStatus.value = false
                        }
                }
            }
    }


}
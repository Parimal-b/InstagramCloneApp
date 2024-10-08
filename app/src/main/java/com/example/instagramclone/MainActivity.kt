package com.example.instagramclone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.instagramclone.auth.LoginScreen
import com.example.instagramclone.auth.ProfileScreen
import com.example.instagramclone.auth.screens.signup.SignUpScreen
import com.example.instagramclone.data.PostData
import com.example.instagramclone.main.ChatListScreen
import com.example.instagramclone.main.CommentScreen
import com.example.instagramclone.main.FeedScreen
import com.example.instagramclone.main.FollowersScreen
import com.example.instagramclone.main.FollowingScreen
import com.example.instagramclone.main.MyPostsScreen
import com.example.instagramclone.main.NewPostScreen
import com.example.instagramclone.main.NotificationMessage
import com.example.instagramclone.main.SearchScreen
import com.example.instagramclone.main.SingleChatScreen
import com.example.instagramclone.main.SinglePostScreen
import com.example.instagramclone.main.SingleStatusScreen
import com.example.instagramclone.main.StatusListScreen
import com.example.instagramclone.main.UserPostsScreen

import com.example.instagramclone.ui.theme.InstagramCloneTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            InstagramCloneTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    InstagramApp()
                }
            }
        }
    }
}

sealed class DestinationScreen(val route: String) {
    object SignUp : DestinationScreen("signUp")
    object Login : DestinationScreen("login")
    object Feed : DestinationScreen("feed")
    object Search : DestinationScreen("search")
    object MyPosts : DestinationScreen("myposts")
    object Profile : DestinationScreen("profile")
    object NewPost : DestinationScreen("newpost/{imageUri}") {
        fun createRoute(uri: String) = "newpost/$uri"
    }
    object ChatListScreen : DestinationScreen("chatscreen")

    object SingleChat : DestinationScreen("chat/{chatId}"){
        fun createRoute(chatId: String) = "chat/${chatId}"
    }

    object SinglePost : DestinationScreen("singlepost")
    object CommentScreen: DestinationScreen("comments/{postId}"){
        fun createRoute(postId: String) = "comments/${postId}"
    }

    object Followers : DestinationScreen("followers/{userId}"){
        fun createRoute(userId: String) = "followers/${userId}"
    }

    object Following: DestinationScreen("following/{userId}"){
        fun createRoute(userId: String) = "following/${userId}"
    }

    object userPosts: DestinationScreen("userPosts/{userId}"){
        fun createRoute(userId: String) = "userPosts/${userId}"
    }

    object StatusList: DestinationScreen("status")

    object SingleStatus: DestinationScreen("status/{userId}"){
        fun createRoute(userId: String) = "status/${userId}"
    }
}

@Composable
fun InstagramApp() {
    val vm = hiltViewModel<IgViewModel>()
    val navController = rememberNavController()

    NotificationMessage(vm = vm)

    NavHost(navController = navController, startDestination = DestinationScreen.SignUp.route) {

        composable(DestinationScreen.SignUp.route) {
            SignUpScreen(navController = navController, vm = vm)
        }

        composable(DestinationScreen.ChatListScreen.route) {
            ChatListScreen(navController = navController, vm = vm)
        }

        composable(DestinationScreen.StatusList.route) {
            StatusListScreen(navController = navController, vm = vm)
        }

        composable(DestinationScreen.Login.route) {
            LoginScreen(navController = navController, vm = vm)
        }
        composable(DestinationScreen.Feed.route) {
            FeedScreen(navController = navController, vm = vm)
        }
        composable(DestinationScreen.Search.route) {
            SearchScreen(navController = navController, vm = vm)
        }
        composable(DestinationScreen.MyPosts.route) {
            MyPostsScreen(navController = navController, vm = vm)
        }
        composable(DestinationScreen.Profile.route) {
            ProfileScreen(navController = navController, vm = vm)
        }
        composable(DestinationScreen.NewPost.route) { navBackStachEntry ->
            val imageUri = navBackStachEntry.arguments?.getString("imageUri")
            imageUri?.let {
                NewPostScreen(navController = navController, vm = vm, encodedUri = it)
            }
        }
        composable(DestinationScreen.SinglePost.route) {
            val postData =navController.previousBackStackEntry
                ?.savedStateHandle?.get<PostData>("post")

            postData?.let {
                SinglePostScreen(
                    navController = navController,
                    vm = vm,
                    post = postData
                )
            }
        }

        composable(DestinationScreen.CommentScreen.route){navBackStackEntry->
            val postId = navBackStackEntry.arguments?.getString("postId")
            postId?.let { CommentScreen(navController = navController, vm = vm, postId = it) }
        }

        composable(DestinationScreen.SingleStatus.route){navBackStackEntry->
            val userId = navBackStackEntry.arguments?.getString("userId")
            userId?.let {
                SingleStatusScreen(navController = navController, vm = vm, userId = userId) }
        }

        composable(DestinationScreen.SingleChat.route){
            val chatId = it.arguments?.getString("chatId")
            chatId?.let {
                SingleChatScreen(navController = navController, vm = vm, chatId = chatId)
            }
        }

        composable(DestinationScreen.Followers.route){navBackStackEntry->
            val userId = navBackStackEntry.arguments?.getString("userId")
            userId?.let { FollowersScreen(navController = navController, vm = vm, userId = userId) }
        }
        composable(DestinationScreen.Following.route){navBackStackEntry ->
            val userId = navBackStackEntry.arguments?.getString("userId")
            userId?.let {
                FollowingScreen(navController = navController, vm= vm, userId = userId)
            }
        }

        composable(DestinationScreen.userPosts.route){navBackStackEntry ->
            val userId = navBackStackEntry.arguments?.getString("userId")
            userId?.let {
                UserPostsScreen(navController = navController, vm= vm, userId = userId)
            }
        }

    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    InstagramCloneTheme {
        InstagramApp()
    }
}

package com.example.instagramclone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.instagramclone.auth.LoginScreen
import com.example.instagramclone.auth.ProfileScreen
import com.example.instagramclone.auth.SignUpScreen
import com.example.instagramclone.main.FeedScreen
import com.example.instagramclone.main.MyPostsScreen
import com.example.instagramclone.main.NotificationMessage
import com.example.instagramclone.main.SearchScreen

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

sealed class DestinationScreen(val route: String){
    object SignUp: DestinationScreen("signUp")
    object Login: DestinationScreen("login")
    object Feed: DestinationScreen("feed")
    object Search: DestinationScreen("search")
    object MyPosts: DestinationScreen("myPosts")
    object Profile: DestinationScreen("profile")
}

@Composable
fun InstagramApp(){
    val vm = hiltViewModel<IgViewModel>()
    val navController = rememberNavController()
    
    NotificationMessage(vm = vm)

    NavHost(navController = navController, startDestination = DestinationScreen.SignUp.route){

        composable(DestinationScreen.SignUp.route){
            SignUpScreen(navController = navController, vm = vm)
        }
        composable(DestinationScreen.Login.route){
            LoginScreen(navController = navController, vm = vm)
        }
        composable(DestinationScreen.Feed.route){
            FeedScreen(navController = navController, vm = vm)
        }
        composable(DestinationScreen.Search.route){
            SearchScreen(navController = navController, vm = vm)
        }
        composable(DestinationScreen.MyPosts.route){
            MyPostsScreen(navController = navController, vm = vm)
        }
        composable(DestinationScreen.Profile.route){
            ProfileScreen(navController = navController, vm = vm)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    InstagramCloneTheme {

    }
}
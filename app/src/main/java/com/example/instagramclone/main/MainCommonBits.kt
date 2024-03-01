package com.example.instagramclone.main


import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.instagramclone.DestinationScreen
import com.example.instagramclone.IgViewModel

@Composable
fun NotificationMessage(vm: IgViewModel){
    val notifState = vm.popUpNotification.value
    val notifMessage = notifState?.getContentOrNull()
    if (notifMessage != null){
        Toast.makeText(LocalContext.current, notifMessage, Toast.LENGTH_LONG).show()
    }
}


@Composable
fun CommonProgressSpinner(){
    Row(
        modifier = Modifier
            .alpha(0.5f)
            .background(Color.LightGray)
            .clickable { false }
            .fillMaxSize(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator()
    }
}

fun navigateTo(navController: NavController, dest: DestinationScreen){
    navController.navigate(dest.route){
        popUpTo(dest.route)
        launchSingleTop = true
    }
}

@Composable
fun checkSignedIn(vm: IgViewModel, navController: NavController){
    val alreadyLoggedIn = remember {
        mutableStateOf(false)
    }

    val signedIn = vm.signedIn.value
    if (signedIn && !alreadyLoggedIn.value){
        alreadyLoggedIn.value = true
        navController.navigate(DestinationScreen.Feed.route){
            popUpTo(0)
        }
    }
}
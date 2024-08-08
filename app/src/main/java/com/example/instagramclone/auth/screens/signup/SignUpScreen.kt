package com.example.instagramclone.auth.screens.signup

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.instagramclone.DestinationScreen
import com.example.instagramclone.IgViewModel
import com.example.instagramclone.main.checkSignedIn
import com.example.instagramclone.main.navigateTo

@Composable
fun SignUpScreen(navController: NavController, vm: IgViewModel) {
    checkSignedIn(vm = vm, navController = navController)

    SignUpScreenScaffold(
        isLoading = vm.inProgress.value,
        onNavToLoginScreenClick = { navigateTo(navController, DestinationScreen.Login) },
        onSignUpClick = { username, email, password ->
            vm.onSignUp(
            username,
            email,
            password
            )
        }
    )
}
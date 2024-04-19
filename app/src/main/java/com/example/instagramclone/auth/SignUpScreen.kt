package com.example.instagramclone.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.instagramclone.DestinationScreen
import com.example.instagramclone.IgViewModel
import com.example.instagramclone.R
import com.example.instagramclone.main.CommonProgressSpinner
import com.example.instagramclone.main.checkSignedIn
import com.example.instagramclone.main.navigateTo

@Composable
fun SignUpScreen(navController: NavController, vm: IgViewModel) {
    checkSignedIn(vm = vm, navController = navController)

    val focus = LocalFocusManager.current
    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentHeight()
                .verticalScroll(
                    rememberScrollState()
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            val userNameState = remember {
                mutableStateOf(TextFieldValue())
            }

            val emailState = remember {
                mutableStateOf(TextFieldValue())
            }

            val passState = remember {
                mutableStateOf(TextFieldValue())
            }


            Image(
                painter = painterResource(id = R.drawable.ig_logo),
                contentDescription = null,
                modifier = Modifier
                    .width(250.dp)
                    .padding(8.dp)
                    .size(150.dp)
            )

            Text(
                text = "SignUp",
                modifier = Modifier.padding(8.dp),
                fontSize = 30.sp,
                fontFamily = FontFamily.SansSerif
            )

            OutlinedTextField(
                value = userNameState.value,
                onValueChange = {
                    userNameState.value = it
                },
                modifier = Modifier.padding(8.dp),
                label = { Text("UserName") },
            )

            OutlinedTextField(
                value = emailState.value,
                onValueChange = {
                    emailState.value = it
                },
                modifier = Modifier.padding(8.dp),
                label = { Text("Email") },
            )

            OutlinedTextField(
                value = passState.value,
                onValueChange = {
                    passState.value = it
                },
                modifier = Modifier.padding(8.dp),
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation()
            )

            Button(
                onClick = {
                    focus.clearFocus(force = true)
                    vm.onSignUp(
                        userNameState.value.text,
                        emailState.value.text,
                        passState.value.text
                    )
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Text(text = "SIGN UP")
            }
            Text(text = "Already a user? Go to Login",
                color = Color.Blue,
                modifier = Modifier
                    .padding(8.dp)
                    .clickable { navigateTo(navController, DestinationScreen.Login) })
        }

        val isLoading = vm.inProgress.value
        if (isLoading) {
            CommonProgressSpinner()
        }
    }
}
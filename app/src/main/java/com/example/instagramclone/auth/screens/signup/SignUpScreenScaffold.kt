package com.example.instagramclone.auth.screens.signup

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.instagramclone.R
import com.example.instagramclone.main.CommonProgressSpinner

@Composable
fun SignUpScreenScaffold(
    isLoading: Boolean,
    onNavToLoginScreenClick: () -> Unit,
    onSignUpClick: (String, String, String) -> Unit
) {

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
                    onSignUpClick(
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
                    .clickable { onNavToLoginScreenClick() })
        }

        if (isLoading) {
            CommonProgressSpinner()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenScaffoldPreview(){
    SignUpScreenScaffold(
        isLoading = false,
        onNavToLoginScreenClick = {  },
        onSignUpClick = { _, _, _ -> }
    )
}
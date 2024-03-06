package com.example.instagramclone.main

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.instagramclone.IgViewModel
import com.example.instagramclone.data.CommentData
import org.w3c.dom.Comment

@Composable
fun CommentScreen(navController: NavController, vm: IgViewModel, postId: String) {
    var commentText by rememberSaveable {
        mutableStateOf("")
    }
    val focusManager = LocalFocusManager.current

    val comments = vm.comments.value
    val commentProgress = vm.commentProgress.value

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        if (commentProgress) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CommonProgressSpinner()

            }
        } else if (comments.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "No comments available")

            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(items = comments) { comment ->
                    CommentRow(comment)
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            TextField(
                value = commentText,
                onValueChange = { commentText = it },
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, Color.LightGray),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent
                )
            )
            Button(
                onClick = {
                    vm.createComment(postId = postId, text = commentText)
                    commentText = ""
                    focusManager.clearFocus()
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(text = "Comment")
            }

        }
    }
}

@Composable
fun CommentRow(comment: CommentData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(text = comment.userName ?: "", fontWeight = FontWeight.Bold, color = Color.Red)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = comment.text ?: "", fontWeight = FontWeight.Bold)
    }
}
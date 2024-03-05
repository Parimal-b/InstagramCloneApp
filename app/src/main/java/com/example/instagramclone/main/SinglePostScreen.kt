package com.example.instagramclone.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.example.instagramclone.IgViewModel
import com.example.instagramclone.data.PostData

@Composable
fun SinglePostScreen(navController: NavController, vm: IgViewModel, post: PostData) {
    post.userId.let {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(8.dp)
        ) {
            Text(text = "Back", modifier = Modifier.clickable { navController.popBackStack() })

            CommonDivider()

            SinglePostDisplay(
                navController = navController,
                vm = vm,
                post = post)

        }
    }
}

@Composable
fun SinglePostDisplay(
    navController: NavController,
    vm: IgViewModel,
    post: PostData,

) {
    val userData = vm.userData.value
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Card(
                shape = CircleShape, modifier = Modifier
                    .padding(8.dp)
                    .size(32.dp)
            ) {
                Image(
                    painter = rememberImagePainter(data = post.userImage),
                    contentDescription = null
                )

            }

            Text(text = post.userName ?: "")
            Text(text = ".", modifier = Modifier.padding(8.dp))

            if (userData?.userId == post.userId){
                //Current user's post
            }else{
                Text(text = "Follow", color = Color.Blue, modifier = Modifier.clickable {
                    //Follow the user
                })
            }
        }
    }
}
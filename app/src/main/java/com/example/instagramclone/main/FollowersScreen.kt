package com.example.instagramclone.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.example.instagramclone.DestinationScreen
import com.example.instagramclone.IgViewModel
import com.example.instagramclone.R
import com.example.instagramclone.data.UserData

@Composable
fun FollowersScreen(navController: NavController, vm: IgViewModel, userId: String) {
    val currentList = vm.sortedUsersList.value
    Column {


        Text(
            text = "Followers", modifier = Modifier.padding(16.dp),
            style = LocalTextStyle.current.copy(fontSize = 25.sp, fontFamily = FontFamily.SansSerif)
        )
        Box(
            modifier = Modifier.background(color = Color.White),

            ) {
            LazyColumn {
                items(items = currentList) {
                    FollowersList(it.userName!!, it.imageUrl ?: "")
                }
            }

        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Composable
fun FollowersList(userName: String, userImage: String){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .shadow(4.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Follower Image (You can replace this with the actual follower image)
                Card(shape = CircleShape,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(32.dp)) {
                    val painter = rememberImagePainter(
                        data = userImage,
                        builder = {
                            placeholder(R.drawable.ic_person) // Replace with your placeholder image resource
                            error(R.drawable.ic_person) // Replace with your error image resource
                        }
                    )

                    Image(
                        painter = painter,
                        contentDescription = null,
                        modifier = Modifier,
                        contentScale = ContentScale.Crop
                        // Add your additional modifiers if needed
                    )
                }


                // Follower Name
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = userName,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
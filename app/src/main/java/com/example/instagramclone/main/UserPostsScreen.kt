package com.example.instagramclone.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.instagramclone.DestinationScreen
import com.example.instagramclone.IgViewModel
import com.example.instagramclone.data.PostData

@Composable
fun UserPostsScreen(navController: NavController, vm: IgViewModel, userId: String) {

    val followingUserList = vm.userProfile.value?.following

    val currentUserData = vm.userData.value
    val userData = vm.userProfile.value
    val isLoading = vm.inProgress.value

    val postsLoading = vm.refreshPostsProgress.value
    val posts = vm.userPosts.value

    val followers = vm.userFollowers.value
    val following = userData?.following

    vm.getChatId(currentUserData?.userId.toString(), userData?.userId.toString())
    val currentChatId by remember { vm.chatId }


    Column {
        Column(modifier = Modifier.weight(1f)) {
            Row {
                UserProfileImage(userData?.imageUrl)
                Text(
                    text = "${posts.size}\nposts",
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "$followers\nfollowers",
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically)
                        .clickable {
                            vm.getCurrentFollowers(userData?.userId!!)
                            navController.navigate(DestinationScreen.Followers.createRoute(userData.userId!!))
                        },
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "${following?.size ?: 0}\nfollowing",
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically)
                        .clickable {
                            navController.navigate(DestinationScreen.Following.createRoute(userData?.userId!!))
                            if (followingUserList != null) {
                                vm.getFollowingData(followingUserList)
                            }
                        },
                    textAlign = TextAlign.Center
                )
            }
            Column(modifier = Modifier.padding(8.dp)) {
                val userNameDisplay =
                    if (userData?.userName == null) "" else "@${userData?.userName}"
                Text(text = userData?.name ?: "", fontWeight = FontWeight.Bold)
                Text(text = userNameDisplay)
                Text(text = userData?.bio ?: "")
            }

            Row(

            ) {
                OutlinedButton(
                    onClick = { vm.onFollowClick(userData?.userId!!) },
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                        .weight(1f),
                    colors = ButtonDefaults.buttonColors(Color.Transparent),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp,
                        disabledElevation = 0.dp
                    ),
                    shape = RoundedCornerShape(10)
                ) {
                    if (currentUserData?.userId == userData?.userId) {
                        //Current user's post
                    } else if (currentUserData?.following?.contains(userData?.userId) == true) {
                        Text(
                            text = "Following",
                            color = Color.Gray,
                            modifier = Modifier.clickable { vm.onFollowClick(userData?.userId!!) })
                    } else {
                        Text(
                            text = "Follow",
                            color = Color.Blue,
                            modifier = Modifier.clickable { vm.onFollowClick(userData?.userId!!) })
                    }
                }

                if (currentUserData?.following?.contains(userData?.userId) == true) {
                    OutlinedButton(
                        onClick = {
                            if (currentChatId.isNotEmpty()) {
                                navController.navigate(DestinationScreen.SingleChat.createRoute(currentChatId))
                            } else {
                                vm.onAddChat(userData?.userName.toString()) {
                                    navController.navigate(DestinationScreen.SingleChat.createRoute(vm.chatId.value))
                                }
                            }

                        },
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
                            .weight(1f),
                        colors = ButtonDefaults.buttonColors(Color.Transparent),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 0.dp,
                            disabledElevation = 0.dp
                        ),
                        shape = RoundedCornerShape(10)
                    ) {
                        Text(
                            text = "Message",
                            color = Color.Blue
                        )
                    }
                }
            }

        }

        UserPostList(
            isContextLoading = isLoading,
            postsLoading = postsLoading,
            posts = posts,
            modifier = Modifier
                .weight(1f)
                .padding(1.dp)
                .fillMaxSize()
        ) { post ->
            //OnPost Click Method
            navigateTo(
                navController = navController,
                DestinationScreen.SinglePost,
                NavParams("post", post)
            )
        }
        BottomNavigationMenu(
            selectedItem = BottomNavigationItem.POSTS,
            navController = navController
        )
    }


}


@Composable
fun UserProfileImage(imageUrl: String?) {
    Box(
        modifier = Modifier
            .padding(top = 16.dp)
    ) {

        userImageCard(
            userImage = imageUrl, modifier = Modifier
                .padding(8.dp)
                .size(80.dp)
        )
    }
}

@Composable
fun UserPostList(
    isContextLoading: Boolean,
    postsLoading: Boolean,
    posts: List<PostData>,
    modifier: Modifier,
    onPostClick: (PostData) -> Unit
) {
    if (postsLoading) {
        CommonProgressSpinner()
    } else if (posts.isEmpty()) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center

        ) {
            if (!isContextLoading) Text(text = "No Posts Available")
        }
    } else {
        LazyColumn(modifier = modifier) {

            val rows = arrayListOf<PostRow>()
            var currentRow = PostRow()
            rows.add(currentRow)
            for (post in posts) {
                if (currentRow.isFull()) {
                    currentRow = PostRow()
                    rows.add(currentRow)
                }
                currentRow.add(post = post)
            }
            items(items = rows) { row ->

                UserPostRow(item = row, onPostClick = onPostClick)

            }
        }
    }
}

@Composable
fun UserPostRow(item: PostRow, onPostClick: (PostData) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        UserPostImage(imageUrl = item.post1?.postImage, modifier = Modifier
            .weight(1f)
            .clickable { item.post1?.let { post -> onPostClick(post) } }
        )
        UserPostImage(imageUrl = item.post2?.postImage, modifier = Modifier
            .weight(1f)
            .clickable { item.post2?.let { post -> onPostClick(post) } }
        )
        UserPostImage(imageUrl = item.post3?.postImage, modifier = Modifier
            .weight(1f)
            .clickable { item.post3?.let { post -> onPostClick(post) } }
        )
    }
}

@Composable
fun UserPostImage(imageUrl: String?, modifier: Modifier) {
    Box(modifier = modifier) {
        var modifier = Modifier
            .padding(1.dp)
            .fillMaxSize()
        if (imageUrl == null) {
            modifier = modifier.clickable(enabled = false) { }
        }
        commonImage(data = imageUrl, modifier = modifier, contentScale = ContentScale.Crop)

    }
}

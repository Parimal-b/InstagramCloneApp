package com.example.instagramclone.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.example.instagramclone.DestinationScreen
import com.example.instagramclone.IgViewModel
import com.example.instagramclone.R
import com.example.instagramclone.data.CommentData
import com.example.instagramclone.data.PostData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SinglePostScreen(navController: NavController, vm: IgViewModel, post: PostData) {
    val comments = vm.comments.value
    LaunchedEffect(key1 = Unit) {
        vm.getComments(post.postId)
    }
    post.userId.let {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {


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
                    post = post,
                    nbComments = comments.size
                ){

                }

            }
        }
    }
}

@Composable
fun SinglePostDisplay(
    navController: NavController,
    vm: IgViewModel,
    post: PostData,
    nbComments: Int,
    onPostCLick: () -> Unit
) {
    vm.getComments(postId = post.postId)
    val userData = vm.userData.value
    val comments = vm.comments.value

    val likeAnimation = remember {
        mutableStateOf(false)
    }
    val dislikeAnimation = remember {
        mutableStateOf(false)
    }

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
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

            }

            Text(text = post.userName ?: "")
            Text(text = ".", modifier = Modifier.padding(8.dp))

            if (userData?.userId == post.userId) {
                //Current user's post
            } else if (userData?.following?.contains(post.userId) == true) {
                Text(
                    text = "Following",
                    color = Color.Gray,
                    modifier = Modifier.clickable { vm.onFollowClick(post.userId!!) })
            } else {
                Text(
                    text = "Follow",
                    color = Color.Blue,
                    modifier = Modifier.clickable { vm.onFollowClick(post.userId!!) })
            }
        }


    }
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        val modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 150.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        if (post.likes?.contains(userData?.userId ?: "") == true) {
                            dislikeAnimation.value = true
                        }else{
                            likeAnimation.value = true
                        }
                        vm.onLikePost(post)
                    },
                    onTap = {
                        onPostCLick.invoke()
                    }
                )
            }

        commonImage(
            data = post.postImage,
            modifier = modifier,
            contentScale = ContentScale.FillWidth
        )
        if (likeAnimation.value) {
            CoroutineScope(Dispatchers.Main).launch {
                delay(1000L)
                likeAnimation.value = false
            }
            LikeAnimation()
        }
        if (dislikeAnimation.value) {
            CoroutineScope(Dispatchers.Main).launch {
                delay(1000L)
                dislikeAnimation.value = false
            }
            LikeAnimation(false)
        }
    }

    Row(
        modifier = Modifier.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(if (post.likes?.contains(userData?.userId ?: "") == true) R.drawable.ic_like else R.drawable.ic_dislike),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            colorFilter = ColorFilter.tint(if(post.likes?.contains(userData?.userId ?: "") == true) Color.Red else Color.Gray)
        )
        Text(text = " ${post.likes?.size ?: 0} likes", modifier = Modifier.padding(start = 0.dp))

    }

    Row(
        modifier = Modifier.padding(8.dp)
    ) {
        Text(text = post.userName ?: "", fontWeight = FontWeight.Bold)
        Text(text = post.postDescription ?: "", modifier = Modifier.padding(start = 8.dp))

    }

    Row(modifier = Modifier.padding(2.dp)) {
        Text(
            text = "${nbComments} comments",
            color = Color.Gray,
            modifier = Modifier
                .padding(8.dp)
                .clickable {
                    post.postId?.let {
                        navController.navigate(DestinationScreen.CommentScreen.createRoute(it))
                    }
                }
        )

    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp) // Set a fixed height to the Box
    ) {
        LazyColumn() {
            items(items = comments.take(2)) { comment ->
                PostCommentRow(comment)
            }
        }
    }
}

@Composable
fun PostCommentRow(comment: CommentData) {
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
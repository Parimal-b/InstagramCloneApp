package com.example.instagramclone.main

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
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
import com.example.instagramclone.DestinationScreen
import com.example.instagramclone.IgViewModel
import com.example.instagramclone.R
import com.example.instagramclone.data.PostData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.TextStyle
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@Composable
fun FeedScreen(navController: NavController, vm: IgViewModel) {

    val userDataLoading = vm.inProgress.value
    val userData = vm.userData.value
    val personalizedFeed = vm.postsFeed.value
    val personalizedFeedLoading = vm.postsFeedProgress.value

    val helloTexts = listOf(
        "Hello",          // English
        "Bonjour",        // French
        "Hola",           // Spanish
        "Ciao",           // Italian
        "Hallo",          // German
        "Olá",            // Portuguese
        "こんにちは",    // Japanese (Konnichiwa)
        "नमस्ते",        // Hindi (Namaste)
        "السلام",         // Arabic (Salam)
        "你好",            // Chinese (Ni Hao)
        "Selamat pagi",   // Indonesian
        "Zdravstvuyte",   // Russian (Здравствуйте)
        "Merhaba",        // Turkish
        "Sawubona",       // Zulu
        "Szia",           // Hungarian
        "Aloha",          // Hawaiian
        "Shalom",         // Hebrew (שָׁלוֹם)
        "Kamusta",        // Filipino
        "Salam",          // Persian (سلام)
        "Sveiki",         // Lithuanian
        // Add more greetings as needed
    )


    val currentHelloIndex = remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1500L) // Wait for 1 second
            currentHelloIndex.value = (currentHelloIndex.value + 1) % helloTexts.size
        }
    }
    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing = false),
        onRefresh = {
            // Your refresh logic here
            vm.refreshData()
        },
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(Color.White)

            ) {
                userImageCard(userImage = userData?.imageUrl)
                Column {
                    Column {
                        Crossfade(
                            targetState = helloTexts[currentHelloIndex.value],
                            animationSpec = tween(durationMillis = 1000, easing = LinearOutSlowInEasing)
                        ) { targetText ->
                            Text(
                                text = "${targetText},",
                                modifier = Modifier
                                    .padding(top = 12.dp, start = 5.dp)
                                    .width(200.dp)
                                    .height(20.dp),
                                color = Color.Red,
                                style = TextStyle(fontWeight = FontWeight.Bold)
                            )
                        }

                        Text(
                            text = "${userData?.userName}", modifier = Modifier
                                .padding(top = 8.dp, start = 5.dp),
                            style = TextStyle(fontWeight = FontWeight.Bold)
                        )
                    }


                }

                Image(
                    painter = painterResource(id = R.drawable.ig_logo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(70.dp)
                        .padding(end = 0.dp)
                        .align(Alignment.Top)
                )
            }

            PostsList(
                posts = personalizedFeed,
                modifier = Modifier.weight(1f),
                loading = personalizedFeedLoading or userDataLoading,
                navController = navController,
                vm = vm,
                currentUserId = userData?.userId ?: ""
            )
            BottomNavigationMenu(
                selectedItem = BottomNavigationItem.FEED,
                navController = navController
            )
        }
    }

}

@Composable
fun PostsList(
    posts: List<PostData>,
    modifier: Modifier,
    loading: Boolean,
    navController: NavController,
    vm: IgViewModel,
    currentUserId: String
) {
    Box(
        modifier = modifier.background(color = Color.Transparent),

        ) {

        LazyColumn {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxHeight()
                        .background(color = Color(android.graphics.Color.parseColor("#e2e8f0")))
                        .padding(12.dp)
                        .shadow(4.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                    ) {
                        // Other UI components...

                        Text(
                            text = "People You May Follow:",
                            color = Color.Black,
                            modifier = Modifier.padding(top = 8.dp, start = 12.dp, bottom = 8.dp),
                            style = TextStyle(fontWeight = FontWeight.Bold)
                        )

                        LazyRow(
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            items(vm.userRecommendation.value) { user ->
                                UserRecommendationItem(
                                    user = user,
                                    onUserItemClick = { userId ->
                                        vm.getUserProfile(userId)
                                        navController.navigate(
                                            DestinationScreen.userPosts.createRoute(
                                                userId
                                            )
                                        )
                                    }
                                )
                            }
                        }
                    }
                }


            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxHeight()
                        .background(color = Color(android.graphics.Color.parseColor("#e2e8f0")))
                        .padding(12.dp)
                        .shadow(4.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                    ) {
                        // Other UI components...

                        Text(
                            text = "Status :",
                            color = Color.Black,
                            modifier = Modifier.padding(top = 8.dp, start = 12.dp, bottom = 8.dp),
                            style = TextStyle(fontWeight = FontWeight.Bold)
                        )

                        LazyRow(
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            items(vm.status.value) { user ->
                                UserStatusItem(
                                    user = user,
                                    onUserItemClick = { userId ->
                                        vm.getUserProfile(userId)
                                        navController.navigate(
                                            DestinationScreen.SingleStatus.createRoute(
                                                userId
                                            )
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            items(items = posts) { post ->
                Card(
                    modifier = Modifier
                        .fillMaxHeight()
                        .background(color = Color(android.graphics.Color.parseColor("#e2e8f0")))
                        .padding(12.dp)
                        .shadow(4.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Post(
                        navController = navController,
                        post = post,
                        currentUserId = currentUserId,
                        vm = vm
                    ) {
                        navigateTo(
                            navController,
                            DestinationScreen.SinglePost,
                            NavParams("post", post)
                        )
                    }
                }
            }
        }

        if (loading) {
            CommonProgressSpinner()
        }
    }
}

@Composable
fun Post(
    navController: NavController,
    post: PostData,
    currentUserId: String,
    vm: IgViewModel,
    onPostCLick: () -> Unit
) {

    val likeAnimation = remember {
        mutableStateOf(false)
    }
    val dislikeAnimation = remember {
        mutableStateOf(false)
    }

    Card(
        shape = RoundedCornerShape(corner = CornerSize(4.dp)),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(color = Color.White)
    ) {
        Column(
            modifier = Modifier.background(Color.White)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(color = Color.White),

                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    shape = CircleShape,
                    modifier = Modifier
                        .padding(4.dp)
                        .size(32.dp)
                ) {
                    commonImage(data = post.userImage, contentScale = ContentScale.Crop)
                }
                Text(text = post.userName ?: "", modifier = Modifier.padding(4.dp))
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
                                if (post.likes?.contains(currentUserId) == true) {
                                    dislikeAnimation.value = true
                                } else {
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
            likeCommentItem(
                post = post,
                vm = vm,
                onCommentClick = {
                    vm.getComments(postId = post.postId)
                    navController.navigate(DestinationScreen.CommentScreen.createRoute(post.postId!!))
                }
            )
        }
    }
}


@Composable
fun likeCommentItem(post: PostData, vm: IgViewModel, onCommentClick: () -> Unit) {

    val userData = vm.userData.value
    val commentData = vm.comments.value

    Row(
        modifier = Modifier.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(
                if (post.likes?.contains(
                        userData?.userId ?: ""
                    ) == true
                ) R.drawable.ic_like else R.drawable.ic_dislike
            ),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            colorFilter = ColorFilter.tint(
                if (post.likes?.contains(
                        userData?.userId ?: ""
                    ) == true
                ) Color.Red else Color.Gray
            )
        )
        Image(
            painter = painterResource(id = R.drawable.ic_comment),
            contentDescription = null,
            modifier = Modifier
                .size(35.dp)
                .padding(start = 12.dp)
                .clickable {
                    onCommentClick.invoke()
                },
            colorFilter = ColorFilter.tint(Color.Gray)
        )

    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(text = post.userName ?: "", fontWeight = FontWeight.Bold, color = Color.Gray)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = post.postDescription ?: "", fontWeight = FontWeight.Bold)
    }
}












package com.example.instagramclone.main

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.instagramclone.DestinationScreen
import com.example.instagramclone.IgViewModel
import com.example.instagramclone.R
import com.example.instagramclone.data.PostData
import com.example.instagramclone.data.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

data class PostRow(
    var post1: PostData? = null,
    var post2: PostData? = null,
    var post3: PostData? = null
) {

    fun isFull() = post1 != null && post2 != null && post3 != null
    fun add(post: PostData) {
        if (post1 == null) {
            post1 = post
        } else if (post2 == null) {
            post2 = post
        } else if (post3 == null) {
            post3 = post
        }
    }
}

@Composable
fun MyPostsScreen(navController: NavController, vm: IgViewModel) {

    val isLoading = vm.inProgress.value

    val postsLoading = vm.refreshPostsProgress.value
    val posts = vm.posts.value

    Column {
        Column(modifier = Modifier.weight(1f)) {
            Column(
                modifier = Modifier
                    .padding(1.dp)
                    .fillMaxSize()
            ) {
                PostList(
                    isContextLoading = isLoading,
                    postsLoading = postsLoading,
                    posts = posts,
                    modifier = Modifier.weight(1f),
                    vm,
                    navController,
                ) { post ->
                    //OnPost Click Method
                    navigateTo(
                        navController = navController,
                        DestinationScreen.SinglePost,
                        NavParams("post", post)
                    )
                }
            }


        }

        BottomNavigationMenu(
            selectedItem = BottomNavigationItem.POSTS,
            navController = navController
        )
    }

    if (isLoading) {
        CommonProgressSpinner()
    }
}

@Composable
fun ProfileImage(imageUrl: String?, onClick: () -> Unit) {
    Box(modifier = Modifier
        .padding(top = 16.dp)
        .clickable { onClick.invoke() }) {

        userImageCard(
            userImage = imageUrl, modifier = Modifier
                .padding(8.dp)
                .size(80.dp)
        )

        Card(
            shape = CircleShape,
            border = BorderStroke(width = 2.dp, color = Color.White),
            modifier = Modifier
                .size(32.dp)
                .align(Alignment.BottomEnd)
                .padding(bottom = 8.dp, end = 8.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_add), contentDescription = null,
                modifier = Modifier.background(Color.Blue)

            )
        }
    }
}

@Composable
fun PostList(
    isContextLoading: Boolean,
    postsLoading: Boolean,
    posts: List<PostData>,
    modifier: Modifier,
    vm: IgViewModel,
    navController: NavController,
    onPostClick: (PostData) -> Unit
) {

    val followingUserList = vm.userData.value?.following


    val newPostImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        uri?.let {
            val encoded = Uri.encode(it.toString())
            val route = DestinationScreen.NewPost.createRoute(encoded)
            navController.navigate(route)
        }
    }

    val userData = vm.userData.value

    val followers = vm.followers.value
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

            item {
                Box {
                    ProfileBgImage(imageUrl = userData?.imageUrl, modifier = Modifier.alpha(0.4f))
                    Column(modifier = Modifier.align(Alignment.Center)){
                        Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                            ProfileImage(userData?.imageUrl) {
                                newPostImageLauncher.launch("image/*")
                            }
                        }
                        Column(modifier = Modifier
                            .padding(5.dp)
                            .align(Alignment.CenterHorizontally)) {
                            val userNameDisplay =
                                if (userData?.userName == null) "" else "@${userData?.userName}"
                            Text(text = userNameDisplay, modifier = Modifier.align(Alignment.CenterHorizontally), fontSize = 32.sp)
                            Text(text = userData?.name ?: "", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
                            Text(text = userData?.bio ?: "", modifier = Modifier.align(Alignment.CenterHorizontally))
                        }
                        OutlinedButton(
                            onClick = { navigateTo(navController, DestinationScreen.Profile) },
                            modifier = Modifier
                                .padding(start = 48.dp, end = 48.dp)
                                .fillMaxWidth()
                                .background(Color.Transparent),
                            colors = ButtonDefaults.buttonColors(Color.Transparent),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 0.dp,
                                pressedElevation = 0.dp,
                                disabledElevation = 0.dp
                            ),
                            shape = RoundedCornerShape(10)
                        ) {
                            Text(text = "Edit Profile", color = Color.Black, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif)
                        }

                    }


                }
                Row(
                    modifier = Modifier.background(Color(0x80B01A86))
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .align(Alignment.CenterVertically)
                            .padding(vertical = 8.dp, horizontal = 2.dp) // Adjusted padding
                            .border(1.dp, Color.White )
                    ) {
                        Text(
                            text = "${posts.size}\nposts",
                            modifier = Modifier.align(Alignment.Center),
                            textAlign = TextAlign.Center,
                            color = Color.White // Set text color to white
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .align(Alignment.CenterVertically)
                            .padding(8.dp) // Adjusted padding
                            .border(1.dp, Color.White)
                            .clickable {
                                vm.getCurrentFollowers(userData?.userId!!)
                                navController.navigate(
                                    DestinationScreen.Followers.createRoute(
                                        userData?.userId!!
                                    )
                                )
                            }
                    ) {
                        Text(
                            text = "$followers\nfollowers",
                            modifier = Modifier.align(Alignment.Center),
                            textAlign = TextAlign.Center,
                            color = Color.White // Set text color to white
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .align(Alignment.CenterVertically)
                            .padding(8.dp) // Adjusted padding
                            .border(1.dp, Color.White)
                            .clickable {
                                navController.navigate(
                                    DestinationScreen.Following.createRoute(
                                        userData?.userId!!
                                    )
                                )
                                if (followingUserList != null) {
                                    vm.getFollowingData(followingUserList)
                                }
                            }
                    ) {
                        Text(
                            text = "${userData?.following?.size ?: 0}\nfollowing",
                            modifier = Modifier.align(Alignment.Center),
                            textAlign = TextAlign.Center,
                            color = Color.White // Set text color to white
                        )
                    }
                }

            }
            items(items = rows) { row ->

                PostRow(item = row, onPostClick = onPostClick)

            }
        }
    }
}

@Composable
fun PostRow(item: PostRow, onPostClick: (PostData) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        PostImage(imageUrl = item.post1?.postImage, modifier = Modifier
            .weight(1f)
            .clickable { item.post1?.let { post -> onPostClick(post) } }
        )
        PostImage(imageUrl = item.post2?.postImage, modifier = Modifier
            .weight(1f)
            .clickable { item.post2?.let { post -> onPostClick(post) } }
        )
        PostImage(imageUrl = item.post3?.postImage, modifier = Modifier
            .weight(1f)
            .clickable { item.post3?.let { post -> onPostClick(post) } }
        )
    }
}

@Composable
fun PostImage(imageUrl: String?, modifier: Modifier) {
    Box(modifier = modifier) {
        var modifier = Modifier
            .padding(3.dp)
            .fillMaxSize()
        if (imageUrl == null) {
            modifier = modifier.clickable(enabled = false) { }
        }
        commonImage(data = imageUrl, modifier = modifier, contentScale = ContentScale.Crop)

    }
}

@Composable
fun ProfileBgImage(imageUrl: String?, modifier: Modifier) {
    Box(modifier = modifier) {
        var modifier = Modifier
            .padding(1.dp)
            .fillMaxWidth()
            .aspectRatio(1.4f)
        if (imageUrl == null) {
            modifier = modifier.clickable(enabled = false) { }
        }
        commonImage(data = imageUrl, modifier = modifier, contentScale = ContentScale.Crop)

    }
}

@Composable
fun ProfileFillBgColor(modifier: Modifier) {
    Box(modifier = modifier.background(Color.Red))
}

@Preview
@Composable
fun PreviewMyPostsScreen() {
    val navController = rememberNavController()
    val viewModel = IgViewModel(
        FirebaseAuth.getInstance(),
        FirebaseFirestore.getInstance(),
        FirebaseStorage.getInstance()
    )
    MyPostsScreen(navController, viewModel)
}





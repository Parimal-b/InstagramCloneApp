package com.example.instagramclone.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.instagramclone.DestinationScreen
import com.example.instagramclone.IgViewModel

@Composable
fun SearchScreen(navController: NavController, vm: IgViewModel) {

    val searchedPostsLoading = vm.searchedPostsProgress.value
    val searchedPosts = (vm.searchedPosts.value + vm.searchedPostsByUser.value).distinctBy { it.postImage }
    val searchedPeople = (vm.searchedPeople.value + vm.searchedPeopleByPost.value).distinctBy { it.userId }
    var searchTerm by rememberSaveable {
        mutableStateOf("")
    }

    Column {
        SearchBar(
            searchTerm = searchTerm,
            onSearchChange = { searchTerm = it },
            onSearch = {
                vm.searchPosts(searchTerm)
            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(color = Color.LightGray)
        ) {
            Text(text = "Posts", fontWeight = FontWeight.Bold, color = Color.Black,  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
        }
        PostList(
            isContextLoading = false,
            postsLoading = searchedPostsLoading,
            posts = searchedPosts,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp)

        ) { post ->
            navigateTo(
                navController = navController,
                dest = DestinationScreen.SinglePost,
                NavParams("post", post)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(color = Color.LightGray)
        ) {
            Text(text = "People", fontWeight = FontWeight.Bold, color = Color.Black,  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
        }
        if (searchedPeople.isEmpty()){
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center

            ) {
                Text(text = "No People Available")
            }
        }else{
            Box(
                modifier = Modifier
                    .background(color = Color.White)
                    .weight(1f),

                ) {
                LazyColumn {
                    items(items = searchedPeople) { item ->
                        val name = item.userName

                        if (name != null) {
                            FollowingsList(userName = name, userImage = item.userImage ?: "", item.userId!!, vm, navController)
                        }
                    }
                }
            }
        }

        BottomNavigationMenu(
            selectedItem = BottomNavigationItem.SEARCH,
            navController = navController
        )
    }
}

@Composable
fun SearchBar(searchTerm: String, onSearchChange: (String) -> Unit, onSearch: () -> Unit) {
    val focusManager = LocalFocusManager.current

    TextField(
        value = searchTerm,
        onValueChange = onSearchChange,
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .border(1.dp, Color.Gray, CircleShape),
        shape = CircleShape,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                onSearch()
                focusManager.clearFocus()
            }
        ),
        maxLines = 1,
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
        ),
        trailingIcon = {
            IconButton(onClick = {
                onSearch()
                focusManager.clearFocus()
            }) {
                Icon(imageVector = Icons.Default.Search, contentDescription = null)
            }
        },
        label = {
            Text(text = "Search Posts here")
        }

    )

}

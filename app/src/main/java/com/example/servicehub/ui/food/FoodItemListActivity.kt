package com.example.servicehub.ui.food

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.servicehub.viewmodel.FoodItemListViewModel

private const val BASE_IMAGE_URL = "https://jmsn.in//images//appimage//"
private fun fullImageUrl(img: String?): String? {
    if (img.isNullOrBlank()) return null
    if (img.startsWith("http", true)) return img
    return BASE_IMAGE_URL + img.trim().removePrefix("/")
}

// Intent extras (must match what you put from FoodActivity)
private const val EXTRA_LIST_ID = "listid"
private const val EXTRA_TITLE = "title"

class FoodItemListActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val listId = intent.getStringExtra(EXTRA_LIST_ID).orEmpty()
        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty()
        Log.d("typeidddddd",listId);
        setContent {
            FoodItemListScreen(
                listId = listId,
                title = title,
                onBack = { finish() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FoodItemListScreen(
    listId: String,
    title: String,
    onBack: () -> Unit,
    vm: FoodItemListViewModel = viewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
   // var query by remember { mutableStateOf("") } // UI only for now

    LaunchedEffect(listId) {
        Log.d("FoodItemListScreen", "COMPOSABLE ENTERED listId=$listId title=$title")
        if (listId.isNotBlank()) vm.loadItems(listId)
    }

    Scaffold(
        containerColor = Color(0xFFF7F7F7),
        topBar = {
            TopAppBar(
                title = { Text(text = title.ifBlank { "Items" }) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->

        when {
            state.loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            state.error != null -> {
                Text(
                    text = "Error: ${state.error}",
                    color = Color.Red,
                    modifier = Modifier.padding(16.dp)
                )
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {

                    // Optional search (UI only)
//                    OutlinedTextField(
//                        value = query,
//                        onValueChange = { query = it },
//                        placeholder = { Text("Search") },
//                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
//                        singleLine = true,
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(14.dp),
//                        colors = OutlinedTextFieldDefaults.colors(
//                            unfocusedContainerColor = Color.White,
//                            focusedContainerColor = Color.White
//                        ),
//                        shape = RoundedCornerShape(12.dp)
//                    )


                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(state.items) { item ->
                            FoodItemCard(
                                name = item.name.orEmpty(),
                                imageUrl = fullImageUrl(item.imgsrc)
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(30.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FoodItemCard(
    name: String,
    imageUrl: String?
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2
            )
        }
    }
}
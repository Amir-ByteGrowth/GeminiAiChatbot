package com.example.geminiaichatbot

import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.launch

@Composable
fun AppContent(viewModel: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {

    val appUiState = viewModel.uiState.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val imageRequestBuilder = ImageRequest.Builder(LocalContext.current)
    val imageLoader = ImageLoader.Builder(LocalContext.current).build()

    HomeScreen(uiState = appUiState.value) { inputText, selectedImages ->
        coroutineScope.launch {
            val bitmaps = selectedImages.mapNotNull {
                val imageRequest = imageRequestBuilder.data(it).size(768).build()
                val imageResult = imageLoader.execute(imageRequest)
                if (imageResult is SuccessResult) {
                    return@mapNotNull (imageResult.drawable as BitmapDrawable).bitmap
                } else {
                    return@mapNotNull null
                }
            }

            viewModel.questioning(inputText,bitmaps)
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState = HomeUiState.Loading,
    onSendClicked: (String, List<Uri>) -> Unit
) {

    var userQues by rememberSaveable {
        mutableStateOf("")
    }

    val imageUris = rememberSaveable(saver = UriCustomSaver()) {
        mutableStateListOf()
    }

    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = {
            it?.let {
                imageUris.add(it)
            }
        }
    )


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Gemini Ai ChatBot") },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            Column {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    // upload image
                    IconButton(onClick = {
                        pickMediaLauncher.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    }, modifier = Modifier.padding(4.dp)) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "To Add Some Image"
                        )
                    }
                    //input query
                    OutlinedTextField(
                        value = userQues,
                        onValueChange = { userQues = it },
                        placeholder = {
                            Text(
                                text = "EUpload Image and ask questions ",
                                style = TextStyle(fontSize = 11.sp)
                            )
                        }, modifier = Modifier.fillMaxWidth(0.82f)
                    )

                    // Send Button
                    IconButton(onClick = {
                        if (userQues.isNotBlank()) {
                            onSendClicked(userQues, imageUris)
                        }
                    }, modifier = Modifier.padding(4.dp)) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = " Send"
                        )
                    }
                }

                AnimatedVisibility(visible = imageUris.size>0) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        LazyRow(modifier = Modifier.padding(16.dp)) {
                            items(imageUris) { imageUri ->
                                Column (horizontalAlignment = Alignment.CenterHorizontally){
                                    AsyncImage(model = imageUri, contentDescription = "", modifier = Modifier.size(50.dp))
                                    TextButton(onClick = { imageUris.remove(imageUri) }) {
                                        Text(text = "Remove")
                                    }
                                }
                            }

                        }
                    }

                }


            }
        }
    )
    { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize().background(color = Color.Green)
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(
                    rememberScrollState(),
                ),
        ) {
            when (uiState) {
                is HomeUiState.Initial -> {}
                is HomeUiState.Loading -> {
                    Box(modifier = Modifier.background(color = Color.Red).fillMaxSize(),contentAlignment = Alignment.Center, ) {
                        CircularProgressIndicator()
                    }
                }

                is HomeUiState.Success -> {
                    Card(
                        modifier = Modifier

                            .fillMaxWidth(), shape = MaterialTheme.shapes.large
                    ) {
                        Text(text = uiState.outputText, modifier = Modifier.padding(15.dp))
                    }
                }

                is HomeUiState.Failure -> {
                    Card(
                        modifier = Modifier
                            .padding(vertical = 10.dp)
                            .fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Text(text = uiState.errorText)
                    }
                }
            }


        }
    }
}
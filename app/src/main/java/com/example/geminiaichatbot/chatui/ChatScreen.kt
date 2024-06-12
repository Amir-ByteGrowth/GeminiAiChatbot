package com.example.geminiaichatbot.chatui

import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.geminiaichatbot.UriCustomSaver
import com.example.geminiaichatbot.models.ConversationModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun ChatContent(viewModel: ChatViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val appUiState = viewModel.uiState.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val imageRequestBuilder = ImageRequest.Builder(LocalContext.current)
    val imageLoader = ImageLoader.Builder(LocalContext.current).build()

    ChatScreen(uiState = appUiState.value) { inputText, selectedImages ->
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

            viewModel.questioning(inputText, bitmaps)
        }
    }
}

@Composable
fun scrollToBottom(lazyListState: LazyListState) {
    LaunchedEffect(Unit) {
        delay(150) // Small delay to ensure the list has been updated
        lazyListState.animateScrollToItem(Int.MAX_VALUE)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    uiState: HomeUiState = HomeUiState.Loading,
    onSendClicked: (String, List<Uri>) -> Unit
) {


//    val messages = remember { mutableStateListOf<ConversationModel>() }

    // Create a mutable list to hold the messages
    val messages = remember { mutableListOf<ConversationModel>() }
    var uiMessages by remember { mutableStateOf(listOf<ConversationModel>()) }


    ////


    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    val lazyListState = rememberLazyListState()


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

    // Function to update the UI messages
    fun updateUiMessages() {
        uiMessages = messages.toList()

    }

    when (uiState) {
        is HomeUiState.Initial -> {}
        is HomeUiState.Loading -> {
            messages.add(ConversationModel(type = ChatItemsUi.LOADING))
            // Function to update the UI messages
            updateUiMessages()
            scrollToBottom(lazyListState)
        }
        is HomeUiState.RemoveLoading ->{
            if (messages.any { it.type == ChatItemsUi.LOADING })
                messages.remove(messages.last { it.type == ChatItemsUi.LOADING })

        }


        is HomeUiState.Success -> {
            if (messages.any { it.id == uiState.id
            }) {
                messages.removeAt(messages.indexOfFirst { it.id == uiState.id })
                messages.add(
                    ConversationModel(
                        message = uiState.outputText,
                        type = ChatItemsUi.SUCCESS,
                        senderType = SenderType.BOOT,
                        id = uiState.id
                    )
                )
                scrollToBottom(lazyListState)

             Log.d("ContainsId","true")
            }else{
                messages.add(
                    ConversationModel(
                        message = uiState.outputText,
                        type = ChatItemsUi.SUCCESS,
                        senderType = SenderType.BOOT,
                        id = uiState.id
                    )
                )
                scrollToBottom(lazyListState)
            }

            // Function to update the UI messages
            updateUiMessages()
        }

        is HomeUiState.Failure -> {
            messages.add(ConversationModel(message = uiState.errorText, type = ChatItemsUi.ERROR))
            updateUiMessages()
        }
    }





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
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                                text = "Upload Image and ask questions ",
                                style = TextStyle(fontSize = 11.sp)
                            )
                        }, modifier = Modifier
                            .fillMaxWidth(0.82f)
                            .focusRequester(focusRequester)
                            .imePadding(), // Ensure the TextField moves above the keyboard
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                            }
                        )
                    )

                    // Send Button
                    IconButton(onClick = {
                        if (userQues.isNotBlank()) {
                            messages.add(
                                ConversationModel(
                                    message = userQues,
                                    type = ChatItemsUi.SUCCESS,
                                    senderType = SenderType.USER
                                )
                            )
                            onSendClicked(userQues, imageUris)
                        }
                    }, modifier = Modifier.padding(4.dp)) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = " Send"
                        )
                    }
                }

                AnimatedVisibility(visible = imageUris.size > 0) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        LazyRow(modifier = Modifier.padding(16.dp)) {
                            items(imageUris) { imageUri ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    AsyncImage(
                                        model = imageUri,
                                        contentDescription = "",
                                        modifier = Modifier.size(50.dp)
                                    )
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .height(500.dp), state = lazyListState
        ) {

            items(uiMessages) { message ->
                when (message.type) {
                    ChatItemsUi.LOADING -> {
                        ProgressBarItem()
                    }

                    ChatItemsUi.SUCCESS -> {
                        MessageItem(message)
                    }

                    ChatItemsUi.ERROR -> {
                        ErrorMessageItem(message.message)
                    }
                }
            }

        }
    }
}
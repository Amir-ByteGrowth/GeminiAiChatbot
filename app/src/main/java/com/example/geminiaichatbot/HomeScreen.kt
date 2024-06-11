package com.example.geminiaichatbot

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AppContent(viewModel: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {

    val appUiState = viewModel.uiState.collectAsState()
    HomeScreen(uiState = appUiState.value){
        inputText, selectedImages ->
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
                    IconButton(onClick = { /*TODO*/ }, modifier = Modifier.padding(4.dp)) {
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
                                text = "EUpload Image and ask questions ", style = TextStyle(fontSize = 11.sp)
                            )
                        }, modifier = Modifier.fillMaxWidth(0.82f))

                    // Send Button
                    IconButton(onClick = { /*TODO*/ }, modifier = Modifier.padding(4.dp)) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = " Send"
                        )
                    }
                }

            }
        }
    )
    { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(
                    rememberScrollState(),
                )
        ) {
            when (uiState) {
                is HomeUiState.Initial -> {}
                is HomeUiState.Loading -> {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is HomeUiState.Success -> {
                    Card(
                        modifier = Modifier
                            .padding(vertical = 10.dp)
                            .fillMaxWidth(), shape = MaterialTheme.shapes.large
                    ) {
                        Text(text = uiState.outputText)
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
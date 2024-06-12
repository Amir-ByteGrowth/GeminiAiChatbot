package com.example.geminiaichatbot.chatui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.geminiaichatbot.models.ConversationModel


@Composable
fun MessageItem(conversationModel: ConversationModel) {
    Card(
        modifier = Modifier
            .padding(
                start = if (conversationModel.type == 0) 30.dp else 0.dp,
                end = if (conversationModel.type == 1) 30.dp else 0.dp
            )
            .fillMaxWidth(), shape = MaterialTheme.shapes.large
    ) {
        Text(text = conversationModel.message, modifier = Modifier.padding(15.dp))
    }
}

@Composable
fun ErrorMessageItem(errorMessage: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth(), shape = MaterialTheme.shapes.large
    ) {
        Text(
            text = errorMessage,
            modifier = Modifier
                .padding(15.dp)
                .fillMaxWidth(),
            style = TextStyle.Default.copy(color = Color.Red),
            textAlign = TextAlign.Center
        )
    }
}


@Composable
fun ProgressBarItem(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}


//@Preview
//@Composable
//fun PreviewMessage() {
////    MessageItem(conversationModel = ConversationModel("Hi there", type = 1))
//    ErrorMessageItem("Error")
//}

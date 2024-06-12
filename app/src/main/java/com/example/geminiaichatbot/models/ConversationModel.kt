package com.example.geminiaichatbot.models

import com.example.geminiaichatbot.chatui.ChatItemsUi
import com.example.geminiaichatbot.chatui.SenderType

data class ConversationModel(
    var message: String = "",
    var type: ChatItemsUi = ChatItemsUi.LOADING,
    var senderType: SenderType = SenderType.USER,var id :Int = -1
)
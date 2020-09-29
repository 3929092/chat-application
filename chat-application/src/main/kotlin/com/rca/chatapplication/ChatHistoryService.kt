package com.rca.chatapplication

import kotlin.math.min

class ChatHistoryService {
    private val chatHistory = mutableListOf<UserMessage>()

    fun saveUserMessage(userMessage: UserMessage) = chatHistory.add(userMessage)

    fun getChatHistory(historyLength: Int): List<UserMessage> = chatHistory.subList(chatHistory.size - min(historyLength, chatHistory.size), chatHistory.size)

}
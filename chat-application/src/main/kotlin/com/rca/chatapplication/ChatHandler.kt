package com.rca.chatapplication

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler

class ChatHandler : TextWebSocketHandler() {
    private val log = LoggerFactory.getLogger(javaClass)

    private val connectedUsers = HashMap<String, WebSocketSession>()
    private val historyRequestRegex = """\bhistory ([5-9]|[1-9][0-9]|100)\b""".toRegex()
    private val chatHistoryService = ChatHistoryService()

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val json = ObjectMapper().readTree(message.payload)
        val user = json.get("user").asText()

        log.info("-Session: {} - Received: {} - Connected users: {}", session.id, json, connectedUsers.keys.map { it })

        when (json.get("type").asText()) {
            "say" -> {
                val text = json.get("text").asText()

                if (historyRequestRegex.matches(text)) {
                    val historyLength = historyRequestRegex.find(text)!!.groupValues[1].toInt()
                    emit(session, MessageFrame("history", chatHistoryService.getChatHistory(historyLength)))
                } else {
                    val userMessage = UserMessage(user, text, System.currentTimeMillis())
                    chatHistoryService.saveUserMessage(userMessage)
                    broadcast(MessageFrame("say", userMessage))
                }
            }
            "join" -> {
                // if username is already taken
                if (connectedUsers.contains(user)) {
                    log.info("Username {} is already taken", user)
                    emit(session, MessageFrame("error", "This username is already taken"))
                } else {
                    connectedUsers[user] = session

                    // send back the list of all connected users
                    emit(session, MessageFrame("users", connectedUsers.keys))

                    // tell the other connected users about this new user
                    broadcastToOthers(session, MessageFrame("join", user))
                }
            }
            "leave" -> {
                connectedUsers.remove(user)
                broadcastToOthers(session, MessageFrame("left", user))
            }

        }
    }

    private fun emit(session: WebSocketSession, msg: MessageFrame) {
        if (session.isOpen) {
            session.sendMessage(TextMessage(jacksonObjectMapper().writeValueAsString(msg)))
        }
    }

    private fun broadcast(msg: MessageFrame) = connectedUsers.forEach { emit(it.value, msg) }

    private fun broadcastToOthers(self: WebSocketSession, msg: MessageFrame) = connectedUsers.filter { it.value != self }.forEach { emit(it.value, msg) }
}
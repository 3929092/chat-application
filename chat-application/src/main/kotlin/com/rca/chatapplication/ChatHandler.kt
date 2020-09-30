package com.rca.chatapplication

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.ConcurrentHashMap

/**
 * Handles incoming WebSocket messages.
 * Defines a simple 'protocol' for communication between clients and server, based on the following types of messages:
 * - join
 * - say
 * - leave
 * - users
 * - left
 * - history
 */
class ChatHandler(private val chatHistoryService: ChatHistoryService) : TextWebSocketHandler() {
    private val log = LoggerFactory.getLogger(javaClass)

    private val connectedUsers = ConcurrentHashMap<String, WebSocketSession>()
    private val historyRequestRegex = """\bhistory ([5-9]|[1-9][0-9]|100)\b""".toRegex()

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        log.info("WebSocketSession: {} - Received: {} - Connected users: {}", session.id, message.payload, connectedUsers.keys)

        val json = ObjectMapper().readTree(message.payload)
        val user = json.get("user").asText()

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
package com.rca.chatapplication

import org.junit.jupiter.api.Assertions.assertEquals
import org.json.JSONArray
import org.json.JSONObject
import org.junit.jupiter.api.*
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient
import org.springframework.web.reactive.socket.client.WebSocketClient
import reactor.core.publisher.EmitterProcessor
import reactor.core.publisher.Mono
import java.net.URI
import java.util.concurrent.CountDownLatch

/**
 * Integration test for Chat Application
 * It starts the server, then simulate the interaction between 3 users:
 * - User 1 joins chat
 * - User 2 joins chat and say something
 * - User 3 joins chat and leave
 * Tests are dependant from each other, the order of execution matters
 */
@SpringBootTest(classes = [ChatApplication::class], webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class ChatApplicationTests {

    companion object {
        val CHAT_HOST: URI = URI.create("ws://localhost/chat")
        const val USER_1 = "User1"
        const val USER_2 = "User 2"
        const val USER_3 = "User 3"
        const val MESSAGE_1 = "Message one"
    }

    fun JSONArray.toMutableList(): MutableList<Any> = MutableList(length(), this::get)

    val outputUserOne = EmitterProcessor.create<Any>()
    val collectorUserOne = mutableListOf<String>()

    @BeforeAll
    internal fun setup() {
        val latch = CountDownLatch(1)

        val client: WebSocketClient = ReactorNettyWebSocketClient()
        client.execute(CHAT_HOST) { session ->
            session.send(Mono.just(session.textMessage("""{"type": "join", "user": "$USER_1" }""")))
                    .thenMany(session.receive()
                            .map(WebSocketMessage::getPayloadAsText).log()
                            .subscribeWith(outputUserOne).then()).then()
        }.subscribe()

        outputUserOne
                .subscribe { i: Any ->
                    collectorUserOne.add(i.toString())
                    latch.countDown()
                }

        latch.await()
    }

    @Test
    @Order(1)
    fun joinAndSay() {
        // User 2: join chat then say something
        val collector = sendAndReceiveTwoMessages("""{"type": "join", "user": "$USER_2" }""", """{"type": "say", "user": "$USER_2", "text": "$MESSAGE_1" }""")

        // first received message: list of connected users
        assertEquals("users", JSONObject(collector[0]).get("type"))
        assertEquals(listOf(USER_1, USER_2), (JSONObject(collector[0]).get("data") as JSONArray).toMutableList())

        // second received message
        assertEquals("say", JSONObject(collector[1]).get("type"))
        assertEquals(USER_2, (JSONObject(collector[1]).get("data") as JSONObject).get("user"))
        assertEquals("$MESSAGE_1", (JSONObject(collector[1]).get("data") as JSONObject).get("text"))

        // User 1 should have received "join" and "say" messages
        assertEquals("join", JSONObject(collectorUserOne[1]).get("type"))
        assertEquals(USER_2, JSONObject(collectorUserOne[1]).get("data"))

        assertEquals("say", JSONObject(collectorUserOne[2]).get("type"))
        assertEquals(USER_2, (JSONObject(collectorUserOne[2]).get("data") as JSONObject).get("user"))
        assertEquals("$MESSAGE_1", (JSONObject(collectorUserOne[2]).get("data") as JSONObject).get("text"))
    }

    @Test
    @Order(2)
    fun joinAndLeave() {
        // User 3: join chat then leave
        val collector = sendAndReceiveTwoMessages("""{"type": "join", "user": "$USER_3" }""", """{"type": "leave", "user": "$USER_3"}""")

        // User 3 first received message: list of connected users
        assertEquals("users", JSONObject(collector[0]).get("type"))
        assertEquals(listOf(USER_1, USER_2, USER_3), (JSONObject(collector[0]).get("data") as JSONArray).toMutableList())

        // User 1 should receive "User 3 left" message
        assertEquals("left", JSONObject(collectorUserOne[4]).get("type"))
        assertEquals(USER_3, JSONObject(collectorUserOne[4]).get("data"))
    }

    private fun sendAndReceiveTwoMessages(first: String, second: String): List<String> {
        val output = EmitterProcessor.create<Any>()
        val latch = CountDownLatch(2)

        val client: WebSocketClient = ReactorNettyWebSocketClient()
        client.execute(CHAT_HOST) { session ->
            session.send(Mono.just(session.textMessage(first)))
                    .then(session.send(Mono.just(session.textMessage(second))))
                    .thenMany(session.receive()
                            .map(WebSocketMessage::getPayloadAsText).log()
                            .subscribeWith(output).then())
                    .then()
        }.subscribe()

        val collector = mutableListOf<String>()
        output
                .subscribe { i: Any ->
                    collector.add(i.toString())
                    latch.countDown()
                }

//        latch.await()
        Thread.sleep(1000)

        return collector
    }

}

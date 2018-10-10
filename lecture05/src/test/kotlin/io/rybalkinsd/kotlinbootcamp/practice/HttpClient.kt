package io.rybalkinsd.kotlinbootcamp.practice

import io.rybalkinsd.kotlinbootcamp.practice.client.ChatClient
import io.rybalkinsd.kotlinbootcamp.util.logger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Test

typealias Client = ChatClient

@Ignore
class ChatClientTest {
    companion object {
        // Change this to your Last name
        private const val MY_NAME_IN_CHAT = "Vasya"
        // Change this to any non-swear text
        private const val MY_MESSAGE_TO_CHAT = "lol"
        private val log = logger()
    }

    @Test
    fun login() {
        val response = Client.login(MY_NAME_IN_CHAT).also { println(it) }
        assertTrue(response.code() == 200 || response.code() == 400)
    }

    @Test
    fun viewHistory() {
        val response = Client.viewHistory().also {
            println(it.body()!!.string())
            println(it.headers()["cookie"])
        }
        assertEquals(200, response.code())
    }

    @Test
    fun viewOnline() {
        val response = Client.viewOnline().also { println(it) }
        assertEquals(200, response.code())
    }

    @Test
    fun say() {
        val response = Client.say(MY_NAME_IN_CHAT, MY_MESSAGE_TO_CHAT).also { println() }
        assertEquals(200, response.code())
    }

    @Test
    fun logout() {
        Client.logout(MY_NAME_IN_CHAT).use {
            println(it.body()!!.string())
            println(it.headers()["cookies"])
            assertTrue(it.code() == 200 || it.code() == 400)
        }
    }
}

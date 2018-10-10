package io.rybalkinsd.kotlinbootcamp.practice.client

import com.kohttp.dsl.httpGet
import com.kohttp.dsl.httpPost
import okhttp3.Request
import okhttp3.Response

object ChatClient {
    // Change to server url
    private const val HOST = "54.224.37.210"
//    private const val HOST = "localhost"
    private const val PORT = 8080

    /**
     * POST /chat/login?name=my_name
     */
    fun login(name: String) = httpPost {
        host = HOST
        port = PORT
        path = "/chat/login"
        body {
            form {
                "name" to name
            }
        }
    }

    /**
     * GET /chat/history
     */
    fun viewHistory(): Response = httpGet {
        host = HOST
        port = PORT
        path = "/chat/history"
    }

    /**
     * POST /chat/say
     *
     * Body: "name=my_name&msg='my_message'"
     */
    fun say(name: String, msg: String): Response = httpPost {
        host = HOST
        port = PORT
        path = "/chat/say"
        body {
            form {
                "name" to name
                "msg" to msg
            }
        }
    }

    /**
     * GET /chat/online
     */
    fun viewOnline(): Response = httpGet {
        host = HOST
        port = PORT
        path = "/chat/online"
    }

    /**
     * POST /chat/logout?name=my_name
     */
    fun logout(name: String): Response {
        val request = Request.Builder().apply {
            url("http://$HOST:$PORT/chat/logout?name=$name")
            delete()
        }.build()
        return okhttp3.OkHttpClient.Builder().build().newCall(
            request
        ).execute()
    }
}

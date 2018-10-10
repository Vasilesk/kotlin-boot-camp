package io.rybalkinsd.kotlinbootcamp.practice.server

import io.rybalkinsd.kotlinbootcamp.util.logger
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.CookieValue
import java.util.Queue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

import com.beust.klaxon.Json
import com.beust.klaxon.Klaxon
import okhttp3.Request

data class VkInfo(
    @Json(name = "access_token")
    val token: String,
    @Json(name = "user_id")
    val userId: Int,
    @Json(name = "expires_in")
    val expiresIn: Int
)

data class UserResponse(
    @Json(name = "response")
    val response: List<UserData>
)

data class UserData(
    @Json(name = "id")
    val userId: Int,
    @Json(name = "first_name")
    val nameF: String,
    @Json(name = "last_name")
    val nameS: String
)

@Controller
@RequestMapping("/chat")
class ChatController {
    val log = logger()
    val messages: Queue<String> = ConcurrentLinkedQueue()
    val usersOnline: MutableMap<Int, Pair<String, String>> = ConcurrentHashMap()
    val appId = "4311097"
    val appToken = "v2JkVyqcw5cPz6SbHW4p"
    val redirectUrl = "http://kotlin.vasilesk.ru/chat/codeprocess"

    fun getToken(code: String): Pair<Int, String>? {
        val url = "https://oauth.vk.com/access_token/?client_id=$appId&client_secret=$appToken&code=$code&redirect_uri=$redirectUrl"
        okhttp3.OkHttpClient.Builder().build().newCall(
                Request.Builder().apply {
                    url(url)
                    get()
                }.build()
        ).execute().use {
            val body = it.body()
            body ?: return null
            val dataStr = body.string()
            val data = Klaxon().parse<VkInfo>(dataStr)
            data ?: return null

            return data.userId to data.token
        }
    }

    fun getName(token: String): String? {
        val url = "https://api.vk.com/method/users.get?access_token=$token&v=5.85"
        okhttp3.OkHttpClient.Builder().build().newCall(
                Request.Builder().apply {
                    url(url)
                    get()
                }.build()
        ).execute().use {
            val body = it.body()
            body ?: return null
            val dataStr = body.string()
            val data = Klaxon().parse<UserResponse>(dataStr)
            data ?: return null
            if (data.response.isEmpty()) return null
            val user = data.response[0]
            return user.nameF + " " + user.nameS
        }
    }

    fun userAsLink(uid: Int, name: String) = """<a href="//vk.com/id$uid" target="_blank">$name</a>"""

    @RequestMapping(
        path = ["/login"],
        method = [RequestMethod.GET],
        produces = [MediaType.TEXT_HTML_VALUE]
    )
    fun login(): ResponseEntity<String> {
        val res = """
            <a
        href="https://oauth.vk.com/authorize?client_id=$appId&display=page&redirect_uri=$redirectUrl&scope=offline&response_type=code&v=5.85"
            >Авторизация vk</a>
        """
        return ResponseEntity.ok().body(res)
    }

    @RequestMapping(
            path = ["/codeprocess"],
            method = [RequestMethod.GET],
            produces = [MediaType.TEXT_HTML_VALUE]
    )
    fun codeprocess(@RequestParam("code") code: String): ResponseEntity<String> {
        val res = """
            <script>
                // alert('data');
                window.location = '/chat/chat';
            </script>
        """.trimIndent()

        if (code.isEmpty()) return ResponseEntity.ok().body("no code provided")
        val data = getToken(code)
        data ?: return ResponseEntity.ok().body("code error")
        val uid = data.first
        val token = data.second

        val headers = HttpHeaders()
        headers.add(HttpHeaders.SET_COOKIE, "uid=$uid")
        headers.add(HttpHeaders.SET_COOKIE, "token=$token")
//        doesn't want to redirect
//        headers.add(HttpHeaders.LOCATION, "/chat/chat")
        val userName = getName(token)
        val userData = if (userName == null) token to "" else token to userName
        usersOnline[uid] = userData
        messages += userAsLink(uid, userData.second) + " is logged in".also { log.info(it) }
        return ResponseEntity.ok().headers(headers).body(res)
    }

    /**
     * curl -X POST -i localhost:8080/chat/logout -d "name=I_AM_STUPID"
     */
    @RequestMapping(
            path = ["/logout"],
            method = [RequestMethod.POST],
            consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE]
    )
    fun logout(@CookieValue("uid") uid: String, @CookieValue("token") token: String): ResponseEntity<String> {
        val res = """
            <script>
                // alert('data');
                window.location = '/chat/login';
            </script>
        """.trimIndent()
        val numUid = uid.toIntOrNull()
        numUid ?: return ResponseEntity.badRequest().body("what r u doin bro")
        if (! usersOnline.contains(numUid) || !(usersOnline[numUid]!!.first == token)) {
            return ResponseEntity.badRequest().body("who are u talkin about?")
        }
        val userLink = userAsLink(numUid, usersOnline[numUid]!!.second)
        messages += "$userLink has left us".also { log.info(it) }
        usersOnline.remove(numUid)
        return ResponseEntity.ok().body(res)
    }
    /**
     *
     * Well formatted sorted list of online users
     *
     * curl -i localhost:8080/chat/online
     */
    @RequestMapping(
        path = ["online"],
        method = [RequestMethod.GET],
        produces = [MediaType.TEXT_HTML_VALUE]
    )
    fun online(@CookieValue("uid") uid: String?, @CookieValue("token") token: String?): ResponseEntity<String> {

        val redirectBody = "<script>window.location = '/chat/login';</script>"
        if (uid == null || token == null) {
            return ResponseEntity.ok().body(redirectBody)
        }
        val uidInt = uid.toIntOrNull()
        uidInt ?: return ResponseEntity.ok().body(redirectBody)

        if (!(usersOnline.contains(uidInt)) || !(usersOnline[uidInt]!!.first == token)) {
            return ResponseEntity.ok().body(redirectBody)
        }

        val result = usersOnline.keys.map {
            userAsLink(it, usersOnline[it]!!.second)
        }.joinToString(separator = ", ")
        return ResponseEntity.ok().body("Online: $result")
    }

    /**
     * curl -X POST -i localhost:8080/chat/say -d "name=I_AM_STUPID&msg=Hello everyone in this chat"
     */
    @RequestMapping(
            path = ["/say"],
            method = [RequestMethod.POST],
            consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE]
    )
    fun say(
        @RequestParam("msg") msg: String,
        @CookieValue("uid") uid: String,
        @CookieValue("token") token: String
    ): ResponseEntity<String> {
        when {
            uid.isEmpty() || msg.isEmpty() -> return ResponseEntity.badRequest().body("msg and name should not be empty")
            msg.length > 200 -> return ResponseEntity.badRequest().body("msg is too long")
            else -> {
                val safeMsg = msg
                        .replace("<", "&lt;")
                        .replace(">", "&gt;")
                        .replace("&", "&amp;")

                val numUid = uid.toIntOrNull()
                numUid ?: return ResponseEntity.badRequest().body("unknown uid")
                if (!usersOnline.contains(numUid) || !(usersOnline[numUid]!!.first == token)) {
                    return ResponseEntity.badRequest().body("bad auth key")
                }

                val userLink = userAsLink(numUid, usersOnline[numUid]!!.second)

                messages += "[$userLink]: $safeMsg".also { log.info(it) }
                val res = """
                <script>
                    window.location = '/chat/chat';
                </script>
            """.trimIndent()
                return ResponseEntity.ok().body(res)
            }
        }
    }

    /**
     * curl -i localhost:8080/chat/chat
     */
    @RequestMapping(
            path = ["chat"],
            method = [RequestMethod.GET],
            produces = [MediaType.TEXT_HTML_VALUE]
    )
    fun chat(@CookieValue("uid") uid: String?, @CookieValue("token") token: String?): ResponseEntity<String> {
        val redirectBody = "<script>window.location = '/chat/login';</script>"
        if (uid == null || token == null) {
            return ResponseEntity.ok().body(redirectBody)
        }
        val uidInt = uid.toIntOrNull()
        uidInt ?: return ResponseEntity.ok().body(redirectBody)

        if (!(usersOnline.contains(uidInt)) || !(usersOnline[uidInt]!!.first == token)) {
            return ResponseEntity.ok().body(redirectBody)
        }

        val messageList = messages.joinToString(separator = "<br />\n")
        val onlineList = usersOnline.keys.map {
            userAsLink(it, usersOnline[it]!!.second)
        }.joinToString(separator = ", ")
        val result = """
            <html>
            <head>
                <title>Chat</title>
            </head>
            <body>
                <span>Сообщения: <br />
                    $messageList
                </span>
                <hr />
                <form id="sendMsgForm" method="post" action="/chat/say">
                    <b id="sendMsgWarning" style="display:none;">сообщение не должно быть пустым<br /></b>
                    <input id="msgInput" value="" name="msg" />
                    <input type="submit" value="send message">
                </form>
                <script>
                document.getElementById('msgInput').focus();
                document.getElementById('sendMsgForm').addEventListener('submit', function(e){
                    e.preventDefault();
                    if (document.getElementById('msgInput').value.length != 0) {
                        e.target.submit()
                    } else {
                        document.getElementById('sendMsgWarning').style = "";
                    }
                });
                </script>
                <br />
                <form method="post" action="/chat/logout">
                    <input type="submit" value="logout">
                </form>
                <hr />
                Online now: $onlineList
            </body>
            </html>
        """.trimIndent()
        return ResponseEntity.ok().body(result)
    }
}

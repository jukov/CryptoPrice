package data

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.slf4j.Logger

class WSHelper(
    private val logger: Logger,
    private val httpClient: HttpClient
) {
    private var webSocketSession: DefaultClientWebSocketSession? = null

    val isWebsocketStarted: Boolean
        get() = webSocketSession != null

    suspend fun connect(url: String,  messageListener: suspend (String) -> Unit) {
        if (webSocketSession != null) error("WebSocket already opened")

        withContext(Dispatchers.IO) {
            launch {
                if (webSocketSession != null) error("WebSocket already opened")

                try {
                    val session = httpClient.webSocketSession(url)
                    webSocketSession = session

                    withContext(Dispatchers.Default) {
                        for (message in session.incoming) {
                            if (message !is Frame.Text) continue
                            val text = message.readText()
                            logger.info("New message: $text")
                            messageListener(text)
                        }
                    }
                } catch (e: Exception) {
                    logger.error("Error while receiving", e)
                }
            }
        }
    }

    suspend fun send(message: String) {
        val webSocketSession = webSocketSession
        require(webSocketSession != null) { "Attempt to send message to inactive websocket" }

        logger.info("Send message: $message")
        webSocketSession.send(message)
    }

    suspend fun disconnect() {
        withContext(Dispatchers.IO) {
            webSocketSession?.close(CloseReason(CloseReason.Codes.NORMAL, "Closed by user"))
            webSocketSession = null
        }
    }

}
package api.rc.extra

//val PORT = 8881 -> real_portal
//val PORT = 8882 -> image_to_qr
//val PORT = 8883 -> test_portal
object Constants {
    private const val DEFAULT_HOST = "81.2.236.122"

    private const val DEFAULT_PORT = 15_000

    val server = ServerConfig(
        DEFAULT_HOST,
        DEFAULT_PORT)

    object Realm {
        val MESSAGE = "api-message"
        val REGISTRATION = "api-reg"
    }

    object Path {
        val API_MESSAGE = "api/message"
        val WS_REGISTRATION = "ws/registration"
    }

    val AES_KEY = "xtyurth54xhWY;[]".toByteArray()
}

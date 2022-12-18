package api.rc.extra

import com.soywiz.krypto.AES
import com.soywiz.krypto.Padding
import com.soywiz.krypto.encoding.base64


fun buildAuthPassword(name: String) =
    AES.encryptAesCbc(
        data = name.encodeToByteArray(),
        key = Constants.AES_KEY,
        iv = ByteArray(16),
        padding = Padding.ZeroPadding
    ).base64

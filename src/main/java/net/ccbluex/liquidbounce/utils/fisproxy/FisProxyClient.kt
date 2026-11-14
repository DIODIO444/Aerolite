package net.ccbluex.liquidbounce.utils.fisproxy

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Base64
import java.util.Locale
import java.util.UUID
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

internal fun JsonObject.stringOrNull(key: String): String? =
    if (has(key) && !get(key).isJsonNull) get(key).asString else null

internal fun JsonObject.string(key: String): String = stringOrNull(key) ?: ""

class FisProxyClient(
    private val token: String,
    private val baseUrl: String = DEFAULT_BASE_URL,
    private val clientId: String = UUID.randomUUID().toString(),
    private val userAgent: String = "fisproxy-java/0.1.0"
) {
    private val lock = Any()
    private var admission: Admission? = null

    fun me(): JsonObject = request("GET", "/api/v1/me")

    fun services(): JsonObject = request("GET", "/api/v1/me/services")

    fun status(): JsonObject = request("GET", "/api/v1/sessions/status")

    fun entrances(serviceId: String? = null): JsonObject =
        request(
            "GET",
            if (serviceId == null) "/api/v1/me/entrances" else "/api/v1/me/entrances?serviceId=${quote(serviceId)}"
        )

    fun start(): JsonObject =
        request("POST", "/api/v1/sessions/start", JsonObject(), newIdempotencyKey())

    fun changeIp(): JsonObject =
        request("POST", "/api/v1/sessions/change-ip", JsonObject(), newIdempotencyKey())

    fun stop(): JsonObject = request("POST", "/api/v1/sessions/stop", JsonObject())

    fun operation(id: String): JsonObject = request("GET", "/api/v1/operations/${quote(id)}")

    fun waitOperation(id: String, timeoutMs: Long = 90_000L, intervalMs: Long = 500L): JsonObject {
        val deadline = System.currentTimeMillis() + timeoutMs
        var last: JsonObject? = null

        while (System.currentTimeMillis() < deadline) {
            last = operation(id)

            when (last.string("status")) {
                "failed", "canceled", "succeeded" -> return last
            }

            Thread.sleep(intervalMs)
        }

        return last ?: JsonObject()
    }

    private fun newIdempotencyKey(): String = UUID.randomUUID().toString().replace("-", "")

    private fun request(
        method: String,
        target: String,
        body: JsonObject? = null,
        idempotencyKey: String? = null,
        sign: Boolean = true,
        allowRefresh: Boolean = true
    ): JsonObject {
        val bytes = body?.toString()?.toByteArray(StandardCharsets.UTF_8) ?: ByteArray(0)
        val contentType = if (body != null) "application/json" else ""

        val headers = linkedMapOf(
            "Accept" to "application/json",
            "Authorization" to "Bearer $token",
            "User-Agent" to userAgent,
            "X-FP-Content-Length" to bytes.size.toString(),
            "X-FP-Content-SHA256" to sha256B64Url(bytes)
        )

        if (contentType.isNotEmpty()) {
            headers["Content-Type"] = contentType
        }

        if (!idempotencyKey.isNullOrEmpty()) {
            headers["Idempotency-Key"] = idempotencyKey
        }

        if (sign) {
            synchronized(lock) {
                headers.putAll(
                    signatureHeaders(ensureAdmission(), method, target, bytes, contentType, idempotencyKey ?: "")
                )
            }
        }

        val (status, text) = exchange(method, "$baseUrl$target", headers, bytes)
        val response = if (text.isBlank()) {
            JsonObject()
        } else {
            runCatching { JsonParser().parse(text).asJsonObject }.getOrNull() ?: JsonObject()
        }

        val errorCode = response.stringOrNull("errorCode")

        if (!sign || !allowRefresh || (status != 401 && status != 403 && errorCode !in REFRESHABLE_CODES)) {
            if (status < 400 && errorCode == null) {
                return response
            }

            throw FisProxyException(
                status,
                errorCode,
                response.stringOrNull("message") ?: "request failed ($status)"
            )
        }

        synchronized(lock) {
            admission = null
        }

        return request(method, target, body, idempotencyKey, sign, false)
    }

    private fun exchange(
        method: String,
        url: String,
        headers: Map<String, String>,
        body: ByteArray
    ): Pair<Int, String> {
        val connection = URL(url).openConnection() as HttpURLConnection

        try {
            connection.requestMethod = method
            connection.connectTimeout = 15_000
            connection.readTimeout = 20_000
            connection.instanceFollowRedirects = false

            headers.forEach { (key, value) -> connection.setRequestProperty(key, value) }

            if (body.isNotEmpty()) {
                connection.doOutput = true
                connection.outputStream.use { it.write(body) }
            }

            val status = connection.responseCode
            val stream = if (status >= 400) connection.errorStream else connection.inputStream

            val text = if (stream != null) {
                stream.use { input ->
                    val buffer = ByteArrayOutputStream()
                    input.copyTo(buffer)
                    buffer.toString("UTF-8")
                }
            } else ""

            return status to text
        } finally {
            connection.disconnect()
        }
    }

    private fun ensureAdmission(): Admission {
        val now = System.currentTimeMillis() / 1000L
        val current = admission

        if (current != null && current.expiresAt > now + ADMISSION_REFRESH_SKEW_SECONDS) {
            return current
        }

        val next = exchangeAdmission()
        admission = next
        return next
    }

    private fun exchangeAdmission(): Admission {
        val admissionToken = JsonObject()
        admissionToken.addProperty("clientId", clientId)

        val payload = request("POST", "/api/v1/auth/admission", admissionToken, null, false, false)

        val admissionValue = payload.stringOrNull("admission")
        val requestKey = payload.stringOrNull("requestKey")
        val subject = payload.stringOrNull("subject")
        val admissionId = payload.stringOrNull("admissionId")
        val expiresAt = payload.stringOrNull("expiresAt")?.toLongOrNull()
        val serverTime = payload.stringOrNull("serverTime")?.toLongOrNull()

        if (admissionValue != null && requestKey != null && subject != null && admissionId != null
            && expiresAt != null && serverTime != null
        ) {
            val parsed = parseAdmission(admissionValue)

            if (subject == parsed.string("subject")
                && admissionId == parsed.string("admissionId")
                && clientId == parsed.string("clientId")
                && expiresAt == parsed.string("expiresAt").toLongOrNull()
            ) {
                return Admission(
                    admissionValue,
                    requestKey,
                    subject,
                    admissionId,
                    parsed,
                    expiresAt,
                    serverTime - System.currentTimeMillis()
                )
            }

            throw FisProxyException(500, "INVALID_ADMISSION", "admission payload mismatch")
        }

        throw FisProxyException(500, "INVALID_ADMISSION", "invalid admission exchange response")
    }

    private fun parseAdmission(tokenText: String): JsonObject {
        val parts = tokenText.split(".")

        if (parts.size == 3 && parts[0] == "a2" && parts[1].isNotEmpty() && parts[2].isNotEmpty()) {
            val payload = runCatching {
                JsonParser().parse(String(b64urlDecode(parts[1]), StandardCharsets.UTF_8)).asJsonObject
            }.getOrNull()

            if (payload == null) {
                throw FisProxyException(500, "INVALID_ADMISSION", "invalid admission payload")
            }

            if (payload.string("version") != "2") {
                throw FisProxyException(500, "INVALID_ADMISSION", "unsupported admission version")
            }

            for (key in listOf(
                "version", "aud", "admissionId", "subject",
                "credentialId", "clientId", "serverEpoch", "expiresAt"
            )) {
                if (!payload.has(key)) {
                    throw FisProxyException(500, "INVALID_ADMISSION", "invalid admission payload")
                }
            }

            return payload
        }

        throw FisProxyException(500, "INVALID_ADMISSION", "invalid admission token")
    }

    private fun signatureHeaders(
        current: Admission,
        method: String,
        target: String,
        body: ByteArray,
        contentType: String,
        idempotencyKey: String
    ): Map<String, String> {
        val sequence = current.sequence.toString()
        current.sequence++

        val timestamp = (System.currentTimeMillis() + current.clockOffsetMs).toString()
        val contentLength = body.size.toString()
        val bodySha256 = sha256B64Url(body)

        val canonical = listOf(
            CANONICAL_PREFIX,
            current.payload.string("aud"),
            current.subject,
            current.payload.string("credentialId"),
            current.admissionId,
            current.payload.string("clientId"),
            sequence,
            timestamp,
            method.uppercase(Locale.ROOT),
            target,
            normalizeContentType(contentType),
            contentLength,
            idempotencyKey,
            bodySha256
        ).joinToString("\n")

        return linkedMapOf(
            "X-FP-Admission" to current.admission,
            "X-FP-Subject" to current.subject,
            "X-FP-Timestamp" to timestamp,
            "X-FP-Client" to current.payload.string("clientId"),
            "X-FP-Sequence" to sequence,
            "X-FP-Content-Length" to contentLength,
            "X-FP-Content-SHA256" to bodySha256,
            "X-FP-Signature" to sign(current.requestKey, canonical)
        )
    }

    private class Admission(
        val admission: String,
        val requestKey: String,
        val subject: String,
        val admissionId: String,
        val payload: JsonObject,
        val expiresAt: Long,
        val clockOffsetMs: Long
    ) {
        internal var sequence = 0L
    }

    companion object {
        const val DEFAULT_BASE_URL = "https://api.fisproxy.org"

        private const val CANONICAL_PREFIX = "FISPROXY-REQUEST-V2"
        private const val ADMISSION_REFRESH_SKEW_SECONDS = 5L

        private val REFRESHABLE_CODES = setOf(
            "ADMISSION_EXPIRED",
            "ADMISSION_PROCESS_EXPIRED",
            "ADMISSION_INVALID",
            "EDGE_ADMISSION_EXPIRED",
            "REQUEST_TIMESTAMP_INVALID"
        )

        private fun b64url(data: ByteArray): String =
            Base64.getUrlEncoder().withoutPadding().encodeToString(data)

        private fun b64urlDecode(value: String): ByteArray = Base64.getUrlDecoder().decode(value)

        private fun sha256B64Url(data: ByteArray): String =
            b64url(MessageDigest.getInstance("SHA-256").digest(data))

        private fun normalizeContentType(value: String): String {
            val lower = value.trim().lowercase(Locale.ROOT)
            return Regex("\\s*=\\s*").replace(Regex("\\s*;\\s*").replace(lower, ";"), "=")
        }

        private fun quote(value: String): String =
            URLEncoder.encode(value, "UTF-8").replace("+", "%20")

        private fun sign(requestKey: String, canonical: String): String {
            val mac = Mac.getInstance("HmacSHA256")
            mac.init(SecretKeySpec(b64urlDecode(requestKey), "HmacSHA256"))
            return b64url(mac.doFinal(canonical.toByteArray(StandardCharsets.UTF_8)))
        }
    }
}

/*
 * Copyright 2025 Dipcoin LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");you may not use this file except in compliance with
 * the License.You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,software distributed under the License is distributed on
 * an "AS IS" BASIS,WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.See the License for the
 * specific language governing permissions and limitations under the License.
 */

package io.dipcoin.sui.perp.client.core

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.dipcoin.sui.perp.client.auth.AuthSession
import io.dipcoin.sui.perp.exception.PerpHttpException
import io.dipcoin.sui.perp.exception.PerpJsonParseException
import io.dipcoin.sui.perp.exception.PerpRpcFailedException
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.slf4j.LoggerFactory
import java.io.IOException
import java.time.Duration
import java.time.format.DateTimeFormatter
import java.time.ZonedDateTime
import kotlin.math.min

abstract class AbstractHttpClient : HttpClient {

    protected val objectMapper: ObjectMapper = jacksonObjectMapper()
    protected val okHttpClient: OkHttpClient

    private val log = LoggerFactory.getLogger(javaClass)

    init {
        okHttpClient = createOkHttpClient()
    }

    override fun <T> post(request: Any, url: String, auth: AuthSession?, typeReference: TypeReference<T>): T? {
        val requestBody = try {
            objectMapper.writeValueAsString(request)
        } catch (e: com.fasterxml.jackson.core.JsonProcessingException) {
            throw PerpJsonParseException("Unable to serialize request body", e)
        }
        val builder = Request.Builder()
            .url(url)
            .post(requestBody.toRequestBody(JSON))
        buildUrlWithAuth(builder, auth)
        val httpRequest = builder.build()
        return try {
            executeWith429Retry(httpRequest, typeReference, "POST")
        } catch (e: PerpHttpException) {
            throw e
        } catch (e: IOException) {
            throw PerpRpcFailedException("Unable to send POST request", e)
        }
    }

    override fun <T> get(url: String, queryParams: Map<String, String>?, auth: AuthSession?, typeReference: TypeReference<T>): T? {
        val finalUrl = buildUrlWithParams(url, queryParams)
        val builder = Request.Builder().url(finalUrl).get()
        buildUrlWithAuth(builder, auth)
        val httpRequest = builder.build()
        return try {
            executeWith429Retry(httpRequest, typeReference, "GET")
        } catch (e: PerpHttpException) {
            throw e
        } catch (e: IOException) {
            throw PerpRpcFailedException("Unable to send GET request", e)
        }
    }

    /**
     * Retries on HTTP 429 using [Retry-After] when present, otherwise exponential backoff.
     */
    private fun <T> executeWith429Retry(
        httpRequest: Request,
        typeReference: TypeReference<T>,
        methodLabel: String,
    ): T? {
        repeat(MAX_RATE_LIMIT_ATTEMPTS) { attempt ->
            okHttpClient.newCall(httpRequest).execute().use { response ->
                val bodyStr = response.body?.string().orEmpty()
                when {
                    response.code == 429 -> {
                        if (attempt >= MAX_RATE_LIMIT_ATTEMPTS - 1) {
                            throw PerpHttpException(
                                "HTTP 429 Too Many Requests after ${MAX_RATE_LIMIT_ATTEMPTS} attempts: ${bodyStr.take(512)}",
                            )
                        }
                        val waitMs = computeRateLimitWaitMs(response.header(HEADER_RETRY_AFTER), attempt)
                        val urlForLog = httpRequest.url.run { "$scheme://$host$encodedPath" }
                        log.warn(
                            "{} {} rate limited (429), waiting {} ms before retry {}/{}",
                            methodLabel,
                            urlForLog,
                            waitMs,
                            attempt + 2,
                            MAX_RATE_LIMIT_ATTEMPTS,
                        )
                        sleepUnchecked(waitMs)
                    }
                    !response.isSuccessful -> {
                        throw PerpHttpException(
                            "HTTP ${response.code} ${response.message.trim()}: ${bodyStr.take(512)}",
                        )
                    }
                    bodyStr.isBlank() -> return null
                    else -> return objectMapper.readValue(bodyStr, typeReference)
                }
            }
        }
        error("unreachable")
    }

    private fun sleepUnchecked(ms: Long) {
        try {
            Thread.sleep(ms)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            throw PerpRpcFailedException("Interrupted during rate-limit backoff", e)
        }
    }

    /** Prefer [Retry-After] (seconds or HTTP-date); else exponential backoff capped at [MAX_BACKOFF_MS]. */
    private fun computeRateLimitWaitMs(retryAfterHeader: String?, attemptIndex: Int): Long {
        val trimmed = retryAfterHeader?.trim()
        if (!trimmed.isNullOrEmpty()) {
            trimmed.toLongOrNull()?.let { sec ->
                return (sec * 1000L).coerceIn(MIN_BACKOFF_MS, MAX_BACKOFF_MS)
            }
            try {
                val zdt = ZonedDateTime.parse(trimmed, DateTimeFormatter.RFC_1123_DATE_TIME)
                val retryAt = zdt.toInstant().toEpochMilli()
                return (retryAt - System.currentTimeMillis()).coerceIn(MIN_BACKOFF_MS, MAX_BACKOFF_MS)
            } catch (_: Exception) {
                // ignore malformed date
            }
        }
        val exp = (INITIAL_BACKOFF_MS shl min(attemptIndex, 5)).coerceAtMost(MAX_BACKOFF_MS)
        return exp.coerceAtLeast(MIN_BACKOFF_MS)
    }

    protected fun toQueryParams(o: Any?): Map<String, String> {
        if (o == null) return emptyMap()
        @Suppress("UNCHECKED_CAST")
        val map = objectMapper.convertValue(o, Map::class.java) as Map<String, Any?>
        return map.filterValues { it != null }.mapValues { it.value!!.toString() }
    }

    private fun buildUrlWithParams(url: String, queryParams: Map<String, String>?): String {
        if (queryParams.isNullOrEmpty()) return url
        val urlBuilder = url.toHttpUrl().newBuilder()
        for ((k, v) in queryParams) {
            if (v != null) urlBuilder.addQueryParameter(k, v)
        }
        return urlBuilder.build().toString()
    }

    private fun buildUrlWithAuth(builder: Request.Builder, auth: AuthSession?) {
        if (auth != null && !auth.token.isNullOrEmpty() && !auth.address.isNullOrEmpty()) {
            builder.header(HEADER_AUTH, HEADER_PREFIX + auth.token)
            builder.header(HEADER_ADDR, auth.address)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(AbstractHttpClient::class.java)
        private val JSON = "application/json; charset=utf-8".toMediaType()
        private const val HEADER_AUTH = "Authorization"
        private const val HEADER_ADDR = "X-Wallet-Address"
        private const val HEADER_RETRY_AFTER = "Retry-After"
        private const val HEADER_PREFIX = "Bearer "

        /** Total attempts including the first call (e.g. 6 => up to 5 backoffs after 429). */
        private const val MAX_RATE_LIMIT_ATTEMPTS = 6
        private const val INITIAL_BACKOFF_MS = 1_000L
        private const val MIN_BACKOFF_MS = 200L
        private const val MAX_BACKOFF_MS = 120_000L

        fun getOkHttpClientBuilder(): OkHttpClient.Builder {
            val builder = OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(30))
                .readTimeout(Duration.ofSeconds(30))
            if (log.isDebugEnabled) {
                val logging = HttpLoggingInterceptor { log.debug(it) }
                logging.level = HttpLoggingInterceptor.Level.BODY
                builder.addInterceptor(logging)
            }
            return builder
        }

        private fun createOkHttpClient(): OkHttpClient = getOkHttpClientBuilder().build()
    }
}

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

abstract class AbstractHttpClient : HttpClient {

    protected val objectMapper: ObjectMapper = jacksonObjectMapper()
    protected val okHttpClient: OkHttpClient

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
            okHttpClient.newCall(httpRequest).execute().use { response ->
                if (response.isSuccessful) {
                    response.body?.string()?.let { objectMapper.readValue(it, typeReference) }
                } else null
            }
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
            okHttpClient.newCall(httpRequest).execute().use { response ->
                if (response.isSuccessful) {
                    response.body?.string()?.let { objectMapper.readValue(it, typeReference) }
                } else null
            }
        } catch (e: IOException) {
            throw PerpRpcFailedException("Unable to send GET request", e)
        }
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
        private const val HEADER_PREFIX = "Bearer "

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

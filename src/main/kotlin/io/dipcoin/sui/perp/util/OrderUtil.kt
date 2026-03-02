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

package io.dipcoin.sui.perp.util

import com.fasterxml.jackson.databind.ObjectMapper
import io.dipcoin.sui.crypto.SuiKeyPair
import io.dipcoin.sui.crypto.exceptions.SigningException
import io.dipcoin.sui.crypto.signature.SignatureScheme
import io.dipcoin.sui.perp.enums.OrderScheme
import io.dipcoin.sui.perp.enums.OrderSide
import io.dipcoin.sui.perp.model.request.PlaceOrderRequest
import org.bouncycastle.util.encoders.Base64
import org.bouncycastle.util.encoders.Hex
import org.slf4j.LoggerFactory
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.util.Random

object OrderUtil {
    private val log = LoggerFactory.getLogger(OrderUtil::class.java)
    private val random = Random()
    private val objectMapper = ObjectMapper()
    private const val CANCEL_KEY = "orderHashes"

    fun getSerializedOrder(request: PlaceOrderRequest): String {
        val reduceOnly = request.reduceOnly
        val isBuy = request.side == OrderSide.BUY.code
        return getSerializedOrder(
            request.market!!,
            isBuy,
            reduceOnly,
            false,
            true,
            false,
            request.price,
            request.quantity,
            request.leverage,
            request.salt?.let { BigInteger(it) } ?: BigInteger.ZERO,
            BigInteger.ZERO,
            request.creator!!,
            getOrderFlags(false, false, reduceOnly, isBuy, true)
        )
    }

    fun getTpSerializedOrder(request: PlaceOrderRequest): String {
        val reduceOnly = true
        val isBuy = request.side != OrderSide.BUY.code
        return getSerializedOrder(
            request.market!!,
            isBuy,
            reduceOnly,
            false,
            true,
            false,
            request.tpOrderPrice,
            request.quantity,
            request.leverage,
            request.tpSalt?.let { BigInteger(it) } ?: BigInteger.ZERO,
            BigInteger.ZERO,
            request.creator!!,
            getOrderFlags(false, false, reduceOnly, isBuy, true)
        )
    }

    fun getSlSerializedOrder(request: PlaceOrderRequest): String {
        val reduceOnly = true
        val isBuy = request.side != OrderSide.BUY.code
        return getSerializedOrder(
            request.market!!,
            isBuy,
            reduceOnly,
            false,
            true,
            false,
            request.slOrderPrice,
            request.quantity,
            request.leverage,
            request.slSalt?.let { BigInteger(it) } ?: BigInteger.ZERO,
            BigInteger.ZERO,
            request.creator!!,
            getOrderFlags(false, false, reduceOnly, isBuy, true)
        )
    }

    fun getSerializedOrder(
        symbol: String,
        isLong: Boolean,
        reduceOnly: Boolean,
        postOnly: Boolean,
        orderbookOnly: Boolean,
        ioc: Boolean,
        price: BigInteger?,
        quantity: BigInteger?,
        leverage: BigInteger?,
        salt: BigInteger?,
        expiration: BigInteger?,
        creator: String,
        orderFlags: Int
    ): String {
        return buildString {
            append("{\n")
            append("\"market\":\"").append(symbol).append("\",\n")
            append("\"creator\":\"").append(creator).append("\",\n")
            append("\"isLong\":\"").append(isLong).append("\",\n")
            append("\"reduceOnly\":\"").append(reduceOnly).append("\",\n")
            append("\"postOnly\":\"").append(postOnly).append("\",\n")
            append("\"orderbookOnly\":\"").append(orderbookOnly).append("\",\n")
            append("\"ioc\":\"").append(ioc).append("\",\n")
            append("\"quantity\":\"").append(quantity?.toString() ?: "0").append("\",\n")
            append("\"price\":\"").append(price?.toString() ?: "0").append("\",\n")
            append("\"leverage\":\"").append(leverage?.toString() ?: "0").append("\",\n")
            append("\"expiration\":\"").append(expiration?.toString() ?: "0").append("\",\n")
            append("\"salt\":\"").append(salt?.toString() ?: "0").append("\",\n")
            append("\"orderFlag\":\"").append(orderFlags).append("\",\n")
            append("\"domain\":\"dipcoin.io\"\n")
            append("}")
        }
    }

    fun getSerializedCancelOrder(orderHashes: List<String>): String {
        return try {
            objectMapper.writeValueAsString(mapOf(CANCEL_KEY to orderHashes))
        } catch (e: Exception) {
            log.error("Failed to encode message", e)
            ""
        }
    }

    fun getOrderFlags(ioc: Boolean, postOnly: Boolean, reduceOnly: Boolean, isBuy: Boolean, orderbookOnly: Boolean): Int {
        var flag = 0
        if (ioc) flag += 1
        if (postOnly) flag += 2
        if (reduceOnly) flag += 4
        if (isBuy) flag += 8
        if (orderbookOnly) flag += 16
        return flag
    }

    fun getSignature(msg: String, suiKeyPair: SuiKeyPair<*>): String {
        return try {
            getMessageSignature(msg.toByteArray(StandardCharsets.UTF_8), suiKeyPair) + Base64.toBase64String(suiKeyPair.publicKeyBytes())
        } catch (e: java.io.IOException) {
            throw SigningException("Failed to generate signature", e)
        }
    }

    fun getSalt(): ByteArray {
        val salt = System.currentTimeMillis() +
            getRandomInt(0, 1_000_000) +
            getRandomInt(0, 1_000_000) +
            getRandomInt(0, 1_000_000)
        return longToBytes(salt)
    }

    private fun getMessageSignature(msg: ByteArray, suiKeyPair: SuiKeyPair<*>): String {
        val signatureScheme = suiKeyPair.signatureScheme()
        return when {
            signatureScheme == SignatureScheme.ED25519 ->
                Hex.toHexString(suiKeyPair.signSignaturePersonalMessage(msg)) + OrderScheme.KP_ED25519.type
            signatureScheme == SignatureScheme.SECP256K1 ->
                Hex.toHexString(suiKeyPair.signSignaturePersonalMessage(msg)) + OrderScheme.KP_SECP256.type
            else -> throw IllegalArgumentException("Unsupported signature scheme: $signatureScheme")
        }
    }

    private fun longToBytes(value: Long): ByteArray {
        var v = value
        val result = ByteArray(8)
        for (i in 7 downTo 0) {
            result[i] = (v and 0xFF).toByte()
            v = v shr 8
        }
        return result
    }

    private fun getRandomInt(min: Int, max: Int): Int = random.nextInt(max - min + 1) + min
}

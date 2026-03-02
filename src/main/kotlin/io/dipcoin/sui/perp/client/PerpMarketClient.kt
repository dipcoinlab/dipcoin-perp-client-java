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

package io.dipcoin.sui.perp.client

import com.fasterxml.jackson.core.type.TypeReference
import io.dipcoin.sui.perp.client.core.AbstractHttpClient
import io.dipcoin.sui.perp.constant.PerpPath
import io.dipcoin.sui.perp.enums.PerpNetwork
import io.dipcoin.sui.perp.exception.ErrorCode
import io.dipcoin.sui.perp.exception.PerpHttpException
import io.dipcoin.sui.perp.model.ApiResponse
import io.dipcoin.sui.perp.model.PerpConfig
import io.dipcoin.sui.perp.model.request.SymbolRequest
import io.dipcoin.sui.perp.model.response.OrderBookResponse
import io.dipcoin.sui.perp.model.response.TickerResponse
import io.dipcoin.sui.perp.model.response.TradingPairResponse
import java.math.BigInteger
import java.util.concurrent.ConcurrentHashMap

class PerpMarketClient(perpNetwork: PerpNetwork) : AbstractHttpClient() {
    private val perpConfig: PerpConfig = perpNetwork.getConfig()
    private val PERP_IDS = ConcurrentHashMap<String, String>()
    private val FEED_IDS = ConcurrentHashMap<String, String>()

    fun ticker(request: SymbolRequest): TickerResponse {
        val response = get(perpConfig.perpEndpoint + PerpPath.TICKER, toQueryParams(request), null, object : TypeReference<ApiResponse<TickerResponse>>() {})
            ?: throw PerpHttpException("Failed to ticker: null response")
        if (response.code == ErrorCode.SUCCESS.code) return response.data!!
        throw PerpHttpException("Failed to orderBook, cause : ${response.message}")
    }

    fun orderBook(request: SymbolRequest): OrderBookResponse {
        val response = get(perpConfig.perpEndpoint + PerpPath.ORDER_BOOK, toQueryParams(request), null, object : TypeReference<ApiResponse<OrderBookResponse>>() {})
            ?: throw PerpHttpException("Failed to orderBook: null response")
        if (response.code == ErrorCode.SUCCESS.code) return response.data!!
        throw PerpHttpException("Failed to orderBook, cause : ${response.message}")
    }

    fun oracle(request: SymbolRequest): BigInteger {
        val response = get(perpConfig.perpEndpoint + PerpPath.ORACLE, toQueryParams(request), null, object : TypeReference<ApiResponse<BigInteger>>() {})
            ?: throw PerpHttpException("Failed to oracle: null response")
        if (response.code == ErrorCode.SUCCESS.code) return response.data!!
        throw PerpHttpException("Failed to tradingPair, cause : ${response.message}")
    }

    fun tradingPair(): List<TradingPairResponse> {
        val response = get(perpConfig.perpEndpoint + PerpPath.TRADING_PAIR, null, null, object : TypeReference<ApiResponse<List<TradingPairResponse>>>() {})
            ?: throw PerpHttpException("Failed to tradingPair: null response")
        if (response.code == ErrorCode.SUCCESS.code) return response.data ?: emptyList()
        throw PerpHttpException("Failed to tradingPair, cause : ${response.message}")
    }

    fun getMarketPerpId(symbol: String): String {
        if (symbol.isBlank()) throw IllegalArgumentException("symbol is null or empty!")
        PERP_IDS[symbol]?.takeIf { it.isNotEmpty() }?.let { return it }
        val list = tradingPair()
        if (list.isEmpty()) throw PerpHttpException("remote service internal error!")
        for (r in list) {
            r.symbol?.let { PERP_IDS[it] = r.perpId ?: "" }
        }
        return PERP_IDS[symbol] ?: throw PerpHttpException("symbol not found: $symbol")
    }

    fun getPythFeedId(symbol: String): String {
        if (symbol.isBlank()) throw IllegalArgumentException("symbol is null or empty!")
        FEED_IDS[symbol]?.takeIf { it.isNotEmpty() }?.let { return it }
        val list = tradingPair()
        if (list.isEmpty()) throw PerpHttpException("remote service internal error!")
        for (r in list) {
            r.symbol?.let { FEED_IDS[it] = r.priceIdentifierId ?: "" }
        }
        return FEED_IDS[symbol] ?: throw PerpHttpException("symbol not found: $symbol")
    }
}

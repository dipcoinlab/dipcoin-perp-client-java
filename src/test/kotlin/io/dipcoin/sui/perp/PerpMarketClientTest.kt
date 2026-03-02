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

package io.dipcoin.sui.perp

import io.dipcoin.sui.perp.client.PerpMarketClient
import io.dipcoin.sui.perp.enums.PerpNetwork
import io.dipcoin.sui.perp.model.request.SymbolRequest
import io.dipcoin.sui.perp.model.response.OrderBookResponse
import io.dipcoin.sui.perp.model.response.TickerResponse
import io.dipcoin.sui.perp.model.response.TradingPairResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.math.BigInteger
import kotlin.test.assertTrue

class PerpMarketClientTest {
    private var perpMarketClient: PerpMarketClient? = null
    private val log = LoggerFactory.getLogger(javaClass)

    @BeforeEach
    fun setUp() {
        perpMarketClient = PerpMarketClient(PerpNetwork.TESTNET)
    }

    @Test
    fun testTicker() {
        val request = SymbolRequest(symbol = "ETH-PERP")
        val response = perpMarketClient!!.ticker(request)
        log.info("Response: {}", response)
        assertTrue(response is TickerResponse)
    }

    @Test
    fun testOrderBook() {
        val request = SymbolRequest(symbol = "ETH-PERP")
        val response = perpMarketClient!!.orderBook(request)
        log.info("Response: {}", response)
        assertTrue(response is OrderBookResponse)
    }

    @Test
    fun testOracle() {
        val request = SymbolRequest(symbol = "ETH-PERP")
        val response = perpMarketClient!!.oracle(request)
        log.info("Response: {}", response)
        assertTrue(response is BigInteger)
    }

    @Test
    fun testTradingPair() {
        val response = perpMarketClient!!.tradingPair()
        log.info("Response: {}", response)
        assertTrue(response is List<*>)
    }
}

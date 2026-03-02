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

import io.dipcoin.sui.perp.client.PerpHttpClient
import io.dipcoin.sui.perp.client.core.PerpClient
import io.dipcoin.sui.perp.config.IntervalExtension
import io.dipcoin.sui.perp.enums.PerpNetwork
import io.dipcoin.sui.perp.model.request.SymbolRequest
import io.dipcoin.sui.perp.model.response.TickerResponse
import io.dipcoin.sui.perp.wallet.WalletKey
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory
import kotlin.test.assertTrue

@ExtendWith(IntervalExtension::class)
class PerpHttpClientTest {
    protected var perpClient: PerpClient? = null
    private val log = LoggerFactory.getLogger(javaClass)

    @BeforeEach
    fun setUp() {
        val perpNetwork = PerpNetwork.TESTNET
        perpClient = PerpHttpClient(perpNetwork, WalletKey.mainKeyPair, WalletKey.subKeyPair)
    }

    @Test
    fun testTicker() {
        val request = SymbolRequest(symbol = "ETH-PERP")
        val response = perpClient!!.ticker(request)
        log.info("Response: {}", response)
        assertTrue(response is TickerResponse)
    }
}

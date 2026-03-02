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

import io.dipcoin.sui.perp.client.PerpUserClient
import io.dipcoin.sui.perp.client.auth.AuthSession
import io.dipcoin.sui.perp.client.core.PerpAuthorization
import io.dipcoin.sui.perp.enums.PerpNetwork
import io.dipcoin.sui.perp.model.request.*
import io.dipcoin.sui.perp.model.response.*
import io.dipcoin.sui.perp.wallet.WalletKey
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import kotlin.test.assertTrue

class PerpUserClientTest {
    private var perpUserClient: PerpUserClient? = null
    private val log = LoggerFactory.getLogger(javaClass)

    @BeforeEach
    fun setUp() {
        val perpNetwork = PerpNetwork.TESTNET
        val perpAuthorization = PerpAuthorization(perpNetwork)
        val authSession: AuthSession = perpAuthorization.authorize(WalletKey.mainKeyPair)
        perpUserClient = PerpUserClient(perpNetwork, authSession)
    }

    @Test
    fun testPositions() {
        val response = perpUserClient!!.positions(PositionRequest())
        log.info("Response: {}", response)
        assertTrue(response is List<*>)
    }

    @Test
    fun testOrders() {
        val request = OrdersRequest(symbol = "ETH-PERP", pageNum = 1, pageSize = 20)
        val response = perpUserClient!!.orders(request)
        log.info("Response: {}", response)
        assertTrue(response is io.dipcoin.sui.perp.model.PageResponse<*>)
    }

    @Test
    fun testAccount() {
        val response = perpUserClient!!.account(AccountRequest())
        log.info("Response: {}", response)
        assertTrue(response is AccountResponse)
    }
}

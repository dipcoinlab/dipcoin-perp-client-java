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
import io.dipcoin.sui.perp.client.PerpTradeClient
import io.dipcoin.sui.perp.client.auth.AuthSession
import io.dipcoin.sui.perp.client.core.PerpAuthorization
import io.dipcoin.sui.perp.enums.OrderSide
import io.dipcoin.sui.perp.enums.OrderType
import io.dipcoin.sui.perp.enums.PerpNetwork
import io.dipcoin.sui.perp.model.request.PlaceOrderRequest
import io.dipcoin.sui.perp.util.DecimalUtil
import io.dipcoin.sui.perp.util.OrderUtil
import io.dipcoin.sui.perp.wallet.WalletKey
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.math.BigInteger
import kotlin.test.assertTrue

class PerpTradeClientTest {
    private var perpTradeClient: PerpTradeClient? = null
    private var perpMarketClient: PerpMarketClient? = null
    private val log = LoggerFactory.getLogger(javaClass)

    @BeforeEach
    fun setUp() {
        val perpNetwork = PerpNetwork.TESTNET
        perpMarketClient = PerpMarketClient(perpNetwork)
        val perpAuthorization = PerpAuthorization(perpNetwork)
        val authSession: AuthSession = perpAuthorization.authorize(WalletKey.subKeyPair)
        perpTradeClient = PerpTradeClient(perpNetwork, authSession)
    }

    @Test
    @Disabled("需先在服务端创建 subAccount，测试用钱包未开通子账户")
    fun testPlaceOrder() {
        val symbol = "ETH-PERP"
        val perpId = perpMarketClient!!.getMarketPerpId(symbol)
        val request = PlaceOrderRequest(
            symbol = symbol,
            market = perpId,
            price = DecimalUtil.toBaseUnit(BigInteger("3940")),
            quantity = DecimalUtil.toBaseUnit(BigInteger.ONE),
            side = OrderSide.SELL.code,
            orderType = OrderType.LIMIT.code,
            leverage = DecimalUtil.toBaseUnit(BigInteger.ONE),
            salt = System.currentTimeMillis().toString(),
            creator = WalletKey.mainKeyPair.address()
        )
        request.orderSignature = OrderUtil.getSignature(OrderUtil.getSerializedOrder(request), WalletKey.subKeyPair)
        val orderHash = perpTradeClient!!.placeOrder(request)
        log.info("Response orderHash: {}", orderHash)
        assertTrue(orderHash is String)
    }
}

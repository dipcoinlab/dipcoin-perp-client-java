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
import io.dipcoin.sui.perp.client.PerpOnSignClient
import io.dipcoin.sui.perp.enums.PerpNetwork
import io.dipcoin.sui.perp.util.DecimalUtil
import io.dipcoin.sui.perp.wallet.WalletKey
import io.dipcoin.sui.protocol.SuiClient
import io.dipcoin.sui.protocol.http.HttpService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.math.BigInteger

class PerpOnSignClientTest {
    protected var suiClient: SuiClient? = null
    protected var perpMarketClient: PerpMarketClient? = null
    protected var perpOnSignClient: PerpOnSignClient? = null
    private val log = LoggerFactory.getLogger(javaClass)

    @BeforeEach
    fun setUp() {
        val perpNetwork = PerpNetwork.TESTNET
        suiClient = SuiClient.build(HttpService(perpNetwork.getConfig().suiRpc))
        perpMarketClient = PerpMarketClient(perpNetwork)
        perpOnSignClient = PerpOnSignClient(suiClient!!, perpNetwork, perpMarketClient!!)
    }

    fun getGasPrice(): Long {
        val response = try {
            suiClient!!.getReferenceGasPrice().send()
        } catch (e: Exception) {
            throw IllegalArgumentException("Get GasPrice failed!", e)
        }
        val result = response.result ?: return 1000L
        if (result.isEmpty()) return 1000L
        return result.toLong()
    }

    @Test
    @Tag("suite")
    @Disabled("需要 testnet 上有 SUI 的测试钱包，CI 中无链上余额")
    fun testSetSubAccount() {
        val subAccountKeyPair = WalletKey.subKeyPair
        val subAccount = subAccountKeyPair.address()
        val response = perpOnSignClient!!.setSubAccount(subAccountKeyPair, subAccount, getGasPrice(), DecimalUtil.toSui(BigDecimal("0.1")))
        log.info("Response: {}", response)
    }
}

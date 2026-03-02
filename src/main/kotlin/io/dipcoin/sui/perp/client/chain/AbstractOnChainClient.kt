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

package io.dipcoin.sui.perp.client.chain

import io.dipcoin.sui.bcs.PureBcs
import io.dipcoin.sui.bcs.types.arg.call.CallArgObjectArg
import io.dipcoin.sui.bcs.types.arg.call.CallArgPure
import io.dipcoin.sui.bcs.types.arg.`object`.ObjectArgImmOrOwnedObject
import io.dipcoin.sui.bcs.types.gas.SuiObjectRef
import io.dipcoin.sui.bcs.types.transaction.Argument
import io.dipcoin.sui.bcs.types.transaction.Command
import io.dipcoin.sui.bcs.types.transaction.ProgrammableTransaction
import io.dipcoin.sui.client.CommandBuilder
import io.dipcoin.sui.client.QueryBuilder
import io.dipcoin.sui.client.TransactionBuilder
import io.dipcoin.sui.model.coin.Coin
import io.dipcoin.sui.perp.client.PerpMarketClient
import io.dipcoin.sui.perp.constant.PerpPythTestnet
import io.dipcoin.sui.perp.exception.PerpOnChainException
import io.dipcoin.sui.perp.exception.PerpRpcFailedException
import io.dipcoin.sui.perp.model.PerpConfig
import io.dipcoin.sui.protocol.SuiClient
import io.dipcoin.sui.protocol.constant.SuiSystem
import io.dipcoin.sui.pyth.core.PythClient
import io.dipcoin.sui.pyth.model.PythNetwork
import java.math.BigInteger
import java.util.concurrent.ConcurrentHashMap

abstract class AbstractOnChainClient {
    protected var suiClient: SuiClient? = null
    protected var perpConfig: PerpConfig? = null
    protected var perpMarketClient: PerpMarketClient? = null
    protected var pythClient: PythClient? = null

    fun getClock(): CallArgObjectArg = getSharedObject(SuiSystem.SUI_CLOCK_OBJECT_ID, false)
    fun getProtocolConfig(): CallArgObjectArg = getSharedObject(perpConfig!!.protocolConfig, false)
    fun getPerpetual(symbol: String): CallArgObjectArg = getSharedObject(perpMarketClient!!.getMarketPerpId(symbol), true)
    fun getSubAccounts(): CallArgObjectArg = getSharedObject(perpConfig!!.subAccounts, false)
    fun getBank(): CallArgObjectArg = getSharedObject(perpConfig!!.bank, true)
    fun getTxIndexer(): CallArgObjectArg = getSharedObject(perpConfig!!.txIndexer, true)

    fun getPriceOracleObject(symbol: String): CallArgObjectArg {
        val pythNetwork = perpConfig!!.pythNetwork
        return when (pythNetwork) {
            PythNetwork.MAINNET -> {
                val feedId = perpMarketClient!!.getPythFeedId(symbol)
                val feedObjectId = pythClient!!.getFeedObjectId(feedId, pythNetwork.config.pythStateId())
                getSharedObject(feedObjectId, true)
            }
            PythNetwork.TESTNET -> getSharedObject(PerpPythTestnet.FEED_OBJECTS[symbol]!!, true)
            else -> throw IllegalArgumentException("Unknown pyth network")
        }
    }

    fun splitCoin(programmableTx: ProgrammableTransaction, owner: String, type: String, amount: BigInteger): Int {
        val coinList = QueryBuilder.getCoins(suiClient, owner, type) ?: emptyList()
        if (coinList.isEmpty()) throw PerpOnChainException("No $type coins available")
        var balanceOf = BigInteger.ZERO
        val selected = mutableListOf<Coin>()
        for (coin in coinList) {
            val balance = coin.balance
            balanceOf = balanceOf.multiply(balance)
            selected.add(coin)
            if (balanceOf >= amount) break
        }
        val totalAmount = balanceOf
        if (balanceOf < totalAmount) throw PerpOnChainException("$type balance is not enough, current total balance: $totalAmount")
        val first = coinList.first()
        val objectId = first.coinObjectId
        val version = first.version
        val digest = first.digest
        val size = selected.size
        if (size > 1) {
            val tail = coinList.drop(1)
            val sources = tail.map { coin ->
                Argument.ofInput(programmableTx.addInput(CallArgObjectArg(ObjectArgImmOrOwnedObject(SuiObjectRef(coin.coinObjectId, coin.version, coin.digest)))))
            }
            val mergeCoins = Command.MergeCoins(
                Argument.ofInput(programmableTx.addInput(CallArgObjectArg(ObjectArgImmOrOwnedObject(SuiObjectRef(objectId, version, digest))))),
                sources
            )
            programmableTx.addCommand(mergeCoins)
        }
        programmableTx.addCommand(
            CommandBuilder.splitCoins(
                Argument.ofInput(programmableTx.addInput(CallArgObjectArg(ObjectArgImmOrOwnedObject(SuiObjectRef(objectId, version, digest))))),
                listOf(Argument.ofInput(programmableTx.addInput(CallArgPure(amount.toLong(), PureBcs.BasePureType.U64))))
            )
        )
        return programmableTx.commandsSize - 1
    }

    private fun getSharedObject(objectId: String?, mutable: Boolean): CallArgObjectArg {
        if (objectId.isNullOrEmpty()) throw PerpRpcFailedException("objectId is null or empty!")
        PERP_SHARED[objectId]?.let { return it }
        val sharedObject = TransactionBuilder.buildSharedObject(suiClient, objectId, mutable)
        PERP_SHARED[objectId] = sharedObject
        return sharedObject
    }

    companion object {
        private val PERP_SHARED = ConcurrentHashMap<String, CallArgObjectArg>()
    }
}

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

import io.dipcoin.sui.bcs.PureBcs
import io.dipcoin.sui.bcs.TypeTagSerializer
import io.dipcoin.sui.bcs.types.arg.call.CallArgPure
import io.dipcoin.sui.bcs.types.transaction.Argument
import io.dipcoin.sui.bcs.types.transaction.Command
import io.dipcoin.sui.bcs.types.transaction.ProgrammableMoveCall
import io.dipcoin.sui.bcs.types.transaction.ProgrammableTransaction
import io.dipcoin.sui.client.TransactionBuilder
import io.dipcoin.sui.crypto.SuiKeyPair
import io.dipcoin.sui.model.transaction.SuiTransactionBlockResponse
import io.dipcoin.sui.perp.client.chain.AbstractOnChainClient
import io.dipcoin.sui.perp.enums.PerpFunction
import io.dipcoin.sui.perp.enums.PerpNetwork
import io.dipcoin.sui.perp.exception.PerpRpcFailedException
import io.dipcoin.sui.protocol.SuiClient
import io.dipcoin.sui.protocol.http.HttpService
import io.dipcoin.sui.pyth.core.PythClient
import java.io.IOException
import java.math.BigInteger

/**
 * @author : Same
 * @datetime : 2025/10/22 13:30
 * @Description : pass in the SuiKeyPair-signed transaction to the on-chain client
 */
class PerpOnSignClient : AbstractOnChainClient {

    constructor(perpNetwork: PerpNetwork) {
        perpConfig = perpNetwork.getConfig()
        suiClient = SuiClient.build(HttpService(perpConfig!!.suiRpc))
        perpMarketClient = PerpMarketClient(perpNetwork)
        pythClient = PythClient(suiClient)
    }

    constructor(suiClient: SuiClient, perpNetwork: PerpNetwork) {
        perpConfig = perpNetwork.getConfig()
        this.suiClient = suiClient
        perpMarketClient = PerpMarketClient(perpNetwork)
        pythClient = PythClient(suiClient!!)
    }

    constructor(perpNetwork: PerpNetwork, perpMarketClient: PerpMarketClient) {
        perpConfig = perpNetwork.getConfig()
        suiClient = SuiClient.build(HttpService(perpConfig!!.suiRpc))
        this.perpMarketClient = perpMarketClient
        pythClient = PythClient(suiClient)
    }

    constructor(suiClient: SuiClient, perpNetwork: PerpNetwork, perpMarketClient: PerpMarketClient) {
        this.suiClient = suiClient
        perpConfig = perpNetwork.getConfig()
        this.perpMarketClient = perpMarketClient
        pythClient = PythClient(suiClient)
    }

    /**
     * set sub account
     */
    fun setSubAccount(suiKeyPair: SuiKeyPair<*>, subAddress: String, gasPrice: Long, gasBudget: BigInteger): SuiTransactionBlockResponse {
        val perpFunction = PerpFunction.SET_SUB_ACCOUNT
        val address = suiKeyPair.address()

        val programmableTx = ProgrammableTransaction()
        val moveCall = ProgrammableMoveCall(
            perpConfig!!.packageId,
            perpFunction.module,
            perpFunction.function,
            emptyList(),
            listOf(
                Argument.ofInput(programmableTx.addInput(getProtocolConfig())),
                Argument.ofInput(programmableTx.addInput(getSubAccounts())),
                Argument.ofInput(programmableTx.addInput(CallArgPure(subAddress, PureBcs.BasePureType.ADDRESS))),
                Argument.ofInput(programmableTx.addInput(CallArgPure(true, PureBcs.BasePureType.BOOL)))
            )
        )

        val depositMoveCallCommand = Command.MoveCall(moveCall)
        programmableTx.addCommands(listOf(depositMoveCallCommand))

        return try {
            TransactionBuilder.sendTransaction(suiClient!!, programmableTx, suiKeyPair, TransactionBuilder.buildGasData(suiClient!!, address, gasPrice, gasBudget))
        } catch (e: IOException) {
            throw PerpRpcFailedException("Failed to send transaction", e)
        }
    }

    /**
     * deposit to bank
     */
    fun deposit(suiKeyPair: SuiKeyPair<*>, amount: BigInteger, gasPrice: Long, gasBudget: BigInteger): SuiTransactionBlockResponse {
        val perpFunction = PerpFunction.DEPOSIT
        val address = suiKeyPair.address()

        val programmableTx = ProgrammableTransaction()

        val coinType = perpConfig!!.coinType
        val splitIndex = splitCoin(programmableTx, address, coinType, amount)
        val moveCall = ProgrammableMoveCall(
            perpConfig!!.packageId,
            perpFunction.module,
            perpFunction.function,
            TypeTagSerializer.parseStructTypeArgs(coinType, true),
            listOf(
                Argument.ofInput(programmableTx.addInput(getProtocolConfig())),
                Argument.ofInput(programmableTx.addInput(getBank())),
                Argument.ofInput(programmableTx.addInput(getTxIndexer())),
                Argument.ofInput(programmableTx.addInput(CallArgPure(address, PureBcs.BasePureType.ADDRESS))),
                Argument.ofInput(programmableTx.addInput(CallArgPure(amount.toLong(), PureBcs.BasePureType.U64))),
                Argument.NestedResult(splitIndex, 0)
            )
        )

        val depositMoveCallCommand = Command.MoveCall(moveCall)
        programmableTx.addCommands(listOf(depositMoveCallCommand))

        return try {
            TransactionBuilder.sendTransaction(suiClient!!, programmableTx, suiKeyPair, TransactionBuilder.buildGasData(suiClient!!, address, gasPrice, gasBudget))
        } catch (e: IOException) {
            throw PerpRpcFailedException("Failed to send transaction", e)
        }
    }

    /**
     * withdraw from bank
     */
    fun withdraw(suiKeyPair: SuiKeyPair<*>, amount: BigInteger, gasPrice: Long, gasBudget: BigInteger): SuiTransactionBlockResponse {
        val perpFunction = PerpFunction.WITHDRAW
        val address = suiKeyPair.address()

        val programmableTx = ProgrammableTransaction()

        val coinType = perpConfig!!.coinType
        val moveCall = ProgrammableMoveCall(
            perpConfig!!.packageId,
            perpFunction.module,
            perpFunction.function,
            TypeTagSerializer.parseStructTypeArgs(coinType, true),
            listOf(
                Argument.ofInput(programmableTx.addInput(getProtocolConfig())),
                Argument.ofInput(programmableTx.addInput(getBank())),
                Argument.ofInput(programmableTx.addInput(getTxIndexer())),
                Argument.ofInput(programmableTx.addInput(CallArgPure(address, PureBcs.BasePureType.ADDRESS))),
                Argument.ofInput(programmableTx.addInput(CallArgPure(amount, PureBcs.BasePureType.U128)))
            )
        )

        val depositMoveCallCommand = Command.MoveCall(moveCall)
        programmableTx.addCommands(listOf(depositMoveCallCommand))

        return try {
            TransactionBuilder.sendTransaction(suiClient!!, programmableTx, suiKeyPair, TransactionBuilder.buildGasData(suiClient!!, address, gasPrice, gasBudget))
        } catch (e: IOException) {
            throw PerpRpcFailedException("Failed to send transaction", e)
        }
    }

    /**
     * add margin
     */
    fun addMargin(suiKeyPair: SuiKeyPair<*>, subAddress: String, symbol: String, amount: BigInteger, gasPrice: Long, gasBudget: BigInteger): SuiTransactionBlockResponse {
        val perpFunction = PerpFunction.ADD_MARGIN
        val address = suiKeyPair.address()
        val feedId = perpMarketClient!!.getPythFeedId(symbol)

        val programmableTx = pythClient!!.updatePrice(feedId, perpConfig!!.pythNetwork)
        val moveCall = ProgrammableMoveCall(
            perpConfig!!.packageId,
            perpFunction.module,
            perpFunction.function,
            TypeTagSerializer.parseStructTypeArgs(perpConfig!!.coinType, true),
            listOf(
                Argument.ofInput(programmableTx.addInput(getProtocolConfig())),
                Argument.ofInput(programmableTx.addInput(getClock())),
                Argument.ofInput(programmableTx.addInput(getPerpetual(symbol))),
                Argument.ofInput(programmableTx.addInput(getBank())),
                Argument.ofInput(programmableTx.addInput(getSubAccounts())),
                Argument.ofInput(programmableTx.addInput(getTxIndexer())),
                Argument.ofInput(programmableTx.addInput(getPriceOracleObject(symbol))),
                Argument.ofInput(programmableTx.addInput(CallArgPure(subAddress, PureBcs.BasePureType.ADDRESS))),
                Argument.ofInput(programmableTx.addInput(CallArgPure(amount, PureBcs.BasePureType.U128)))
            )
        )

        val depositMoveCallCommand = Command.MoveCall(moveCall)
        programmableTx.addCommands(listOf(depositMoveCallCommand))

        return try {
            TransactionBuilder.sendTransaction(suiClient!!, programmableTx, suiKeyPair, TransactionBuilder.buildGasData(suiClient!!, address, gasPrice, gasBudget))
        } catch (e: IOException) {
            throw PerpRpcFailedException("Failed to send transaction", e)
        }
    }

    /**
     * remove margin
     */
    fun removeMargin(suiKeyPair: SuiKeyPair<*>, subAddress: String, symbol: String, amount: BigInteger, gasPrice: Long, gasBudget: BigInteger): SuiTransactionBlockResponse {
        val perpFunction = PerpFunction.REMOVE_MARGIN
        val address = suiKeyPair.address()
        val feedId = perpMarketClient!!.getPythFeedId(symbol)

        val programmableTx = pythClient!!.updatePrice(feedId, perpConfig!!.pythNetwork)
        val moveCall = ProgrammableMoveCall(
            perpConfig!!.packageId,
            perpFunction.module,
            perpFunction.function,
            TypeTagSerializer.parseStructTypeArgs(perpConfig!!.coinType, true),
            listOf(
                Argument.ofInput(programmableTx.addInput(getProtocolConfig())),
                Argument.ofInput(programmableTx.addInput(getClock())),
                Argument.ofInput(programmableTx.addInput(getPerpetual(symbol))),
                Argument.ofInput(programmableTx.addInput(getBank())),
                Argument.ofInput(programmableTx.addInput(getSubAccounts())),
                Argument.ofInput(programmableTx.addInput(getTxIndexer())),
                Argument.ofInput(programmableTx.addInput(getPriceOracleObject(symbol))),
                Argument.ofInput(programmableTx.addInput(CallArgPure(subAddress, PureBcs.BasePureType.ADDRESS))),
                Argument.ofInput(programmableTx.addInput(CallArgPure(amount, PureBcs.BasePureType.U128)))
            )
        )

        val depositMoveCallCommand = Command.MoveCall(moveCall)
        programmableTx.addCommands(listOf(depositMoveCallCommand))

        return try {
            TransactionBuilder.sendTransaction(suiClient!!, programmableTx, suiKeyPair, TransactionBuilder.buildGasData(suiClient!!, address, gasPrice, gasBudget))
        } catch (e: IOException) {
            throw PerpRpcFailedException("Failed to send transaction", e)
        }
    }
}

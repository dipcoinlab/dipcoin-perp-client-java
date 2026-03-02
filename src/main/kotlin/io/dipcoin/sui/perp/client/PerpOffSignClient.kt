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
import io.dipcoin.sui.model.transaction.SuiTransactionBlockResponse
import io.dipcoin.sui.perp.client.chain.AbstractOnChainClient
import io.dipcoin.sui.perp.client.chain.WalletService
import io.dipcoin.sui.perp.enums.PerpFunction
import io.dipcoin.sui.perp.enums.PerpNetwork
import io.dipcoin.sui.perp.exception.PerpOnChainException
import io.dipcoin.sui.perp.model.PerpConfig
import io.dipcoin.sui.protocol.SuiClient
import io.dipcoin.sui.protocol.exceptions.RpcRequestFailedException
import io.dipcoin.sui.protocol.http.HttpService
import io.dipcoin.sui.pyth.core.PythClient
import org.bouncycastle.util.encoders.Base64
import java.io.IOException
import java.math.BigInteger

/**
 * @author : Same
 * @datetime : 2025/10/28 14:00
 * @Description : self-implemented wallet signature for offline signed transactions to the on-chain client (implement WalletService)
 */
class PerpOffSignClient : AbstractOnChainClient {

    private val walletService: WalletService

    constructor(perpNetwork: PerpNetwork, perpMarketClient: PerpMarketClient, walletService: WalletService) {
        val config: PerpConfig = perpNetwork.getConfig()
        suiClient = SuiClient.build(HttpService(config.suiRpc))
        perpConfig = config
        this.perpMarketClient = perpMarketClient
        pythClient = PythClient(suiClient)
        this.walletService = walletService
    }

    constructor(suiClient: SuiClient, perpNetwork: PerpNetwork, perpMarketClient: PerpMarketClient, walletService: WalletService) {
        this.suiClient = suiClient
        perpConfig = perpNetwork.getConfig()
        this.perpMarketClient = perpMarketClient
        pythClient = PythClient(suiClient)
        this.walletService = walletService
    }

    /**
     * set sub account
     */
    fun setSubAccount(sender: String, subAddress: String, gasPrice: Long, gasBudget: BigInteger): SuiTransactionBlockResponse {
        val perpFunction = PerpFunction.SET_SUB_ACCOUNT

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

        val txBytes = try {
            TransactionBuilder.serializeTransactionBytes(programmableTx, sender, TransactionBuilder.buildGasData(suiClient!!, sender, gasPrice, gasBudget))
        } catch (e: IOException) {
            throw PerpOnChainException("unsafe moveCall setSubAccount failed!", e)
        }

        val signature = walletService.sign(sender, Base64.decode(txBytes))

        return try {
            TransactionBuilder.sendTransaction(suiClient!!, txBytes, listOf(signature))
        } catch (e: IOException) {
            throw RpcRequestFailedException("Failed to send setSubAccount transaction", e)
        }
    }

    /**
     * deposit to bank
     */
    fun deposit(sender: String, amount: BigInteger, gasPrice: Long, gasBudget: BigInteger): SuiTransactionBlockResponse {
        val perpFunction = PerpFunction.DEPOSIT

        val programmableTx = ProgrammableTransaction()

        val coinType = perpConfig!!.coinType
        val splitIndex = splitCoin(programmableTx, sender, coinType, amount)
        val moveCall = ProgrammableMoveCall(
            perpConfig!!.packageId,
            perpFunction.module,
            perpFunction.function,
            TypeTagSerializer.parseStructTypeArgs(coinType, true),
            listOf(
                Argument.ofInput(programmableTx.addInput(getProtocolConfig())),
                Argument.ofInput(programmableTx.addInput(getBank())),
                Argument.ofInput(programmableTx.addInput(getTxIndexer())),
                Argument.ofInput(programmableTx.addInput(CallArgPure(sender, PureBcs.BasePureType.ADDRESS))),
                Argument.ofInput(programmableTx.addInput(CallArgPure(amount.toLong(), PureBcs.BasePureType.U64))),
                Argument.NestedResult(splitIndex, 0)
            )
        )

        val depositMoveCallCommand = Command.MoveCall(moveCall)
        programmableTx.addCommands(listOf(depositMoveCallCommand))

        val txBytes = try {
            TransactionBuilder.serializeTransactionBytes(programmableTx, sender, TransactionBuilder.buildGasData(suiClient!!, sender, gasPrice, gasBudget))
        } catch (e: IOException) {
            throw PerpOnChainException("unsafe moveCall deposit failed!", e)
        }

        val signature = walletService.sign(sender, Base64.decode(txBytes))

        return try {
            TransactionBuilder.sendTransaction(suiClient!!, txBytes, listOf(signature))
        } catch (e: IOException) {
            throw RpcRequestFailedException("Failed to send deposit transaction", e)
        }
    }

    /**
     * withdraw from bank
     */
    fun withdraw(sender: String, amount: BigInteger, gasPrice: Long, gasBudget: BigInteger): SuiTransactionBlockResponse {
        val perpFunction = PerpFunction.WITHDRAW

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
                Argument.ofInput(programmableTx.addInput(CallArgPure(sender, PureBcs.BasePureType.ADDRESS))),
                Argument.ofInput(programmableTx.addInput(CallArgPure(amount, PureBcs.BasePureType.U128)))
            )
        )

        val depositMoveCallCommand = Command.MoveCall(moveCall)
        programmableTx.addCommands(listOf(depositMoveCallCommand))

        val txBytes = try {
            TransactionBuilder.serializeTransactionBytes(programmableTx, sender, TransactionBuilder.buildGasData(suiClient!!, sender, gasPrice, gasBudget))
        } catch (e: IOException) {
            throw PerpOnChainException("unsafe moveCall withdraw failed!", e)
        }

        val signature = walletService.sign(sender, Base64.decode(txBytes))

        return try {
            TransactionBuilder.sendTransaction(suiClient!!, txBytes, listOf(signature))
        } catch (e: IOException) {
            throw RpcRequestFailedException("Failed to send withdraw transaction", e)
        }
    }

    /**
     * add margin
     */
    fun addMargin(sender: String, subAddress: String, symbol: String, amount: BigInteger, gasPrice: Long, gasBudget: BigInteger): SuiTransactionBlockResponse {
        val perpFunction = PerpFunction.ADD_MARGIN
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

        val txBytes = try {
            TransactionBuilder.serializeTransactionBytes(programmableTx, sender, TransactionBuilder.buildGasData(suiClient!!, sender, gasPrice, gasBudget))
        } catch (e: IOException) {
            throw PerpOnChainException("unsafe moveCall addMargin failed!", e)
        }

        val signature = walletService.sign(sender, Base64.decode(txBytes))

        return try {
            TransactionBuilder.sendTransaction(suiClient!!, txBytes, listOf(signature))
        } catch (e: IOException) {
            throw RpcRequestFailedException("Failed to send addMargin transaction", e)
        }
    }

    /**
     * remove margin
     */
    fun removeMargin(sender: String, subAddress: String, symbol: String, amount: BigInteger, gasPrice: Long, gasBudget: BigInteger): SuiTransactionBlockResponse {
        val perpFunction = PerpFunction.REMOVE_MARGIN
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

        val txBytes = try {
            TransactionBuilder.serializeTransactionBytes(programmableTx, sender, TransactionBuilder.buildGasData(suiClient!!, sender, gasPrice, gasBudget))
        } catch (e: IOException) {
            throw PerpOnChainException("unsafe moveCall removeMargin failed!", e)
        }

        val signature = walletService.sign(sender, Base64.decode(txBytes))

        return try {
            TransactionBuilder.sendTransaction(suiClient!!, txBytes, listOf(signature))
        } catch (e: IOException) {
            throw RpcRequestFailedException("Failed to send removeMargin transaction", e)
        }
    }
}

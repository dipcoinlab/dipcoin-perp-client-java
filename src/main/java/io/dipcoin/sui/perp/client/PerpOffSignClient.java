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

package io.dipcoin.sui.perp.client;

import io.dipcoin.sui.bcs.PureBcs;
import io.dipcoin.sui.bcs.TypeTagSerializer;
import io.dipcoin.sui.bcs.types.arg.call.CallArgPure;
import io.dipcoin.sui.bcs.types.transaction.Argument;
import io.dipcoin.sui.bcs.types.transaction.Command;
import io.dipcoin.sui.bcs.types.transaction.ProgrammableMoveCall;
import io.dipcoin.sui.bcs.types.transaction.ProgrammableTransaction;
import io.dipcoin.sui.client.TransactionBuilder;
import io.dipcoin.sui.crypto.Ed25519KeyPair;
import io.dipcoin.sui.model.transaction.SuiTransactionBlockResponse;
import io.dipcoin.sui.perp.client.chain.AbstractOnChainClient;
import io.dipcoin.sui.perp.client.chain.WalletService;
import io.dipcoin.sui.perp.enums.PerpFunction;
import io.dipcoin.sui.perp.enums.PerpNetwork;
import io.dipcoin.sui.perp.exception.PerpOnChainException;
import io.dipcoin.sui.perp.model.PerpConfig;
import io.dipcoin.sui.protocol.SuiClient;
import io.dipcoin.sui.protocol.exceptions.RpcRequestFailedException;
import io.dipcoin.sui.protocol.http.HttpService;
import io.dipcoin.sui.pyth.core.PythClient;
import org.bouncycastle.util.encoders.Base64;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author : Same
 * @datetime : 2025/10/28 14:00
 * @Description : self-implemented wallet signature for offline signed transactions to the on-chain client (implement WalletService)
 */
public class PerpOffSignClient extends AbstractOnChainClient {

    private final WalletService walletService;

    public PerpOffSignClient(PerpNetwork perpNetwork, PerpMarketClient perpMarketClient, WalletService walletService) {
        PerpConfig perpConfig = perpNetwork.getConfig();
        super.suiClient = SuiClient.build(new HttpService(perpConfig.suiRpc()));
        super.perpConfig = perpConfig;
        super.perpMarketClient = perpMarketClient;
        super.pythClient = new PythClient(suiClient);
        this.walletService = walletService;
    }

    public PerpOffSignClient(SuiClient suiClient, PerpNetwork perpNetwork, PerpMarketClient perpMarketClient, WalletService walletService) {
        super.suiClient = suiClient;
        super.perpConfig = perpNetwork.getConfig();
        super.perpMarketClient = perpMarketClient;
        super.pythClient = new PythClient(suiClient);
        this.walletService = walletService;
    }

    /**
     * set sub account
     * @param sender
     * @param subAddress
     * @param gasPrice
     * @param gasBudget
     * @return
     */
    public SuiTransactionBlockResponse setSubAccount(String sender, String subAddress, long gasPrice, BigInteger gasBudget) {
        PerpFunction perpFunction = PerpFunction.SET_SUB_ACCOUNT;

        ProgrammableTransaction programmableTx = new ProgrammableTransaction();
        // ProgrammableMoveCall
        ProgrammableMoveCall moveCall = new ProgrammableMoveCall(
                perpConfig.packageId(),
                perpFunction.getModule(),
                perpFunction.getFunction(),
                Collections.emptyList(),
                Arrays.asList(
                        Argument.ofInput(programmableTx.addInput(this.getProtocolConfig())),
                        Argument.ofInput(programmableTx.addInput(this.getSubAccounts())),
                        Argument.ofInput(programmableTx.addInput(new CallArgPure(subAddress,
                                PureBcs.BasePureType.ADDRESS))),
                        Argument.ofInput(programmableTx.addInput(new CallArgPure(Boolean.TRUE,
                                PureBcs.BasePureType.BOOL))))
        );

        // Command
        Command depositMoveCallCommand = new Command.MoveCall(moveCall);
        List<Command> commands = new ArrayList<>(List.of(
                depositMoveCallCommand
        ));
        programmableTx.addCommands(commands);
        String txBytes;
        try {
            txBytes = TransactionBuilder.serializeTransactionBytes(programmableTx, sender, TransactionBuilder.buildGasData(suiClient, sender, gasPrice, gasBudget));
        } catch (IOException e) {
            throw new PerpOnChainException("unsafe moveCall setSubAccount failed!", e);
        }

        String signature = walletService.sign(sender, Base64.decode(txBytes));

        try {
            return TransactionBuilder.sendTransaction(suiClient, txBytes, List.of(signature));
        } catch (IOException e) {
            throw new RpcRequestFailedException("Failed to send setSubAccount transaction", e);
        }
    }

    /**
     * deposit to bank
     * @param sender
     * @param gasPrice
     * @param gasBudget
     * @return
     */
    public SuiTransactionBlockResponse deposit(String sender, BigInteger amount, long gasPrice, BigInteger gasBudget) {
        PerpFunction perpFunction = PerpFunction.DEPOSIT;

        ProgrammableTransaction programmableTx = new ProgrammableTransaction();

        String coinType = perpConfig.coinType();
        int splitIndex = this.splitCoin(programmableTx, sender, coinType, amount);
        // ProgrammableMoveCall
        ProgrammableMoveCall moveCall = new ProgrammableMoveCall(
                perpConfig.packageId(),
                perpFunction.getModule(),
                perpFunction.getFunction(),
                TypeTagSerializer.parseStructTypeArgs(coinType, true),
                Arrays.asList(
                        Argument.ofInput(programmableTx.addInput(this.getProtocolConfig())),
                        Argument.ofInput(programmableTx.addInput(this.getBank())),
                        Argument.ofInput(programmableTx.addInput(this.getTxIndexer())),
//                        Argument.ofInput(programmableTx.addInput(new CallArgPure(OrderUtil.getSalt(),
                        Argument.ofInput(programmableTx.addInput(new CallArgPure(Ed25519KeyPair.generate().privateKey(),
                                PureBcs.BasePureType.VECTOR_U8))),
                        Argument.ofInput(programmableTx.addInput(new CallArgPure(sender,
                                PureBcs.BasePureType.ADDRESS))),
                        Argument.ofInput(programmableTx.addInput(new CallArgPure(amount.longValue(),
                                PureBcs.BasePureType.U64))),
                        new Argument.NestedResult(splitIndex, 0)
                ));

        // Command
        Command depositMoveCallCommand = new Command.MoveCall(moveCall);
        List<Command> commands = new ArrayList<>(List.of(
                depositMoveCallCommand
        ));
        programmableTx.addCommands(commands);
        String txBytes;
        try {
            txBytes = TransactionBuilder.serializeTransactionBytes(programmableTx, sender, TransactionBuilder.buildGasData(suiClient, sender, gasPrice, gasBudget));
        } catch (IOException e) {
            throw new PerpOnChainException("unsafe moveCall deposit failed!", e);
        }

        String signature = walletService.sign(sender, Base64.decode(txBytes));

        try {
            return TransactionBuilder.sendTransaction(suiClient, txBytes, List.of(signature));
        } catch (IOException e) {
            throw new RpcRequestFailedException("Failed to send deposit transaction", e);
        }
    }

    /**
     * withdraw from bank
     * @param sender
     * @param gasPrice
     * @param gasBudget
     * @return
     */
    public SuiTransactionBlockResponse withdraw(String sender, BigInteger amount, long gasPrice, BigInteger gasBudget) {
        PerpFunction perpFunction = PerpFunction.WITHDRAW;

        ProgrammableTransaction programmableTx = new ProgrammableTransaction();

        String coinType = perpConfig.coinType();
        // ProgrammableMoveCall
        ProgrammableMoveCall moveCall = new ProgrammableMoveCall(
                perpConfig.packageId(),
                perpFunction.getModule(),
                perpFunction.getFunction(),
                TypeTagSerializer.parseStructTypeArgs(coinType, true),
                Arrays.asList(
                        Argument.ofInput(programmableTx.addInput(this.getProtocolConfig())),
                        Argument.ofInput(programmableTx.addInput(this.getBank())),
                        Argument.ofInput(programmableTx.addInput(this.getTxIndexer())),
//                        Argument.ofInput(programmableTx.addInput(new CallArgPure(OrderUtil.getSalt(),
                        Argument.ofInput(programmableTx.addInput(new CallArgPure(Ed25519KeyPair.generate().privateKey(),
                                PureBcs.BasePureType.VECTOR_U8))),
                        Argument.ofInput(programmableTx.addInput(new CallArgPure(sender,
                                PureBcs.BasePureType.ADDRESS))),
                        Argument.ofInput(programmableTx.addInput(new CallArgPure(amount,
                                PureBcs.BasePureType.U128)))
                ));

        // Command
        Command depositMoveCallCommand = new Command.MoveCall(moveCall);
        List<Command> commands = new ArrayList<>(List.of(
                depositMoveCallCommand
        ));
        programmableTx.addCommands(commands);
        String txBytes;
        try {
            txBytes = TransactionBuilder.serializeTransactionBytes(programmableTx, sender, TransactionBuilder.buildGasData(suiClient, sender, gasPrice, gasBudget));
        } catch (IOException e) {
            throw new PerpOnChainException("unsafe moveCall withdraw failed!", e);
        }

        String signature = walletService.sign(sender, Base64.decode(txBytes));

        try {
            return TransactionBuilder.sendTransaction(suiClient, txBytes, List.of(signature));
        } catch (IOException e) {
            throw new RpcRequestFailedException("Failed to send withdraw transaction", e);
        }
    }

    /**
     * add margin
     * @param sender
     * @param subAddress
     * @param gasPrice
     * @param gasBudget
     * @return
     */
    public SuiTransactionBlockResponse addMargin(String sender, String subAddress, String symbol, BigInteger amount, long gasPrice, BigInteger gasBudget) {
        PerpFunction perpFunction = PerpFunction.ADD_MARGIN;
        String feedId = perpMarketClient.getPythFeedId(symbol);

        ProgrammableTransaction programmableTx = pythClient.updatePrice(feedId, perpConfig.pythNetwork());
        // ProgrammableMoveCall
        ProgrammableMoveCall moveCall = new ProgrammableMoveCall(
                perpConfig.packageId(),
                perpFunction.getModule(),
                perpFunction.getFunction(),
                TypeTagSerializer.parseStructTypeArgs(perpConfig.coinType(), true),
                Arrays.asList(
                        Argument.ofInput(programmableTx.addInput(this.getProtocolConfig())),
                        Argument.ofInput(programmableTx.addInput(this.getClock())),
                        Argument.ofInput(programmableTx.addInput(this.getPerpetual(symbol))),
                        Argument.ofInput(programmableTx.addInput(this.getBank())),
                        Argument.ofInput(programmableTx.addInput(this.getSubAccounts())),
                        Argument.ofInput(programmableTx.addInput(this.getTxIndexer())),
                        Argument.ofInput(programmableTx.addInput(this.getPriceOracleObject(symbol))),
                        Argument.ofInput(programmableTx.addInput(new CallArgPure(subAddress,
                                PureBcs.BasePureType.ADDRESS))),
                        Argument.ofInput(programmableTx.addInput(new CallArgPure(amount,
                                PureBcs.BasePureType.U128))),
                        Argument.ofInput(programmableTx.addInput(new CallArgPure(Ed25519KeyPair.generate().privateKey(),
                                PureBcs.BasePureType.VECTOR_U8)))
                )
        );

        // Command
        Command depositMoveCallCommand = new Command.MoveCall(moveCall);
        List<Command> commands = new ArrayList<>(List.of(
                depositMoveCallCommand
        ));
        programmableTx.addCommands(commands);
        String txBytes;
        try {
            txBytes = TransactionBuilder.serializeTransactionBytes(programmableTx, sender, TransactionBuilder.buildGasData(suiClient, sender, gasPrice, gasBudget));
        } catch (IOException e) {
            throw new PerpOnChainException("unsafe moveCall addMargin failed!", e);
        }

        String signature = walletService.sign(sender, Base64.decode(txBytes));

        try {
            return TransactionBuilder.sendTransaction(suiClient, txBytes, List.of(signature));
        } catch (IOException e) {
            throw new RpcRequestFailedException("Failed to send addMargin transaction", e);
        }
    }

}

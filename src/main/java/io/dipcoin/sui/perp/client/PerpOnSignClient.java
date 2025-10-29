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
import io.dipcoin.sui.crypto.SuiKeyPair;
import io.dipcoin.sui.model.transaction.SuiTransactionBlockResponse;
import io.dipcoin.sui.perp.client.chain.AbstractOnChainClient;
import io.dipcoin.sui.perp.enums.PerpFunction;
import io.dipcoin.sui.perp.enums.PerpNetwork;
import io.dipcoin.sui.perp.exception.PerpRpcFailedException;
import io.dipcoin.sui.protocol.SuiClient;
import io.dipcoin.sui.protocol.http.HttpService;
import io.dipcoin.sui.pyth.core.PythClient;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author : Same
 * @datetime : 2025/10/22 13:30
 * @Description : pass in the SuiKeyPair-signed transaction to the on-chain client
 */
public class PerpOnSignClient extends AbstractOnChainClient {

    public PerpOnSignClient(PerpNetwork perpNetwork) {
        super.perpConfig = perpNetwork.getConfig();
        super.suiClient = SuiClient.build(new HttpService(perpConfig.suiRpc()));
        super.perpMarketClient = new PerpMarketClient(perpNetwork);
        super.pythClient = new PythClient(suiClient);
    }

    public PerpOnSignClient(SuiClient suiClient, PerpNetwork perpNetwork) {
        super.perpConfig = perpNetwork.getConfig();
        super.suiClient = suiClient;
        super.perpMarketClient = new PerpMarketClient(perpNetwork);
        super.pythClient = new PythClient(suiClient);
    }

    public PerpOnSignClient(PerpNetwork perpNetwork, PerpMarketClient perpMarketClient) {
        super.perpConfig = perpNetwork.getConfig();
        super.suiClient = SuiClient.build(new HttpService(perpConfig.suiRpc()));
        super.perpMarketClient = perpMarketClient;
        super.pythClient = new PythClient(suiClient);
    }

    public PerpOnSignClient(SuiClient suiClient, PerpNetwork perpNetwork, PerpMarketClient perpMarketClient) {
        super.suiClient = suiClient;
        super.perpConfig = perpNetwork.getConfig();
        super.perpMarketClient = perpMarketClient;
        super.pythClient = new PythClient(suiClient);
    }

    /**
     * set sub account
     * @param suiKeyPair
     * @param subAddress
     * @param gasPrice
     * @param gasBudget
     * @return
     */
    public SuiTransactionBlockResponse setSubAccount(SuiKeyPair suiKeyPair, String subAddress, long gasPrice, BigInteger gasBudget) {
        PerpFunction perpFunction = PerpFunction.SET_SUB_ACCOUNT;
        String address = suiKeyPair.address();

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

        try {
            return TransactionBuilder.sendTransaction(suiClient, programmableTx, suiKeyPair, TransactionBuilder.buildGasData(suiClient, address, gasPrice, gasBudget));
        } catch (IOException e) {
            throw new PerpRpcFailedException("Failed to send transaction", e);
        }
    }

    /**
     * deposit to bank
     * @param suiKeyPair
     * @param gasPrice
     * @param gasBudget
     * @return
     */
    public SuiTransactionBlockResponse deposit(SuiKeyPair suiKeyPair, BigInteger amount, long gasPrice, BigInteger gasBudget) {
        PerpFunction perpFunction = PerpFunction.DEPOSIT;
        String address = suiKeyPair.address();

        ProgrammableTransaction programmableTx = new ProgrammableTransaction();

        String coinType = perpConfig.coinType();
        int splitIndex = this.splitCoin(programmableTx, address, coinType, amount);
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
                        Argument.ofInput(programmableTx.addInput(new CallArgPure(address,
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

        try {
            return TransactionBuilder.sendTransaction(suiClient, programmableTx, suiKeyPair, TransactionBuilder.buildGasData(suiClient, address, gasPrice, gasBudget));
        } catch (IOException e) {
            throw new PerpRpcFailedException("Failed to send transaction", e);
        }
    }

    /**
     * withdraw from bank
     * @param suiKeyPair
     * @param gasPrice
     * @param gasBudget
     * @return
     */
    public SuiTransactionBlockResponse withdraw(SuiKeyPair suiKeyPair, BigInteger amount, long gasPrice, BigInteger gasBudget) {
        PerpFunction perpFunction = PerpFunction.WITHDRAW;
        String address = suiKeyPair.address();

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
                        Argument.ofInput(programmableTx.addInput(new CallArgPure(address,
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

        try {
            return TransactionBuilder.sendTransaction(suiClient, programmableTx, suiKeyPair, TransactionBuilder.buildGasData(suiClient, address, gasPrice, gasBudget));
        } catch (IOException e) {
            throw new PerpRpcFailedException("Failed to send transaction", e);
        }
    }

    /**
     * add margin
     * @param suiKeyPair
     * @param subAddress
     * @param gasPrice
     * @param gasBudget
     * @return
     */
    public SuiTransactionBlockResponse addMargin(SuiKeyPair suiKeyPair, String subAddress, String symbol, BigInteger amount, long gasPrice, BigInteger gasBudget) {
        PerpFunction perpFunction = PerpFunction.ADD_MARGIN;
        String address = suiKeyPair.address();
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

        try {
            return TransactionBuilder.sendTransaction(suiClient, programmableTx, suiKeyPair, TransactionBuilder.buildGasData(suiClient, address, gasPrice, gasBudget));
        } catch (IOException e) {
            throw new PerpRpcFailedException("Failed to send transaction", e);
        }
    }

}

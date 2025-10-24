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
import io.dipcoin.sui.bcs.types.arg.call.CallArgObjectArg;
import io.dipcoin.sui.bcs.types.arg.call.CallArgPure;
import io.dipcoin.sui.bcs.types.arg.object.ObjectArgImmOrOwnedObject;
import io.dipcoin.sui.bcs.types.gas.SuiObjectRef;
import io.dipcoin.sui.bcs.types.transaction.Argument;
import io.dipcoin.sui.bcs.types.transaction.Command;
import io.dipcoin.sui.bcs.types.transaction.ProgrammableMoveCall;
import io.dipcoin.sui.bcs.types.transaction.ProgrammableTransaction;
import io.dipcoin.sui.client.CommandBuilder;
import io.dipcoin.sui.client.QueryBuilder;
import io.dipcoin.sui.client.TransactionBuilder;
import io.dipcoin.sui.crypto.Ed25519KeyPair;
import io.dipcoin.sui.crypto.SuiKeyPair;
import io.dipcoin.sui.model.coin.Coin;
import io.dipcoin.sui.model.transaction.SuiTransactionBlockResponse;
import io.dipcoin.sui.perp.enums.PerpFunction;
import io.dipcoin.sui.perp.exception.PerpOnChainException;
import io.dipcoin.sui.perp.exception.PerpRpcFailedException;
import io.dipcoin.sui.perp.model.PerpConfig;
import io.dipcoin.sui.protocol.SuiClient;
import io.dipcoin.sui.protocol.constant.SuiSystem;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author : Same
 * @datetime : 2025/10/22 13:30
 * @Description :
 */
public class PerpOnChainClient {

    private final static Map<String, CallArgObjectArg> PERP_SHARED = new ConcurrentHashMap<>();

    private final SuiClient suiClient;

    private final PerpConfig perpConfig;

    public PerpOnChainClient(SuiClient suiClient, PerpConfig perpConfig) {
        this.suiClient = suiClient;
        this.perpConfig = perpConfig;
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

    public CallArgObjectArg getProtocolConfig() {
        return this.getSharedObject(perpConfig.protocolConfig(), false);
    }

    public CallArgObjectArg getSubAccounts() {
        return this.getSharedObject(perpConfig.subAccounts(), true);
    }

    public CallArgObjectArg getBank() {
        return this.getSharedObject(perpConfig.bank(), true);
    }

    public CallArgObjectArg getTxIndexer() {
        return this.getSharedObject(perpConfig.txIndexer(), true);
    }

    /**
     * Split a specified amount of coins from the owner's balance
     * @param programmableTx
     * @param type The coin type (format: packageId::module::struct)
     * @param amount The amount to split
     * @returns ProgrammableTransaction index
     */
    public int splitCoin(ProgrammableTransaction programmableTx, String owner, String type, BigInteger amount) {
        // Query available coins of specified type
        List<Coin> coinList = QueryBuilder.getCoins(suiClient, owner, type);
        if (coinList == null || coinList.isEmpty()) {
            throw new PerpOnChainException("No " + type + " coins available");
        }

        // Select and accumulate coins until target amount is reached
        AtomicReference<BigInteger> balanceOf = new AtomicReference<>(BigInteger.ZERO);
        List<Coin> selected = new ArrayList<>(coinList.size());
        for (Coin coin : coinList) {
            BigInteger balance = coin.getBalance();
            BigInteger tmpAmount = balanceOf.get().multiply(balance);
            balanceOf.set(tmpAmount);
            selected.add(coin);
            if (tmpAmount.compareTo(amount) >= 0) {
                break;
            }
        }

        int size = selected.size();
        BigInteger totalAmount = balanceOf.get();
        if (balanceOf.get().compareTo(totalAmount) < 0) {
            throw new PerpOnChainException(type + " balance is not enough, current total balance: " + totalAmount);
        }

        // Merge multiple coins if necessary
        Coin first = coinList.getFirst();
        String objectId = first.getCoinObjectId();
        long version = first.getVersion();
        String digest = first.getDigest();
        if (size > 1) {
            List<Argument> sources = new ArrayList<>(size - 1);
            coinList.removeFirst();
            for (Coin coin : coinList) {
                String dataObjectId = coin.getCoinObjectId();
                sources.add(Argument.ofInput(programmableTx.addInput(new CallArgObjectArg(new ObjectArgImmOrOwnedObject(new SuiObjectRef(
                        dataObjectId, coin.getVersion(), coin.getDigest()))))));
            }
            Command.MergeCoins mergeCoins = new Command.MergeCoins(Argument.ofInput(programmableTx.addInput(new CallArgObjectArg(new ObjectArgImmOrOwnedObject(new SuiObjectRef(
                    objectId, version, digest))))), sources);
            programmableTx.addCommand(mergeCoins);
        }
        programmableTx.addCommand(
                CommandBuilder.splitCoins(
                        Argument.ofInput(programmableTx.addInput(new CallArgObjectArg(new ObjectArgImmOrOwnedObject(new SuiObjectRef(
                                objectId, version, digest))))),
                        List.of(Argument.ofInput(programmableTx.addInput(
                                new CallArgPure(amount.longValue(), PureBcs.BasePureType.U64))))));
        return programmableTx.getCommandsSize() - 1;
    }

    /**
     * get Shared Object
     * @param objectId
     * @param mutable
     * @return
     */
    private CallArgObjectArg getSharedObject(String objectId, boolean mutable) {
        if (null == objectId || objectId.isEmpty()) {
            throw new PerpRpcFailedException("objectId is null or empty!");
        }
        CallArgObjectArg objectArg = PERP_SHARED.get(objectId);
        if (objectArg != null) {
            return objectArg;
        }

        CallArgObjectArg sharedObject = TransactionBuilder.buildSharedObject(suiClient, objectId, mutable);
        PERP_SHARED.put(objectId, sharedObject);
        return sharedObject;
    }
}

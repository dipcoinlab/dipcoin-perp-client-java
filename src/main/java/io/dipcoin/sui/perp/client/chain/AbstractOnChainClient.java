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

package io.dipcoin.sui.perp.client.chain;

import io.dipcoin.sui.bcs.PureBcs;
import io.dipcoin.sui.bcs.types.arg.call.CallArgObjectArg;
import io.dipcoin.sui.bcs.types.arg.call.CallArgPure;
import io.dipcoin.sui.bcs.types.arg.object.ObjectArgImmOrOwnedObject;
import io.dipcoin.sui.bcs.types.gas.SuiObjectRef;
import io.dipcoin.sui.bcs.types.transaction.Argument;
import io.dipcoin.sui.bcs.types.transaction.Command;
import io.dipcoin.sui.bcs.types.transaction.ProgrammableTransaction;
import io.dipcoin.sui.client.CommandBuilder;
import io.dipcoin.sui.client.QueryBuilder;
import io.dipcoin.sui.client.TransactionBuilder;
import io.dipcoin.sui.model.coin.Coin;
import io.dipcoin.sui.perp.client.PerpMarketClient;
import io.dipcoin.sui.perp.constant.PerpPythTestnet;
import io.dipcoin.sui.perp.exception.PerpOnChainException;
import io.dipcoin.sui.perp.exception.PerpRpcFailedException;
import io.dipcoin.sui.perp.model.PerpConfig;
import io.dipcoin.sui.protocol.SuiClient;
import io.dipcoin.sui.protocol.constant.SuiSystem;
import io.dipcoin.sui.pyth.core.PythClient;
import io.dipcoin.sui.pyth.model.PythNetwork;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author : Same
 * @datetime : 2025/10/28 14:01
 * @Description :
 */
public abstract class AbstractOnChainClient {

    private final static Map<String, CallArgObjectArg> PERP_SHARED = new ConcurrentHashMap<>();

    protected SuiClient suiClient;

    protected PerpConfig perpConfig;

    protected PerpMarketClient perpMarketClient;

    protected PythClient pythClient;

    public CallArgObjectArg getClock() {
        return this.getSharedObject(SuiSystem.SUI_CLOCK_OBJECT_ID, false);
    }

    public CallArgObjectArg getProtocolConfig() {
        return this.getSharedObject(perpConfig.protocolConfig(), false);
    }

    public CallArgObjectArg getPerpetual(String symbol) {
        String perpId = perpMarketClient.getMarketPerpId(symbol);
        return this.getSharedObject(perpId, true);
    }

    public CallArgObjectArg getSubAccounts() {
        return this.getSharedObject(perpConfig.subAccounts(), false);
    }

    public CallArgObjectArg getBank() {
        return this.getSharedObject(perpConfig.bank(), true);
    }

    public CallArgObjectArg getTxIndexer() {
        return this.getSharedObject(perpConfig.txIndexer(), true);
    }

    public CallArgObjectArg getPriceOracleObject(String symbol) {
        PythNetwork pythNetwork = perpConfig.pythNetwork();
        if (pythNetwork.equals(PythNetwork.MAINNET)) {
            String feedId = perpMarketClient.getPythFeedId(symbol);
            String feedObjectId = pythClient.getFeedObjectId(feedId, pythNetwork.getConfig().pythStateId());
            return this.getSharedObject(feedObjectId, true);
        } else if (pythNetwork.equals(PythNetwork.TESTNET)) {
            return this.getSharedObject(PerpPythTestnet.FEED_OBJECTS.get(symbol), true);
        } else {
            throw new IllegalArgumentException("Unknown pyth network");
        }
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

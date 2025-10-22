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
import io.dipcoin.sui.bcs.types.arg.call.CallArgObjectArg;
import io.dipcoin.sui.bcs.types.arg.call.CallArgPure;
import io.dipcoin.sui.bcs.types.transaction.Argument;
import io.dipcoin.sui.bcs.types.transaction.Command;
import io.dipcoin.sui.bcs.types.transaction.ProgrammableMoveCall;
import io.dipcoin.sui.bcs.types.transaction.ProgrammableTransaction;
import io.dipcoin.sui.client.TransactionBuilder;
import io.dipcoin.sui.crypto.SuiKeyPair;
import io.dipcoin.sui.crypto.exceptions.SigningException;
import io.dipcoin.sui.model.transaction.SuiTransactionBlockResponse;
import io.dipcoin.sui.perp.constant.PerpConstant;
import io.dipcoin.sui.perp.constant.PerpFunction;
import io.dipcoin.sui.perp.exception.PerpRpcFailedException;
import io.dipcoin.sui.perp.model.PerpConfig;
import io.dipcoin.sui.protocol.SuiClient;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
     * onboarding sign
     * @param suiKeyPair
     * @return
     */
    public String getSignature(SuiKeyPair suiKeyPair) {
        try {
            return suiKeyPair.signPersonalMessageBase64(PerpConstant.ONBOARDING_MSG);
        } catch (IOException e) {
            throw new SigningException("Failed to generate signature", e);
        }
    }

    /**
     * set sub account
     * @param perpConfig
     * @param suiKeyPair
     * @param subAddress
     * @param gasPrice
     * @param gasBudget
     * @return
     */
    public SuiTransactionBlockResponse setSubAccount(PerpConfig perpConfig, SuiKeyPair suiKeyPair, String subAddress, long gasPrice, BigInteger gasBudget) {
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

    public CallArgObjectArg getProtocolConfig() {
        return this.getSharedObject(perpConfig.protocolConfig(), false);
    }

    public CallArgObjectArg getSubAccounts() {
        return this.getSharedObject(perpConfig.subAccounts(), false);
    }

    public CallArgObjectArg getTxIndexer() {
        return this.getSharedObject(perpConfig.txIndexer(), true);
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

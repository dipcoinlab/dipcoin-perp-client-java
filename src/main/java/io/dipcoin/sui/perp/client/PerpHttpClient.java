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

import com.fasterxml.jackson.core.type.TypeReference;
import io.dipcoin.sui.crypto.SuiKeyPair;
import io.dipcoin.sui.perp.client.core.AbstractHttpClient;
import io.dipcoin.sui.perp.constant.PerpNetwork;
import io.dipcoin.sui.perp.constant.PerpPath;
import io.dipcoin.sui.perp.exception.ErrorCode;
import io.dipcoin.sui.perp.exception.PerpHttpException;
import io.dipcoin.sui.perp.model.ApiResponse;
import io.dipcoin.sui.perp.model.PerpConfig;
import io.dipcoin.sui.perp.model.request.AuthorizationRequest;
import io.dipcoin.sui.perp.model.response.AuthorizationResponse;
import io.dipcoin.sui.protocol.SuiClient;
import io.dipcoin.sui.protocol.http.HttpService;

/**
 * @author : Same
 * @datetime : 2025/10/21 10:51
 * @Description :
 */
public class PerpHttpClient extends AbstractHttpClient {

    private final PerpOnChainClient perpOnChainClient;

    private final SuiClient suiClient;

    private final PerpConfig perpConfig;

    private final SuiKeyPair mainAccount;

    private final SuiKeyPair subAccount;

    public PerpHttpClient(PerpNetwork perpNetwork, SuiKeyPair mainAccount) {
        this(perpNetwork, SuiClient.build(new HttpService(perpNetwork.getConfig().suiRpc())), mainAccount);
    }

    public PerpHttpClient(PerpNetwork perpNetwork, SuiKeyPair mainAccount, SuiKeyPair subAccount) {
        this(perpNetwork, SuiClient.build(new HttpService(perpNetwork.getConfig().suiRpc())), mainAccount, subAccount);
    }

    public PerpHttpClient(PerpNetwork perpNetwork, SuiClient suiClient, SuiKeyPair mainAccount) {
        this.perpConfig = perpNetwork.getConfig();
        this.suiClient = suiClient;
        this.perpOnChainClient = new PerpOnChainClient(suiClient, perpConfig);
        this.mainAccount = mainAccount;
        this.subAccount = mainAccount;
        String address = mainAccount.address();
        super.setAddress(address, address);
        AuthorizationResponse authorize = this.authorize();
        super.setAuthHeader(authorize.getToken());
    }

    public PerpHttpClient(PerpNetwork perpNetwork, SuiClient suiClient, SuiKeyPair mainAccount, SuiKeyPair subAccount) {
        this.perpConfig = perpNetwork.getConfig();
        this.suiClient = suiClient;
        this.perpOnChainClient = new PerpOnChainClient(suiClient, perpConfig);
        this.mainAccount = mainAccount;
        this.subAccount = subAccount;
        super.setAddress(mainAccount.address(), subAccount.address());
        AuthorizationResponse authorize = this.authorize();
        super.setAuthHeader(authorize.getToken());
    }

    // ------------------------- authorize API -------------------------

    public AuthorizationResponse authorize(AuthorizationRequest request) {
        ApiResponse<AuthorizationResponse> response = post(request, perpConfig.perpEndpoint() + PerpPath.AUTHORIZE, new TypeReference<>() {});
        if (response.getCode() == ErrorCode.SUCCESS.getCode()) {
            return response.getData();
        } else {
            throw new PerpHttpException("Failed to authorize, cause : " + response.getMessage());
        }
    }

    public AuthorizationResponse authorize() {
        String signature = perpOnChainClient.getSignature(subAccount);
        return authorize(new AuthorizationRequest()
                .setSignature(signature)
                .setUserAddress(subAddress)
                .setIsTermAccepted(true));
    }

    // ------------------------- write API -------------------------

}

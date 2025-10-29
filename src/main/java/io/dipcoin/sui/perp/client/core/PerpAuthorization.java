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

package io.dipcoin.sui.perp.client.core;

import com.fasterxml.jackson.core.type.TypeReference;
import io.dipcoin.sui.crypto.SuiKeyPair;
import io.dipcoin.sui.perp.client.auth.AuthSession;
import io.dipcoin.sui.perp.constant.PerpConstant;
import io.dipcoin.sui.perp.constant.PerpPath;
import io.dipcoin.sui.perp.enums.PerpNetwork;
import io.dipcoin.sui.perp.exception.ErrorCode;
import io.dipcoin.sui.perp.exception.PerpHttpException;
import io.dipcoin.sui.perp.model.ApiResponse;
import io.dipcoin.sui.perp.model.PerpConfig;
import io.dipcoin.sui.perp.model.request.AuthorizationRequest;
import io.dipcoin.sui.perp.model.response.AuthorizationResponse;
import io.dipcoin.sui.perp.util.OrderUtil;

/**
 * @author : Same
 * @datetime : 2025/10/29 16:20
 * @Description : perp authorization
 */
public class PerpAuthorization extends AbstractHttpClient {

    private final PerpConfig perpConfig;

    public PerpAuthorization(PerpNetwork perpNetwork) {
        this.perpConfig = perpNetwork.getConfig();
    }

    /**
     * authorize
     * @param request
     * @return
     */
    public AuthorizationResponse authorize(AuthorizationRequest request) {
        ApiResponse<AuthorizationResponse> response = post(request, perpConfig.perpEndpoint() + PerpPath.AUTHORIZE, null, new TypeReference<>() {});
        if (response.getCode() == ErrorCode.SUCCESS.getCode()) {
            return response.getData();
        } else {
            throw new PerpHttpException("Failed to authorize, cause : " + response.getMessage());
        }
    }

    /**
     * authorize by SuiKeyPair
     * @param suiKeyPair
     * @return
     */
    public AuthSession authorize(SuiKeyPair suiKeyPair) {
        String address = suiKeyPair.address();
        String signature = OrderUtil.getSignature(PerpConstant.ONBOARDING_MSG, suiKeyPair);
        AuthorizationResponse authorize = authorize(new AuthorizationRequest()
                .setSignature(signature)
                .setUserAddress(address)
                .setIsTermAccepted(true));
        return new AuthSession(address, authorize.getToken());
    }

}

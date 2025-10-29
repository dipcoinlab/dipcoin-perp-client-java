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
import io.dipcoin.sui.perp.client.auth.AuthSession;
import io.dipcoin.sui.perp.client.core.AbstractHttpClient;
import io.dipcoin.sui.perp.constant.PerpPath;
import io.dipcoin.sui.perp.enums.PerpNetwork;
import io.dipcoin.sui.perp.exception.ErrorCode;
import io.dipcoin.sui.perp.exception.PerpHttpException;
import io.dipcoin.sui.perp.model.ApiResponse;
import io.dipcoin.sui.perp.model.PerpConfig;
import io.dipcoin.sui.perp.model.request.CancelOrderRequest;
import io.dipcoin.sui.perp.model.request.PlaceOrderRequest;
import io.dipcoin.sui.perp.model.response.CancelOrderResponse;

/**
 * @author : Same
 * @datetime : 2025/10/28 17:52
 * @Description : perp trade client
 */
public class PerpTradeClient extends AbstractHttpClient {

    private final PerpConfig perpConfig;

    private final AuthSession subAuth;

    public PerpTradeClient(PerpNetwork perpNetwork, AuthSession subAuth) {
        this.perpConfig = perpNetwork.getConfig();
        this.subAuth = subAuth;
    }

    /**
     * place order
     * @param request
     * @return
     */
    public String placeOrder(PlaceOrderRequest request) {
        ApiResponse<String> response = post(request, perpConfig.perpEndpoint() + PerpPath.PLACE_ORDER, subAuth, new TypeReference<>() {});
        if (response.getCode() == ErrorCode.SUCCESS.getCode()) {
            return response.getData();
        } else {
            throw new PerpHttpException("Failed to placeOrder, cause : " + response.getMessage());
        }
    }

    /**
     * cancel order
     * @param request
     * @return
     */
    public CancelOrderResponse cancelOrder(CancelOrderRequest request) {
        ApiResponse<CancelOrderResponse> response = post(request, perpConfig.perpEndpoint() + PerpPath.CANCEL_ORDER, subAuth, new TypeReference<>() {});
        if (response.getCode() == ErrorCode.SUCCESS.getCode()) {
            return response.getData();
        } else {
            throw new PerpHttpException("Failed to cancelOrder, cause : " + response.getMessage());
        }
    }

}

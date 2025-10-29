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
import io.dipcoin.sui.perp.model.PageResponse;
import io.dipcoin.sui.perp.model.PerpConfig;
import io.dipcoin.sui.perp.model.request.HistoryOrdersRequest;
import io.dipcoin.sui.perp.model.request.OrdersRequest;
import io.dipcoin.sui.perp.model.request.PageRequest;
import io.dipcoin.sui.perp.model.response.*;

import java.util.List;

/**
 * @author : Same
 * @datetime : 2025/10/28 18:01
 * @Description : perp user data client
 */
public class PerpUserClient extends AbstractHttpClient {

    private final PerpConfig perpConfig;

    private final AuthSession mainAuth;

    public PerpUserClient(PerpNetwork perpNetwork, AuthSession mainAuth) {
        this.perpConfig = perpNetwork.getConfig();
        this.mainAuth = mainAuth;
    }

    /**
     * positions
     * @return
     */
    public List<PositionResponse> positions() {
        ApiResponse<List<PositionResponse>> response = get(perpConfig.perpEndpoint() + PerpPath.POSITIONS, null, mainAuth, new TypeReference<>() {});
        if (response.getCode() == ErrorCode.SUCCESS.getCode()) {
            return response.getData();
        } else {
            throw new PerpHttpException("Failed to positions, cause : " + response.getMessage());
        }
    }

    /**
     * current orders
     * @param request
     * @return
     */
    public PageResponse<OrdersResponse> orders(OrdersRequest request) {
        ApiResponse<PageResponse<OrdersResponse>> response = get(perpConfig.perpEndpoint() + PerpPath.ORDERS, super.toQueryParams(request), mainAuth, new TypeReference<>() {});
        if (response.getCode() == ErrorCode.SUCCESS.getCode()) {
            return response.getData();
        } else {
            throw new PerpHttpException("Failed to orders, cause : " + response.getMessage());
        }
    }

    /**
     * account info
     * @return
     */
    public AccountResponse account() {
        ApiResponse<AccountResponse> response = get(perpConfig.perpEndpoint() + PerpPath.ACCOUNT, null, mainAuth, new TypeReference<>() {});
        if (response.getCode() == ErrorCode.SUCCESS.getCode()) {
            return response.getData();
        } else {
            throw new PerpHttpException("Failed to account, cause : " + response.getMessage());
        }
    }

    /**
     * history orders
     * @param request
     * @return
     */
    public PageResponse<HistoryOrdersResponse> historyOrders(HistoryOrdersRequest request) {
        ApiResponse<PageResponse<HistoryOrdersResponse>> response = get(perpConfig.perpEndpoint() + PerpPath.HISTORY_ORDERS, super.toQueryParams(request), mainAuth, new TypeReference<>() {});
        if (response.getCode() == ErrorCode.SUCCESS.getCode()) {
            return response.getData();
        } else {
            throw new PerpHttpException("Failed to historyOrders, cause : " + response.getMessage());
        }
    }

    /**
     * history funding settlements
     * @param request
     * @return
     */
    public PageResponse<FundingSettlementsResponse> fundingSettlements(PageRequest request) {
        ApiResponse<PageResponse<FundingSettlementsResponse>> response = get(perpConfig.perpEndpoint() + PerpPath.FUNDING_SETTLEMENTS, super.toQueryParams(request), mainAuth, new TypeReference<>() {});
        if (response.getCode() == ErrorCode.SUCCESS.getCode()) {
            return response.getData();
        } else {
            throw new PerpHttpException("Failed to fundingSettlements, cause : " + response.getMessage());
        }
    }

    /**
     * history balance changes
     * @param request
     * @return
     */
    public PageResponse<BalanceChangesResponse> balanceChanges(PageRequest request) {
        ApiResponse<PageResponse<BalanceChangesResponse>> response = get(perpConfig.perpEndpoint() + PerpPath.BALANCE_CHANGES, super.toQueryParams(request), mainAuth, new TypeReference<>() {});
        if (response.getCode() == ErrorCode.SUCCESS.getCode()) {
            return response.getData();
        } else {
            throw new PerpHttpException("Failed to balanceChanges, cause : " + response.getMessage());
        }
    }

}

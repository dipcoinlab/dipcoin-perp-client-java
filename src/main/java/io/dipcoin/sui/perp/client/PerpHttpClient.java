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
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dipcoin.sui.crypto.SuiKeyPair;
import io.dipcoin.sui.model.transaction.SuiTransactionBlockResponse;
import io.dipcoin.sui.perp.client.core.AbstractHttpClient;
import io.dipcoin.sui.perp.constant.PerpConstant;
import io.dipcoin.sui.perp.constant.PerpPath;
import io.dipcoin.sui.perp.enums.PerpNetwork;
import io.dipcoin.sui.perp.exception.ErrorCode;
import io.dipcoin.sui.perp.exception.PerpHttpException;
import io.dipcoin.sui.perp.model.ApiResponse;
import io.dipcoin.sui.perp.model.PageResponse;
import io.dipcoin.sui.perp.model.PerpConfig;
import io.dipcoin.sui.perp.model.request.AuthorizationRequest;
import io.dipcoin.sui.perp.model.request.CancelOrderRequest;
import io.dipcoin.sui.perp.model.request.OrdersRequest;
import io.dipcoin.sui.perp.model.request.PlaceOrderRequest;
import io.dipcoin.sui.perp.model.response.*;
import io.dipcoin.sui.perp.util.OrderUtil;
import io.dipcoin.sui.protocol.SuiClient;
import io.dipcoin.sui.protocol.http.HttpService;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author : Same
 * @datetime : 2025/10/21 10:51
 * @Description :
 */
public class PerpHttpClient extends AbstractHttpClient {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final PerpOnChainClient perpOnChainClient;

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
        this.perpOnChainClient = new PerpOnChainClient(suiClient, perpConfig);
        this.mainAccount = mainAccount;
        this.subAccount = mainAccount;
        String address = mainAccount.address();
        super.setAddress(address, address);
        AuthorizationResponse authorize = this.authorize(mainAccount, address);
        String token = authorize.getToken();
        super.setMainAuthHeader(token);
        super.setSubAuthHeader(token);
    }

    public PerpHttpClient(PerpNetwork perpNetwork, SuiClient suiClient, SuiKeyPair mainAccount, SuiKeyPair subAccount) {
        this.perpConfig = perpNetwork.getConfig();
        this.perpOnChainClient = new PerpOnChainClient(suiClient, perpConfig);
        this.mainAccount = mainAccount;
        this.subAccount = subAccount;
        String mainAddress = mainAccount.address();
        String subAddress = subAccount.address();
        super.setAddress(mainAddress, subAddress);
        AuthorizationResponse mainAuthorize = this.authorize(mainAccount, mainAddress);
        super.setMainAuthHeader(mainAuthorize.getToken());
        AuthorizationResponse subAuthorize = this.authorize(subAccount, subAddress);
        super.setSubAuthHeader(subAuthorize.getToken());
    }

    // ------------------------- authorize API -------------------------

    /**
     * authorize
     * @param request
     * @return
     */
    public AuthorizationResponse authorize(AuthorizationRequest request) {
        ApiResponse<AuthorizationResponse> response = post(request, perpConfig.perpEndpoint() + PerpPath.AUTHORIZE, new TypeReference<>() {});
        if (response.getCode() == ErrorCode.SUCCESS.getCode()) {
            return response.getData();
        } else {
            throw new PerpHttpException("Failed to authorize, cause : " + response.getMessage());
        }
    }

    /**
     * authorize
     * @return
     */
    public AuthorizationResponse authorize(SuiKeyPair suiKeyPair, String address) {
        String signature = OrderUtil.getSignature(PerpConstant.ONBOARDING_MSG, suiKeyPair);
        return authorize(new AuthorizationRequest()
                .setSignature(signature)
                .setUserAddress(address)
                .setIsTermAccepted(true));
    }

    // ------------------------- write API -------------------------

    /**
     * place order
     * @param request
     * @return
     */
    public String placeOrder(PlaceOrderRequest request) {
        ApiResponse<String> response = postWithSubAuth(request, perpConfig.perpEndpoint() + PerpPath.PLACE_ORDER, new TypeReference<>() {});
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
        ApiResponse<CancelOrderResponse> response = postWithSubAuth(request, perpConfig.perpEndpoint() + PerpPath.CANCEL_ORDER, new TypeReference<>() {});
        if (response.getCode() == ErrorCode.SUCCESS.getCode()) {
            return response.getData();
        } else {
            throw new PerpHttpException("Failed to cancelOrder, cause : " + response.getMessage());
        }
    }

    // ------------------------- query API -------------------------

    /**
     * positions
     * @return
     */
    public List<PositionResponse> positions() {
        ApiResponse<List<PositionResponse>> response = getWithMainAuth(perpConfig.perpEndpoint() + PerpPath.POSITIONS, null, new TypeReference<>() {});
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
    public PageResponse<List<OrdersResponse>> orders(OrdersRequest request) {
        ApiResponse<PageResponse<List<OrdersResponse>>> response = getWithMainAuth(perpConfig.perpEndpoint() + PerpPath.ORDERS, this.toQueryParams(request), new TypeReference<>() {});
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
        ApiResponse<AccountResponse> response = getWithMainAuth(perpConfig.perpEndpoint() + PerpPath.ACCOUNT, null, new TypeReference<>() {});
        if (response.getCode() == ErrorCode.SUCCESS.getCode()) {
            return response.getData();
        } else {
            throw new PerpHttpException("Failed to orders, cause : " + response.getMessage());
        }
    }

    /**
     * get all trading pairs
     * @return
     */
    public List<TradingPairResponse> tradingPair() {
        ApiResponse<List<TradingPairResponse>> response = get(perpConfig.perpEndpoint() + PerpPath.TRADING_PAIR, null, new TypeReference<>() {});
        if (response.getCode() == ErrorCode.SUCCESS.getCode()) {
            return response.getData();
        } else {
            throw new PerpHttpException("Failed to orders, cause : " + response.getMessage());
        }
    }

    /**
     * get trading pairs by symbol
     * @param symbol
     * @return
     */
    public TradingPairResponse getTradingPair(String symbol) {
        if (null == symbol || symbol.isEmpty()) {
            throw new IllegalArgumentException("symbol is null or empty!");
        }
        TradingPairResponse tradingPair = TRADING_PAIRS.get(symbol);
        if (tradingPair != null) {
            return tradingPair;
        }

        List<TradingPairResponse> response = this.tradingPair();
        if (response == null || response.isEmpty()) {
            throw new PerpHttpException("remote service internal error!");
        }
        for (TradingPairResponse tradingPairResponse : response) {
            TRADING_PAIRS.put(tradingPairResponse.getSymbol(), tradingPairResponse);
        }
        return TRADING_PAIRS.get(symbol);
    }

    // ------------------------- on chain API -------------------------

    /**
     * set sub account
     * @param subAddress
     * @param gasPrice
     * @param gasBudget
     * @return
     */
    public SuiTransactionBlockResponse setSubAccount(String subAddress, long gasPrice, BigInteger gasBudget) {
        return perpOnChainClient.setSubAccount(mainAccount, subAddress, gasPrice, gasBudget);
    }

    /**
     * deposit
     * @param amount
     * @param gasPrice
     * @param gasBudget
     * @return
     */
    public SuiTransactionBlockResponse deposit(BigInteger amount, long gasPrice, BigInteger gasBudget) {
        return perpOnChainClient.deposit(mainAccount, amount, gasPrice, gasBudget);
    }

    /**
     * withdraw
     * @param amount
     * @param gasPrice
     * @param gasBudget
     * @return
     */
    public SuiTransactionBlockResponse withdraw(BigInteger amount, long gasPrice, BigInteger gasBudget) {
        return perpOnChainClient.withdraw(mainAccount, amount, gasPrice, gasBudget);
    }

    public SuiKeyPair getMainKeyPair() {
        return mainAccount;
    }

    public SuiKeyPair getSubAccount() {
        return subAccount;
    }

    /**
     * convert to queryParams
     * @param o
     * @return
     */
    private Map<String, String> toQueryParams(Object o) {
        Map<String, String> queryParams = new HashMap<>();
        if (o == null) {
            return queryParams;
        }

        Map<String, Object> map = objectMapper.convertValue(o, Map.class);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() != null) {
                queryParams.put(entry.getKey(), entry.getValue().toString());
            }
        }
        return queryParams;
    }

}

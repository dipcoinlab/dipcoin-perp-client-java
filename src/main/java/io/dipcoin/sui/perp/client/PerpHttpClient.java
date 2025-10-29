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

import io.dipcoin.sui.crypto.SuiKeyPair;
import io.dipcoin.sui.perp.client.auth.AuthSession;
import io.dipcoin.sui.perp.client.core.AbstractHttpClient;
import io.dipcoin.sui.perp.client.core.PerpAuthorization;
import io.dipcoin.sui.perp.client.core.PerpClient;
import io.dipcoin.sui.perp.enums.PerpNetwork;
import io.dipcoin.sui.perp.model.PageResponse;
import io.dipcoin.sui.perp.model.request.*;
import io.dipcoin.sui.perp.model.response.*;

import java.math.BigInteger;
import java.util.List;

/**
 * @author : Same
 * @datetime : 2025/10/21 10:51
 * @Description :
 */
public class PerpHttpClient extends AbstractHttpClient implements PerpClient {

    private final PerpAuthorization perpAuthorization;

    private final PerpMarketClient perpMarketClient;

    private final PerpUserClient perpUserClient;

    private final PerpTradeClient perpTradeClient;

    private final String mainAddress;

    private final String subAddress;

    private final SuiKeyPair mainAccount;

    private final SuiKeyPair subAccount;

    public PerpHttpClient(PerpNetwork perpNetwork, SuiKeyPair main, SuiKeyPair sub) {
        this.perpAuthorization = new PerpAuthorization(perpNetwork);
        AuthSession mainAuth = authorize(main);
        AuthSession subAuth = authorize(sub);

        this.perpMarketClient = new PerpMarketClient(perpNetwork);
        this.perpUserClient = new PerpUserClient(perpNetwork, mainAuth);
        this.perpTradeClient = new PerpTradeClient(perpNetwork, subAuth);
        this.mainAddress = main.address();
        this.subAddress = sub.address();
        this.mainAccount = main;
        this.subAccount = sub;
    }

    // ------------------------- authorize API -------------------------

    @Override
    public AuthorizationResponse authorize(AuthorizationRequest request) {
        return perpAuthorization.authorize(request);
    }

    @Override
    public AuthSession authorize(SuiKeyPair suiKeyPair) {
        return perpAuthorization.authorize(suiKeyPair);
    }

    // ------------------------- trade API -------------------------

    @Override
    public String placeOrder(PlaceOrderRequest request) {
        return perpTradeClient.placeOrder(request);
    }

    @Override
    public CancelOrderResponse cancelOrder(CancelOrderRequest request) {
        return perpTradeClient.cancelOrder(request);
    }

    // ------------------------- user API -------------------------

    @Override
    public List<PositionResponse> positions() {
        return perpUserClient.positions();
    }

    @Override
    public PageResponse<OrdersResponse> orders(OrdersRequest request) {
        return perpUserClient.orders(request);
    }

    @Override
    public AccountResponse account() {
        return perpUserClient.account();
    }

    @Override
    public PageResponse<HistoryOrdersResponse> historyOrders(HistoryOrdersRequest request) {
        return perpUserClient.historyOrders(request);
    }

    @Override
    public PageResponse<FundingSettlementsResponse> fundingSettlements(PageRequest request) {
        return perpUserClient.fundingSettlements(request);
    }

    @Override
    public PageResponse<BalanceChangesResponse> balanceChanges(PageRequest request) {
        return perpUserClient.balanceChanges(request);
    }

    // ------------------------- market API -------------------------

    @Override
    public TickerResponse ticker(SymbolRequest request) {
        return perpMarketClient.ticker(request);
    }

    @Override
    public OrderBookResponse orderBook(SymbolRequest request) {
        return perpMarketClient.orderBook(request);
    }

    @Override
    public BigInteger oracle(SymbolRequest request) {
        return perpMarketClient.oracle(request);
    }

    @Override
    public List<TradingPairResponse> tradingPair() {
        return perpMarketClient.tradingPair();
    }

    @Override
    public String getMarketPerpId(String symbol) {
        return perpMarketClient.getMarketPerpId(symbol);
    }

    @Override
    public String getPythFeedId(String symbol) {
        return perpMarketClient.getPythFeedId(symbol);
    }

    @Override
    public SuiKeyPair getMainAccount() {
        return mainAccount;
    }

    @Override
    public SuiKeyPair getSubAccount() {
        return subAccount;
    }

    @Override
    public String getMainAddress() {
        return mainAddress;
    }

    @Override
    public String getSubAddress() {
        return subAddress;
    }
}

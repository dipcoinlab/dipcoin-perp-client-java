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

import io.dipcoin.sui.crypto.SuiKeyPair;
import io.dipcoin.sui.perp.client.auth.AuthSession;
import io.dipcoin.sui.perp.model.PageResponse;
import io.dipcoin.sui.perp.model.request.*;
import io.dipcoin.sui.perp.model.response.*;

import java.math.BigInteger;
import java.util.List;

/**
 * @author : Same
 * @datetime : 2025/10/28 18:35
 * @Description : perp client
 */
public interface PerpClient {

    // ------------------------- authorize API -------------------------

    /**
     * authorize
     * @param request
     * @return
     */
    AuthorizationResponse authorize(AuthorizationRequest request);

    /**
     * authorize by SuiKeyPair
     * @param suiKeyPair
     * @return
     */
    AuthSession authorize(SuiKeyPair suiKeyPair);

    // ------------------------- trade API -------------------------

    /**
     * place order
     * @param request
     * @return
     */
    String placeOrder(PlaceOrderRequest request);

    /**
     * cancel order
     * @param request
     * @return
     */
    CancelOrderResponse cancelOrder(CancelOrderRequest request);

    // ------------------------- user API -------------------------

    /**
     * positions
     * @return
     */
    List<PositionResponse> positions();

    /**
     * current orders
     * @param request
     * @return
     */
    PageResponse<OrdersResponse> orders(OrdersRequest request);

    /**
     * account info
     * @return
     */
    AccountResponse account();

    /**
     * history orders
     * @param request
     * @return
     */
    PageResponse<HistoryOrdersResponse> historyOrders(HistoryOrdersRequest request);

    /**
     * history funding settlements
     * @param request
     * @return
     */
    PageResponse<FundingSettlementsResponse> fundingSettlements(PageRequest request);

    /**
     * history balance changes
     * @param request
     * @return
     */
    PageResponse<BalanceChangesResponse> balanceChanges(PageRequest request);

    // ------------------------- market API -------------------------

    /**
     * retrieve ticker information for the trading pair
     * @param request
     * @return
     */
    TickerResponse ticker(SymbolRequest request);

    /**
     * retrieve the order book for the trading pair, with asks sorted in ascending order and bids sorted in descending order
     * @param request
     * @return
     */
    OrderBookResponse orderBook(SymbolRequest request);

    /**
     * retrieve the oracle price for the trading pair
     * @param request
     * @return
     */
    BigInteger oracle(SymbolRequest request);

    /**
     * get all trading pairs
     * @return
     */
    List<TradingPairResponse> tradingPair();

    /**
     * get market perp id by symbol
     * @param symbol
     * @return
     */
    String getMarketPerpId(String symbol);

    /**
     * get pyth feed id by symbol
     * @param symbol
     * @return
     */
    String getPythFeedId(String symbol);

    /**
     * get main SuiKeyPair
     * @return
     */
    SuiKeyPair getMainAccount();

    /**
     * get sub SuiKeyPair
     * @return
     */
    SuiKeyPair getSubAccount();

    /**
     * get main account address
     * @return
     */
    String getMainAddress();

    /**
     * get sub account address
     * @return
     */
    String getSubAddress();

}

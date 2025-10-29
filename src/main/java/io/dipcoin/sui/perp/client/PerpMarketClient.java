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
import io.dipcoin.sui.perp.client.core.AbstractHttpClient;
import io.dipcoin.sui.perp.constant.PerpPath;
import io.dipcoin.sui.perp.enums.PerpNetwork;
import io.dipcoin.sui.perp.exception.ErrorCode;
import io.dipcoin.sui.perp.exception.PerpHttpException;
import io.dipcoin.sui.perp.model.ApiResponse;
import io.dipcoin.sui.perp.model.PerpConfig;
import io.dipcoin.sui.perp.model.request.SymbolRequest;
import io.dipcoin.sui.perp.model.response.OrderBookResponse;
import io.dipcoin.sui.perp.model.response.TickerResponse;
import io.dipcoin.sui.perp.model.response.TradingPairResponse;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author : Same
 * @datetime : 2025/10/28 18:05
 * @Description : perp market client
 */
public class PerpMarketClient extends AbstractHttpClient {

    /**
     * trading pairs map
     * key - symbol
     * value - market perp id
     */
    Map<String, String> PERP_IDS = new ConcurrentHashMap<>();

    /**
     * trading pairs map
     * key - symbol
     * value - pyth feed id
     */
    Map<String, String> FEED_IDS = new ConcurrentHashMap<>();

    private final PerpConfig perpConfig;

    public PerpMarketClient(PerpNetwork perpNetwork) {
        this.perpConfig = perpNetwork.getConfig();
    }

    /**
     * retrieve ticker information for the trading pair
     * @param request
     * @return
     */
    public TickerResponse ticker(SymbolRequest request) {
        ApiResponse<TickerResponse> response = get(perpConfig.perpEndpoint() + PerpPath.TICKER, super.toQueryParams(request), null, new TypeReference<>() {});
        if (response.getCode() == ErrorCode.SUCCESS.getCode()) {
            return response.getData();
        } else {
            throw new PerpHttpException("Failed to orderBook, cause : " + response.getMessage());
        }
    }

    /**
     * retrieve the order book for the trading pair, with asks sorted in ascending order and bids sorted in descending order
     * @param request
     * @return
     */
    public OrderBookResponse orderBook(SymbolRequest request) {
        ApiResponse<OrderBookResponse> response = get(perpConfig.perpEndpoint() + PerpPath.ORDER_BOOK, super.toQueryParams(request), null, new TypeReference<>() {});
        if (response.getCode() == ErrorCode.SUCCESS.getCode()) {
            return response.getData();
        } else {
            throw new PerpHttpException("Failed to orderBook, cause : " + response.getMessage());
        }
    }

    /**
     * retrieve the oracle price for the trading pair
     * @param request
     * @return
     */
    public BigInteger oracle(SymbolRequest request) {
        ApiResponse<BigInteger> response = get(perpConfig.perpEndpoint() + PerpPath.ORACLE, super.toQueryParams(request), null, new TypeReference<>() {});
        if (response.getCode() == ErrorCode.SUCCESS.getCode()) {
            return response.getData();
        } else {
            throw new PerpHttpException("Failed to tradingPair, cause : " + response.getMessage());
        }
    }

    /**
     * get all trading pairs
     * @return
     */
    public List<TradingPairResponse> tradingPair() {
        ApiResponse<List<TradingPairResponse>> response = get(perpConfig.perpEndpoint() + PerpPath.TRADING_PAIR, null, null, new TypeReference<>() {});
        if (response.getCode() == ErrorCode.SUCCESS.getCode()) {
            return response.getData();
        } else {
            throw new PerpHttpException("Failed to tradingPair, cause : " + response.getMessage());
        }
    }

    /**
     * get market perp id by symbol
     * @param symbol
     * @return
     */
    public String getMarketPerpId(String symbol) {
        if (null == symbol || symbol.isEmpty()) {
            throw new IllegalArgumentException("symbol is null or empty!");
        }
        String perpId = PERP_IDS.get(symbol);
        if (perpId != null && !perpId.isEmpty()) {
            return perpId;
        }

        List<TradingPairResponse> response = this.tradingPair();
        if (response == null || response.isEmpty()) {
            throw new PerpHttpException("remote service internal error!");
        }
        for (TradingPairResponse tradingPairResponse : response) {
            PERP_IDS.put(tradingPairResponse.getSymbol(), tradingPairResponse.getPerpId());
        }
        return PERP_IDS.get(symbol);
    }

    /**
     * get pyth feed id by symbol
     * @param symbol
     * @return
     */
    public String getPythFeedId(String symbol) {
        if (null == symbol || symbol.isEmpty()) {
            throw new IllegalArgumentException("symbol is null or empty!");
        }
        String perpId = FEED_IDS.get(symbol);
        if (perpId != null && !perpId.isEmpty()) {
            return perpId;
        }

        List<TradingPairResponse> response = this.tradingPair();
        if (response == null || response.isEmpty()) {
            throw new PerpHttpException("remote service internal error!");
        }
        for (TradingPairResponse tradingPairResponse : response) {
            PERP_IDS.put(tradingPairResponse.getSymbol(), tradingPairResponse.getPriceIdentifierId());
        }
        return PERP_IDS.get(symbol);
    }

}

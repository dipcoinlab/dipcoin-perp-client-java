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

package io.dipcoin.sui.perp.model.response;

import lombok.Data;

/**
 * @author : Same
 * @datetime : 2025/10/27 15:42
 * @Description : Ticker response
 */
@Data
public class TickerResponse {

    /**
     * trading pair
     */
    private String symbol;

    /**
     * last price
     */
    private String lastPrice;

    /**
     * mark price
     */
    private String markPrice;

    /**
     * best ask price
     */
    private String bestAskPrice;

    /**
     * best bid price
     */
    private String bestBidPrice;

    /**
     * 24-hour highest price
     */
    private String high24h;

    /**
     * 24-hour lowest price
     */
    private String low24h;

    /**
     * 24-hour opening price
     */
    private String open24h;

    /**
     * 24-hour trading amount
     */
    private String amount24h;

    /**
     * 24-hour trading volume
     */
    private String volume24h;

    /**
     * best ask amount
     */
    private String bestAskAmount;

    /**
     * best bid amount
     */
    private String bestBidAmount;

    /**
     * timestamp
     */
    private Long timestamp;

    /**
     * 24-hour price change percentage
     */
    private String change24h;

    /**
     * 24-hour price change rate
     */
    private String rate24h;

    /**
     * open price
     */
    private String openPrice;

    /**
     * oracle price
     */
    private String oraclePrice;

    /**
     * funding rate
     */
    private String fundingRate;

    /**
     * open interest
     */
    private String openInterest;

}

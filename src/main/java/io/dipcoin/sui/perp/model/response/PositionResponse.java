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
 * @datetime : 2025/10/23 11:15
 * @Description : Position response
 */
@Data
public class PositionResponse {

    /**
     * ID
     */
    private Long id;

    /**
     * user address
     */
    private String userAddress;

    /**
     * trading pair
     */
    private String symbol;

    /**
     * average opening price
     */
    private String avgEntryPrice;

    /**
     * margin
     */
    private String margin;

    /**
     * leverage
     */
    private String leverage;

    /**
     * quantity
     */
    private String quantity;

    /**
     * created time
     */
    private Long createdAt;

    /**
     * updated time
     */
    private Long updatedAt;

    /**
     * selected leverage
     */
    private String positionSelectedLeverage;

    /**
     * margin type
     */
    private String marginType;

    /**
     * oracle price
     */
    private String oraclePrice;

    /**
     * mid-market price
     */
    private String midMarketPrice;

    /**
     * liquidation price
     */
    private String liquidationPrice;

    /**
     * trade direction: BUY / SELL
     * @see io.dipcoin.sui.perp.enums.OrderSide
     */
    private String side;

    /**
     * position value
     */
    private String positionValue;

    /**
     * unrealized profit/loss
     */
    private String unrealizedProfit;

    /**
     * unrealized profit/loss percentage
     */
    private String roe;

    /**
     * unsettled funding rate
     */
    private String fundingDue;

    /**
     * estimated next funding rate
     */
    private String fundingFeeNext;

    /**
     * settled funding rate for this position
     */
    private String settlementFundingFee;

    /**
     * net margin
     */
    private String netMargin;

    /**
     * whether it has been delisted
     */
    private int isDeliste;

    /**
     * reducable position quantity
     */
    private String positionQtyReducible;

    /**
     * take profit price
     */
    private String tpPrice;

    /**
     * stop loss price
     */
    private String slPrice;

    /**
     * total number of take profit/stop loss orders
     */
    private Integer tpslNum;

}

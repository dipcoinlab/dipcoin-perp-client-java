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
 * @datetime : 2025/10/24 15:50
 * @Description : HistoryOrders response
 */
@Data
public class HistoryOrdersResponse {

    /**
     * ID
     */
    private Long id;

    /**
     * client ID
     */
    private String clientId;

    /**
     * order status
     */
    private String orderStatus;

    /**
     * order hash
     */
    private String orderHash;

    /**
     * trading pair
     */
    private String symbol;

    /**
     * order type: LIMIT, MARKET
     * @see io.dipcoin.sui.perp.enums.OrderType
     */
    private String orderType;

    /**
     * user address
     */
    private String creator;

    /**
     * trade direction: BUY / SELL
     * @see io.dipcoin.sui.perp.enums.OrderSide
     */
    private String side;

    /**
     * price
     */
    private String price;

    /**
     * quantity
     */
    private String quantity;

    /**
     * leverage
     */
    private String leverage;

    /**
     * average filled price
     */
    private String avgPrice;

    /**
     * filled quantity
     */
    private String filledQuantity;

    /**
     * filled transaction fees
     */
    private String filledFee;

    /**
     * realized profit/loss
     */
    private String realizedPnl;

    /**
     * create time
     */
    private Long createdAt;

    /**
     * update time
     */
    private Long updatedAt;

    /**
     * if this order has a "close" operation, it represents the average opening price
     */
    private String entryPrice;

    /**
     * if this order has a "close" operation, it represents the quantity of the closed position
     */
    private String closeQuantity;

    /**
     * trigger condition type
     */
    private String triggerConditionType;

    /**
     * trigger price
     */
    private String triggerPrice;

    /**
     * trigger direction
     */
    private Integer triggerDirection;

    /**
     * reduce only
     */
    private Boolean reduceOnly;

    /**
     * planned types: open (regular order), takeProfit (take profit order), stopLoss (stop loss order)
     */
    private String planOrderType;

}

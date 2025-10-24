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
 * @datetime : 2025/10/23 13:27
 * @Description : Orders response
 */
@Data
public class OrdersResponse {

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
    private String hash;

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
     * random number
     */
    private Long salt;

    /**
     * fee
     */
    private String fee;

    /**
     * filled fee
     */
    private String filledFee;

    /**
     * create time
     */
    private Long createdAt;

    /**
     * update time
     */
    private Long updatedAt;

    /**
     * filled quantity
     */
    private String filledQty;

    /**
     * average opening price
     */
    private String avgFillPrice;

    /**
     * unfilled quantity
     */
    private String openQty;

    /**
     * order value
     */
    private String orderValue;

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

    /**
     * planned batch number
     */
    private Long planBatchId = 0L;

}

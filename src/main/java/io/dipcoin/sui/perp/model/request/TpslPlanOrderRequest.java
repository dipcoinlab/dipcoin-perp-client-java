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

package io.dipcoin.sui.perp.model.request;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigInteger;

/**
 * @author : Same
 * @datetime : 2025/11/25 16:52
 * @Description : TpslPlanOrder request
 */
@Accessors(chain = true)
@Data
public class TpslPlanOrderRequest {

    /**
     * trading pair
     */
    private String symbol;

    /**
     * trade direction: BUY / SELL
     * @see io.dipcoin.sui.perp.enums.OrderSide
     */
    private String side;

    /**
     * leverage multiplier
     */
    private BigInteger leverage;

    /**
     * order belongs to the master account
     */
    private String creator;

    // -------------------- take profit --------------------

    /**
     * take-profit order hash to be modified
     */
    private Long tpPlanId;
        
    /**
     * order types: Planned Limit (LIMIT), Planned Market (MARKET)
     */
    private String tpOrderType;
        
    /**
     * take-profit/stop-loss types: normal (regular) or position (position-based)
     */
    private String tpTpslType;
        
    /**
     * trigger price
     */
    private BigInteger tpTriggerPrice;
        
    /**
     * order price
     */
    private BigInteger tpOrderPrice;
        
    /**
     * quantity: Required when tpslType = normal; when position is specified, pass the maximum allowed value for the trading pair's market/limit order
     */
    private BigInteger tpQuantity;
        
    /**
     * mark price triggered by oracle
     */
    private String tpTriggerWay = "oracle";

    /**
     * signature salt
     */
    private String tpSalt;
        
    /**
     * order signature
     */
    private String tpOrderSignature;
        
    /**
     * cancel order signature
     */
    private String tpCancelSignature;
        
    /**
     * whether to cancel the take-profit order
     */
    private Boolean tpRemove = false;

    // -------------------- stop loss --------------------
        
    /**
     * stop-loss order ID to be modified
     */
    private Long slPlanId;
        
    /**
     * order types: Planned Limit (LIMIT), Planned Market (MARKET)
     */
    private String slOrderType;
        
    /**
     * take-profit/stop-loss types: normal (regular) or position (position-based)
     */
    private String slTpslType;
        
    /**
     * trigger price
     */
    private BigInteger slTriggerPrice;
        
    /**
     * order price
     */
    private BigInteger slOrderPrice;
        
    /**
     * quantity: Required when tpslType = normal; when position is specified, pass the maximum allowed value for the trading pair's market/limit order
     */
    private BigInteger slQuantity;
        
    /**
     * mark price triggered by oracle
     */
    private String slTriggerWay = "oracle";

    /**
     * signature salt
     */
    private String slSalt;
        
    /**
     * order signature
     */
    private String slOrderSignature;
        
    /**
     * cancel order signature
     */
    private String slCancelSignature;
        
    /**
     * whether to cancel the stop-loss order
     */
    private Boolean slRemove = false;

}

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
 * @datetime : 2025/10/23 09:51
 * @Description : PlaceOrder request
 */
@Accessors(chain = true)
@Data
public class PlaceOrderRequest {

    /**
     * trading pair
     */
    private String symbol;

    /**
     * perp id
     */
    private String market;

    /**
     * price
     */
    private BigInteger price;

    /**
     * quantity
     */
    private BigInteger quantity;

    /**
     * trade direction: BUY / SELL
     * @see io.dipcoin.sui.perp.enums.OrderSide
     */
    private String side;

    /**
     * order types: LIMIT, MARKET
     * @see io.dipcoin.sui.perp.enums.OrderType
     */
    private String orderType;

    /**
     * leverage multiplier
     */
    private BigInteger leverage;

    /**
     * whether to reduce position only
     */
    private Boolean reduceOnly = false;

    /**
     * signature salt
     */
    private String salt;

    /**
     * order belongs to the master account
     */
    private String creator;

    /**
     * client ID
     */
    private String clientId = "";

    /**
     * order signature
     */
    private String orderSignature;

}

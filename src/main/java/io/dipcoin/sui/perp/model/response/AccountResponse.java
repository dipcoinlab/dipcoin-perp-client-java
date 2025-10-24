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

import java.util.List;

/**
 * @author : Same
 * @datetime : 2025/10/23 13:42
 * @Description : Account response
 */
@Data
public class AccountResponse {

    /**
     * address
     */
    private String address;

    /**
     * whether trading is allowed
     */
    private Boolean canTrade;

    /**
     * update time
     */
    private Long updateTime;

    /**
     * fee tier
     */
    private String feeTier;

    /**
     * wallet balance
     */
    private String walletBalance;

    /**
     * total position margin
     */
    private String totalPositionMargin;

    /**
     * total unrealized profit
     */
    private String totalUnrealizedProfit;

    /**
     * available margin
     */
    private String freeCollateral;

    /**
     * account value
     */
    private String accountValue;

    /**
     * account data for each market
     */
    private List<AccountDataByMarketResponse> accountDataByMarket;

}

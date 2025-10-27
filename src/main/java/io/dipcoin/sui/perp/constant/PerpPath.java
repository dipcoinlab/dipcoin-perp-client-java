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

package io.dipcoin.sui.perp.constant;

/**
 * @author : Same
 * @datetime : 2025/10/22 13:44
 * @Description :
 */
public interface PerpPath {

    String AUTHORIZE = "/authorize";

    String PLACE_ORDER = "/perp-trade-api/trade/placeorder";

    String CANCEL_ORDER = "/perp-trade-api/trade/cancelorder";

    String POSITIONS = "/perp-trade-api/curr-info/positions";

    String ORDERS = "/perp-trade-api/curr-info/orders";

    String ACCOUNT = "/perp-trade-api/curr-info/account";

    String HISTORY_ORDERS = "/perp-trade-api/history/orders";

    String FUNDING_SETTLEMENTS = "/perp-trade-api/history/funding-settlements";

    String BALANCE_CHANGES = "/perp-trade-api/history/balance-changes";

    String TICKER = "/perp-market-api/ticker";

    String ORDER_BOOK = "/perp-market-api/orderBook";

    String ORACLE = "/perp-market-api/oracle";

    String TRADING_PAIR = "/perp-market-api/list";

}

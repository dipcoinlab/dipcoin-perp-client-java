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

package io.dipcoin.sui.perp.constant

object PerpPath {
    const val AUTHORIZE = "/authorize"
    const val PLACE_ORDER = "/perp-trade-api/trade/placeorder"
    const val CANCEL_ORDER = "/perp-trade-api/trade/cancelorder"
    const val QUERY_TPSL_PLAN = "/perp-trade-api/plan/position/tpsl"
    const val TPSL_CLOSE_ORDER = "/perp-trade-api/plan/batch/plancloseorder"
    const val POSITIONS = "/perp-trade-api/curr-info/positions"
    const val ORDERS = "/perp-trade-api/curr-info/orders"
    const val ACCOUNT = "/perp-trade-api/curr-info/account"
    const val HISTORY_ORDERS = "/perp-trade-api/history/orders"
    const val FUNDING_SETTLEMENTS = "/perp-trade-api/history/funding-settlements"
    const val BALANCE_CHANGES = "/perp-trade-api/history/balance-changes"
    const val TICKER = "/perp-market-api/ticker"
    const val ORDER_BOOK = "/perp-market-api/orderBook"
    const val ORACLE = "/perp-market-api/oracle"
    const val TRADING_PAIR = "/perp-market-api/list"
}

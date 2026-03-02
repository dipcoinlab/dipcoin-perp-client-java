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

package io.dipcoin.sui.perp.client.core

import io.dipcoin.sui.crypto.SuiKeyPair
import io.dipcoin.sui.perp.client.auth.AuthSession
import io.dipcoin.sui.perp.model.PageResponse
import io.dipcoin.sui.perp.model.request.*
import io.dipcoin.sui.perp.model.response.*
import java.math.BigInteger

interface PerpClient {
    fun authorize(request: AuthorizationRequest): AuthorizationResponse
    fun authorize(suiKeyPair: SuiKeyPair<*>): AuthSession
    fun placeOrder(request: PlaceOrderRequest): String?
    fun cancelOrder(request: CancelOrderRequest): CancelOrderResponse?
    fun queryTpslPlan(request: QueryTpslPlanRequest): OrdersResponse?
    fun planCloseOrder(request: TpslPlanOrderRequest): String?
    fun positions(request: PositionRequest): List<PositionResponse>?
    fun orders(request: OrdersRequest): PageResponse<OrdersResponse>?
    fun account(request: AccountRequest): AccountResponse?
    fun historyOrders(request: HistoryOrdersRequest): PageResponse<HistoryOrdersResponse>?
    fun fundingSettlements(request: FundingPageRequest): PageResponse<FundingSettlementsResponse>?
    fun balanceChanges(request: BalancePageRequest): PageResponse<BalanceChangesResponse>?
    fun ticker(request: SymbolRequest): TickerResponse?
    fun orderBook(request: SymbolRequest): OrderBookResponse?
    fun oracle(request: SymbolRequest): BigInteger?
    fun tradingPair(): List<TradingPairResponse>?
    fun getMarketPerpId(symbol: String): String?
    fun getPythFeedId(symbol: String): String?
    fun getMainAccount(): SuiKeyPair<*>
    fun getSubAccount(): SuiKeyPair<*>
    fun getMainAddress(): String
    fun getSubAddress(): String
}

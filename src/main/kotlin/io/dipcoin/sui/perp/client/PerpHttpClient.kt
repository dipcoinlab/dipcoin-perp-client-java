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

package io.dipcoin.sui.perp.client

import io.dipcoin.sui.crypto.SuiKeyPair
import io.dipcoin.sui.perp.client.auth.AuthSession
import io.dipcoin.sui.perp.client.core.AbstractHttpClient
import io.dipcoin.sui.perp.client.core.PerpAuthorization
import io.dipcoin.sui.perp.client.core.PerpClient
import io.dipcoin.sui.perp.enums.PerpNetwork
import io.dipcoin.sui.perp.model.PageResponse
import io.dipcoin.sui.perp.model.request.*
import io.dipcoin.sui.perp.model.response.*
import java.math.BigInteger

class PerpHttpClient(
    perpNetwork: PerpNetwork,
    main: SuiKeyPair<*>,
    sub: SuiKeyPair<*>
) : AbstractHttpClient(), PerpClient {

    private val perpAuthorization = PerpAuthorization(perpNetwork)
    private val mainAuth: AuthSession = authorize(main)
    private val subAuth: AuthSession = authorize(sub)
    private val perpMarketClient = PerpMarketClient(perpNetwork)
    private val perpUserClient = PerpUserClient(perpNetwork, mainAuth)
    private val perpTradeClient = PerpTradeClient(perpNetwork, subAuth)
    private val mainAddress = main.address()
    private val subAddress = sub.address()
    private val mainAccount = main
    private val subAccount = sub

    override fun authorize(request: AuthorizationRequest): AuthorizationResponse =
        perpAuthorization.authorize(request)

    override fun authorize(suiKeyPair: SuiKeyPair<*>): AuthSession =
        perpAuthorization.authorize(suiKeyPair)

    override fun placeOrder(request: PlaceOrderRequest): String? =
        perpTradeClient.placeOrder(request)

    override fun cancelOrder(request: CancelOrderRequest): CancelOrderResponse? =
        perpTradeClient.cancelOrder(request)

    override fun queryTpslPlan(request: QueryTpslPlanRequest): OrdersResponse? =
        perpTradeClient.queryTpslPlan(request)

    override fun planCloseOrder(request: TpslPlanOrderRequest): String? =
        perpTradeClient.planCloseOrder(request)

    override fun positions(request: PositionRequest): List<PositionResponse>? =
        perpUserClient.positions(request)

    override fun orders(request: OrdersRequest): PageResponse<OrdersResponse>? =
        perpUserClient.orders(request)

    override fun account(request: AccountRequest): AccountResponse? =
        perpUserClient.account(request)

    override fun historyOrders(request: HistoryOrdersRequest): PageResponse<HistoryOrdersResponse>? =
        perpUserClient.historyOrders(request)

    override fun fundingSettlements(request: FundingPageRequest): PageResponse<FundingSettlementsResponse>? =
        perpUserClient.fundingSettlements(request)

    override fun balanceChanges(request: BalancePageRequest): PageResponse<BalanceChangesResponse>? =
        perpUserClient.balanceChanges(request)

    override fun ticker(request: SymbolRequest): TickerResponse? =
        perpMarketClient.ticker(request)

    override fun orderBook(request: SymbolRequest): OrderBookResponse? =
        perpMarketClient.orderBook(request)

    override fun oracle(request: SymbolRequest): BigInteger? =
        perpMarketClient.oracle(request)

    override fun tradingPair(): List<TradingPairResponse>? =
        perpMarketClient.tradingPair()

    override fun getMarketPerpId(symbol: String): String? =
        perpMarketClient.getMarketPerpId(symbol)

    override fun getPythFeedId(symbol: String): String? =
        perpMarketClient.getPythFeedId(symbol)

    override fun getMainAccount(): SuiKeyPair<*> = mainAccount
    override fun getSubAccount(): SuiKeyPair<*> = subAccount
    override fun getMainAddress(): String = mainAddress
    override fun getSubAddress(): String = subAddress
}

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

import com.fasterxml.jackson.core.type.TypeReference
import io.dipcoin.sui.perp.client.auth.AuthSession
import io.dipcoin.sui.perp.client.core.AbstractHttpClient
import io.dipcoin.sui.perp.constant.PerpPath
import io.dipcoin.sui.perp.enums.PerpNetwork
import io.dipcoin.sui.perp.exception.ErrorCode
import io.dipcoin.sui.perp.exception.PerpHttpException
import io.dipcoin.sui.perp.model.ApiResponse
import io.dipcoin.sui.perp.model.PageResponse
import io.dipcoin.sui.perp.model.request.*
import io.dipcoin.sui.perp.model.response.*

class PerpUserClient(perpNetwork: PerpNetwork, private val mainAuth: AuthSession) : AbstractHttpClient() {
    private val perpConfig = perpNetwork.getConfig()

    fun positions(request: PositionRequest): List<PositionResponse> {
        val response = get(perpConfig.perpEndpoint + PerpPath.POSITIONS, toQueryParams(request), mainAuth, object : TypeReference<ApiResponse<List<PositionResponse>>>() {})
            ?: throw PerpHttpException("Failed to positions: null response")
        if (response.code == ErrorCode.SUCCESS.code) return response.data ?: emptyList()
        throw PerpHttpException("Failed to positions, cause : ${response.message}")
    }

    fun orders(request: OrdersRequest): PageResponse<OrdersResponse> {
        val response = get(perpConfig.perpEndpoint + PerpPath.ORDERS, toQueryParams(request), mainAuth, object : TypeReference<ApiResponse<PageResponse<OrdersResponse>>>() {})
            ?: throw PerpHttpException("Failed to orders: null response")
        if (response.code == ErrorCode.SUCCESS.code) return response.data!!
        throw PerpHttpException("Failed to orders, cause : ${response.message}")
    }

    fun account(request: AccountRequest): AccountResponse {
        val response = get(perpConfig.perpEndpoint + PerpPath.ACCOUNT, toQueryParams(request), mainAuth, object : TypeReference<ApiResponse<AccountResponse>>() {})
            ?: throw PerpHttpException("Failed to account: null response")
        if (response.code == ErrorCode.SUCCESS.code) return response.data!!
        throw PerpHttpException("Failed to account, cause : ${response.message}")
    }

    fun historyOrders(request: HistoryOrdersRequest): PageResponse<HistoryOrdersResponse> {
        val response = get(perpConfig.perpEndpoint + PerpPath.HISTORY_ORDERS, toQueryParams(request), mainAuth, object : TypeReference<ApiResponse<PageResponse<HistoryOrdersResponse>>>() {})
            ?: throw PerpHttpException("Failed to historyOrders: null response")
        if (response.code == ErrorCode.SUCCESS.code) return response.data!!
        throw PerpHttpException("Failed to historyOrders, cause : ${response.message}")
    }

    fun fundingSettlements(request: FundingPageRequest): PageResponse<FundingSettlementsResponse> {
        val response = get(perpConfig.perpEndpoint + PerpPath.FUNDING_SETTLEMENTS, toQueryParams(request), mainAuth, object : TypeReference<ApiResponse<PageResponse<FundingSettlementsResponse>>>() {})
            ?: throw PerpHttpException("Failed to fundingSettlements: null response")
        if (response.code == ErrorCode.SUCCESS.code) return response.data!!
        throw PerpHttpException("Failed to fundingSettlements, cause : ${response.message}")
    }

    fun balanceChanges(request: BalancePageRequest): PageResponse<BalanceChangesResponse> {
        val response = get(perpConfig.perpEndpoint + PerpPath.BALANCE_CHANGES, toQueryParams(request), mainAuth, object : TypeReference<ApiResponse<PageResponse<BalanceChangesResponse>>>() {})
            ?: throw PerpHttpException("Failed to balanceChanges: null response")
        if (response.code == ErrorCode.SUCCESS.code) return response.data!!
        throw PerpHttpException("Failed to balanceChanges, cause : ${response.message}")
    }
}

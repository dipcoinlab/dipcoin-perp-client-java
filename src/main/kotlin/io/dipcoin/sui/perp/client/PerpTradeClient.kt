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
import io.dipcoin.sui.perp.model.PerpConfig
import io.dipcoin.sui.perp.model.request.CancelOrderRequest
import io.dipcoin.sui.perp.model.request.PlaceOrderRequest
import io.dipcoin.sui.perp.model.request.QueryTpslPlanRequest
import io.dipcoin.sui.perp.model.request.TpslPlanOrderRequest
import io.dipcoin.sui.perp.model.response.CancelOrderResponse
import io.dipcoin.sui.perp.model.response.OrdersResponse

class PerpTradeClient(perpNetwork: PerpNetwork, private val subAuth: AuthSession) : AbstractHttpClient() {
    private val perpConfig: PerpConfig = perpNetwork.getConfig()

    fun placeOrder(request: PlaceOrderRequest): String {
        val response = post(request, perpConfig.perpEndpoint + PerpPath.PLACE_ORDER, subAuth, object : TypeReference<ApiResponse<String>>() {})
            ?: throw PerpHttpException("Failed to placeOrder: null response")
        if (response.code == ErrorCode.SUCCESS.code) return response.data ?: ""
        throw PerpHttpException("Failed to placeOrder, cause : ${response.message}")
    }

    fun cancelOrder(request: CancelOrderRequest): CancelOrderResponse {
        val response = post(request, perpConfig.perpEndpoint + PerpPath.CANCEL_ORDER, subAuth, object : TypeReference<ApiResponse<CancelOrderResponse>>() {})
            ?: throw PerpHttpException("Failed to cancelOrder: null response")
        if (response.code == ErrorCode.SUCCESS.code) return response.data!!
        throw PerpHttpException("Failed to cancelOrder, cause : ${response.message}")
    }

    fun queryTpslPlan(request: QueryTpslPlanRequest): OrdersResponse {
        val response = get(perpConfig.perpEndpoint + PerpPath.QUERY_TPSL_PLAN, toQueryParams(request), subAuth, object : TypeReference<ApiResponse<OrdersResponse>>() {})
            ?: throw PerpHttpException("Failed to tpslPlan: null response")
        if (response.code == ErrorCode.SUCCESS.code) return response.data!!
        throw PerpHttpException("Failed to tpslPlan, cause : ${response.message}")
    }

    fun planCloseOrder(request: TpslPlanOrderRequest): String {
        val response = post(request, perpConfig.perpEndpoint + PerpPath.TPSL_CLOSE_ORDER, subAuth, object : TypeReference<ApiResponse<String>>() {})
            ?: throw PerpHttpException("Failed to planCloseOrder: null response")
        if (response.code == ErrorCode.SUCCESS.code) return response.data ?: ""
        throw PerpHttpException("Failed to planCloseOrder, cause : ${response.message}")
    }
}

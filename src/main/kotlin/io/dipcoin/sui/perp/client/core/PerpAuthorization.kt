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

import com.fasterxml.jackson.core.type.TypeReference
import io.dipcoin.sui.crypto.SuiKeyPair
import io.dipcoin.sui.perp.client.auth.AuthSession
import io.dipcoin.sui.perp.constant.PerpConstant
import io.dipcoin.sui.perp.constant.PerpPath
import io.dipcoin.sui.perp.enums.PerpNetwork
import io.dipcoin.sui.perp.exception.ErrorCode
import io.dipcoin.sui.perp.exception.PerpHttpException
import io.dipcoin.sui.perp.model.ApiResponse
import io.dipcoin.sui.perp.model.PerpConfig
import io.dipcoin.sui.perp.model.request.AuthorizationRequest
import io.dipcoin.sui.perp.model.response.AuthorizationResponse
import io.dipcoin.sui.perp.util.OrderUtil

class PerpAuthorization(perpNetwork: PerpNetwork) : AbstractHttpClient() {
    private val perpConfig: PerpConfig = perpNetwork.getConfig()

    fun authorize(request: AuthorizationRequest): AuthorizationResponse {
        val response = post(
            request,
            perpConfig.perpEndpoint + PerpPath.AUTHORIZE,
            null,
            object : TypeReference<ApiResponse<AuthorizationResponse>>() {}
        ) ?: throw PerpHttpException("Failed to authorize: null response")
        if (response.code == ErrorCode.SUCCESS.code) {
            return response.data ?: throw PerpHttpException("Failed to authorize: null data")
        }
        throw PerpHttpException("Failed to authorize, cause : ${response.message}")
    }

    fun authorize(suiKeyPair: SuiKeyPair<*>): AuthSession {
        val address = suiKeyPair.address()
        val signature = OrderUtil.getSignature(PerpConstant.ONBOARDING_MSG, suiKeyPair)
        val authorize = authorize(
            AuthorizationRequest(
                signature = signature,
                userAddress = address,
                isTermAccepted = true
            )
        )
        return AuthSession(address, authorize.token ?: "")
    }
}

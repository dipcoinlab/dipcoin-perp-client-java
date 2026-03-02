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

package io.dipcoin.sui.perp.model.response

data class HistoryOrdersResponse(
    var id: Long? = null,
    var clientId: String? = null,
    var orderStatus: String? = null,
    var orderHash: String? = null,
    var symbol: String? = null,
    var orderType: String? = null,
    var creator: String? = null,
    var side: String? = null,
    var price: String? = null,
    var quantity: String? = null,
    var leverage: String? = null,
    var avgPrice: String? = null,
    var filledQuantity: String? = null,
    var filledFee: String? = null,
    var realizedPnl: String? = null,
    var createdAt: Long? = null,
    var updatedAt: Long? = null,
    var entryPrice: String? = null,
    var closeQuantity: String? = null,
    var triggerConditionType: String? = null,
    var triggerPrice: String? = null,
    var triggerDirection: Int? = null,
    var reduceOnly: Boolean? = null,
    var planOrderType: String? = null,
)

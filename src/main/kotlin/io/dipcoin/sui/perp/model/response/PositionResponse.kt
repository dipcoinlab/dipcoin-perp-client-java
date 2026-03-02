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

data class PositionResponse(
    var id: Long? = null,
    var userAddress: String? = null,
    var symbol: String? = null,
    var avgEntryPrice: String? = null,
    var margin: String? = null,
    var leverage: String? = null,
    var quantity: String? = null,
    var createdAt: Long? = null,
    var updatedAt: Long? = null,
    var positionSelectedLeverage: String? = null,
    var marginType: String? = null,
    var oraclePrice: String? = null,
    var midMarketPrice: String? = null,
    var liquidationPrice: String? = null,
    var side: String? = null,
    var positionValue: String? = null,
    var unrealizedProfit: String? = null,
    var roe: String? = null,
    var fundingDue: String? = null,
    var fundingFeeNext: String? = null,
    var settlementFundingFee: String? = null,
    var netMargin: String? = null,
    var isDeliste: Int = 0,
    var positionQtyReducible: String? = null,
    var tpPrice: String? = null,
    var slPrice: String? = null,
    var tpslNum: Int? = null,
)

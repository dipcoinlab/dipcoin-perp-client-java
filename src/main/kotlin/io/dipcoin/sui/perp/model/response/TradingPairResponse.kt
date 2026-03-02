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

data class TradingPairResponse(
    var perpId: String? = null,
    var symbol: String? = null,
    var coinName: String? = null,
    var status: Int? = null,
    var initialMargin: String? = null,
    var maintenanceMargin: String? = null,
    var makerFee: String? = null,
    var takerFee: String? = null,
    var stepSize: String? = null,
    var tickSize: String? = null,
    var maxQtyLimit: String? = null,
    var maxQtyMarket: String? = null,
    var feePoolAddress: String? = null,
    var mtbLong: String? = null,
    var mtbShort: String? = null,
    var maxFunding: String? = null,
    var maxLeverage: Int? = null,
    var defaultLeverage: Int? = null,
    var perpOiLimitVOList: List<PerpOiLimitResponse>? = null,
    var priceIdentifierId: String? = null,
    var startTime: String? = null,
    var fundingRate: String? = null,
    var eightHFundingRate: String? = null,
)

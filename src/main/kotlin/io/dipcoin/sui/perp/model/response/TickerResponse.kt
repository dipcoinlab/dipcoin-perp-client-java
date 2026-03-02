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

data class TickerResponse(
    var symbol: String? = null,
    var lastPrice: String? = null,
    var markPrice: String? = null,
    var bestAskPrice: String? = null,
    var bestBidPrice: String? = null,
    var high24h: String? = null,
    var low24h: String? = null,
    var open24h: String? = null,
    var amount24h: String? = null,
    var volume24h: String? = null,
    var bestAskAmount: String? = null,
    var bestBidAmount: String? = null,
    var timestamp: Long? = null,
    var change24h: String? = null,
    var rate24h: String? = null,
    var openPrice: String? = null,
    var oraclePrice: String? = null,
    var fundingRate: String? = null,
    var openInterest: String? = null,
)

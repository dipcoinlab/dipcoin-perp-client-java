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

package io.dipcoin.sui.perp.model.request

import java.math.BigInteger

data class PlaceOrderRequest(
    var symbol: String? = null,
    var market: String? = null,
    var price: BigInteger? = null,
    var quantity: BigInteger? = null,
    var side: String? = null,
    var orderType: String? = null,
    var leverage: BigInteger? = null,
    var reduceOnly: Boolean = false,
    var salt: String? = null,
    var creator: String? = null,
    var clientId: String = "",
    var orderSignature: String? = null,
    var triggerWay: String = "oracle",
    // tp/sl
    var tpOrderType: String? = null,
    var tpTriggerPrice: BigInteger? = null,
    var tpOrderPrice: BigInteger? = null,
    var tpSalt: String? = null,
    var tpOrderSignature: String? = null,
    var slOrderType: String? = null,
    var slTriggerPrice: BigInteger? = null,
    var slOrderPrice: BigInteger? = null,
    var slSalt: String? = null,
    var slOrderSignature: String? = null,
)

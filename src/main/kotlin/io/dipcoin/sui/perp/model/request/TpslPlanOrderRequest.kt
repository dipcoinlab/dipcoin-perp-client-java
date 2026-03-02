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

data class TpslPlanOrderRequest(
    var symbol: String? = null,
    var side: String? = null,
    var leverage: BigInteger? = null,
    var creator: String? = null,
    var tpPlanId: Long? = null,
    var tpOrderType: String? = null,
    var tpTpslType: String? = null,
    var tpTriggerPrice: BigInteger? = null,
    var tpOrderPrice: BigInteger? = null,
    var tpQuantity: BigInteger? = null,
    var tpTriggerWay: String = "oracle",
    var tpSalt: String? = null,
    var tpOrderSignature: String? = null,
    var tpCancelSignature: String? = null,
    var tpRemove: Boolean = false,
    var slPlanId: Long? = null,
    var slOrderType: String? = null,
    var slTpslType: String? = null,
    var slTriggerPrice: BigInteger? = null,
    var slOrderPrice: BigInteger? = null,
    var slQuantity: BigInteger? = null,
    var slTriggerWay: String = "oracle",
    var slSalt: String? = null,
    var slOrderSignature: String? = null,
    var slCancelSignature: String? = null,
    var slRemove: Boolean = false,
)

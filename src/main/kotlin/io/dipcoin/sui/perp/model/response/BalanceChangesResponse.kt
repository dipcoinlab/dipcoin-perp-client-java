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

import java.math.BigDecimal

data class BalanceChangesResponse(
    var account: String? = null,
    var txDigest: String? = null,
    var txIndex: Long? = null,
    var bizType: Int? = null,
    var bizTypeDesc: String? = null,
    var settlementAmount: BigDecimal? = null,
    var createdTime: Long = 0L,
)

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

package io.dipcoin.sui.perp.enums

enum class TpslTypeEnum(val tpslType: String, val code: Int) {
    /** regular order */
    OPEN("open", 0),
    /** planned opening position */
    PLAN_OPEN("planOpen", 1),
    /** regular take profit/stop loss */
    NORMAL("normal", 2),
    /** position take profit/stop loss */
    POSITION("position", 3),
    /** pre-set take profit/stop loss for opening positions */
    ORDER_BASES_PLAN("orderBasePlan", 4),
    /** parent order for position take profit/stop loss */
    ORDER_BASES_OPEN("orderBaseOpen", 5),
}

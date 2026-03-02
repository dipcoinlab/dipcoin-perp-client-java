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

/**
 * order side
 */
enum class OrderSide(val code: String, val value: Int) {
    BUY("BUY", 1),
    SELL("SELL", 2);

    companion object {
        fun fromValue(value: Int): OrderSide =
            entries.find { it.value == value }
                ?: throw IllegalArgumentException("Invalid OrderSide value: $value")

        fun fromCode(code: String): OrderSide =
            entries.find { it.code == code }
                ?: throw IllegalArgumentException("Invalid OrderSide code: $code")
    }
}

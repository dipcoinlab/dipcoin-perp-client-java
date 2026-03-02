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

package io.dipcoin.sui.perp.util

import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

object DecimalUtil {
    private const val UINT = 18
    private const val SUI_UINT = 9
    private val BASE_UINT_INTEGER = BigInteger.TEN.pow(UINT)
    private val BASE_UINT_DECIMAL = BigDecimal.TEN.pow(UINT)
    private val SUI_UINT_INTEGER = BigInteger.TEN.pow(SUI_UINT)
    private val SUI_UINT_DECIMAL = BigDecimal.TEN.pow(SUI_UINT)
    private val HALF_BASE_UINT = BASE_UINT_INTEGER.divide(BigInteger.valueOf(2))

    fun getBaseUintInteger(): BigInteger = BASE_UINT_INTEGER
    fun getBaseUintDecimal(): BigDecimal = BASE_UINT_DECIMAL
    fun getHalfBaseUint(): BigInteger = HALF_BASE_UINT

    fun baseMul(value: BigInteger, baseValue: BigInteger): BigInteger =
        value.multiply(baseValue).divide(BASE_UINT_INTEGER)

    fun baseDiv(value: BigInteger, baseValue: BigInteger): BigInteger =
        value.multiply(BASE_UINT_INTEGER).divide(baseValue)

    fun ceil(a: BigInteger, m: BigInteger): BigInteger =
        a.add(m).subtract(BigInteger.ONE).divide(m).multiply(m)

    fun floor(a: BigInteger, m: BigInteger): BigInteger =
        a.divide(m).multiply(m)

    fun min(a: BigInteger, b: BigInteger): BigInteger = if (a.compareTo(b) < 0) a else b
    fun sub(a: BigInteger, b: BigInteger): BigInteger = a.subtract(b)

    fun toBaseUnit(value: String): BigInteger = toBaseUnit(BigDecimal(value))
    fun toBaseUnitDecimal(value: BigDecimal): BigDecimal = value.multiply(BASE_UINT_DECIMAL)
    fun toBaseUnit(value: BigDecimal): BigInteger = value.multiply(BASE_UINT_DECIMAL).toBigInteger()
    fun toBaseUnit(value: BigInteger): BigInteger = value.multiply(BASE_UINT_INTEGER)
    fun toSui(value: BigDecimal): BigInteger = value.multiply(SUI_UINT_DECIMAL).toBigInteger()
    fun toSui(value: BigInteger): BigInteger = value.multiply(SUI_UINT_INTEGER)

    fun fromBaseUnit(value: BigInteger): BigDecimal =
        BigDecimal(value).divide(BASE_UINT_DECIMAL, UINT, RoundingMode.DOWN)
}

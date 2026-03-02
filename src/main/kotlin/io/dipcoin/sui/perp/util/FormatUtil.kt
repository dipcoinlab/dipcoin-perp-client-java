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
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

object FormatUtil {
    private const val UINT = 18
    private val BASE_UINT_MAP = ConcurrentHashMap<Int, BigDecimal>()
    private val BASE_UINT_DECIMAL = BigDecimal.TEN.pow(UINT)
    private val DF_ONE: DecimalFormat
    private val DF_TEN: DecimalFormat
    private const val ZERO = "0"
    private const val DOT = "."
    private const val NEGATIVE = "-"
    private val ZERO_REGEX = Regex("0+$")

    init {
        DF_TEN = DecimalFormat("#,##0.##").apply {
            decimalFormatSymbols = DecimalFormatSymbols.getInstance(Locale.US)
            roundingMode = RoundingMode.DOWN
        }
        DF_ONE = DecimalFormat("#,##0.###").apply {
            decimalFormatSymbols = DecimalFormatSymbols.getInstance(Locale.US)
            roundingMode = RoundingMode.DOWN
        }
    }

    fun format(value: BigInteger?, decimal: Int): String {
        if (value == null || value == BigInteger.ZERO) return ZERO
        val decimalValue = BigDecimal(value).divide(getBaseUint(decimal), decimal, RoundingMode.DOWN)
        return formatDecimal(decimalValue)
    }

    fun format(value: BigDecimal?, decimal: Int): String {
        if (value == null || value == BigDecimal.ZERO) return ZERO
        val decimalValue = value.divide(getBaseUint(decimal), decimal, RoundingMode.DOWN)
        return formatDecimal(decimalValue)
    }

    fun format(value: BigInteger?): String {
        if (value == null || value == BigInteger.ZERO) return ZERO
        val decimalValue = BigDecimal(value).divide(BASE_UINT_DECIMAL, UINT, RoundingMode.DOWN)
        return formatDecimal(decimalValue)
    }

    fun format(value: BigDecimal?): String {
        if (value == null || value == BigDecimal.ZERO) return ZERO
        val decimalValue = value.divide(BASE_UINT_DECIMAL, UINT, RoundingMode.DOWN)
        return formatDecimal(decimalValue)
    }

    private fun formatDecimal(value: BigDecimal?): String {
        if (value == null || value.compareTo(BigDecimal.ZERO) == 0) return ZERO
        val absValue = value.abs()
        return when {
            absValue >= BigDecimal.TEN -> formatGreaterThanOrEqualTen(value)
            absValue >= BigDecimal.ONE -> formatGreaterThanOrEqualOne(value)
            else -> formatLessThanOne(value)
        }
    }

    private fun formatGreaterThanOrEqualTen(value: BigDecimal): String =
        removeTrailingZeros(DF_TEN.format(value))

    private fun formatGreaterThanOrEqualOne(value: BigDecimal): String =
        removeTrailingZeros(DF_ONE.format(value))

    private fun formatLessThanOne(value: BigDecimal): String {
        var stringValue = value.toPlainString()
        val isNegative = value < BigDecimal.ZERO
        if (isNegative) stringValue = stringValue.substring(1)
        if (stringValue.contains("E") || stringValue.contains("e")) {
            stringValue = value.toEngineeringString()
        }
        val decimalPointIndex = stringValue.indexOf('.')
        if (decimalPointIndex == -1) {
            return if (isNegative) NEGATIVE + stringValue else stringValue
        }
        var firstNonZeroIndex = -1
        for (i in (decimalPointIndex + 1) until stringValue.length) {
            if (stringValue[i] != '0') {
                firstNonZeroIndex = i
                break
            }
        }
        if (firstNonZeroIndex == -1) return ZERO
        val zerosAfterDecimal = firstNonZeroIndex - decimalPointIndex - 1
        val digitsToKeep = zerosAfterDecimal + 4
        val endIndex = minOf(stringValue.length, decimalPointIndex + digitsToKeep + 1)
        var result = stringValue.substring(0, endIndex)
        result = removeTrailingZeros(result)
        return if (isNegative) NEGATIVE + result else result
    }

    private fun removeTrailingZeros(number: String): String {
        if (!number.contains(DOT)) return number
        var result = ZERO_REGEX.replace(number, "")
        if (result.endsWith(DOT)) result = result.substring(0, result.length - 1)
        return result
    }

    private fun getBaseUint(decimal: Int): BigDecimal {
        BASE_UINT_MAP[decimal]?.let { return it }
        val decimalPow = BigDecimal.TEN.pow(decimal)
        BASE_UINT_MAP[decimal] = decimalPow
        return decimalPow
    }
}

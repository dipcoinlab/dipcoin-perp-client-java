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

package io.dipcoin.sui.perp.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/**
 * @author : Same
 * @datetime : 2025/10/30 10:48
 * @Description : decimal util
 */
public class DecimalUtil {

    // 18 power
    private static final int UINT = 18;
    // base unit 1e18
    private static final BigInteger BASE_UINT_INTEGER = BigInteger.TEN.pow(UINT);
    // base unit 1e18
    private static final BigDecimal BASE_UINT_DECIMAL = BigDecimal.TEN.pow(UINT);
    // half of base unit 0.5e18
    private static final BigInteger HALF_BASE_UINT = BASE_UINT_INTEGER.divide(BigInteger.valueOf(2));

    /**
     * retrieve base unit constants
     */
    public static BigInteger getBaseUintInteger() {
        return BASE_UINT_INTEGER;
    }

    /**
     * retrieve base unit constants
     */
    public static BigDecimal getBaseUintDecimal() {
        return BASE_UINT_DECIMAL;
    }

    /**
     * retrieve constants for half of the base unit
     */
    public static BigInteger getHalfBaseUint() {
        return HALF_BASE_UINT;
    }

    /**
     * multiply the value by the base value and round down
     * @param value value to be calculated
     * @param baseValue base value
     * @return calculation result
     */
    public static BigInteger baseMul(BigInteger value, BigInteger baseValue) {
        return value.multiply(baseValue).divide(BASE_UINT_INTEGER);
    }

    /**
     * divide the value by the base value and round down
     * @param value value to be calculated
     * @param baseValue base value
     * @return calculation result
     */
    public static BigInteger baseDiv(BigInteger value, BigInteger baseValue) {
        return value.multiply(BASE_UINT_INTEGER).divide(baseValue);
    }

    /**
     * return the result rounded up. ceil(a/m)*m
     * @param a dividend
     * @param m divisor
     * @return result after rounding up
     */
    public static BigInteger ceil(BigInteger a, BigInteger m) {
        return a.add(m).subtract(BigInteger.ONE).divide(m).multiply(m);
    }

    /**
     * return the result rounded down. floor(a/m)*m
     * @param a dividend
     * @param m divisor
     * @return result after rounding down
     */
    public static BigInteger floor(BigInteger a, BigInteger m) {
        return a.divide(m).multiply(m);
    }

    /**
     * return the smaller of the two numbers
     */
    public static BigInteger min(BigInteger a, BigInteger b) {
        return a.compareTo(b) < 0 ? a : b;
    }

    /**
     * if a > b, return a - b
     */
    public static BigInteger sub(BigInteger a, BigInteger b) {
        return a.subtract(b);
    }

    /**
     * convert a String-type ordinary numerical value (likely a decimal) to a BigInteger with 18-digit precision
     * @param value value to be converted
     * @return BigInteger with 18-digit precision
     */
    public static BigInteger toBaseUnit(String value) {
        return toBaseUnit(new BigDecimal(value));
    }

    /**
     * convert a BigDecimal-type ordinary numerical value to a BigDecimal with 18-digit precision
     * @param value value to be converted
     * @return BigInteger with 18-digit precision
     */
    public static BigDecimal toBaseUnitDecimal(BigDecimal value) {
        return value.multiply(BASE_UINT_DECIMAL);
    }

    /**
     * convert a BigDecimal-type ordinary numerical value to a BigInteger with 18-digit precision
     * @param value value to be converted
     * @return BigInteger with 18-digit precision
     */
    public static BigInteger toBaseUnit(BigDecimal value) {
        return value.multiply(BASE_UINT_DECIMAL).toBigInteger();
    }

    /**
     * convert a BigInteger-type ordinary numerical value to a BigInteger with 18-digit precision
     * @param value value to be converted
     * @return BigInteger with 18-digit precision
     */
    public static BigInteger toBaseUnit(BigInteger value) {
        return value.multiply(BASE_UINT_INTEGER);
    }

    /**
     * convert an 18-digit precision BigInteger to an ordinary numerical value (default retains 18 decimal places)
     * @param value BigInteger with 18-digit precision
     * @return ordinary numerical value
     */
    public static BigDecimal fromBaseUnit(BigInteger value) {
        return new BigDecimal(value).divide(BASE_UINT_DECIMAL, UINT, RoundingMode.DOWN);
    }

}

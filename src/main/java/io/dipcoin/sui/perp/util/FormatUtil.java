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
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author : Same
 * @datetime : 2025/11/25 18:10
 * @Description : format number
 */
public class FormatUtil {

    // 9 power
    private static final int UINT = 18;
    // base unit 1e18
    private static final Map<Integer, BigDecimal> BASE_UINT_MAP = new ConcurrentHashMap<>();
    // base unit 1e18
    private static final BigDecimal BASE_UINT_DECIMAL = BigDecimal.TEN.pow(UINT);

    private static final DecimalFormat DF_ONE;

    private static final DecimalFormat DF_TEN;

    private static final String ZERO = "0";

    private static final String DOT = ".";

    private static final String NEGATIVE = "-";

    // remove trailing zeros
    private static final String ZERO_REGEX = "0+$";

    /**
     * Convert BigInteger to a formatted string (divided by 10^n)
     * @param value
     * @return
     */
    public static String format(BigInteger value, int decimal) {
        if (value == null || value.equals(BigInteger.ZERO)) {
            return ZERO;
        }

        // Convert to BigDecimal and divide by 10^decimal
        BigDecimal decimalValue = new BigDecimal(value)
                .divide(getBaseUint(decimal), decimal, RoundingMode.DOWN);

        return formatDecimal(decimalValue);
    }

    /**
     * Convert BigDecimal to a formatted string (divided by 10^n)
     * @param value
     * @return
     */
    public static String format(BigDecimal value, int decimal) {
        if (value == null || value.equals(BigDecimal.ZERO)) {
            return ZERO;
        }

        // Convert to BigDecimal and divide by 10^decimal
        BigDecimal decimalValue = value
                .divide(getBaseUint(decimal), decimal, RoundingMode.DOWN);

        return formatDecimal(decimalValue);
    }

    /**
     * Convert BigInteger to a formatted string (divided by 10^18)
     * @param value
     * @return
     */
    public static String format(BigInteger value) {
        if (value == null || value.equals(BigInteger.ZERO)) {
            return ZERO;
        }

        // Convert to BigDecimal and divide by 10^18
        BigDecimal decimalValue = new BigDecimal(value)
                .divide(BASE_UINT_DECIMAL, UINT, RoundingMode.DOWN);

        return formatDecimal(decimalValue);
    }

    /**
     * Convert BigDecimal to a formatted string (divided by 10^18)
     * @param value
     * @return
     */
    public static String format(BigDecimal value) {
        if (value == null || value.equals(BigDecimal.ZERO)) {
            return ZERO;
        }

        // Divide by 10^18
        BigDecimal decimalValue = value
                .divide(BASE_UINT_DECIMAL, UINT, RoundingMode.DOWN);

        return formatDecimal(decimalValue);
    }

    /**
     * Format the BigDecimal value after precision processing
     * @param value
     * @return
     */
    private static String formatDecimal(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) == 0) {
            return ZERO;
        }

        // Get the absolute value
        BigDecimal absValue = value.abs();

        if (absValue.compareTo(BigDecimal.TEN) >= 0) {
            // Handle cases greater than or equal to 10
            return formatGreaterThanOrEqualTen(value);
        } else if (absValue.compareTo(BigDecimal.ONE) >= 0) {
            // Handle cases greater than or equal to 1
            return formatGreaterThanOrEqualOne(value);
        } else {
            // Handle cases less than 1
            return formatLessThanOne(value);
        }
    }

    /**
     * Format numbers greater than or equal to 10
     */
    private static String formatGreaterThanOrEqualTen(BigDecimal value) {
        String result = DF_TEN.format(value);

        // Remove trailing zeros and the decimal point (if any)
        return removeTrailingZeros(result);
    }

    /**
     * Format numbers greater than or equal to 1
     */
    private static String formatGreaterThanOrEqualOne(BigDecimal value) {
        String result = DF_ONE.format(value);

        // Remove trailing zeros and the decimal point (if any)
        return removeTrailingZeros(result);
    }

    /**
     * Format numbers less than 1
     */
    private static String formatLessThanOne(BigDecimal value) {
        String stringValue = value.toPlainString();

        // Handle the negative sign
        boolean isNegative = value.compareTo(BigDecimal.ZERO) < 0;
        if (isNegative) {
            stringValue = stringValue.substring(1); // Remove the negative sign and add it back later
        }

        // Handle numbers represented in scientific notation
        if (stringValue.contains("E") || stringValue.contains("e")) {
            stringValue = value.toEngineeringString();
        }

        // Find the position of the decimal point
        int decimalPointIndex = stringValue.indexOf('.');
        if (decimalPointIndex == -1) {
            return isNegative ? NEGATIVE + stringValue : stringValue;
        }

        // Find the position of the first non-zero digit
        int firstNonZeroIndex = -1;
        for (int i = decimalPointIndex + 1; i < stringValue.length(); i++) {
            if (stringValue.charAt(i) != '0') {
                firstNonZeroIndex = i;
                break;
            }
        }

        // If there are no non-zero digits, return 0
        if (firstNonZeroIndex == -1) {
            return ZERO;
        }

        // Calculate the number of digits to retain
        int zerosAfterDecimal = firstNonZeroIndex - decimalPointIndex - 1;
        int digitsToKeep = zerosAfterDecimal + 4; // Leading zeros + 4 significant digits

        // Ensure it does not exceed the string length and maximum precision
        int endIndex = Math.min(stringValue.length(), decimalPointIndex + digitsToKeep + 1);
        String result = stringValue.substring(0, endIndex);

        // Remove trailing zeros
        result = removeTrailingZeros(result);

        // Restore the negative sign
        return isNegative ? NEGATIVE + result : result;
    }

    /**
     * Remove trailing zeros and the decimal point from the numeric string
     */
    private static String removeTrailingZeros(String number) {
        if (!number.contains(DOT)) {
            return number;
        }

        // Remove trailing zeros
        String result = number.replaceAll(ZERO_REGEX, "");

        // If there are no digits after the decimal point, remove the decimal point
        if (result.endsWith(DOT)) {
            result = result.substring(0, result.length() - 1);
        }

        return result;
    }

    /**
     * Cache precision
     * @param decimal
     * @return
     */
    private static BigDecimal getBaseUint(int decimal) {
        BigDecimal pow = BASE_UINT_MAP.get(decimal);
        if (pow != null) {
            return pow;
        }
        BigDecimal decimalPow = BigDecimal.TEN.pow(decimal);
        BASE_UINT_MAP.put(decimal, decimalPow);
        return decimalPow;
    }

    static {
        // For numbers greater than or equal to 10, limit to at most 2 decimal places and add thousand separators
        DF_TEN = new DecimalFormat("#,##0.##");
        DF_TEN.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.US));
        DF_TEN.setRoundingMode(RoundingMode.DOWN);
        // For numbers less than 10, limit to at most 3 decimal places and add thousand separators
        DF_ONE = new DecimalFormat("#,##0.###");
        DF_ONE.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.US));
        DF_ONE.setRoundingMode(RoundingMode.DOWN);
    }

}

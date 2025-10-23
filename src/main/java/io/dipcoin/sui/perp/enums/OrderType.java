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

package io.dipcoin.sui.perp.enums;

import lombok.Getter;

/**
 * @author : Same
 * @datetime : 2025/10/23 15:38
 * @Description : order type enum
 */
@Getter
public enum OrderType {

    LIMIT("LIMIT", 1, "Limit"),
    MARKET("MARKET", 2, "Market"),
    LIQ("Liquidation", 3, "Liquidation"),
    ADL("ADL", 4, "ADL");

    private final String code;
    private final Integer value;
    private final String desc;

    OrderType(String code, Integer value, String desc) {
        this.code = code;
        this.value = value;
        this.desc = desc;
    }

    public static OrderType fromValue(Integer value) {
        for (OrderType type : OrderType.values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid OrderType value: " + value);
    }

    public static OrderType fromCode(String code) {
        for (OrderType type : OrderType.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid OrderType code: " + code);
    }

}

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
 * @datetime : 2025/10/23 15:37
 * @Description : order side
 */
@Getter
public enum OrderSide {

    BUY("BUY", 1),
    SELL("SELL", 2);

    private final String code;
    private final Integer value;

    OrderSide(String code, Integer value) {
        this.code = code;
        this.value = value;
    }

    public static OrderSide fromValue(Integer value) {
        for (OrderSide side : OrderSide.values()) {
            if (side.getValue().equals(value)) {
                return side;
            }
        }
        throw new IllegalArgumentException("Invalid OrderSide value: " + value);
    }

    public static OrderSide fromCode(String code) {
        for (OrderSide side : OrderSide.values()) {
            if (side.getCode().equals(code)) {
                return side;
            }
        }
        throw new IllegalArgumentException("Invalid OrderSide code: " + code);
    }

}

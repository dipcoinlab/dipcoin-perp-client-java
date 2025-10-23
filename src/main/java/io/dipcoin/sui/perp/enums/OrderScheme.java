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
 * @datetime : 2025/10/23 14:56
 * @Description : order signature enum
 */
@Getter
public enum OrderScheme {

    KP_SECP256("0"),
    KP_ED25519("1"),
    UI_ED25519("2"),
    ;

    OrderScheme(String type) {
        this.type = type;
    }

    private final String type;

}

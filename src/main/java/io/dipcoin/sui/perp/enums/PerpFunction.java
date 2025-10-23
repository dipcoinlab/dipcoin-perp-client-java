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
 * @datetime : 2025/10/22 17:08
 * @Description :
 */
@Getter
public enum PerpFunction {

    SET_SUB_ACCOUNT("sub_accounts", "set_sub_account"),

    DEPOSIT("bank", "deposit"),

    WITHDRAW("bank", "withdraw"),

    ;

    PerpFunction(String module, String function) {
        this.module = module;
        this.function = function;
    }

    private final String module;
    private final String function;
}

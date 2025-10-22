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

package io.dipcoin.sui.perp.constant;

import io.dipcoin.sui.perp.config.PerpConfigs;
import io.dipcoin.sui.perp.model.PerpConfig;

/**
 * @author : Same
 * @datetime : 2025/10/21 10:53
 * @Description : perp network selection
 */
public enum PerpNetwork {

    MAINNET,
    TESTNET;

    public PerpConfig getConfig() {
        return switch (this) {
            case MAINNET -> PerpConfigs.MAINNET_CONFIG;
            case TESTNET -> PerpConfigs.TESTNET_CONFIG;
        };
    }

}

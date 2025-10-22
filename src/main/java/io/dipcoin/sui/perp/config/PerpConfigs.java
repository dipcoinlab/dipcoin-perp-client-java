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

package io.dipcoin.sui.perp.config;

import io.dipcoin.sui.perp.model.PerpConfig;

/**
 * @author : Same
 * @datetime : 2025/10/21 10:04
 * @Description : perp network configuration
 */
public class PerpConfigs {

    private PerpConfigs() {}

    public static final PerpConfig MAINNET_CONFIG = new PerpConfig(
            "https://fullnode.mainnet.sui.io:443",  // RPC node
            "https://gray-api.dipcoin.io/api",  // perp endpoint
            "0x978fed071cca22dd26bec3cf4a5d5a00ab10f39cb8c659bbfdfbec4397241001", // packageId
            "0xdeff2ed27dfe5402e38d60b090a7dcf9b4842c16ec63e472119272173603dfd8", // protocolConfig
            "0x3ad8c911dff3ee0aeeaf86f0c7e7a540a23743477e831d14f62b63e58fb8eb0d", // subAccounts
            "0x5dd7fa4c14b88167458df2ea281f4253213137ef4cd91d9b83fb56d0494f6741" // txIndexer
    );

    public static final PerpConfig TESTNET_CONFIG = new PerpConfig(
            "https://fullnode.testnet.sui.io:443",  // RPC node
            "https://demoapi.dipcoin.io/exchange/api",  // perp endpoint
            "0x0114b1d4656ac42a9523da1c7241f0291918f9517fd30f3e6e84b9fd5b3e3730", // packageId
            "0x05a630c36e8a6cb9ff99e2d2595e55ec70d002a8069a90c2d1bac0bfa12271fa", // protocolConfig
            "0x62a28e07b1e3ddb2cb1108349761ec1cf096b0c3523863af3bfd4e36e14beb5b", // subAccounts
            "0xaed1352c3f6f2a44fd521350f53a98f675d4b07cc36916607eae24c2650a9cb9" // txIndexer
    );

}

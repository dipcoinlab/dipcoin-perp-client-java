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

package io.dipcoin.sui.perp.wallet;

import io.dipcoin.sui.crypto.Ed25519KeyPair;
import io.dipcoin.sui.crypto.SuiKeyPair;

/**
 * @author : Same
 * @datetime : 2025/10/23 18:24
 * @Description :
 */
public interface WalletKey {

    /**
     * test main wallet
     * 0x123
     */
    SuiKeyPair mainKeyPair = Ed25519KeyPair.decodeHex("123");

    /**
     * test sub wallet
     * 0x456
     */
    SuiKeyPair subKeyPair = Ed25519KeyPair.decodeHex("456");

}

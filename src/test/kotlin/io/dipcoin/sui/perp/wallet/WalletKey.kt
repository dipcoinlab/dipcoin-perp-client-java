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

package io.dipcoin.sui.perp.wallet

import io.dipcoin.sui.crypto.Ed25519KeyPair
import io.dipcoin.sui.crypto.SuiKeyPair

object WalletKey {
    // Ed25519 私钥为 32 字节 = 64 个十六进制字符，此处为仅用于测试的占位密钥
    private const val TEST_MAIN_HEX = "0000000000000000000000000000000000000000000000000000000000000001"
    private const val TEST_SUB_HEX = "0000000000000000000000000000000000000000000000000000000000000002"
    /** test main wallet */
    val mainKeyPair: SuiKeyPair<*> = Ed25519KeyPair.decodeHex(TEST_MAIN_HEX)
    /** test sub wallet */
    val subKeyPair: SuiKeyPair<*> = Ed25519KeyPair.decodeHex(TEST_SUB_HEX)
}

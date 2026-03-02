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

package io.dipcoin.sui.perp.constant

import java.util.concurrent.ConcurrentHashMap

/**
 * perp pyth testnet
 */
object PerpPythTestnet {
    val FEED_OBJECTS: MutableMap<String, String> = ConcurrentHashMap<String, String>().apply {
        put("ETH-PERP", "0x362f009be96a1d74ff76156cec96876b89aa09529c1261d491751903ee798e4d")
        put("BTC-PERP", "0x8c65003d5d1a529adc4be78cfceb3855ef529d9807fcd58b06caab0a96caa806")
        put("SUI-PERP", "0x1e9be81a16c22896f2b4852e8b5c5e59d247c5566dee7b390477f4b7f70914df")
    }
}

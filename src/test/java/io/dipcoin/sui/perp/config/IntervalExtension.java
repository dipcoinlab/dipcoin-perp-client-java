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

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * @author : Same
 * @datetime : 2025/10/23 16:12
 * @Description :
 */
public class IntervalExtension implements AfterEachCallback {

    @Override
    public void afterEach(ExtensionContext context) throws InterruptedException {
        if (isSuiteMode(context)) {
            Thread.sleep(2500L);
        }
    }

    private boolean isSuiteMode(ExtensionContext context) {
        return context.getTags().contains("suite");
    }

}

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

package io.dipcoin.sui.perp.exception;

import lombok.Getter;

/**
 * @author : Same
 * @datetime : 2025/10/21 10:59
 * @Description :
 */
@Getter
public enum ErrorCode {

    // ---------------- system ----------------
    SUCCESS(200, "success"),
    SYSTEM_ERROR(500, "system error"),
    CLIENT_ERROR(400,  "client error"),
    PARAM_ILLEGAL(401, "illegal args of request parameters"),
    EXTERNAL_SERVICE_ERROR(503, "external service error"),
    ;

    ErrorCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private final int code;

    private final String desc;

}


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

package io.dipcoin.sui.perp.model

import io.dipcoin.sui.perp.exception.ErrorCode

data class ApiResponse<T>(
    var code: Int = 0,
    var message: String? = null,
    var timezone: Long? = null,
    var data: T? = null,
) {
    companion object {
        fun <T> success(data: T): ApiResponse<T> = ApiResponse(
            code = ErrorCode.SUCCESS.code,
            message = ErrorCode.SUCCESS.desc,
            data = data
        )

        fun <T> success(): ApiResponse<T> = ApiResponse(
            code = ErrorCode.SUCCESS.code,
            message = ErrorCode.SUCCESS.desc
        )

        fun <T> error(message: String): ApiResponse<T> = ApiResponse(
            code = ErrorCode.SYSTEM_ERROR.code,
            message = message
        )

        fun <T> error(code: Int, message: String): ApiResponse<T> = ApiResponse(
            code = code,
            message = message
        )

        fun <T> error(errorCode: ErrorCode): ApiResponse<T> = ApiResponse(
            code = errorCode.code,
            message = errorCode.desc
        )
    }
}

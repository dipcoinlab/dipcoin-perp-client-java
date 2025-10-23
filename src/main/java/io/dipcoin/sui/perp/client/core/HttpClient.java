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

package io.dipcoin.sui.perp.client.core;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Map;

/**
 * @author : Same
 * @datetime : 2025/10/22 13:48
 * @Description : 
 */
public interface HttpClient {

    <T> T post(Object request, String url, TypeReference<T> typeReference);

    <T> T get(String url, Map<String, String> queryParams, TypeReference<T> typeReference);

    <T> T postWithMainAuth(Object request, String url, TypeReference<T> typeReference);

    <T> T getWithMainAuth(String url, Map<String, String> queryParams, TypeReference<T> typeReference);

    <T> T postWithSubAuth(Object request, String url, TypeReference<T> typeReference);

    <T> T getWithSubAuth(String url, Map<String, String> queryParams, TypeReference<T> typeReference);
}

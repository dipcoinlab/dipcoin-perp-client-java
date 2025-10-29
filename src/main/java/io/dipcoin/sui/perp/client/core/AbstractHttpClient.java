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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dipcoin.sui.perp.client.auth.AuthSession;
import io.dipcoin.sui.perp.exception.PerpJsonParseException;
import io.dipcoin.sui.perp.exception.PerpRpcFailedException;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author : Same
 * @datetime : 2025/10/22 13:48
 * @Description : 
 */
public abstract class AbstractHttpClient implements HttpClient {

    private static final Logger log = LoggerFactory.getLogger(AbstractHttpClient.class);

    private final static MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final static String HEADER_AUTH = "Authorization";
    private final static String HEADER_ADDR = "X-Wallet-Address";
    private final static String HEADER_PREFIX = "Bearer ";

    private final ObjectMapper objectMapper;

    private OkHttpClient okHttpClient;

    public AbstractHttpClient() {
        this.objectMapper = new ObjectMapper();
        this.okHttpClient = createOkHttpClient();
    }

    public static OkHttpClient.Builder getOkHttpClientBuilder() {
        final OkHttpClient.Builder builder =
                new OkHttpClient.Builder()
                        .connectTimeout(Duration.ofSeconds(30))
                        .readTimeout(Duration.ofSeconds(30));
        configureLogging(builder);
        return builder;
    }

    private static OkHttpClient createOkHttpClient() {
        return getOkHttpClientBuilder().build();
    }

    private static void configureLogging(OkHttpClient.Builder builder) {
        if (log.isDebugEnabled()) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor(log::debug);
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(logging);
        }
    }

    @Override
    public <T> T post(Object request, String url, AuthSession auth, TypeReference<T> typeReference) {
        String requestBody = null;
        try {
            requestBody = objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new PerpJsonParseException("Unable to serialize request body", e);
        }
        Request.Builder builder = new Request.Builder()
                .url(url)
                .post(RequestBody.create(requestBody, JSON));
        this.buildUrlWithAuth(builder, auth);
        Request httpRequest = builder.build();

        try (Response response = okHttpClient.newCall(httpRequest).execute()) {
            if (response.isSuccessful()) {
                ResponseBody body = response.body();
                if (body != null) {
                    String responseBody = body.string();
                    return objectMapper.readValue(responseBody, typeReference);
                }
            }
            return null;
        } catch (IOException e) {
            throw new PerpRpcFailedException("Unable to send POST request", e);
        }
    }

    @Override
    public <T> T get(String url, Map<String, String> queryParams, AuthSession auth, TypeReference<T> typeReference) {
        String finalUrl = this.buildUrlWithParams(url, queryParams);
        Request.Builder builder = new Request.Builder()
                .url(finalUrl)
                .get();
        this.buildUrlWithAuth(builder, auth);
        Request httpRequest = builder.build();

        try (Response response = okHttpClient.newCall(httpRequest).execute()) {
            if (response.isSuccessful()) {
                ResponseBody body = response.body();
                if (body != null) {
                    String responseBody = body.string();
                    return objectMapper.readValue(responseBody, typeReference);
                }
            }
            return null;
        } catch (IOException e) {
            throw new PerpRpcFailedException("Unable to send GET request", e);
        }
    }

    /**
     * convert to queryParams
     * @param o
     * @return
     */
    protected Map<String, String> toQueryParams(Object o) {
        Map<String, String> queryParams = new HashMap<>();
        if (o == null) {
            return queryParams;
        }

        Map<String, Object> map = objectMapper.convertValue(o, Map.class);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() != null) {
                queryParams.put(entry.getKey(), entry.getValue().toString());
            }
        }
        return queryParams;
    }

    /**
     * build url with params
     * @param url
     * @param queryParams
     * @return
     */
    private String buildUrlWithParams(String url, Map<String, String> queryParams) {
        if (queryParams != null) {
            HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                if (entry.getValue() != null) {
                    urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
                }
            }
            return urlBuilder.build().toString();
        }
        return url;
    }

    /**
     * build url with auth
     * @param builder
     * @param auth
     * @return
     */
    private void buildUrlWithAuth(Request.Builder builder, AuthSession auth) {
        if (auth != null && auth.isValid()) {
            builder.header(HEADER_AUTH, HEADER_PREFIX + auth.token());
            builder.header(HEADER_ADDR, auth.address());
        }
    }

}

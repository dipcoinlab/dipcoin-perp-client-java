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

package io.dipcoin.sui.perp;

import io.dipcoin.sui.perp.client.PerpUserClient;
import io.dipcoin.sui.perp.client.auth.AuthSession;
import io.dipcoin.sui.perp.client.core.PerpAuthorization;
import io.dipcoin.sui.perp.enums.PerpNetwork;
import io.dipcoin.sui.perp.model.PageResponse;
import io.dipcoin.sui.perp.model.request.HistoryOrdersRequest;
import io.dipcoin.sui.perp.model.request.OrdersRequest;
import io.dipcoin.sui.perp.model.request.PageRequest;
import io.dipcoin.sui.perp.model.response.*;
import io.dipcoin.sui.perp.wallet.WalletKey;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author : Same
 * @datetime : 2025/10/29 17:02
 * @Description :
 */
@Slf4j
public class PerpUserClientTest {

    private PerpUserClient perpUserClient;

    private PerpAuthorization perpAuthorization;

    @BeforeEach
    protected void setUp() {
        PerpNetwork perpNetwork = PerpNetwork.TESTNET;
        this.perpAuthorization = new PerpAuthorization(perpNetwork);
        // Must use the main account
        AuthSession authSession = perpAuthorization.authorize(WalletKey.mainKeyPair);
        this.perpUserClient = new PerpUserClient(perpNetwork, authSession);
    }


    @Test
    void testPositions() {
        List<PositionResponse> response = perpUserClient.positions();
        log.info("Response: {}", response);
        assertThat(response)
                .isInstanceOf(List.class);
    }

    @Test
    void testOrders() {
        OrdersRequest request = new OrdersRequest();
        request.setSymbol("ETH-PERP")
                .setPageNum(1)
                .setPageSize(20);
        PageResponse<OrdersResponse> response = perpUserClient.orders(request);
        log.info("Response: {}", response);
        assertThat(response)
                .isInstanceOf(PageResponse.class);
    }

    @Test
    void testAccount() {
        AccountResponse response = perpUserClient.account();
        log.info("Response: {}", response);
        assertThat(response)
                .isInstanceOf(AccountResponse.class);
    }

    @Test
    void testHistoryOrders() {
        HistoryOrdersRequest request = new HistoryOrdersRequest();
        long now = System.currentTimeMillis();
        request.setSymbol("ETH-PERP")
                .setPageNum(1)
                .setPageSize(20)
                .setBeginTime(now - 60 * 24 * 60 * 1000L)
                .setEndTime(now);
        PageResponse<HistoryOrdersResponse> response = perpUserClient.historyOrders(request);
        log.info("Response: {}", response);
        assertThat(response)
                .isInstanceOf(AccountResponse.class);
    }

    @Test
    void testFundingSettlements() {
        PageRequest request = new PageRequest();
        request.setPageNum(1)
                .setPageSize(20)
                .setBeginTime(System.currentTimeMillis() - 60 * 24 * 60 * 1000L);
        PageResponse<FundingSettlementsResponse> response = perpUserClient.fundingSettlements(request);
        log.info("Response: {}", response);
        assertThat(response)
                .isInstanceOf(AccountResponse.class);
    }

    @Test
    void testBalanceChanges() {
        PageRequest request = new PageRequest();
        request.setPageNum(1)
                .setPageSize(20)
                .setBeginTime(System.currentTimeMillis() - 60 * 24 * 60 * 1000L);
        PageResponse<BalanceChangesResponse> response = perpUserClient.balanceChanges(request);
        log.info("Response: {}", response);
        assertThat(response)
                .isInstanceOf(AccountResponse.class);
    }

}

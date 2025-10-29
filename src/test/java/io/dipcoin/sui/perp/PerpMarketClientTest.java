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

import io.dipcoin.sui.perp.client.PerpMarketClient;
import io.dipcoin.sui.perp.enums.PerpNetwork;
import io.dipcoin.sui.perp.model.request.SymbolRequest;
import io.dipcoin.sui.perp.model.response.OrderBookResponse;
import io.dipcoin.sui.perp.model.response.TickerResponse;
import io.dipcoin.sui.perp.model.response.TradingPairResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author : Same
 * @datetime : 2025/10/29 15:42
 * @Description :
 */
@Slf4j
public class PerpMarketClientTest {

    private PerpMarketClient perpMarketClient;

    @BeforeEach
    protected void setUp() {
        PerpNetwork perpNetwork = PerpNetwork.TESTNET;
        this.perpMarketClient = new PerpMarketClient(perpNetwork);
    }

    @Test
    void testTicker() {
        SymbolRequest request = new SymbolRequest();
        request.setSymbol("ETH-PERP");
        TickerResponse response = perpMarketClient.ticker(request);
        log.info("Response: {}", response);
        assertThat(response)
                .isInstanceOf(TickerResponse.class);
    }

    @Test
    void testOrderBook() {
        SymbolRequest request = new SymbolRequest();
        request.setSymbol("ETH-PERP");
        OrderBookResponse response = perpMarketClient.orderBook(request);
        log.info("Response: {}", response);
        assertThat(response)
                .isInstanceOf(OrderBookResponse.class);
    }

    @Test
    void testOracle() {
        SymbolRequest request = new SymbolRequest();
        request.setSymbol("ETH-PERP");
        BigInteger response = perpMarketClient.oracle(request);
        log.info("Response: {}", response);
        assertThat(response)
                .isInstanceOf(BigInteger.class);
    }

    @Test
    void testTradingPair() {
        List<TradingPairResponse> response = perpMarketClient.tradingPair();
        log.info("Response: {}", response);
        assertThat(response)
                .isInstanceOf(List.class);
    }

    @Test
    void testGetMarketPerpId() {
        String response = perpMarketClient.getMarketPerpId("ETH-PERP");
        log.info("Response: {}", response);
        assertThat(response)
                .isInstanceOf(String.class);
    }

    @Test
    void testGetPythFeedId() {
        String response = perpMarketClient.getPythFeedId("ETH-PERP");
        log.info("Response: {}", response);
        assertThat(response)
                .isInstanceOf(String.class);
    }

}

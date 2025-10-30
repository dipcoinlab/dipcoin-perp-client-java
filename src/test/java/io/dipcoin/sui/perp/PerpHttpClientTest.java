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

import io.dipcoin.sui.perp.client.PerpHttpClient;
import io.dipcoin.sui.perp.client.core.PerpClient;
import io.dipcoin.sui.perp.config.IntervalExtension;
import io.dipcoin.sui.perp.enums.OrderSide;
import io.dipcoin.sui.perp.enums.OrderType;
import io.dipcoin.sui.perp.enums.PerpNetwork;
import io.dipcoin.sui.perp.model.PageResponse;
import io.dipcoin.sui.perp.model.request.*;
import io.dipcoin.sui.perp.model.response.*;
import io.dipcoin.sui.perp.util.DecimalUtil;
import io.dipcoin.sui.perp.util.OrderUtil;
import io.dipcoin.sui.perp.wallet.WalletKeyReal;
import io.dipcoin.sui.protocol.SuiClient;
import io.dipcoin.sui.protocol.http.HttpService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigInteger;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author : Same
 * @datetime : 2025/10/23 13:59
 * @Description :
 */
@Slf4j
@ExtendWith(IntervalExtension.class)
public class PerpHttpClientTest {

    protected SuiClient suiClient;

    protected PerpClient perpClient;

    @BeforeEach
    protected void setUp() {
        PerpNetwork perpNetwork = PerpNetwork.TESTNET;
        HttpService suiService = new HttpService(perpNetwork.getConfig().suiRpc());
        this.suiClient = SuiClient.build(suiService);
        this.perpClient = new PerpHttpClient(perpNetwork, WalletKeyReal.mainKeyPair, WalletKeyReal.subKeyPair);
    }

    // ------------------------- trade API -------------------------

    @Test
    void testPlaceOrder() {
        String symbol = "ETH-PERP";
        // get market perp id by symbol
        String perpId = perpClient.getMarketPerpId(symbol);

        // place order
        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setSymbol(symbol)
                .setMarket(perpId)
                // price $3940 (18 decimals)
                .setPrice(DecimalUtil.toBaseUnit(new BigInteger("3940")))
                // quantity 1 ETH (18 decimals)
                .setQuantity(DecimalUtil.toBaseUnit(new BigInteger("1")))
                .setSide(OrderSide.SELL.getCode())
                .setOrderType(OrderType.LIMIT.getCode())
                // leverage 1x (18 decimals)
                .setLeverage(DecimalUtil.toBaseUnit(BigInteger.ONE))
                .setSalt(String.valueOf(System.currentTimeMillis()))
                .setCreator(perpClient.getMainAddress())
                .setOrderSignature(OrderUtil.getSignature(OrderUtil.getSerializedOrder(request), perpClient.getSubAccount()));

        String orderHash = perpClient.placeOrder(request);
        log.info("Response orderHash: {}", orderHash);
        assertThat(orderHash)
                .isInstanceOf(String.class);
    }

    @Test
    @Tag("suite")
    void testCancelOrder() {
        // cancel order
        // be105d39ac54cda71b4e0ea12e7c7c07abef626e8acca318247f8588537d41d5
        List<String> orders = List.of("be105d39ac54cda71b4e0ea12e7c7c07abef626e8acca318247f8588537d41d5");
        CancelOrderRequest request = new CancelOrderRequest();
        request.setSymbol("ETH-PERP")
                .setOrderHashes(orders)
                .setParentAddress(perpClient.getMainAddress())
                .setSignature(OrderUtil.getSignature(OrderUtil.getSerializedCancelOrder(orders), perpClient.getSubAccount()));

        CancelOrderResponse response = perpClient.cancelOrder(request);
        log.info("Response: {}", response);
        assertThat(response)
                .isInstanceOf(CancelOrderResponse.class);
    }

    // ------------------------- user API -------------------------

    @Test
    void testPositions() {
        List<PositionResponse> response = perpClient.positions();
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
        PageResponse<OrdersResponse> response = perpClient.orders(request);
        log.info("Response: {}", response);
        assertThat(response)
                .isInstanceOf(PageResponse.class);
    }

    @Test
    void testAccount() {
        AccountResponse response = perpClient.account();
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
        PageResponse<HistoryOrdersResponse> response = perpClient.historyOrders(request);
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
        PageResponse<FundingSettlementsResponse> response = perpClient.fundingSettlements(request);
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
        PageResponse<BalanceChangesResponse> response = perpClient.balanceChanges(request);
        log.info("Response: {}", response);
        assertThat(response)
                .isInstanceOf(AccountResponse.class);
    }

    // ------------------------- market API -------------------------

    @Test
    void testTicker() {
        SymbolRequest request = new SymbolRequest();
        request.setSymbol("ETH-PERP");
        TickerResponse response = perpClient.ticker(request);
        log.info("Response: {}", response);
        assertThat(response)
                .isInstanceOf(TickerResponse.class);
    }

    @Test
    void testOrderBook() {
        SymbolRequest request = new SymbolRequest();
        request.setSymbol("ETH-PERP");
        OrderBookResponse response = perpClient.orderBook(request);
        log.info("Response: {}", response);
        assertThat(response)
                .isInstanceOf(OrderBookResponse.class);
    }

    @Test
    void testOracle() {
        SymbolRequest request = new SymbolRequest();
        request.setSymbol("ETH-PERP");
        BigInteger response = perpClient.oracle(request);
        log.info("Response: {}", response);
        assertThat(response)
                .isInstanceOf(BigInteger.class);
    }

    @Test
    void testTradingPair() {
        List<TradingPairResponse> response = perpClient.tradingPair();
        log.info("Response: {}", response);
        assertThat(response)
                .isInstanceOf(List.class);
    }

    @Test
    void testGetMarketPerpId() {
        String response = perpClient.getMarketPerpId("ETH-PERP");
        log.info("Response: {}", response);
        assertThat(response)
                .isInstanceOf(String.class);
    }

    @Test
    void testGetPythFeedId() {
        String response = perpClient.getPythFeedId("ETH-PERP");
        log.info("Response: {}", response);
        assertThat(response)
                .isInstanceOf(String.class);
    }

}

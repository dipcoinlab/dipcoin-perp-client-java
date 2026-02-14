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

import io.dipcoin.sui.crypto.SuiKeyPair;
import io.dipcoin.sui.perp.client.PerpHttpClient;
import io.dipcoin.sui.perp.client.core.PerpClient;
import io.dipcoin.sui.perp.config.IntervalExtension;
import io.dipcoin.sui.perp.enums.OrderSide;
import io.dipcoin.sui.perp.enums.OrderType;
import io.dipcoin.sui.perp.enums.PerpNetwork;
import io.dipcoin.sui.perp.enums.TpslTypeEnum;
import io.dipcoin.sui.perp.model.PageResponse;
import io.dipcoin.sui.perp.model.request.*;
import io.dipcoin.sui.perp.model.response.*;
import io.dipcoin.sui.perp.util.DecimalUtil;
import io.dipcoin.sui.perp.util.OrderUtil;
import io.dipcoin.sui.perp.wallet.WalletKey;
import io.dipcoin.sui.protocol.SuiClient;
import io.dipcoin.sui.protocol.http.HttpService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
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
        this.perpClient = new PerpHttpClient(perpNetwork, WalletKey.mainKeyPair, WalletKey.subKeyPair);
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
        List<String> orders = List.of("7257d4dd9b31b5cde59f40e117e45899e93f966c5439ca8464c0e9c3310ece3d");
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

    @Test
    void testTpslPlaceOrder() {
        // place tpsl order
        SuiKeyPair subAccountKeyPair = perpClient.getSubAccount();
        String mainAddress = subAccountKeyPair.address();
        String symbol = "ETH-PERP";

        // get market perp id by symbol
        String perpId = perpClient.getMarketPerpId(symbol);
        BigInteger openOrderPrice = DecimalUtil.toBaseUnit(new BigInteger("2920"));
        long now = System.currentTimeMillis();
        // take profit 20%
        BigInteger tpPrice = openOrderPrice.multiply(new BigInteger("120")).divide(new BigInteger("100"));
        // stop loss -20%
        BigInteger slPrice = openOrderPrice.multiply(new BigInteger("80")).divide(new BigInteger("100"));

        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setSymbol(symbol)
                .setMarket(perpId)
                // price $2895 (18 decimals)
                .setPrice(openOrderPrice)
                // quantity 0.1 ETH (18 decimals)
                .setQuantity(DecimalUtil.toBaseUnit(new BigDecimal("0.1")))
                .setSide(OrderSide.BUY.getCode())
                .setOrderType(OrderType.LIMIT.getCode())
                // leverage 1x (18 decimals)
                .setLeverage(DecimalUtil.toBaseUnit(BigInteger.ONE))
                .setSalt(String.valueOf(now))
                .setCreator(mainAddress)
                .setOrderSignature(OrderUtil.getSignature(OrderUtil.getSerializedOrder(request), subAccountKeyPair))
                // -------------------- take profit --------------------
                .setTpOrderType(OrderType.LIMIT.getCode())
                .setTpTriggerPrice(tpPrice)
                .setTpOrderPrice(tpPrice)
                .setTpSalt(String.valueOf(now + 1L))
                .setTpOrderSignature(OrderUtil.getSignature(OrderUtil.getTpSerializedOrder(request), subAccountKeyPair))
                // -------------------- stop loss --------------------
                .setSlOrderType(OrderType.LIMIT.getCode())
                .setSlTriggerPrice(slPrice)
                .setSlOrderPrice(slPrice)
                .setSlSalt(String.valueOf(now + 2L))
                .setSlOrderSignature(OrderUtil.getSignature(OrderUtil.getSlSerializedOrder(request), subAccountKeyPair))
        ;

        String orderHash = perpClient.placeOrder(request);
        log.info("Response orderHash: {}", orderHash);
        assertThat(orderHash)
                .isInstanceOf(String.class);
    }

    @Test
    @Tag("suite")
    void testTpslCancelOrder() {
        SuiKeyPair subAccountKeyPair = perpClient.getSubAccount();
        String mainAddress = subAccountKeyPair.address();
        // cancel tpsl order
        // a11249047813b79a47ef36689e2dcea6e075308f8fcb2af0c628f6ee4a2d7898
        List<String> orders = List.of("a11249047813b79a47ef36689e2dcea6e075308f8fcb2af0c628f6ee4a2d7898");
        CancelOrderRequest request = new CancelOrderRequest();
        request.setSymbol("ETH-PERP")
                .setOrderHashes(orders)
                .setParentAddress(mainAddress)
                .setSignature(OrderUtil.getSignature(OrderUtil.getSerializedCancelOrder(orders), subAccountKeyPair));

        CancelOrderResponse response = perpClient.cancelOrder(request);
        log.info("Response: {}", response);
        assertThat(response)
                .isInstanceOf(CancelOrderResponse.class);
    }

    @Test
    @Tag("suite")
    void testQueryTpslPlan() {
        // query tpsl plan
        QueryTpslPlanRequest request = new QueryTpslPlanRequest();
        request.setPositionId(0L)
                .setTpslType(TpslTypeEnum.NORMAL.getTpslType());

        OrdersResponse response = perpClient.queryTpslPlan(request);
        log.info("Response: {}", response);
        assertThat(response)
                .isInstanceOf(OrdersResponse.class);
    }

    @Test
    @Tag("suite")
    void testPlanCloseOrder() {
        // TODO PlanCloseOrder
        SuiKeyPair subAccountKeyPair = perpClient.getSubAccount();
        String mainAddress = subAccountKeyPair.address();
        // cancel tpsl order
        List<String> orders = List.of("3d40af25599bb4069da03a249ca968942b1e09a8499b7564601d170ee0562f0a");
        TpslPlanOrderRequest request = new TpslPlanOrderRequest();
//        request.setSymbol("ETH-PERP")
//                .setOrderHashes(orders)
//                .setParentAddress(mainAddress)
//                .setSignature(OrderUtil.getSignature(OrderUtil.getSerializedCancelOrder(orders), subAccountKeyPair));

        String response = perpClient.planCloseOrder(request);
        log.info("Response: {}", response);
        assertThat(response)
                .isInstanceOf(String.class);
    }

    // ------------------------- user API -------------------------

    @Test
    void testPositions() {
        // Query current address
        List<PositionResponse> response = perpClient.positions(null);
        log.info("Response: {}", response);
        assertThat(response)
                .isInstanceOf(List.class);
    }

    @Test
    void testVaultPositions() {
        // Query vault address.
        PositionRequest request = new PositionRequest()
                .setSymbol("ETH-PERP")
                // The parent address is the vault address.
                .setParentAddress("0xvault_address");

        List<PositionResponse> response = perpClient.positions(request);
        log.info("Response: {}", response);
        assertThat(response)
                .isInstanceOf(List.class);
    }

    @Test
    void testOrders() {
        // Query current address
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
    void testVaultOrders() {
        // Query vault address.
        OrdersRequest request = new OrdersRequest();
        request.setSymbol("ETH-PERP")
                // The parent address is the vault address.
                .setParentAddress("0xvault_address")
                .setPageNum(1)
                .setPageSize(20);
        PageResponse<OrdersResponse> response = perpClient.orders(request);
        log.info("Response: {}", response);
        assertThat(response)
                .isInstanceOf(PageResponse.class);
    }

    @Test
    void testAccount() {
        // Query current address
        AccountResponse response = perpClient.account(null);
        log.info("Response: {}", response);
        assertThat(response)
                .isInstanceOf(AccountResponse.class);
    }

    @Test
    void testVaultAccount() {
        // Query vault address.
        AccountRequest request = new AccountRequest()
                // The parent address is the vault address.
                .setParentAddress("0xvault_address");
        AccountResponse response = perpClient.account(request);
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
                .isInstanceOf(PageResponse.class);
    }

    @Test
    void testVaultHistoryOrders() {
        // Query vault address.
        HistoryOrdersRequest request = new HistoryOrdersRequest();
        long now = System.currentTimeMillis();
        request.setSymbol("ETH-PERP")
                // The parent address is the vault address.
                .setParentAddress("0xvault_address")
                .setPageNum(1)
                .setPageSize(20)
                .setBeginTime(now - 60 * 24 * 60 * 1000L)
                .setEndTime(now);
        PageResponse<HistoryOrdersResponse> response = perpClient.historyOrders(request);
        log.info("Response: {}", response);
        assertThat(response)
                .isInstanceOf(PageResponse.class);
    }

    @Test
    void testFundingSettlements() {
        FundingPageRequest request = new FundingPageRequest();
        request.setPageNum(1)
                .setPageSize(20)
                .setBeginTime(System.currentTimeMillis() - 60 * 24 * 60 * 1000L);
        PageResponse<FundingSettlementsResponse> response = perpClient.fundingSettlements(request);
        log.info("Response: {}", response);
        assertThat(response)
                .isInstanceOf(PageResponse.class);
    }

    @Test
    void testVaultFundingSettlements() {
        // Query vault address.
        FundingPageRequest request = new FundingPageRequest();
        request.setPageNum(1)
                .setPageSize(20)
                // The parent address is the vault address.
                .setParentAddress("0xvault_address")
                .setBeginTime(System.currentTimeMillis() - 60 * 24 * 60 * 1000L);
        PageResponse<FundingSettlementsResponse> response = perpClient.fundingSettlements(request);
        log.info("Response: {}", response);
        assertThat(response)
                .isInstanceOf(PageResponse.class);
    }

    @Test
    void testBalanceChanges() {
        BalancePageRequest request = new BalancePageRequest();
        request.setPageNum(1)
                .setPageSize(20)
                .setBeginTime(System.currentTimeMillis() - 60 * 24 * 60 * 1000L);
        PageResponse<BalanceChangesResponse> response = perpClient.balanceChanges(request);
        log.info("Response: {}", response);
        assertThat(response)
                .isInstanceOf(PageResponse.class);
    }

    @Test
    void testVaultBalanceChanges() {
        // Query vault address.
        BalancePageRequest request = new BalancePageRequest();
        request.setPageNum(1)
                .setPageSize(20)
                // The parent address is the vault address.
                .setParentAddress("0xvault_address")
                .setBeginTime(System.currentTimeMillis() - 60 * 24 * 60 * 1000L);
        PageResponse<BalanceChangesResponse> response = perpClient.balanceChanges(request);
        log.info("Response: {}", response);
        assertThat(response)
                .isInstanceOf(PageResponse.class);
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

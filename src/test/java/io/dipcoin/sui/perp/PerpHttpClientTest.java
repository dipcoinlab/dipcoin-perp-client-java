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

import io.dipcoin.sui.model.transaction.SuiTransactionBlockResponse;
import io.dipcoin.sui.perp.client.PerpHttpClient;
import io.dipcoin.sui.perp.config.IntervalExtension;
import io.dipcoin.sui.perp.enums.OrderSide;
import io.dipcoin.sui.perp.enums.OrderType;
import io.dipcoin.sui.perp.enums.PerpNetwork;
import io.dipcoin.sui.perp.model.PageResponse;
import io.dipcoin.sui.perp.model.request.*;
import io.dipcoin.sui.perp.model.response.*;
import io.dipcoin.sui.perp.util.OrderUtil;
import io.dipcoin.sui.perp.wallet.WalletKey;
import io.dipcoin.sui.protocol.SuiClient;
import io.dipcoin.sui.protocol.http.HttpService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

/**
 * @author : Same
 * @datetime : 2025/10/23 13:59
 * @Description :
 */
@Slf4j
@ExtendWith(IntervalExtension.class)
public class PerpHttpClientTest {

    protected SuiClient suiClient;

    protected PerpHttpClient perpHttpClient;

    @BeforeEach
    protected void setUp() {
        PerpNetwork perpNetwork = PerpNetwork.TESTNET;
        HttpService suiService = new HttpService(perpNetwork.getConfig().suiRpc());
        this.suiClient = SuiClient.build(suiService);
        this.perpHttpClient = new PerpHttpClient(perpNetwork, WalletKey.mainKeyPair, WalletKey.subKeyPair);
    }

    @Test
    @Tag("suite")
    void testSetSubAccount() {
        // set subAccount if u need
        String subAccount = perpHttpClient.getSubAddress();
        // gas price 1000 (For dynamic queries, please refer to the `getReferenceGasPrice()` method in `SuiClient`)
        // gas limit 0.1 SUI (BigInteger.TEN.pow(8))1000
        SuiTransactionBlockResponse response = perpHttpClient.setSubAccount(subAccount, 1000L, BigInteger.TEN.pow(8));
        // https://testnet.suivision.xyz/txblock/3TMtqn7QPRmdtUEhchX48ZVf2HpjNEH229D8qzytJsX5
        log.info("Response: {}", response);
    }

    @Test
    @Tag("suite")
    void testDeposit() {
        // deposit 10, 000 testUSDC
        BigInteger amount = new BigInteger("10000").multiply(BigInteger.TEN.pow(6));
        // gas price 1000 (For dynamic queries, please refer to the `getReferenceGasPrice()` method in `SuiClient`)
        // gas limit 0.1 SUI (BigInteger.TEN.pow(8))1000
        SuiTransactionBlockResponse response = perpHttpClient.deposit(amount, 1000L, BigInteger.TEN.pow(8));
        // https://testnet.suivision.xyz/txblock/CvS8oKVvHkjQHMtnatEgNQ1AjKd2upLAs429BimTgX6c
        log.info("Response: {}", response);
    }

    @Test
    void testPlaceOrder() {
        // place order
        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setSymbol("ETH-PERP")
                .setMarket("0x44e07a44992498d610f455310b839d9f29aca7657bce65aa8d614b240900a5c7")
                .setPrice(new BigInteger("3940").multiply(BigInteger.TEN.pow(18)))
                .setQuantity(new BigInteger("1").multiply(BigInteger.TEN.pow(17)))
                .setSide(OrderSide.SELL.getCode())
                .setOrderType(OrderType.LIMIT.getCode())
                .setLeverage(BigInteger.ONE.multiply(BigInteger.TEN.pow(18)))
                .setSalt(String.valueOf(System.currentTimeMillis()))
                .setCreator(perpHttpClient.getMainAddress())
                .setOrderSignature(OrderUtil.getSignature(OrderUtil.getSerializedOrder(request), perpHttpClient.getSubAccount()));

        String orderHash = perpHttpClient.placeOrder(request);
        log.info("Response orderHash: {}", orderHash);
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
                .setParentAddress(perpHttpClient.getMainAddress())
                .setSignature(OrderUtil.getSignature(OrderUtil.getSerializedCancelOrder(orders), perpHttpClient.getSubAccount()));

        CancelOrderResponse response = perpHttpClient.cancelOrder(request);
        log.info("Response: {}", response);
    }

    @Test
    @Tag("suite")
    void testAddMargin() {
        // addmargin 200 testUSDC
        String symbol = "ETH-PERP";
        BigInteger amount = new BigInteger("200").multiply(BigInteger.TEN.pow(18));
        // gas price 1000 (For dynamic queries, please refer to the `getReferenceGasPrice()` method in `SuiClient`)
        // gas limit 0.1 SUI (BigInteger.TEN.pow(8))1000
        SuiTransactionBlockResponse response = perpHttpClient.addMargin(symbol, amount, 1000L, BigInteger.TEN.pow(8));
        // https://testnet.suivision.xyz/txblock/HfimayLEjWDQkntX1kUxMheiKNoDAG8bvBSBLJA8hhHk
        log.info("Response: {}", response);
    }

    @Test
    @Tag("suite")
    void testWithdraw() {
        // withdraw 10, 000 testUSDC
        BigInteger amount = new BigInteger("10000").multiply(BigInteger.TEN.pow(6));
        // gas price 1000 (For dynamic queries, please refer to the `getReferenceGasPrice()` method in `SuiClient`)
        // gas limit 0.1 SUI (BigInteger.TEN.pow(8))1000
        SuiTransactionBlockResponse response = perpHttpClient.withdraw(amount, 1000L, BigInteger.TEN.pow(8));
        // https://testnet.suivision.xyz/txblock/FtyJ1nT4kwC8MDXwqrYVqEDePy2RquWhbf2xuGNJK7q9
        log.info("Response: {}", response);
    }

    @Test
    void testPositions() {
        List<PositionResponse> response = perpHttpClient.positions();
        log.info("Response: {}", response);
    }

    @Test
    void testOrders() {
        OrdersRequest request = new OrdersRequest();
        request.setSymbol("ETH-PERP")
                .setPageNum(1)
                .setPageSize(20);
        PageResponse<OrdersResponse> response = perpHttpClient.orders(request);
        log.info("Response: {}", response);
    }

    @Test
    void testAccount() {
        AccountResponse response = perpHttpClient.account();
        log.info("Response: {}", response);
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
        PageResponse<HistoryOrdersResponse> response = perpHttpClient.historyOrders(request);
        log.info("Response: {}", response);
    }

    @Test
    void testFundingSettlements() {
        PageRequest request = new PageRequest();
        request.setPageNum(1)
                .setPageSize(20)
                .setBeginTime(System.currentTimeMillis() - 60 * 24 * 60 * 1000L);
        PageResponse<FundingSettlementsResponse> response = perpHttpClient.fundingSettlements(request);
        log.info("Response: {}", response);
    }

    @Test
    void testBalanceChanges() {
        PageRequest request = new PageRequest();
        request.setPageNum(1)
                .setPageSize(20)
                .setBeginTime(System.currentTimeMillis() - 60 * 24 * 60 * 1000L);
        PageResponse<BalanceChangesResponse> response = perpHttpClient.balanceChanges(request);
        log.info("Response: {}", response);
    }

    @Test
    void testTicker() {
        SymbolRequest request = new SymbolRequest();
        request.setSymbol("ETH-PERP");
        TickerResponse response = perpHttpClient.ticker(request);
        log.info("Response: {}", response);
    }

    @Test
    void testOrderBook() {
        SymbolRequest request = new SymbolRequest();
        request.setSymbol("ETH-PERP");
        OrderBookResponse response = perpHttpClient.orderBook(request);
        log.info("Response: {}", response);
    }

    @Test
    void testOracle() {
        SymbolRequest request = new SymbolRequest();
        request.setSymbol("ETH-PERP");
        BigInteger response = perpHttpClient.oracle(request);
        log.info("Response: {}", response);
    }

    @Test
    void testTradingPair() {
        List<TradingPairResponse> response = perpHttpClient.tradingPair();
        log.info("Response: {}", response);
    }

}

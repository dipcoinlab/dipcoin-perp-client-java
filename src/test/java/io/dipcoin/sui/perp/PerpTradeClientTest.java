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
import io.dipcoin.sui.perp.client.PerpMarketClient;
import io.dipcoin.sui.perp.client.PerpTradeClient;
import io.dipcoin.sui.perp.client.auth.AuthSession;
import io.dipcoin.sui.perp.client.core.PerpAuthorization;
import io.dipcoin.sui.perp.enums.OrderSide;
import io.dipcoin.sui.perp.enums.OrderType;
import io.dipcoin.sui.perp.enums.PerpNetwork;
import io.dipcoin.sui.perp.enums.TpslTypeEnum;
import io.dipcoin.sui.perp.model.request.CancelOrderRequest;
import io.dipcoin.sui.perp.model.request.PlaceOrderRequest;
import io.dipcoin.sui.perp.model.request.QueryTpslPlanRequest;
import io.dipcoin.sui.perp.model.request.TpslPlanOrderRequest;
import io.dipcoin.sui.perp.model.response.CancelOrderResponse;
import io.dipcoin.sui.perp.model.response.OrdersResponse;
import io.dipcoin.sui.perp.util.DecimalUtil;
import io.dipcoin.sui.perp.util.OrderUtil;
import io.dipcoin.sui.perp.wallet.WalletKey;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author : Same
 * @datetime : 2025/10/29 16:09
 * @Description :
 */
@Slf4j
public class PerpTradeClientTest {

    private PerpTradeClient perpTradeClient;

    private PerpMarketClient perpMarketClient;

    private PerpAuthorization perpAuthorization;

    @BeforeEach
    protected void setUp() {
        PerpNetwork perpNetwork = PerpNetwork.TESTNET;
        this.perpMarketClient = new PerpMarketClient(perpNetwork);
        this.perpAuthorization = new PerpAuthorization(perpNetwork);
        // Sign using a subaccount
        AuthSession authSession = perpAuthorization.authorize(WalletKey.subKeyPair);
        this.perpTradeClient = new PerpTradeClient(perpNetwork, authSession);
    }

    @Test
    void testPlaceOrder() {
        // place order
        SuiKeyPair subAccountKeyPair = WalletKey.subKeyPair;
        String mainAddress = WalletKey.mainKeyPair.address();
        String symbol = "ETH-PERP";

        // get market perp id by symbol
        String perpId = perpMarketClient.getMarketPerpId(symbol);

        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setSymbol(symbol)
                .setMarket(perpId)
                // price $3940 (18 decimals)
                .setPrice(DecimalUtil.toBaseUnit(new BigInteger("3940")))
                // quantity 0.1 ETH (18 decimals)
                .setQuantity(DecimalUtil.toBaseUnit(new BigDecimal("0.1")))
                .setSide(OrderSide.SELL.getCode())
                .setOrderType(OrderType.LIMIT.getCode())
                // leverage 1x (18 decimals)
                .setLeverage(DecimalUtil.toBaseUnit(BigInteger.ONE))
                .setSalt(String.valueOf(System.currentTimeMillis()))
                .setCreator(mainAddress)
                .setOrderSignature(OrderUtil.getSignature(OrderUtil.getSerializedOrder(request), subAccountKeyPair));

        String orderHash = perpTradeClient.placeOrder(request);
        log.info("Response orderHash: {}", orderHash);
        assertThat(orderHash)
                .isInstanceOf(String.class);
    }

    @Test
    @Tag("suite")
    void testCancelOrder() {
        SuiKeyPair subAccountKeyPair = WalletKey.subKeyPair;
        String mainAddress = WalletKey.mainKeyPair.address();
        // cancel order
        // be105d39ac54cda71b4e0ea12e7c7c07abef626e8acca318247f8588537d41d5
        List<String> orders = List.of("be105d39ac54cda71b4e0ea12e7c7c07abef626e8acca318247f8588537d41d5");
        CancelOrderRequest request = new CancelOrderRequest();
        request.setSymbol("ETH-PERP")
                .setOrderHashes(orders)
                .setParentAddress(mainAddress)
                .setSignature(OrderUtil.getSignature(OrderUtil.getSerializedCancelOrder(orders), subAccountKeyPair));

        CancelOrderResponse response = perpTradeClient.cancelOrder(request);
        log.info("Response: {}", response);
        assertThat(response)
                .isInstanceOf(CancelOrderResponse.class);
    }

    @Test
    void testTpslPlaceOrder() {
        // place tpsl order
        SuiKeyPair subAccountKeyPair = WalletKey.subKeyPair;
        String mainAddress = WalletKey.mainKeyPair.address();
        String symbol = "ETH-PERP";

        // get market perp id by symbol
        String perpId = perpMarketClient.getMarketPerpId(symbol);
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

        String orderHash = perpTradeClient.placeOrder(request);
        log.info("Response orderHash: {}", orderHash);
        assertThat(orderHash)
                .isInstanceOf(String.class);
    }

    @Test
    @Tag("suite")
    void testTpslCancelOrder() {
        SuiKeyPair subAccountKeyPair = WalletKey.subKeyPair;
        String mainAddress = WalletKey.mainKeyPair.address();
        // cancel tpsl order
        // a11249047813b79a47ef36689e2dcea6e075308f8fcb2af0c628f6ee4a2d7898
        List<String> orders = List.of("a11249047813b79a47ef36689e2dcea6e075308f8fcb2af0c628f6ee4a2d7898");
        CancelOrderRequest request = new CancelOrderRequest();
        request.setSymbol("ETH-PERP")
                .setOrderHashes(orders)
                .setParentAddress(mainAddress)
                .setSignature(OrderUtil.getSignature(OrderUtil.getSerializedCancelOrder(orders), subAccountKeyPair));

        CancelOrderResponse response = perpTradeClient.cancelOrder(request);
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

        OrdersResponse response = perpTradeClient.queryTpslPlan(request);
        log.info("Response: {}", response);
        assertThat(response)
                .isInstanceOf(OrdersResponse.class);
    }

    @Test
    @Tag("suite")
    void testPlanCloseOrder() {
        // TODO PlanCloseOrder
        SuiKeyPair subAccountKeyPair = WalletKey.subKeyPair;
        String mainAddress = WalletKey.mainKeyPair.address();
        // cancel tpsl order
        List<String> orders = List.of("3d40af25599bb4069da03a249ca968942b1e09a8499b7564601d170ee0562f0a");
        TpslPlanOrderRequest request = new TpslPlanOrderRequest();
//        request.setSymbol("ETH-PERP")
//                .setOrderHashes(orders)
//                .setParentAddress(mainAddress)
//                .setSignature(OrderUtil.getSignature(OrderUtil.getSerializedCancelOrder(orders), subAccountKeyPair));

        String response = perpTradeClient.planCloseOrder(request);
        log.info("Response: {}", response);
        assertThat(response)
                .isInstanceOf(String.class);
    }

}

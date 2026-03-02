# dipcoin-perp-client-kotlin

Kotlin Implementation of the Dipcoin Perpetual Trading Client Library

## 📋 Table of Contents

- [Overview](#overview)
- [Installation](#installation)
- [Quick Start](#quick-start)
- [Off-Chain API Modules](#off-chain-api-modules)
  - [PerpTradeClient](#perptradeclient)
  - [PerpUserClient](#perpuserclient)
  - [PerpMarketClient](#perpmarketclient)
  - [PerpHttpClient](#perphttpclient)
- [On-Chain Operation Modules](#on-chain-operation-modules)
  - [PerpOnSignClient](#perponsignclient)
  - [PerpOffSignClient](#perpoffsignclient)
- [Data Models](#data-models)
- [Utilities](#utilities)
- [Examples](#examples)
- [Best Practices](#best-practices)
- [License](#license)

## Overview

The Dipcoin Perpetual Client Library provides a modular Kotlin SDK for interacting with the Dipcoin perpetual futures trading platform. The library is divided into specialized modules for different use cases, supporting both HTTP API operations and on-chain blockchain transactions.

### Key Features

- **Modular Design**: Separate clients for trading, user data, and market data
- **Sub Account Architecture**: Enhanced security by isolating trading operations from fund custody
- **Automatic Authorization**: Built-in authentication management
- **18 Decimal Precision**: All perp-related parameters (e.g., order sizes, quantity, prices, leverage) use 18 decimal places precision
- **6 Decimal Precision**: Deposit/withdrawal/Add margin operations for USDC are handled with 6 decimal places precision
- **Type-Safe API**: Strongly typed requests and responses
- **Flexible On-Chain Operations**: Support for both direct signing and wallet integration

## Installation

### Maven

```xml
<dependency>
    <groupId>io.dipcoin</groupId>
    <artifactId>dipcoin-perp-client-kotlin</artifactId>
    <version>1.0.4</version>
</dependency>
```

### Gradle

```gradle
implementation 'io.dipcoin:dipcoin-perp-client-kotlin:1.0.4'
```

## Quick Start

Complete example demonstrating the modular client initialization and basic operations:

```kotlin
import io.dipcoin.sui.crypto.Ed25519KeyPair;
import io.dipcoin.sui.crypto.SuiKeyPair;
import io.dipcoin.sui.perp.client.*;
import io.dipcoin.sui.perp.enums.OrderSide;
import io.dipcoin.sui.perp.enums.OrderType;
import io.dipcoin.sui.perp.enums.PerpNetwork;
import io.dipcoin.sui.perp.model.request.PlaceOrderRequest;
import io.dipcoin.sui.perp.util.DecimalUtil;
import io.dipcoin.sui.perp.util.OrderUtil;

import java.math.BigDecimal;
import java.math.BigInteger;

fun main() {
    // 1. Initialize keypairs
        // There are three methods to construct a private key pair: `suiPrivKey`, mnemonic phrase, and hexadecimal private key.
//        SuiKeyPair mainKeyPair = SuiKeyPair.decodeSuiPrivateKey("suiprivKeyxxxx");
//        SuiKeyPair mainKeyPair = Ed25519KeyPair.deriveKeypair("mnemonics", null);
        val mainKeyPair = Ed25519KeyPair.decodeHex("main_private_key_hex")
        val subKeyPair = Ed25519KeyPair.decodeHex("sub_private_key_hex")
        
        // 2. Create unified HTTP client
        val httpClient = PerpHttpClient(PerpNetwork.TESTNET, mainKeyPair, subKeyPair)
        
        // 3. Create on-chain client for blockchain operations
        val onChainClient = PerpOnSignClient(PerpNetwork.TESTNET)
        
        // 4. Set sub account (one-time setup)
        onChainClient.setSubAccount(
            mainKeyPair,
            subKeyPair.address(),
            1000L,
            DecimalUtil.toSui(BigDecimal("0.1"))
        );
        
        // 5. Deposit funds - USDC use 6 decimal precision
        val depositAmount = BigDecimal("1000").multiply(BigDecimal.TEN.pow(6)) // 1000 USDC
        onChainClient.deposit(mainKeyPair, depositAmount, 1000L, DecimalUtil.toSui(BigDecimal("0.1")))
        
        // 6. Get market info
        val perpId = httpClient.getMarketPerpId("BTC-PERP")
        
        // 7. Place order
        val orderRequest = PlaceOrderRequest(
            symbol = "BTC-PERP",
            market = perpId,
            price = DecimalUtil.toBaseUnit(BigDecimal("50000")),
            quantity = DecimalUtil.toBaseUnit(BigDecimal("1")),
            side = OrderSide.BUY.code,
            orderType = OrderType.LIMIT.code,
            leverage = DecimalUtil.toBaseUnit(BigInteger("10")),
            reduceOnly = false,
            creator = mainKeyPair.address(),
            clientId = "order_001"
        )
        
        // Sign with sub account
        val salt = String(OrderUtil.getSalt());
        orderRequest.salt = salt
        val signature = OrderUtil.getSignature(OrderUtil.getSerializedOrder(orderRequest), subKeyPair)
        orderRequest.orderSignature = signature
        
        val orderId = httpClient.placeOrder(orderRequest)
        println("Order placed: " + orderId)
}
```

Complete example demonstrating the modular client initialization and basic operations for the vault version:

> Replace the vault address `0xvault_address` below with the actual vault address that has been created and associated with your order address. The request field to be set is `creator`.

```kotlin
import io.dipcoin.sui.crypto.Ed25519KeyPair;
import io.dipcoin.sui.crypto.SuiKeyPair;
import io.dipcoin.sui.perp.client.*;
import io.dipcoin.sui.perp.enums.OrderSide;
import io.dipcoin.sui.perp.enums.OrderType;
import io.dipcoin.sui.perp.enums.PerpNetwork;
import io.dipcoin.sui.perp.model.request.PlaceOrderRequest;
import io.dipcoin.sui.perp.util.DecimalUtil;
import io.dipcoin.sui.perp.util.OrderUtil;

import java.math.BigDecimal;
import java.math.BigInteger;

fun main() {
    // 1. Initialize keypairs
        // There are three methods to construct a private key pair: `suiPrivKey`, mnemonic phrase, and hexadecimal private key.
//        SuiKeyPair mainKeyPair = SuiKeyPair.decodeSuiPrivateKey("suiprivKeyxxxx");
//        SuiKeyPair mainKeyPair = Ed25519KeyPair.deriveKeypair("mnemonics", null);
        val mainKeyPair = Ed25519KeyPair.decodeHex("main_private_key_hex")
        val subKeyPair = Ed25519KeyPair.decodeHex("sub_private_key_hex")
        
        // 2. Create unified HTTP client
        val httpClient = PerpHttpClient(PerpNetwork.TESTNET, mainKeyPair, subKeyPair)
        
        // 3. Create on-chain client for blockchain operations
        val onChainClient = PerpOnSignClient(PerpNetwork.TESTNET)
        
        // 4. Set sub account (one-time setup)
        onChainClient.setSubAccount(
            mainKeyPair,
            subKeyPair.address(),
            1000L,
            DecimalUtil.toSui(BigDecimal("0.1"))
        );
        
        // 5. Deposit funds - USDC use 6 decimal precision
        val depositAmount = BigDecimal("1000").multiply(BigDecimal.TEN.pow(6)) // 1000 USDC
        onChainClient.deposit(mainKeyPair, depositAmount, 1000L, DecimalUtil.toSui(BigDecimal("0.1")))
        
        // 6. Get market info
        val perpId = httpClient.getMarketPerpId("BTC-PERP")
        
        // 7. Place order
        val orderRequest = PlaceOrderRequest(
            symbol = "BTC-PERP",
            market = perpId,
            price = DecimalUtil.toBaseUnit(BigDecimal("50000")),
            quantity = DecimalUtil.toBaseUnit(BigDecimal("1")),
            side = OrderSide.BUY.code,
            orderType = OrderType.LIMIT.code,
            leverage = DecimalUtil.toBaseUnit(BigInteger("10")),
            reduceOnly = false,
            creator = "0xvault_address",
            clientId = "order_001"
        )
        
        // Sign with sub account
        val salt = String(OrderUtil.getSalt());
        orderRequest.salt = salt
        val signature = OrderUtil.getSignature(OrderUtil.getSerializedOrder(orderRequest), subKeyPair)
        orderRequest.orderSignature = signature
        
        val orderId = httpClient.placeOrder(orderRequest)
        println("Order placed: " + orderId)
    }
}
```

## Off-Chain API Modules

### PerpTradeClient

**Purpose**: Handles all trading operations including order placement and cancellation.

**Security Model**: Uses sub account authentication to protect main account funds. The sub account can only execute trading operations (place/cancel orders) and cannot access or withdraw funds. This separation ensures that even if the sub account private key is compromised, the main account's assets remain secure.

#### Initialization

```kotlin
import io.dipcoin.sui.perp.client.PerpTradeClient;
import io.dipcoin.sui.perp.client.auth.AuthSession;
import io.dipcoin.sui.perp.client.core.PerpAuthorization;
import io.dipcoin.sui.perp.enums.PerpNetwork;

// Create authorization and get auth session
val perpAuth = PerpAuthorization(PerpNetwork.TESTNET)
val subAuth = perpAuth.authorize(subKeyPair)

// Initialize trade client with sub account auth
val tradeClient = PerpTradeClient(PerpNetwork.TESTNET, subAuth)
```

#### Place Order

```kotlin
import io.dipcoin.sui.perp.enums.OrderSide;
import io.dipcoin.sui.perp.enums.OrderType;
import io.dipcoin.sui.perp.model.request.PlaceOrderRequest;
import io.dipcoin.sui.perp.util.DecimalUtil;
import io.dipcoin.sui.perp.util.OrderUtil;

import java.math.BigDecimal;
import java.math.BigInteger;

// Get market perp ID first
val perpId = marketClient.getMarketPerpId("BTC-PERP")

// Create order request with 18 decimal precision
val request = PlaceOrderRequest(
    symbol = "BTC-PERP",
    market = perpId,
    price = DecimalUtil.toBaseUnit(BigDecimal("50000")),      // 50000 USDC
    quantity = DecimalUtil.toBaseUnit(BigDecimal("2")),       // 2 BTC
    side = OrderSide.BUY.code,
    orderType = OrderType.LIMIT.code,
    leverage = DecimalUtil.toBaseUnit(BigInteger("10")),      // 10x leverage
    reduceOnly = false,
    creator = mainAddress,
    clientId = "unique_client_id"
)

// Sign order with sub account
val salt = String(OrderUtil.getSalt());
request.salt = salt
val signature = OrderUtil.getSignature(OrderUtil.getSerializedOrder(request), subKeyPair)
request.orderSignature = signature

// Place order
val orderId = tradeClient.placeOrder(request)
println("Order ID: " + orderId);
```
For the vault version:

> Replace the vault address `0xvault_address` below with the actual vault address that has been created and associated with your order address. The request field to be set is `creator`.

```kotlin
import io.dipcoin.sui.perp.enums.OrderSide;
import io.dipcoin.sui.perp.enums.OrderType;
import io.dipcoin.sui.perp.model.request.PlaceOrderRequest;
import io.dipcoin.sui.perp.util.DecimalUtil;
import io.dipcoin.sui.perp.util.OrderUtil;

import java.math.BigDecimal;
import java.math.BigInteger;

// Get market perp ID first
val perpId = marketClient.getMarketPerpId("BTC-PERP")

// Create order request with 18 decimal precision
val request = PlaceOrderRequest(
    symbol = "BTC-PERP",
    market = perpId,
    price = DecimalUtil.toBaseUnit(BigDecimal("50000")),      // 50000 USDC
    quantity = DecimalUtil.toBaseUnit(BigDecimal("2")),       // 2 BTC
    side = OrderSide.BUY.code,
    orderType = OrderType.LIMIT.code,
    leverage = DecimalUtil.toBaseUnit(BigInteger("10")),      // 10x leverage
    reduceOnly = false,
    creator = "0xvault_address",
    clientId = "unique_client_id"
)

// Sign order with sub account
val salt = String(OrderUtil.getSalt());
request.salt = salt
val signature = OrderUtil.getSignature(OrderUtil.getSerializedOrder(request), subKeyPair)
request.orderSignature = signature

// Place order
val orderId = tradeClient.placeOrder(request)
println("Order ID: " + orderId);
```

**Market Order Example:**

```kotlin
val marketOrder = PlaceOrderRequest(
    symbol = "BTC-PERP",
    market = perpId,
    quantity = DecimalUtil.toBaseUnit(BigDecimal("1")),  // 1 BTC
    side = OrderSide.SELL.code,
    orderType = OrderType.MARKET.code,
    leverage = DecimalUtil.toBaseUnit(BigInteger("5")),
    reduceOnly = false,
    creator = mainAddress,
    clientId = "market_order_001"
)

val salt = String(OrderUtil.getSalt());
marketOrder.salt = salt
marketOrder.orderSignature = OrderUtil.getSignature(OrderUtil.getSerializedOrder(marketOrder), subKeyPair)

val orderId = tradeClient.placeOrder(marketOrder)
```
For the vault version:

> Replace the vault address `0xvault_address` below with the actual vault address that has been created and associated with your order address. The request field to be set is `creator`.

```kotlin
val marketOrder = PlaceOrderRequest(
    symbol = "BTC-PERP",
    market = perpId,
    quantity = DecimalUtil.toBaseUnit(BigDecimal("1")),  // 1 BTC
    side = OrderSide.SELL.code,
    orderType = OrderType.MARKET.code,
    leverage = DecimalUtil.toBaseUnit(BigInteger("5")),
    reduceOnly = false,
    creator = "0xvault_address",
    clientId = "market_order_001"
)

val salt = String(OrderUtil.getSalt());
marketOrder.salt = salt
marketOrder.orderSignature = OrderUtil.getSignature(OrderUtil.getSerializedOrder(marketOrder), subKeyPair)

val orderId = tradeClient.placeOrder(marketOrder)
```

#### Cancel Order

```kotlin
import io.dipcoin.sui.perp.model.request.CancelOrderRequest;
import io.dipcoin.sui.perp.model.response.CancelOrderResponse;

val request = CancelOrderRequest(orderId = "order_id_to_cancel", symbol = "BTC-PERP")
val response = tradeClient.cancelOrder(request)
println("Cancelled order: " + response.orderId)
```
For the vault version:

> Replace the vault address `0xvault_address` below with the actual vault address that has been created and associated with your order address. The request field to be set is `creator`.

```kotlin
import io.dipcoin.sui.perp.model.request.CancelOrderRequest
import io.dipcoin.sui.perp.model.response.CancelOrderResponse

val request = CancelOrderRequest(parentAddress = "0xvault_address", orderId = "order_id_to_cancel", symbol = "BTC-PERP")
val response = tradeClient.cancelOrder(request)
println("Cancelled order: " + response.orderId)
```

---

### PerpUserClient

**Purpose**: Manages user account data including positions, orders, balance history, and funding settlements. Requires main account authorization to access sensitive account information.

#### Initialization

```kotlin
import io.dipcoin.sui.perp.client.PerpUserClient
import io.dipcoin.sui.perp.client.auth.AuthSession
import io.dipcoin.sui.perp.client.core.PerpAuthorization

// Create authorization with main account
val perpAuth = PerpAuthorization(PerpNetwork.TESTNET)
val mainAuth = perpAuth.authorize(mainKeyPair)

// Initialize user client
val userClient = PerpUserClient(PerpNetwork.TESTNET, mainAuth)
```

#### Get Account Information

```kotlin
import io.dipcoin.sui.perp.model.response.AccountResponse;

AccountResponse account = userClient.account(null);
println("Wallet balance: " + account.walletBalance);
println("Free collateral: " + account.freeCollateral);
println("Total position margin: " + account.totalPositionMargin)
println("Total unrealized profit: " + account.totalUnrealizedProfit)
println("Account value: " + account.accountValue)
```

For the vault version:

> Replace the vault address `0xvault_address` below with the actual vault address that has been created and associated with your order address. The request field to be set is `creator`.

```kotlin
import io.dipcoin.sui.perp.model.response.AccountResponse;

// Query vault address.
val request = AccountRequest(parentAddress = "0xvault_address")
val account = userClient.account(request)
println("Wallet balance: " + account.walletBalance);
println("Free collateral: " + account.freeCollateral);
println("Total position margin: " + account.totalPositionMargin);
println("Total unrealized profit: " + account.totalUnrealizedProfit);
println("Account value: " + account.accountValue);
```

#### Get Positions

```kotlin
import io.dipcoin.sui.perp.model.response.PositionResponse;
import java.util.List;

val positions = userClient.positions(null)
for (PositionResponse position : positions) {
    println("Symbol: " + position.symbol);
    println("Side: " + position.side);
    println("Quantity: " + position.quantity);
    println("Entry price: " + position.avgEntryPrice);
    println("Unrealized P&L: " + position.unrealizedProfit);
    println("Liquidation price: " + position.liquidationPrice);
}
```

For the vault version:

> Replace the vault address `0xvault_address` below with the actual vault address that has been created and associated with your order address. The request field to be set is `creator`.

```kotlin
import io.dipcoin.sui.perp.model.response.PositionResponse;
import java.util.List;

// Query vault address.
val request = PositionRequest(symbol = "ETH-PERP", parentAddress = "0xvault_address")

val positions = userClient.positions(request)
for (PositionResponse position : positions) {
    println("Symbol: " + position.symbol);
    println("Side: " + position.side);
    println("Quantity: " + position.quantity);
    println("Entry price: " + position.avgEntryPrice);
    println("Unrealized P&L: " + position.unrealizedProfit);
    println("Liquidation price: " + position.liquidationPrice);
}
```

#### Get Active Orders

```kotlin
import io.dipcoin.sui.perp.model.PageResponse;
import io.dipcoin.sui.perp.model.request.OrdersRequest;
import io.dipcoin.sui.perp.model.response.OrdersResponse;

val request = OrdersRequest(symbol = "BTC-PERP", pageNum = 1, pageSize = 20)

val orders = userClient.orders(request)
println("Total orders: " + orders.total);
```

For the vault version:

> Replace the vault address `0xvault_address` below with the actual vault address that has been created and associated with your order address. The request field to be set is `creator`.

```kotlin
import io.dipcoin.sui.perp.model.PageResponse;
import io.dipcoin.sui.perp.model.request.OrdersRequest;
import io.dipcoin.sui.perp.model.response.OrdersResponse;

// Query vault address.
val request = OrdersRequest(symbol = "BTC-PERP", parentAddress = "0xvault_address", pageNum = 1, pageSize = 20)

val orders = userClient.orders(request)
println("Total orders: " + orders.total);
```

#### Get Order History

```kotlin
import io.dipcoin.sui.perp.model.request.HistoryOrdersRequest;
import io.dipcoin.sui.perp.model.response.HistoryOrdersResponse;

val request = HistoryOrdersRequest(symbol = "BTC-PERP", pageNum = 1, pageSize = 20)

val history = userClient.historyOrders(request)
```

For the vault version:

> Replace the vault address `0xvault_address` below with the actual vault address that has been created and associated with your order address. The request field to be set is `creator`.

```kotlin
import io.dipcoin.sui.perp.model.request.HistoryOrdersRequest;
import io.dipcoin.sui.perp.model.response.HistoryOrdersResponse;

// Query vault address.
val request = HistoryOrdersRequest(parentAddress = "0xvault_address", symbol = "BTC-PERP", pageNum = 1, pageSize = 20)

val history = userClient.historyOrders(request)
```

#### Get Funding Settlements

```kotlin
import io.dipcoin.sui.perp.model.request.FundingPageRequest;
import io.dipcoin.sui.perp.model.request.BalancePageRequest;
import io.dipcoin.sui.perp.model.response.FundingSettlementsResponse;

val request = FundingPageRequest(symbol = "BTC-PERP", pageNum = 1, pageSize = 20)

val settlements = userClient.fundingSettlements(request)
```

For the vault version:

> Replace the vault address `0xvault_address` below with the actual vault address that has been created and associated with your order address. The request field to be set is `creator`.

```kotlin
import io.dipcoin.sui.perp.model.request.FundingPageRequest;
import io.dipcoin.sui.perp.model.request.BalancePageRequest;
import io.dipcoin.sui.perp.model.response.FundingSettlementsResponse;

// Query vault address.
val request = FundingPageRequest(parentAddress = "0xvault_address", symbol = "BTC-PERP", pageNum = 1, pageSize = 20)

        val settlements = userClient.fundingSettlements(request)
```

#### Get Balance Changes

```kotlin
import io.dipcoin.sui.perp.model.response.BalanceChangesResponse;

val request = BalancePageRequest(pageNum = 1, pageSize = 20)

val changes = userClient.balanceChanges(request)
```

For the vault version:

> Replace the vault address `0xvault_address` below with the actual vault address that has been created and associated with your order address. The request field to be set is `creator`.

```kotlin
import io.dipcoin.sui.perp.model.response.BalanceChangesResponse;

// Query vault address.
val request = BalancePageRequest(parentAddress = "0xvault_address", pageNum = 1, pageSize = 20)

val changes = userClient.balanceChanges(request)
```

---

### PerpMarketClient

**Purpose**: Provides public market data without requiring authentication. Access real-time ticker information, order books, oracle prices, and trading pair details.

#### Initialization

```kotlin
import io.dipcoin.sui.perp.client.PerpMarketClient;
import io.dipcoin.sui.perp.enums.PerpNetwork;

// No authentication required for market data
PerpMarketClient marketClient = PerpMarketClient(PerpNetwork.TESTNET);
```

#### Get Trading Pairs

```kotlin
import io.dipcoin.sui.perp.model.response.TradingPairResponse;
import java.util.List;

val pairs = marketClient.tradingPair()
for (TradingPairResponse pair : pairs) {
    println("Symbol: " + pair.symbol);
    println("Perp ID: " + pair.perpId);
    println("Max leverage: " + pair.maxLeverage);
    println("Maker fee: " + pair.makerFee);
    println("Taker fee: " + pair.takerFee);
    println("Step size: " + pair.stepSize);
    println("Tick size: " + pair.tickSize);
}
```

#### Get Market Perp ID

```kotlin
// Required when placing orders
val perpId = marketClient.getMarketPerpId("BTC-PERP")
println("Market Perp ID: " + perpId);
```

**Note**: The perp ID is cached internally for performance. The first call fetches all trading pairs, subsequent calls use the cache.

#### Get Pyth Feed ID

```kotlin
// Used for oracle price updates in on-chain operations
String feedId = marketClient.getPythFeedId("BTC-PERP");
println("Pyth Feed ID: " + feedId);
```

#### Get Ticker

```kotlin
import io.dipcoin.sui.perp.model.request.SymbolRequest;
import io.dipcoin.sui.perp.model.response.TickerResponse;

SymbolRequest request = SymbolRequest(symbol = "BTC-PERP")
TickerResponse ticker = marketClient.ticker(request);
println("Last price: " + ticker.lastPrice);
println("24h high: " + ticker.high24h);
println("24h low: " + ticker.low24h);
println("24h volume: " + ticker.volume24h);
println("Price change 24h: " + ticker.priceChange24h);
```

#### Get Order Book

```kotlin
import io.dipcoin.sui.perp.model.response.OrderBookResponse;

SymbolRequest request = SymbolRequest(symbol = "BTC-PERP")
OrderBookResponse orderBook = marketClient.orderBook(request);

println("Bids (sorted descending):");
orderBook.bids.forEach(bid -> 
    println("Price: " + bid.price + ", Qty: " + bid.quantity)
);

println("Asks (sorted ascending):");
orderBook.asks.forEach(ask -> 
    println("Price: " + ask.price + ", Qty: " + ask.quantity)
);
```

#### Get Oracle Price

```kotlin
import java.math.BigInteger;

val request = SymbolRequest(symbol = "BTC-PERP")
val oraclePrice = marketClient.oracle(request)
println("Oracle price: " + oraclePrice);
```

---

### PerpHttpClient

**Purpose**: Unified HTTP client that aggregates all off-chain API modules (PerpTradeClient, PerpUserClient, PerpMarketClient). Provides a single entry point for all HTTP API operations with automatic authorization management.

#### Initialization

```kotlin
import io.dipcoin.sui.perp.client.PerpHttpClient;
import io.dipcoin.sui.crypto.SuiKeyPair;
import io.dipcoin.sui.perp.enums.PerpNetwork;

val mainKeyPair = Ed25519KeyPair.decodeHex("main_private_key")
val subKeyPair = Ed25519KeyPair.decodeHex("sub_private_key")

// Automatically authorizes both accounts on initialization
val client = PerpHttpClient(PerpNetwork.TESTNET, mainKeyPair, subKeyPair)

// Access account information
val mainAddress = client.mainAddress
val subAddress = client.subAddress
SuiKeyPair mainAccount = client.mainAccount;
SuiKeyPair subAccount = client.subAccount;
```

#### Features

- **Automatic Authorization**: Both main and sub accounts are authorized during initialization
- **Unified Interface**: Access all API operations through a single client instance
- **Internal Module Management**: Automatically delegates calls to appropriate specialized clients

#### Usage Examples

**Trading Operations** (uses PerpTradeClient internally):
```kotlin
// Place order
String orderId = client.placeOrder(orderRequest);

// Cancel order
CancelOrderResponse response = client.cancelOrder(cancelRequest);
```

**User Data Operations** (uses PerpUserClient internally):
```kotlin
// Get account info
AccountResponse account = client.account();

// Get positions
List<PositionResponse> positions = client.positions();

// Get orders
PageResponse<OrdersResponse> orders = client.orders(ordersRequest);

// Get history
PageResponse<HistoryOrdersResponse> history = client.historyOrders(historyRequest);

// Get funding settlements
PageResponse<FundingSettlementsResponse> settlements = client.fundingSettlements(pageRequest);

// Get balance changes
PageResponse<BalanceChangesResponse> changes = client.balanceChanges(pageRequest);
```

**Market Data Operations** (uses PerpMarketClient internally):
```kotlin
// Get trading pairs
List<TradingPairResponse> pairs = client.tradingPair();

// Get market perp ID
String perpId = client.getMarketPerpId("BTC-PERP");

// Get Pyth feed ID
String feedId = client.getPythFeedId("BTC-PERP");

// Get ticker
TickerResponse ticker = client.ticker(symbolRequest);

// Get order book
OrderBookResponse orderBook = client.orderBook(symbolRequest);

// Get oracle price
BigInteger oraclePrice = client.oracle(symbolRequest);
```

---

## On-Chain Operation Modules

### PerpOnSignClient

**Purpose**: Handles on-chain blockchain operations with direct private key signing. Suitable for scenarios where you have direct access to private keys and want to execute blockchain transactions.

#### Initialization

```kotlin
import io.dipcoin.sui.perp.client.PerpOnSignClient;
import io.dipcoin.sui.perp.enums.PerpNetwork;
import io.dipcoin.sui.protocol.SuiClient;
import io.dipcoin.sui.protocol.http.HttpService;

// Basic initialization
PerpOnSignClient onChainClient = PerpOnSignClient(PerpNetwork.TESTNET);

// With custom SuiClient
SuiClient suiClient = SuiClient.build(new HttpService("https://fullnode.testnet.sui.io:443"));
PerpOnSignClient onChainClient = PerpOnSignClient(suiClient, PerpNetwork.TESTNET);

// With custom market client
PerpMarketClient marketClient = PerpMarketClient(PerpNetwork.TESTNET);
PerpOnSignClient onChainClient = PerpOnSignClient(suiClient, PerpNetwork.TESTNET, marketClient);
```

#### Set Sub Account

Bind sub account to main account on-chain (one-time setup required before trading with sub account).

```kotlin
import io.dipcoin.sui.model.transaction.SuiTransactionBlockResponse;
import io.dipcoin.sui.perp.util.DecimalUtil;

import java.math.BigDecimal;

String subAddress = subKeyPair.address();
long gasPrice = 1000L;
BigDecimal gasBudget = DecimalUtil.toSui(BigDecimal("0.1")); // 0.1 SUI

val response = onChainClient.setSubAccount(
    mainKeyPair,
    subAddress,
    gasPrice,
    gasBudget
);

println("Transaction digest: " + response.digest);
println("Status: " + response.effects.status.status);
```

#### Deposit

Deposit USDC into trading account. All numerical values use 18 decimal precision.

```kotlin
import java.math.BigDecimal;

// Deposit 1000 USDC
BigDecimal depositAmount = BigDecimal("1000").multiply(BigInteger.TEN.pow(6));
long gasPrice = 1000L;
BigDecimal gasBudget = DecimalUtil.toSui(BigDecimal("0.1"));

val response = onChainClient.deposit(
    mainKeyPair,
    depositAmount,
    gasPrice,
    gasBudget
);

println("Deposit transaction: " + response.digest);
```

#### Withdraw

Withdraw USDC from trading account.

```kotlin
// Withdraw 500 USDC
BigDecimal withdrawAmount = BigDecimal("500").multiply(BigInteger.TEN.pow(6));
long gasPrice = 1000L;
BigDecimal gasBudget = DecimalUtil.toSui(BigDecimal("0.1"));

val response = onChainClient.withdraw(
    mainKeyPair,
    withdrawAmount,
    gasPrice,
    DecimalUtil.toSui(gasBudget)
);

println("Withdraw transaction: " + response.digest);
```

#### Add Margin

Add margin to an existing position.

```kotlin
String symbol = "BTC-PERP";
String subAddress = subKeyPair.address();
BigDecimal marginAmount = BigDecimal("100").multiply(BigInteger.TEN.pow(6)); // 100 USDC
long gasPrice = 1000L;
BigDecimal gasBudget = DecimalUtil.toSui(BigDecimal("0.1"));

val response = onChainClient.addMargin(
    mainKeyPair,
    subAddress,
    symbol,
    marginAmount,
    gasPrice,
    gasBudget
);

println("Add margin transaction: " + response.digest);
```

---

### PerpOffSignClient

**Purpose**: Handles on-chain operations with external wallet integration. Designed for scenarios where private keys are managed by external wallet systems (hardware wallets, wallet SDKs, custody solutions). Requires implementing the `WalletService` interface.

#### WalletService Interface

You must implement this interface to integrate with your wallet system:

```kotlin
import io.dipcoin.sui.perp.client.chain.WalletService;

class MyWalletService : WalletService {
    
    @Override
    public String sign(String address, byte[] txData) {
        // Implement your wallet signing logic here
        // txData is the BCS-encoded transaction bytes
        // Return the signature string
        
        // Example with hardware wallet:
        // HardwareWallet wallet = getWalletForAddress(address);
        // byte[] signature = wallet.signTransaction(txData);
        // return Base64.toBase64String(signature);
        
        // Example with key management service:
        // KeyManagementService kms = getKMSClient();
        // return kms.signTransaction(address, txData);
        
        return yourSigningImplementation(address, txData);
    }
}
```

#### Initialization

```kotlin
import io.dipcoin.sui.perp.client.PerpOffSignClient;
import io.dipcoin.sui.perp.client.PerpMarketClient;
import io.dipcoin.sui.perp.client.chain.WalletService;
import io.dipcoin.sui.perp.enums.PerpNetwork;

// Implement wallet service
WalletService walletService = new MyWalletService();

// Initialize market client
PerpMarketClient marketClient = PerpMarketClient(PerpNetwork.TESTNET);

// Create off-sign client
PerpOffSignClient offSignClient = PerpOffSignClient(
    PerpNetwork.TESTNET,
    marketClient,
    walletService
);

// With custom SuiClient
SuiClient suiClient = SuiClient.build(new HttpService("https://fullnode.testnet.sui.io:443"));
PerpOffSignClient offSignClient = PerpOffSignClient(
    suiClient,
    PerpNetwork.TESTNET,
    marketClient,
    walletService
);
```

#### Set Sub Account

```kotlin
val sender = "0x..." // Main account address
val subAddress = "0x..." // Sub account address
val gasPrice = 1000L
val gasBudget = BigDecimal("0.1")

val response = offSignClient.setSubAccount(
    sender,
    subAddress,
    gasPrice,
    DecimalUtil.toBaseUnit(gasBudget)
);
```

#### Deposit

```kotlin
String sender = "0x..."; // Main account address
BigDecimal depositAmount = BigDecimal("1000");

val response = offSignClient.deposit(
    sender,
    DecimalUtil.toBaseUnit(depositAmount),
    gasPrice,
    DecimalUtil.toBaseUnit(gasBudget)
);
```

#### Withdraw

```kotlin
val sender = "0x..." // Main account address
val withdrawAmount = BigDecimal("500")

val response = offSignClient.withdraw(
    sender,
    DecimalUtil.toBaseUnit(withdrawAmount),
    gasPrice,
    DecimalUtil.toBaseUnit(gasBudget)
);
```

#### Add Margin

```kotlin
val sender = "0x..." // Main account address
val subAddress = "0x..." // Sub account address
String symbol = "BTC-PERP";
BigDecimal marginAmount = BigDecimal("100");

val response = offSignClient.addMargin(
    sender,
    subAddress,
    symbol,
    DecimalUtil.toBaseUnit(marginAmount),
    gasPrice,
    DecimalUtil.toBaseUnit(gasBudget)
);
```

#### Remove Margin

```kotlin
val sender = "0x..." // Main account address
val subAddress = "0x..." // Sub account address
String symbol = "BTC-PERP";
BigDecimal marginAmount = BigDecimal("100");

val response = offSignClient.removeMargin(
    sender,
    subAddress,
    symbol,
    DecimalUtil.toBaseUnit(marginAmount),
    gasPrice,
    DecimalUtil.toBaseUnit(gasBudget)
);
```

**Use Cases for PerpOffSignClient**:
- Hardware wallet integration
- Multi-signature wallet systems
- Custodial wallet services
- Key management services (KMS)
- Enterprise-grade security solutions

---

## Data Models

### PlaceOrderRequest

```kotlin
// Kotlin data class
data class PlaceOrderRequest(
    var symbol: String? = null,           // Trading pair (e.g., "BTC-PERP")
    var market: String? = null,           // Market perp ID from getMarketPerpId()
    var price: BigInteger? = null,       // Order price (18 decimals)
    var quantity: BigInteger? = null,    // Order quantity (18 decimals)
    var side: String? = null,             // OrderSide: BUY, SELL
    var orderType: String? = null,        // OrderType: LIMIT, MARKET
    var leverage: BigInteger? = null,     // Leverage multiplier
    var reduceOnly: Boolean = false,      // Reduce-only flag
    var salt: String? = null,             // Random salt
    var creator: String? = null,          // Main account address
    var clientId: String = "",            // Client-defined order ID
    var orderSignature: String? = null,   // Order signature
)
```

### AccountResponse

```kotlin
// Kotlin data class
data class AccountResponse(
    var address: String? = null,
    var canTrade: Boolean? = null,
    var walletBalance: String? = null,
    var totalPositionMargin: String? = null,
    var totalUnrealizedProfit: String? = null,
    var freeCollateral: String? = null,
    var accountValue: String? = null,
    var feeTier: String? = null,
    var accountDataByMarket: List<AccountDataByMarketResponse>? = null,
)
```

### PositionResponse

```kotlin
// Kotlin data class
data class PositionResponse(
    var symbol: String? = null,
    var side: String? = null,
    var quantity: String? = null,
    var avgEntryPrice: String? = null,
    var margin: String? = null,
    var leverage: String? = null,
    var positionValue: String? = null,
    var unrealizedProfit: String? = null,
    var roe: String? = null,
    var liquidationPrice: String? = null,
    var oraclePrice: String? = null,
    var fundingDue: String? = null,
)
```

### TradingPairResponse

```kotlin
// Kotlin data class
data class TradingPairResponse(
    var perpId: String? = null,
    var symbol: String? = null,
    var coinName: String? = null,
    var status: Int? = null,
    var initialMargin: String? = null,
    var maintenanceMargin: String? = null,
    var makerFee: String? = null,
    var takerFee: String? = null,
    var stepSize: String? = null,
    var tickSize: String? = null,
    var maxQtyLimit: String? = null,
    var maxQtyMarket: String? = null,
    var maxLeverage: Int? = null,
    var priceIdentifierId: String? = null,
)
```

### Enums

#### OrderSide
```kotlin
enum class OrderSide(val code: String, val value: Int) {
    BUY("BUY", 1),
    SELL("SELL", 2),
}
```

#### OrderType
```kotlin
enum class OrderType(val code: String, val value: Int) {
    LIMIT("LIMIT", 1),      // Limit order
    MARKET("MARKET", 2),    // Market order
    LIQ("Liquidation", 3),  // Liquidation order
    ADL("ADL", 4),          // Auto-deleveraging order
}
```

#### PerpNetwork
```kotlin
enum class PerpNetwork {
    MAINNET,  // Production environment
    TESTNET,  // Test environment
}
```

---

## Utilities

### DecimalUtil

All numerical values in the Dipcoin Perpetual system use **18 decimal places precision**. The `DecimalUtil` class provides conversion methods between human-readable values and base unit values.

#### Convert to Base Unit (18 decimals)

```kotlin
import io.dipcoin.sui.perp.util.DecimalUtil;
import java.math.BigDecimal;
import java.math.BigInteger;

// From String
BigInteger amount1 = DecimalUtil.toBaseUnit("1000.5");

// From BigDecimal
BigDecimal value = BigDecimal("1000.5");
BigInteger amount2 = DecimalUtil.toBaseUnit(value);

// From BigInteger (multiplies by 10^18)
BigInteger value3 = BigInteger("1000");
BigInteger amount3 = DecimalUtil.toBaseUnit(value3);

// Get base unit constants
BigInteger baseUnit = DecimalUtil.getBaseUintInteger();  // 10^18
BigDecimal baseUnitDecimal = DecimalUtil.getBaseUintDecimal();  // 10^18
BigInteger halfBaseUnit = DecimalUtil.getHalfBaseUint();  // 0.5 * 10^18
```

#### Convert from Base Unit

```kotlin
// Convert 18 decimal base unit to human-readable value
BigInteger baseUnitValue = BigInteger("1500000000000000000000"); // 1500 * 10^18
BigDecimal readableValue = DecimalUtil.fromBaseUnit(baseUnitValue);  // 1500.000000000000000000
```

#### Arithmetic Operations

```kotlin
// Base multiplication (value * baseValue / 10^18)
BigInteger result1 = DecimalUtil.baseMul(value1, value2);

// Base division (value * 10^18 / baseValue)
BigInteger result2 = DecimalUtil.baseDiv(value1, value2);

// Ceiling (ceil(a/m) * m)
BigInteger result3 = DecimalUtil.ceil(a, m);

// Floor (floor(a/m) * m)
BigInteger result4 = DecimalUtil.floor(a, m);

// Minimum
BigInteger result5 = DecimalUtil.min(a, b);

// Safe subtraction (returns a - b if a > b, else 0)
BigInteger result6 = DecimalUtil.sub(a, b);
```

### OrderUtil

Utility class for order operations and cryptographic signing.

#### Generate Salt

```kotlin
import io.dipcoin.sui.perp.util.OrderUtil

val salt = OrderUtil.getSalt()
val saltString = String(salt)
```

#### Serialize Order

```kotlin
val request = PlaceOrderRequest(
    symbol = "BTC-PERP",
    market = perpId,
    price = DecimalUtil.toBaseUnit(BigDecimal("50000")),
    quantity = DecimalUtil.toBaseUnit(BigDecimal("1")),
    side = OrderSide.BUY.code,
    orderType = OrderType.LIMIT.code,
    leverage = BigInteger("10"),
    reduceOnly = false,
    creator = mainAddress,
    salt = saltString
)
val serializedOrder = OrderUtil.getSerializedOrder(request)
```

#### Sign Order

```kotlin
val signature = OrderUtil.getSignature(serializedOrder, subKeyPair)
request.orderSignature = signature
```

#### Sign Message

```kotlin
val message = "Your message"
val signature = OrderUtil.getSignature(message, keyPair)
```

---

## Examples

### Complete Trading Flow

```kotlin
import io.dipcoin.sui.crypto.Ed25519KeyPair;
import io.dipcoin.sui.crypto.SuiKeyPair;
import io.dipcoin.sui.perp.client.*;
import io.dipcoin.sui.perp.enums.*;
import io.dipcoin.sui.perp.model.request.*;
import io.dipcoin.sui.perp.model.response.*;
import io.dipcoin.sui.perp.util.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

fun main() {
    // Initialize
    val mainKeyPair = Ed25519KeyPair.decodeHex("main_key")
    val subKeyPair = Ed25519KeyPair.decodeHex("sub_key")
    
    val httpClient = PerpHttpClient(PerpNetwork.TESTNET, mainKeyPair, subKeyPair)
    val onChainClient = PerpOnSignClient(PerpNetwork.TESTNET)
        
    // Setup (one-time)
    onChainClient.setSubAccount(
        mainKeyPair,
        subKeyPair.address(),
        1000L,
        DecimalUtil.toBaseUnit(BigDecimal("0.1"))
    )
    
    // Deposit funds
    onChainClient.deposit(
        mainKeyPair,
        BigDecimal("10000").multiply(BigInteger.TEN.pow(6)),
        1000L,
        DecimalUtil.toBaseUnit(BigDecimal("0.1"))
    )
    
    // Check balance
    val account = httpClient.account(null)
    val freeCollateral = DecimalUtil.fromBaseUnit(BigInteger(account.freeCollateral))
    println("Free collateral: " + freeCollateral)
    
    // Get market info
    val perpId = httpClient.getMarketPerpId("BTC-PERP")
    
    // Place buy order
    val buyOrder = PlaceOrderRequest(
        symbol = "BTC-PERP",
        market = perpId,
        price = DecimalUtil.toBaseUnit(BigDecimal("45000")),
        quantity = DecimalUtil.toBaseUnit(BigDecimal("2")),
        side = OrderSide.BUY.code,
        orderType = OrderType.LIMIT.code,
        leverage = DecimalUtil.toBaseUnit(BigInteger("5")),
        reduceOnly = false,
        creator = mainKeyPair.address(),
        clientId = "buy_001"
    )
    val salt = String(OrderUtil.getSalt())
    buyOrder.salt = salt
    val signature = OrderUtil.getSignature(OrderUtil.getSerializedOrder(buyOrder), subKeyPair)
    buyOrder.orderSignature = signature
    
    val orderId = httpClient.placeOrder(buyOrder)
    println("Buy order placed: " + orderId)
    
    // Monitor positions
    val positions = httpClient.positions(null)
    positions.forEach { pos ->
        val pnl = DecimalUtil.fromBaseUnit(BigInteger(pos.unrealizedProfit))
        println("Position: " + pos.symbol + " " + pos.side)
        println("P&L: " + pnl)
    }
    
    // Close position with market order
    val sellOrder = PlaceOrderRequest(
        symbol = "BTC-PERP",
        market = perpId,
        quantity = DecimalUtil.toBaseUnit(BigDecimal("2")),
        side = OrderSide.SELL.code,
        orderType = OrderType.MARKET.code,
        leverage = DecimalUtil.toBaseUnit(BigInteger("5")),
        reduceOnly = true,
        creator = mainKeyPair.address(),
        clientId = "sell_001"
    )
    val sellSalt = String(OrderUtil.getSalt())
    sellOrder.salt = sellSalt
    sellOrder.orderSignature = OrderUtil.getSignature(OrderUtil.getSerializedOrder(sellOrder), subKeyPair)
    
    val sellOrderId = httpClient.placeOrder(sellOrder)
    println("Sell order placed: " + sellOrderId)
    
    // Withdraw profits
    val withdrawAmount = BigDecimal("1000")
    onChainClient.withdraw(
        mainKeyPair,
        DecimalUtil.toBaseUnit(withdrawAmount),
        1000L,
        DecimalUtil.toBaseUnit(BigDecimal("0.1"))
    )
}
```

### Market Making Example

```kotlin
fun main() {
    val client = PerpHttpClient(PerpNetwork.TESTNET, mainKeyPair, subKeyPair)
    val perpId = client.getMarketPerpId("BTC-PERP")
    
    // Get current market price
    val ticker = client.ticker(SymbolRequest(symbol = "BTC-PERP"))
    val lastPrice = DecimalUtil.fromBaseUnit(BigInteger(ticker.lastPrice))
        
    // Calculate bid/ask prices (0.1% spread)
    val bidPrice = lastPrice.multiply(BigDecimal("0.9995"))
    val askPrice = lastPrice.multiply(BigDecimal("1.0005"))
    val quantity = BigDecimal("1")
    
    // Place bid order
    val bidOrder = createOrder(perpId, DecimalUtil.toBaseUnit(bidPrice), DecimalUtil.toBaseUnit(quantity), OrderSide.BUY, mainKeyPair.address())
    val bidSalt = String(OrderUtil.getSalt())
    bidOrder.salt = bidSalt
    bidOrder.orderSignature = OrderUtil.getSignature(OrderUtil.getSerializedOrder(bidOrder), subKeyPair)
    client.placeOrder(bidOrder)
    
    // Place ask order
    val askOrder = createOrder(perpId, DecimalUtil.toBaseUnit(askPrice), DecimalUtil.toBaseUnit(quantity), OrderSide.SELL, mainKeyPair.address())
    val askSalt = String(OrderUtil.getSalt())
    askOrder.salt = askSalt
    askOrder.orderSignature = OrderUtil.getSignature(OrderUtil.getSerializedOrder(askOrder), subKeyPair)
    client.placeOrder(askOrder)
}

fun createOrder(perpId: String, price: BigInteger, quantity: BigInteger, side: OrderSide, creator: String) =
    PlaceOrderRequest(
        symbol = "BTC-PERP",
        market = perpId,
        price = price,
        quantity = quantity,
        side = side.code,
        orderType = OrderType.LIMIT.code,
        leverage = DecimalUtil.toBaseUnit(BigInteger("1")),
        reduceOnly = false,
        creator = creator,
        clientId = "mm_" + System.currentTimeMillis()
    )
```

### Using Modular Clients

```kotlin
fun main() {
    // Initialize separate clients for different purposes
    val network = PerpNetwork.TESTNET
    
    // 1. Market data client (no auth required)
    val marketClient = PerpMarketClient(network)
    val pairs = marketClient.tradingPair()
    
    // 2. User data client (main account auth)
    val perpAuth = PerpAuthorization(network)
    val mainAuth = perpAuth.authorize(mainKeyPair)
    val userClient = PerpUserClient(network, mainAuth)
    val account = userClient.account(null)
    
    // 3. Trade client (sub account auth)
    val subAuth = perpAuth.authorize(subKeyPair)
    val tradeClient = PerpTradeClient(network, subAuth)
    val orderId = tradeClient.placeOrder(orderRequest)
    
    // OR use unified client for convenience
    val unifiedClient = PerpHttpClient(network, mainKeyPair, subKeyPair)
    val orderId2 = unifiedClient.placeOrder(orderRequest)
}
```

---

## Best Practices

### Security

1. **Use Sub Account for Trading**: Keep main account private key offline, use sub account for order operations
2. **Implement WalletService Properly**: For production systems, integrate with hardware wallets or secure key management services
3. **Never Hardcode Private Keys**: Use environment variables or secure key storage
4. **Validate Inputs**: Always validate order parameters before submission
5. **Monitor Liquidation Prices**: Set up alerts for positions approaching liquidation

### Precision Handling

```kotlin
// ALWAYS use DecimalUtil for value conversions
import io.dipcoin.sui.perp.util.DecimalUtil;
import java.math.BigDecimal;

// Convert human-readable values to base unit (18 decimals)
BigDecimal userInput = BigDecimal("1000.5");
BigInteger baseUnitValue = DecimalUtil.toBaseUnit(userInput);

// Convert base unit back to human-readable
BigDecimal displayValue = DecimalUtil.fromBaseUnit(baseUnitValue);

// For display purposes, format to appropriate decimal places
String formatted = displayValue.setScale(2, RoundingMode.DOWN).toPlainString();
```

### Performance

1. **Reuse Clients**: Create client instances once and reuse them
2. **Cache Market Data**: Use `PerpMarketClient` caching for perp IDs and feed IDs
3. **Batch Operations**: Use pagination for large data sets
4. **Handle Rate Limits**: Implement exponential backoff
5. **Connection Pooling**: Reuse HTTP connections when possible

### Trading

1. **Check Free Collateral**: Ensure sufficient margin before placing orders
   ```kotlin
   AccountResponse account = client.account();
   BigDecimal freeCollateral = DecimalUtil.fromBaseUnit(BigInteger(account.freeCollateral));
   if (freeCollateral.compareTo(requiredMargin) >= 0) {
       // Place order
   }
   ```

2. **Use Reduce-Only for Closing**: Set `reduceOnly=true` when closing positions
   ```kotlin
   request.reduceOnly = true
   ```

3. **Monitor Funding**: Track funding rates and settlements
   ```kotlin
   PageResponse<FundingSettlementsResponse> settlements = client.fundingSettlements(pageRequest);
   ```

4. **Use Appropriate Leverage**: Conservative leverage reduces liquidation risk
   ```kotlin
   request.leverage = BigInteger("5") // 5x leverage
   ```

5. **Implement Client IDs**: Use unique client IDs for order tracking
   ```kotlin
   request.clientId = "strategy1_" + System.currentTimeMillis()
   ```

### Error Handling

```kotlin
import io.dipcoin.sui.perp.exception.*;

try {
    String orderId = client.placeOrder(request);
} catch (e: PerpHttpException) {
    // HTTP API errors
    eprintln("HTTP error: " + e.message)
} catch (e: PerpRpcFailedException) {
    // Blockchain RPC errors
    eprintln("RPC failed: " + e.message)
} catch (e: PerpOnChainException) {
    // On-chain transaction errors
    eprintln("On-chain error: " + e.message)
} catch (e: PerpJsonParseException) {
    // JSON parsing errors
    eprintln("Parse error: " + e.message)
}
```

### Gas Management

```kotlin
// Get dynamic gas price
SuiClient suiClient = SuiClient.build(new HttpService(networkConfig.suiRpc()));
Long gasPrice = suiClient.getReferenceGasPrice();

// Use appropriate gas budget (in 18 decimals)
BigDecimal normalGasBudget = BigDecimal("0.1");  // 0.1 SUI
BigDecimal largeGasBudget = BigDecimal("1.0");   // 1 SUI for complex operations

BigInteger gasBudget = DecimalUtil.toBaseUnit(normalGasBudget);
```

### Module Selection Guide

**Use PerpHttpClient when:**
- You need a simple, unified interface
- You want automatic authorization management
- You're building a simple trading application

**Use Individual Modules when:**
- You need fine-grained control over authentication
- You want to minimize dependencies
- You're building a microservices architecture
- You need to separate concerns (e.g., read-only market data service)

**Use PerpOnSignClient when:**
- You have direct access to private keys
- You're building automated trading systems
- You need fast transaction signing

**Use PerpOffSignClient when:**
- You're integrating with external wallet systems
- You need hardware wallet support
- You're building enterprise-grade systems with strict security requirements

---

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

---

**Note**: This SDK uses 18 decimal places precision for all numerical values. Always use `DecimalUtil.toBaseUnit()` and `DecimalUtil.fromBaseUnit()` for conversions. The SDK is under active development, and APIs may change in future versions. Always test thoroughly on testnet before production use.

For support and questions, please open an issue on the GitHub repository.

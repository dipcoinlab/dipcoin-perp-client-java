# dipcoin-perp-client-java

Java Implementation of the Dipcoin Perpetual Trading Client Library

## 📋 Table of Contents

- [Overview](#overview)
- [Installation](#installation)
  - [Maven](#maven)
  - [Gradle](#gradle)
- [Quick Start](#quick-start)
- [Core Modules](#core-modules)
  - [PerpHttpClient](#perphttpclient)
  - [PerpOnSignClient](#perponsignclient)
  - [PerpOffSignClient](#perpoffsignclient)
- [API Reference](#api-reference)
  - [Authorization API](#authorization-api)
  - [Trading API](#trading-api)
  - [User Data API](#user-data-api)
  - [Market Data API](#market-data-api)
  - [On-Chain API](#on-chain-api)
- [Data Models](#data-models)
- [Utilities](#utilities)
- [Examples](#examples)
- [Error Handling](#error-handling)
- [Best Practices](#best-practices)
- [License](#license)

## Overview

The Dipcoin Perpetual Client Library provides a comprehensive Java SDK for interacting with the Dipcoin perpetual futures trading platform. It supports both HTTP API operations and on-chain transactions on the Sui blockchain.

### Key Features

- **Dual Account Architecture**: Separate main and sub accounts for enhanced security
- **Automatic Authorization**: Built-in authentication management
- **Type-Safe API**: Strongly typed requests and responses
- **On-Chain Operations**: Direct blockchain interaction support
- **Comprehensive Trading**: Full order lifecycle management

## Installation

### Maven

```xml
<dependency>
    <groupId>io.dipcoin</groupId>
    <artifactId>sui-perp-client-java</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle

```gradle
implementation 'io.dipcoin:sui-perp-client-java:1.0.0'
```

## Quick Start

Complete example showing client initialization, fund deposit, and order placement:

```java
import io.dipcoin.sui.crypto.Ed25519KeyPair;
import io.dipcoin.sui.crypto.SuiKeyPair;
import io.dipcoin.sui.perp.client.PerpHttpClient;
import io.dipcoin.sui.perp.client.PerpOnSignClient;
import io.dipcoin.sui.perp.enums.OrderSide;
import io.dipcoin.sui.perp.enums.OrderType;
import io.dipcoin.sui.perp.enums.PerpNetwork;
import io.dipcoin.sui.perp.model.request.PlaceOrderRequest;
import io.dipcoin.sui.perp.model.response.AccountResponse;
import io.dipcoin.sui.perp.util.DecimalUtil;
import io.dipcoin.sui.perp.util.OrderUtil;

import java.math.BigDecimal;
import java.math.BigInteger;

public class QuickStartExample {
  public static void main(String[] args) {
    // 1. Initialize keypairs
    SuiKeyPair mainKeyPair = Ed25519KeyPair.decodeHex("your_main_private_key");
    SuiKeyPair subKeyPair = Ed25519KeyPair.decodeHex("your_sub_private_key");

    // 2. Create HTTP client (for API operations)
    PerpClient httpClient = new PerpHttpClient(PerpNetwork.TESTNET, mainKeyPair, subKeyPair);

    // 3. Create on-chain client (for blockchain operations)
    PerpOnSignClient onChainClient = new PerpOnSignClient(PerpNetwork.TESTNET);

    // 4. Set sub account (one-time setup)
    onChainClient.setSubAccount(mainKeyPair, subKeyPair.address(), 1000L, BigInteger.TEN.pow(8));

    // 5. Deposit funds (USDC spot 6 decimals)
    BigInteger depositAmount = new BigInteger("1000000000"); // 1000 USDC
    onChainClient.deposit(mainKeyPair, depositAmount, 1000L, BigInteger.TEN.pow(8));

    // 6. Check account balance
    AccountResponse account = httpClient.account();
    System.out.println("Available margin: " + account.getFreeCollateral());

    // 7. Get market perpId
    String perpId = httpClient.getMarketPerpId("ETH-PERP");

    // 8. Place an order
    PlaceOrderRequest orderRequest = new PlaceOrderRequest()
            .setSymbol("ETH-PERP")
            .setMarket(perpId)
            // price 3800 (18 decimals)
            .setPrice(DecimalUtil.toBaseUnit(new BigInteger("3800")))
            // quantity 1 ETH (18 decimals)
            .setQuantity(DecimalUtil.toBaseUnit(BigInteger.ONE))
            // or quantity 0.01 ETH (18 decimals)
//            .setQuantity(DecimalUtil.toBaseUnit(new BigDecimal("0.01")))
            .setSide(OrderSide.BUY.getCode())
            .setOrderType(OrderType.LIMIT.getCode())
            // 5x leverage (18 decimals)
            .setLeverage(DecimalUtil.toBaseUnit(new BigInteger("5")))
            .setReduceOnly(false)
            .setCreator(mainKeyPair.address());

    // 9. Sign order with sub account
    String salt = new String(OrderUtil.getSalt());
    orderRequest.setSalt(salt);
    String signature = OrderUtil.getSignature(OrderUtil.getSerializedOrder(orderRequest), subKeyPair);
    orderRequest.setOrderSignature(signature);

    // 10. Submit order
    String orderId = httpClient.placeOrder(orderRequest);
    System.out.println("Order placed: " + orderId);
  }
}
```

## Core Modules

### PerpHttpClient

Main HTTP client for API operations. Automatically manages authorization for both main and sub accounts.

**Initialization:**

```java
// Dual account mode (recommended for production)
PerpClient client = new PerpHttpClient(PerpNetwork.MAINNET, mainKeyPair, subKeyPair);

// Access account information
String mainAddress = client.getMainAddress();
String subAddress = client.getSubAddress();
SuiKeyPair mainAccount = client.getMainAccount();
SuiKeyPair subAccount = client.getSubAccount();
```

**Features:**
- Automatic authorization on initialization
- Dual account support (main for funds, sub for trading)
- Thread-safe operations
- Built-in error handling

### PerpOnSignClient

On-chain client for blockchain operations with signature management.

**Initialization:**

```java
// Basic initialization
PerpOnSignClient onChainClient = new PerpOnSignClient(PerpNetwork.TESTNET);

// With custom SuiClient
SuiClient suiClient = SuiClient.build(new HttpService("https://fullnode.testnet.sui.io:443"));
PerpOnSignClient onChainClient = new PerpOnSignClient(suiClient, PerpNetwork.TESTNET);
```

**Use Cases:**
- Deposit and withdraw funds
- Set sub account binding
- Add margin to positions
- Direct blockchain transactions

### PerpOffSignClient

Off-chain signing client for preparing transactions without immediate submission.

**Features:**
- Transaction preparation
- Offline signing support
- Batch transaction building

## API Reference

### Authorization API

#### Authorize

Authenticate with the Dipcoin platform.

```java
// Automatic authorization (done during client initialization)
PerpHttpClient client = new PerpHttpClient(PerpNetwork.TESTNET, mainKeyPair, subKeyPair);

// Manual authorization
AuthorizationRequest request = new AuthorizationRequest()
        .setUserAddress(mainKeyPair.address())
        .setIsTermAccepted(true)
        .setSignature(signature);
AuthorizationResponse response = client.authorize(request);
```

**Response:**
- `token`: Authentication token for subsequent requests
- Automatically set in request headers

### Trading API

#### Place Order

Submit a new order to the order book.

```java
import io.dipcoin.sui.perp.enums.OrderSide;
import io.dipcoin.sui.perp.enums.OrderType;
import io.dipcoin.sui.perp.model.request.PlaceOrderRequest;
import io.dipcoin.sui.perp.util.OrderUtil;

// Get market perpId
String perpId = client.getMarketPerpId("BTC-PERP");

// Create order request
PlaceOrderRequest request = new PlaceOrderRequest()
        .setSymbol("BTC-PERP")
        .setMarket(perpId)
        .setPrice(DecimalUtil.toBaseUnit(new BigInteger("110000"))     // 110000 USDC (18 decimals)
        .setQuantity(DecimalUtil.toBaseUnit(BigInteger.ONE))   // 1 BTC (18 decimals)
        .setSide(OrderSide.BUY.getCode())
        .setOrderType(OrderType.LIMIT.getCode())
        .setLeverage(DecimalUtil.toBaseUnit(BigInteger.TEN)) // 10x leverage (18 decimals)
        .setReduceOnly(false)
        .setCreator(mainKeyPair.address())
        .setClientId("");

// Sign order with sub account
String salt = new String(OrderUtil.getSalt());
request.setSalt(salt);
String signature = OrderUtil.getSignature(OrderUtil.getSerializedOrder(request), subKeyPair);
request.setOrderSignature(signature);

// Place order
String orderId = client.placeOrder(request);
```

**Parameters:**
- `symbol`: Trading pair (e.g., "BTC-PERP")
- `market`: Market perpetual ID from `getMarketPerpId()`
- `price`: Order price (6 decimals for USDC)
- `quantity`: Order quantity (9 decimals)
- `side`: Order side (BUY or SELL)
- `orderType`: Order type (LIMIT or MARKET)
- `leverage`: Leverage multiplier
- `reduceOnly`: Whether order reduces position only
- `creator`: Main account address
- `clientId`: Client-defined order ID, default ""

**Returns:** Order ID string

#### Cancel Order

Cancel an existing order.

```java
import io.dipcoin.sui.perp.model.request.CancelOrderRequest;
import io.dipcoin.sui.perp.model.response.CancelOrderResponse;

CancelOrderRequest request = new CancelOrderRequest()
        .setOrderId("order_id_to_cancel")
        .setSymbol("BTC-PERP");

CancelOrderResponse response = client.cancelOrder(request);
System.out.println("Cancelled order: " + response.getOrderId());
```

### User Data API

#### Get Account Information

Retrieve comprehensive account data including balances and margins.

```java
import io.dipcoin.sui.perp.model.response.AccountResponse;

AccountResponse account = client.account();
System.out.println("Wallet balance: " + account.getWalletBalance());
System.out.println("Free collateral: " + account.getFreeCollateral());
System.out.println("Total position margin: " + account.getTotalPositionMargin());
System.out.println("Total unrealized profit: " + account.getTotalUnrealizedProfit());
System.out.println("Account value: " + account.getAccountValue());
```

**Response Fields:**
- `address`: Account address
- `canTrade`: Trading permission status
- `walletBalance`: Available wallet balance
- `totalPositionMargin`: Total margin in positions
- `totalUnrealizedProfit`: Unrealized P&L
- `freeCollateral`: Available margin for trading
- `accountValue`: Total account value
- `feeTier`: Current fee tier
- `accountDataByMarket`: Per-market account data

#### Get Positions

Retrieve all current open positions.

```java
import io.dipcoin.sui.perp.model.response.PositionResponse;
import java.util.List;

List<PositionResponse> positions = client.positions();
for (PositionResponse position : positions) {
    System.out.println("Symbol: " + position.getSymbol());
    System.out.println("Side: " + position.getSide());
    System.out.println("Quantity: " + position.getQuantity());
    System.out.println("Entry price: " + position.getAvgEntryPrice());
    System.out.println("Unrealized profit: " + position.getUnrealizedProfit());
    System.out.println("Liquidation price: " + position.getLiquidationPrice());
}
```

**Position Fields:**
- `symbol`: Trading pair
- `side`: Position side (BUY/SELL)
- `quantity`: Position size
- `avgEntryPrice`: Average entry price
- `margin`: Position margin
- `leverage`: Current leverage
- `positionValue`: Total position value
- `unrealizedProfit`: Unrealized P&L
- `liquidationPrice`: Liquidation price
- `fundingDue`: Pending funding payment

#### Get Active Orders

Retrieve current active orders with pagination.

```java
import io.dipcoin.sui.perp.model.PageResponse;
import io.dipcoin.sui.perp.model.request.OrdersRequest;
import io.dipcoin.sui.perp.model.response.OrdersResponse;

OrdersRequest request = new OrdersRequest()
        .setSymbol("BTC-PERP")  // Optional filter
        .setPage(1)
        .setPageSize(20);

PageResponse<OrdersResponse> orders = client.orders(request);
System.out.println("Total orders: " + orders.getTotal());
for (OrdersResponse order : orders.getData()) {
    System.out.println("Order ID: " + order.getOrderId());
    System.out.println("Status: " + order.getStatus());
}
```

#### Get Order History

Retrieve historical orders with pagination.

```java
import io.dipcoin.sui.perp.model.PageResponse;
import io.dipcoin.sui.perp.model.request.HistoryOrdersRequest;
import io.dipcoin.sui.perp.model.response.HistoryOrdersResponse;

HistoryOrdersRequest request = new HistoryOrdersRequest()
        .setSymbol("BTC-PERP")  // Optional filter
        .setPage(1)
        .setPageSize(20);

PageResponse<HistoryOrdersResponse> history = client.historyOrders(request);
```

#### Get Funding Settlements

Retrieve funding rate settlement history.

```java
import io.dipcoin.sui.perp.model.PageResponse;
import io.dipcoin.sui.perp.model.request.PageRequest;
import io.dipcoin.sui.perp.model.response.FundingSettlementsResponse;

PageRequest request = new PageRequest()
        .setPage(1)
        .setPageSize(20);

PageResponse<FundingSettlementsResponse> settlements = client.fundingSettlements(request);
```

#### Get Balance Changes

Retrieve account balance change history.

```java
import io.dipcoin.sui.perp.model.PageResponse;
import io.dipcoin.sui.perp.model.request.PageRequest;
import io.dipcoin.sui.perp.model.response.BalanceChangesResponse;

PageRequest request = new PageRequest()
        .setPage(1)
        .setPageSize(20);

PageResponse<BalanceChangesResponse> changes = client.balanceChanges(request);
```

### Market Data API

#### Get Trading Pairs

Retrieve all available trading pairs.

```java
import io.dipcoin.sui.perp.model.response.TradingPairResponse;
import java.util.List;

List<TradingPairResponse> pairs = client.tradingPair();
for (TradingPairResponse pair : pairs) {
    System.out.println("Symbol: " + pair.getSymbol());
    System.out.println("Max leverage: " + pair.getMaxLeverage());
    System.out.println("Maker fee: " + pair.getMakerFee());
    System.out.println("Taker fee: " + pair.getTakerFee());
}
```

#### Get Market Perp ID

Get the perpetual market ID for a trading pair.

```java
String perpId = client.getMarketPerpId("BTC-PERP");
System.out.println("Market Perp ID: " + perpId);
```

**Note:** This is required when placing orders.

#### Get Pyth Feed ID

Get the Pyth oracle feed ID for a trading pair.

```java
String feedId = client.getPythFeedId("BTC-PERP");
System.out.println("Pyth Feed ID: " + feedId);
```

#### Get Ticker

Get real-time ticker data for a trading pair.

```java
import io.dipcoin.sui.perp.model.request.SymbolRequest;
import io.dipcoin.sui.perp.model.response.TickerResponse;

SymbolRequest request = new SymbolRequest().setSymbol("BTC-PERP");
TickerResponse ticker = client.ticker(request);
System.out.println("Last price: " + ticker.getLastPrice());
System.out.println("24h volume: " + ticker.getVolume24h());
```

#### Get Order Book

Retrieve order book depth.

```java
import io.dipcoin.sui.perp.model.request.SymbolRequest;
import io.dipcoin.sui.perp.model.response.OrderBookResponse;

SymbolRequest request = new SymbolRequest().setSymbol("BTC-PERP");
OrderBookResponse orderBook = client.orderBook(request);

// Bids sorted descending
orderBook.getBids().forEach(bid -> 
    System.out.println("Bid: " + bid.getPrice() + " @ " + bid.getQuantity())
);

// Asks sorted ascending
orderBook.getAsks().forEach(ask -> 
    System.out.println("Ask: " + ask.getPrice() + " @ " + ask.getQuantity())
);
```

#### Get Oracle Price

Get current oracle price from Pyth.

```java
import io.dipcoin.sui.perp.model.request.SymbolRequest;
import java.math.BigInteger;

SymbolRequest request = new SymbolRequest().setSymbol("BTC-PERP");
BigInteger oraclePrice = client.oracle(request);
System.out.println("Oracle price: " + oraclePrice);
```

### On-Chain API

#### Set Sub Account

Bind sub account to main account on-chain (one-time setup).

```java
import io.dipcoin.sui.model.transaction.SuiTransactionBlockResponse;
import java.math.BigInteger;

PerpOnSignClient onChainClient = new PerpOnSignClient(PerpNetwork.TESTNET);

String subAddress = subKeyPair.address();
long gasPrice = 1000L;
BigInteger gasBudget = BigInteger.TEN.pow(8); // 0.1 SUI

SuiTransactionBlockResponse response = onChainClient.setSubAccount(
    mainKeyPair,
    subAddress,
    gasPrice,
    gasBudget
);

System.out.println("Transaction digest: " + response.getDigest());
```

**Requirements:**
- Must be called before using sub account for trading
- Consumes gas fees from main account
- One sub account per main account

#### Deposit

Deposit USDC into trading account.

```java
import io.dipcoin.sui.model.transaction.SuiTransactionBlockResponse;
import java.math.BigInteger;

PerpOnSignClient onChainClient = new PerpOnSignClient(PerpNetwork.TESTNET);

BigInteger amount = new BigInteger("1000000000"); // 1000 USDC (6 decimals)
long gasPrice = 1000L;
BigInteger gasBudget = BigInteger.TEN.pow(8);

SuiTransactionBlockResponse response = onChainClient.deposit(
    mainKeyPair,
    amount,
    gasPrice,
    gasBudget
);
```

#### Withdraw

Withdraw USDC from trading account.

```java
import io.dipcoin.sui.model.transaction.SuiTransactionBlockResponse;
import java.math.BigInteger;

PerpOnSignClient onChainClient = new PerpOnSignClient(PerpNetwork.TESTNET);

BigInteger amount = new BigInteger("500000000"); // 500 USDC (6 decimals)
long gasPrice = 1000L;
BigInteger gasBudget = BigInteger.TEN.pow(8);

SuiTransactionBlockResponse response = onChainClient.withdraw(
    mainKeyPair,
    amount,
    gasPrice,
    gasBudget
);
```

#### Add Margin

Add margin to an existing position.

```java
import io.dipcoin.sui.model.transaction.SuiTransactionBlockResponse;
import java.math.BigInteger;

PerpOnSignClient onChainClient = new PerpOnSignClient(PerpNetwork.TESTNET);

String symbol = "BTC-PERP";
String subAddress = subKeyPair.address();
BigInteger amount = new BigInteger("100000000"); // 100 USDC (6 decimals)
long gasPrice = 1000L;
BigInteger gasBudget = BigInteger.TEN.pow(8);

SuiTransactionBlockResponse response = onChainClient.addMargin(
    mainKeyPair,
    subAddress,
    symbol,
    amount,
    gasPrice,
    gasBudget
);
```

## Data Models

### PlaceOrderRequest

```java
public class PlaceOrderRequest {
    private String symbol;           // Trading pair (e.g., "BTC-PERP")
    private String market;           // Market perp ID from getMarketPerpId()
    private BigInteger price;        // Order price (6 decimals)
    private BigInteger quantity;     // Order quantity (9 decimals)
    private String side;             // OrderSide: BUY, SELL
    private String orderType;        // OrderType: LIMIT, MARKET
    private BigInteger leverage;     // Leverage multiplier
    private Boolean reduceOnly;      // Reduce-only flag
    private String salt;             // Random salt from OrderUtil.getSalt()
    private String creator;          // Main account address
    private String clientId;         // Client-defined order ID
    private String orderSignature;   // Order signature
}
```

### AccountResponse

```java
public class AccountResponse {
    private String address;                    // Account address
    private Boolean canTrade;                  // Trading permission
    private String walletBalance;              // Available balance
    private String totalPositionMargin;        // Total position margin
    private String totalUnrealizedProfit;      // Total unrealized P&L
    private String freeCollateral;             // Available margin
    private String accountValue;               // Total account value
    private String feeTier;                    // Fee tier
    private List<AccountDataByMarketResponse> accountDataByMarket;
}
```

### PositionResponse

```java
public class PositionResponse {
    private String symbol;              // Trading pair
    private String side;                // Position side (BUY/SELL)
    private String quantity;            // Position size
    private String avgEntryPrice;       // Average entry price
    private String margin;              // Position margin
    private String leverage;            // Current leverage
    private String positionValue;       // Position value
    private String unrealizedProfit;    // Unrealized P&L
    private String roe;                 // Return on equity (%)
    private String liquidationPrice;    // Liquidation price
    private String oraclePrice;         // Oracle price
    private String fundingDue;          // Pending funding
}
```

### TradingPairResponse

```java
public class TradingPairResponse {
    private String perpId;              // Perpetual market ID
    private String symbol;              // Trading pair symbol
    private String coinName;            // Base coin name
    private Integer status;             // Market status
    private String initialMargin;       // Initial margin requirement
    private String maintenanceMargin;   // Maintenance margin requirement
    private String makerFee;            // Maker fee rate
    private String takerFee;            // Taker fee rate
    private String stepSize;            // Quantity step size
    private String tickSize;            // Price tick size
    private String maxQtyLimit;         // Max quantity for limit orders
    private String maxQtyMarket;        // Max quantity for market orders
    private Integer maxLeverage;        // Maximum leverage
    private String priceIdentifierId;   // Pyth price feed ID
}
```

### Enums

#### OrderSide
```java
public enum OrderSide {
    BUY("BUY", 1),
    SELL("SELL", 2);
}
```

#### OrderType
```java
public enum OrderType {
    LIMIT("LIMIT", 1),
    MARKET("MARKET", 2),
    LIQ("Liquidation", 3),
    ADL("ADL", 4);
}
```

#### PerpNetwork
```java
public enum PerpNetwork {
    MAINNET,  // Production environment
    TESTNET;  // Test environment
}
```

## Utilities

### OrderUtil

Utility class for order operations.

#### Generate Salt

```java
byte[] salt = OrderUtil.getSalt();
String saltString = new String(salt);
```

#### Serialize Order

```java
PlaceOrderRequest request = new PlaceOrderRequest()
        .setSymbol("BTC-PERP")
        .setMarket(perpId)
        .setPrice(price)
        .setQuantity(quantity)
        .setSide(OrderSide.BUY.getCode())
        .setOrderType(OrderType.LIMIT.getCode())
        .setLeverage(leverage)
        .setReduceOnly(false)
        .setCreator(mainAddress)
        .setSalt(saltString);

String serializedOrder = OrderUtil.getSerializedOrder(request);
```

#### Sign Order

```java
String signature = OrderUtil.getSignature(serializedOrder, subKeyPair);
request.setOrderSignature(signature);
```

#### Sign Message

```java
String message = "Your message";
String signature = OrderUtil.getSignature(message, keyPair);
```

## Examples

### Complete Trading Flow

```java
public class TradingExample {
    public static void main(String[] args) {
        // Initialize
        SuiKeyPair mainKeyPair = Ed25519KeyPair.decodeHex("main_key");
        SuiKeyPair subKeyPair = Ed25519KeyPair.decodeHex("sub_key");
        
        PerpHttpClient httpClient = new PerpHttpClient(PerpNetwork.TESTNET, mainKeyPair, subKeyPair);
        PerpOnSignClient onChainClient = new PerpOnSignClient(PerpNetwork.TESTNET);
        
        // Setup (one-time)
        onChainClient.setSubAccount(mainKeyPair, subKeyPair.address(), 1000L, BigInteger.TEN.pow(8));
        
        // Deposit funds (USDC spot 6 decimals)
        onChainClient.deposit(mainKeyPair, new BigInteger("10000000000"), 1000L, BigInteger.TEN.pow(8));
        
        // Check balance
        AccountResponse account = httpClient.account();
        System.out.println("Free collateral: " + account.getFreeCollateral());
        
        // Get market info
        String perpId = httpClient.getMarketPerpId("BTC-PERP");
        
        // Place buy order
        PlaceOrderRequest buyOrder = new PlaceOrderRequest()
                .setSymbol("BTC-PERP")
                .setMarket(perpId)
                // price $110000 (18 decimals)
                .setPrice(DecimalUtil.toBaseUnit(new BigInteger("110000")))
                // quantity 2 BTC (18 decimals)
                .setQuantity(DecimalUtil.toBaseUnit(new BigInteger("2")))
                .setSide(OrderSide.BUY.getCode())
                .setOrderType(OrderType.LIMIT.getCode())
                // leverage 5x (18 decimals)
                .setLeverage(DecimalUtil.toBaseUnit(new BigInteger("5")))
                .setReduceOnly(false)
                .setCreator(mainKeyPair.address());
        
        String salt = new String(OrderUtil.getSalt());
        buyOrder.setSalt(salt);
        String signature = OrderUtil.getSignature(OrderUtil.getSerializedOrder(buyOrder), subKeyPair);
        buyOrder.setOrderSignature(signature);
        
        String orderId = httpClient.placeOrder(buyOrder);
        System.out.println("Buy order placed: " + orderId);
        
        // Monitor positions
        List<PositionResponse> positions = httpClient.positions();
        positions.forEach(pos -> {
            System.out.println("Position: " + pos.getSymbol() + " " + pos.getSide());
            System.out.println("P&L: " + pos.getUnrealizedProfit());
        });
        
        // Place sell order to close position
        PlaceOrderRequest sellOrder = new PlaceOrderRequest()
                .setSymbol("BTC-PERP")
                .setMarket(perpId)
                // quantity 2 BTC (18 decimals)
                .setQuantity(DecimalUtil.toBaseUnit(new BigInteger("2")))
                .setSide(OrderSide.SELL.getCode())
                .setOrderType(OrderType.MARKET.getCode())
                // leverage 5x (18 decimals)
                .setLeverage(DecimalUtil.toBaseUnit(new BigInteger("5")))
                .setReduceOnly(true)
                .setCreator(mainKeyPair.address());
        
        String sellSalt = new String(OrderUtil.getSalt());
        sellOrder.setSalt(sellSalt);
        String sellSignature = OrderUtil.getSignature(OrderUtil.getSerializedOrder(sellOrder), subKeyPair);
        sellOrder.setOrderSignature(sellSignature);
        
        String sellOrderId = httpClient.placeOrder(sellOrder);
        System.out.println("Sell order placed: " + sellOrderId);
        
        // Withdraw profits (6 decimals)
        BigInteger withdrawAmount = new BigInteger("1000000000");
        onChainClient.withdraw(mainKeyPair, withdrawAmount, 1000L, BigInteger.TEN.pow(8));
    }
}
```

### Market Making Example

```java
public class MarketMakingExample {
    public static void main(String[] args) {
        PerpClient client = new PerpHttpClient(PerpNetwork.TESTNET, mainKeyPair, subKeyPair);
        String perpId = client.getMarketPerpId("BTC-PERP");
        
        // Get current market price
        TickerResponse ticker = client.ticker(new SymbolRequest().setSymbol("BTC-PERP"));
        BigInteger lastPrice = new BigInteger(ticker.getLastPrice());
        
        // Calculate bid/ask prices (0.1% spread)
        BigInteger bidPrice = lastPrice.multiply(new BigInteger("9995")).divide(new BigInteger("10000"));
        BigInteger askPrice = lastPrice.multiply(new BigInteger("10005")).divide(new BigInteger("10000"));
        
        BigInteger quantity = DecimalUtil.toBaseUnit(new BigInteger("10")); // 10 unit
        
        // Place bid order
        PlaceOrderRequest bidOrder = createOrder(perpId, bidPrice, quantity, OrderSide.BUY, mainKeyPair.address());
        String bidSalt = new String(OrderUtil.getSalt());
        bidOrder.setSalt(bidSalt);
        bidOrder.setOrderSignature(OrderUtil.getSignature(OrderUtil.getSerializedOrder(bidOrder), subKeyPair));
        client.placeOrder(bidOrder);
        
        // Place ask order
        PlaceOrderRequest askOrder = createOrder(perpId, askPrice, quantity, OrderSide.SELL, mainKeyPair.address());
        String askSalt = new String(OrderUtil.getSalt());
        askOrder.setSalt(askSalt);
        askOrder.setOrderSignature(OrderUtil.getSignature(OrderUtil.getSerializedOrder(askOrder), subKeyPair));
        client.placeOrder(askOrder);
    }
    
    private static PlaceOrderRequest createOrder(String perpId, BigInteger price, BigInteger quantity, OrderSide side, String creator) {
        return new PlaceOrderRequest()
                .setSymbol("BTC-PERP")
                .setMarket(perpId)
                .setPrice(price)
                .setQuantity(quantity)
                .setSide(side.getCode())
                .setOrderType(OrderType.LIMIT.getCode())
                .setLeverage(DecimalUtil.toBaseUnit(new BigInteger("1")))
                .setReduceOnly(false)
                .setCreator(creator)
                .setClientId("mm_" + System.currentTimeMillis());
    }
}
```

## Error Handling

The library uses custom exceptions for error handling:

### Exception Types

```java
try {
    String orderId = client.placeOrder(request);
} catch (PerpHttpException e) {
    // HTTP API errors
    System.err.println("HTTP error: " + e.getMessage());
} catch (PerpRpcFailedException e) {
    // Blockchain RPC errors
    System.err.println("RPC failed: " + e.getMessage());
} catch (PerpOnChainException e) {
    // On-chain transaction errors
    System.err.println("On-chain error: " + e.getMessage());
} catch (PerpJsonParseException e) {
    // JSON parsing errors
    System.err.println("Parse error: " + e.getMessage());
}
```

### Error Codes

Common error codes from `ErrorCode` enum:
- `SUCCESS (0)`: Operation successful
- `INVALID_PARAMS`: Invalid request parameters
- `UNAUTHORIZED`: Authentication failed
- `NOT_FOUND`: Resource not found
- `RATE_LIMIT`: Rate limit exceeded
- `INSUFFICIENT_BALANCE`: Insufficient funds
- `POSITION_NOT_FOUND`: Position not found

## Best Practices

### Security

1. **Use Dual Account Mode**: Keep main account offline, use sub account for trading
2. **Store Keys Securely**: Never hardcode private keys
3. **Validate Inputs**: Always validate order parameters before submission
4. **Monitor Positions**: Regularly check position status and liquidation prices

### Performance

1. **Reuse Clients**: Create client instances once and reuse them
2. **Batch Operations**: Use pagination for large data sets
3. **Handle Rate Limits**: Implement exponential backoff for rate limit errors
4. **Cache Market Data**: Cache trading pair information to reduce API calls

### Trading

1. **Check Free Collateral**: Ensure sufficient margin before placing orders
2. **Use Reduce-Only**: Set `reduceOnly=true` for closing orders
3. **Monitor Funding**: Track funding rates and settlements
4. **Set Appropriate Leverage**: Use conservative leverage for risk management
5. **Use Client IDs**: Implement unique client IDs for order tracking

### Development

1. **Test on Testnet**: Always test thoroughly on testnet before mainnet
2. **Log Transactions**: Keep logs of all transaction digests
3. **Handle Errors Gracefully**: Implement proper error handling and retries
4. **Version Compatibility**: Keep SDK updated to latest version

### Gas Management

```java
// Get dynamic gas price
SuiClient suiClient = SuiClient.build(new HttpService(networkConfig.suiRpc()));
Long gasPrice = suiClient.getReferenceGasPrice();

// Use appropriate gas budget
BigInteger gasBudget = BigInteger.TEN.pow(8); // 0.1 SUI (typical for most operations)
BigInteger largeGasBudget = BigInteger.TEN.pow(9); // 1 SUI (for complex operations)
```

### Precision Handling

```java
// USDC has 6 decimals
BigInteger usdcAmount = new BigInteger("1000000"); // 1 USDC

// Position quantities use 18 decimals
BigInteger quantity = DecimalUtil.toBaseUnit(new BigInteger("1")); // 1 unit

// Prices use 18 decimals (USDC)
BigInteger price = DecimalUtil.toBaseUnit(new BigInteger("50000")); // 50000 USDC

// Leverage use 18 decimals
BigInteger price = DecimalUtil.toBaseUnit(new BigInteger("5")); // leverage 5x

// Always use BigInteger for financial calculations
// Never use floating point for money
```

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

---

**Note**: This SDK is under active development. APIs may change in future versions. Always refer to the latest documentation and test thoroughly before production use.

For support and questions, please open an issue on the GitHub repository.

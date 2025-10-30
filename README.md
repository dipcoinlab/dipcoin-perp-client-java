# dipcoin-perp-client-java

Java Implementation of the Dipcoin Perpetual Trading Client Library

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

The Dipcoin Perpetual Client Library provides a modular Java SDK for interacting with the Dipcoin perpetual futures trading platform. The library is divided into specialized modules for different use cases, supporting both HTTP API operations and on-chain blockchain transactions.

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
    <artifactId>sui-perp-client-java</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle

```gradle
implementation 'io.dipcoin:sui-perp-client-java:1.0.0'
```

## Quick Start

Complete example demonstrating the modular client initialization and basic operations:

```java
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

public class QuickStartExample {
    public static void main(String[] args) {
        // 1. Initialize keypairs
        // There are three methods to construct a private key pair: `suiPrivKey`, mnemonic phrase, and hexadecimal private key.
//        SuiKeyPair mainKeyPair = SuiKeyPair.decodeSuiPrivateKey("suiprivKeyxxxx");
//        SuiKeyPair mainKeyPair = Ed25519KeyPair.deriveKeypair("mnemonics", null);
        SuiKeyPair mainKeyPair = Ed25519KeyPair.decodeHex("main_private_key_hex");
        SuiKeyPair subKeyPair = Ed25519KeyPair.decodeHex("sub_private_key_hex");
        
        // 2. Create unified HTTP client
        PerpHttpClient httpClient = new PerpHttpClient(PerpNetwork.TESTNET, mainKeyPair, subKeyPair);
        
        // 3. Create on-chain client for blockchain operations
        PerpOnSignClient onChainClient = new PerpOnSignClient(PerpNetwork.TESTNET);
        
        // 4. Set sub account (one-time setup)
        onChainClient.setSubAccount(
            mainKeyPair,
            subKeyPair.address(),
            1000L,
            DecimalUtil.toSui(new BigDecimal("0.1"))
        );
        
        // 5. Deposit funds - USDC use 6 decimal precision
        BigInteger depositAmount = new BigDecimal("1000").multiply(BigDecimal.TEN.pow(6)); // 1000 USDC
        onChainClient.deposit(mainKeyPair, depositAmount, 1000L, DecimalUtil.toSui(new BigDecimal("0.1")));
        
        // 6. Get market info
        String perpId = httpClient.getMarketPerpId("BTC-PERP");
        
        // 7. Place order
        PlaceOrderRequest orderRequest = new PlaceOrderRequest()
                .setSymbol("BTC-PERP")
                .setMarket(perpId)
                .setPrice(DecimalUtil.toBaseUnit(new BigDecimal("50000")))
                .setQuantity(DecimalUtil.toBaseUnit(new BigDecimal("1")))
                .setSide(OrderSide.BUY.getCode())
                .setOrderType(OrderType.LIMIT.getCode())
                .setLeverage(DecimalUtil.toBaseUnit(new BigInteger("10")))
                .setReduceOnly(false)
                .setCreator(mainKeyPair.address())
                .setClientId("order_001");
        
        // Sign with sub account
        String salt = new String(OrderUtil.getSalt());
        orderRequest.setSalt(salt);
        String signature = OrderUtil.getSignature(OrderUtil.getSerializedOrder(orderRequest), subKeyPair);
        orderRequest.setOrderSignature(signature);
        
        String orderId = httpClient.placeOrder(orderRequest);
        System.out.println("Order placed: " + orderId);
    }
}
```

## Off-Chain API Modules

### PerpTradeClient

**Purpose**: Handles all trading operations including order placement and cancellation.

**Security Model**: Uses sub account authentication to protect main account funds. The sub account can only execute trading operations (place/cancel orders) and cannot access or withdraw funds. This separation ensures that even if the sub account private key is compromised, the main account's assets remain secure.

#### Initialization

```java
import io.dipcoin.sui.perp.client.PerpTradeClient;
import io.dipcoin.sui.perp.client.auth.AuthSession;
import io.dipcoin.sui.perp.client.core.PerpAuthorization;
import io.dipcoin.sui.perp.enums.PerpNetwork;

// Create authorization and get auth session
PerpAuthorization perpAuth = new PerpAuthorization(PerpNetwork.TESTNET);
AuthSession subAuth = perpAuth.authorize(subKeyPair);

// Initialize trade client with sub account auth
PerpTradeClient tradeClient = new PerpTradeClient(PerpNetwork.TESTNET, subAuth);
```

#### Place Order

```java
import io.dipcoin.sui.perp.enums.OrderSide;
import io.dipcoin.sui.perp.enums.OrderType;
import io.dipcoin.sui.perp.model.request.PlaceOrderRequest;
import io.dipcoin.sui.perp.util.DecimalUtil;
import io.dipcoin.sui.perp.util.OrderUtil;

import java.math.BigDecimal;
import java.math.BigInteger;

// Get market perp ID first
String perpId = marketClient.getMarketPerpId("BTC-PERP");

// Create order request with 18 decimal precision
PlaceOrderRequest request = new PlaceOrderRequest()
        .setSymbol("BTC-PERP")
        .setMarket(perpId)
        .setPrice(DecimalUtil.toBaseUnit(new BigDecimal("50000")))      // 50000 USDC
        .setQuantity(DecimalUtil.toBaseUnit(new BigDecimal("2")))       // 2 BTC
        .setSide(OrderSide.BUY.getCode())
        .setOrderType(OrderType.LIMIT.getCode())
        .setLeverage(DecimalUtil.toBaseUnit(new BigInteger("10")))      // 10x leverage
        .setReduceOnly(false)
        .setCreator(mainAddress)
        .setClientId("unique_client_id");

// Sign order with sub account
String salt = new String(OrderUtil.getSalt());
request.setSalt(salt);
String signature = OrderUtil.getSignature(OrderUtil.getSerializedOrder(request), subKeyPair);
request.setOrderSignature(signature);

// Place order
String orderId = tradeClient.placeOrder(request);
System.out.println("Order ID: " + orderId);
```

**Market Order Example:**

```java
PlaceOrderRequest marketOrder = new PlaceOrderRequest()
        .setSymbol("BTC-PERP")
        .setMarket(perpId)
        .setQuantity(DecimalUtil.toBaseUnit(new BigDecimal("1")))  // 1 BTC
        .setSide(OrderSide.SELL.getCode())
        .setOrderType(OrderType.MARKET.getCode())
        .setLeverage(DecimalUtil.toBaseUnit(new BigInteger("5")))
        .setReduceOnly(false)
        .setCreator(mainAddress)
        .setClientId("market_order_001");

String salt = new String(OrderUtil.getSalt());
marketOrder.setSalt(salt);
marketOrder.setOrderSignature(OrderUtil.getSignature(OrderUtil.getSerializedOrder(marketOrder), subKeyPair));

String orderId = tradeClient.placeOrder(marketOrder);
```

#### Cancel Order

```java
import io.dipcoin.sui.perp.model.request.CancelOrderRequest;
import io.dipcoin.sui.perp.model.response.CancelOrderResponse;

CancelOrderRequest request = new CancelOrderRequest()
        .setOrderId("order_id_to_cancel")
        .setSymbol("BTC-PERP");

CancelOrderResponse response = tradeClient.cancelOrder(request);
System.out.println("Cancelled order: " + response.getOrderId());
```

---

### PerpUserClient

**Purpose**: Manages user account data including positions, orders, balance history, and funding settlements. Requires main account authorization to access sensitive account information.

#### Initialization

```java
import io.dipcoin.sui.perp.client.PerpUserClient;
import io.dipcoin.sui.perp.client.auth.AuthSession;
import io.dipcoin.sui.perp.client.core.PerpAuthorization;

// Create authorization with main account
PerpAuthorization perpAuth = new PerpAuthorization(PerpNetwork.TESTNET);
AuthSession mainAuth = perpAuth.authorize(mainKeyPair);

// Initialize user client
PerpUserClient userClient = new PerpUserClient(PerpNetwork.TESTNET, mainAuth);
```

#### Get Account Information

```java
import io.dipcoin.sui.perp.model.response.AccountResponse;

AccountResponse account = userClient.account();
System.out.println("Wallet balance: " + account.getWalletBalance());
System.out.println("Free collateral: " + account.getFreeCollateral());
System.out.println("Total position margin: " + account.getTotalPositionMargin());
System.out.println("Total unrealized profit: " + account.getTotalUnrealizedProfit());
System.out.println("Account value: " + account.getAccountValue());
```

#### Get Positions

```java
import io.dipcoin.sui.perp.model.response.PositionResponse;
import java.util.List;

List<PositionResponse> positions = userClient.positions();
for (PositionResponse position : positions) {
    System.out.println("Symbol: " + position.getSymbol());
    System.out.println("Side: " + position.getSide());
    System.out.println("Quantity: " + position.getQuantity());
    System.out.println("Entry price: " + position.getAvgEntryPrice());
    System.out.println("Unrealized P&L: " + position.getUnrealizedProfit());
    System.out.println("Liquidation price: " + position.getLiquidationPrice());
}
```

#### Get Active Orders

```java
import io.dipcoin.sui.perp.model.PageResponse;
import io.dipcoin.sui.perp.model.request.OrdersRequest;
import io.dipcoin.sui.perp.model.response.OrdersResponse;

OrdersRequest request = new OrdersRequest()
        .setSymbol("BTC-PERP")  // Optional filter
        .setPage(1)
        .setPageSize(20);

PageResponse<OrdersResponse> orders = userClient.orders(request);
System.out.println("Total orders: " + orders.getTotal());
```

#### Get Order History

```java
import io.dipcoin.sui.perp.model.request.HistoryOrdersRequest;
import io.dipcoin.sui.perp.model.response.HistoryOrdersResponse;

HistoryOrdersRequest request = new HistoryOrdersRequest()
        .setSymbol("BTC-PERP")
        .setPage(1)
        .setPageSize(20);

PageResponse<HistoryOrdersResponse> history = userClient.historyOrders(request);
```

#### Get Funding Settlements

```java
import io.dipcoin.sui.perp.model.request.PageRequest;
import io.dipcoin.sui.perp.model.response.FundingSettlementsResponse;

PageRequest request = new PageRequest()
        .setPage(1)
        .setPageSize(20);

PageResponse<FundingSettlementsResponse> settlements = userClient.fundingSettlements(request);
```

#### Get Balance Changes

```java
import io.dipcoin.sui.perp.model.response.BalanceChangesResponse;

PageRequest request = new PageRequest()
        .setPage(1)
        .setPageSize(20);

PageResponse<BalanceChangesResponse> changes = userClient.balanceChanges(request);
```

---

### PerpMarketClient

**Purpose**: Provides public market data without requiring authentication. Access real-time ticker information, order books, oracle prices, and trading pair details.

#### Initialization

```java
import io.dipcoin.sui.perp.client.PerpMarketClient;
import io.dipcoin.sui.perp.enums.PerpNetwork;

// No authentication required for market data
PerpMarketClient marketClient = new PerpMarketClient(PerpNetwork.TESTNET);
```

#### Get Trading Pairs

```java
import io.dipcoin.sui.perp.model.response.TradingPairResponse;
import java.util.List;

List<TradingPairResponse> pairs = marketClient.tradingPair();
for (TradingPairResponse pair : pairs) {
    System.out.println("Symbol: " + pair.getSymbol());
    System.out.println("Perp ID: " + pair.getPerpId());
    System.out.println("Max leverage: " + pair.getMaxLeverage());
    System.out.println("Maker fee: " + pair.getMakerFee());
    System.out.println("Taker fee: " + pair.getTakerFee());
    System.out.println("Step size: " + pair.getStepSize());
    System.out.println("Tick size: " + pair.getTickSize());
}
```

#### Get Market Perp ID

```java
// Required when placing orders
String perpId = marketClient.getMarketPerpId("BTC-PERP");
System.out.println("Market Perp ID: " + perpId);
```

**Note**: The perp ID is cached internally for performance. The first call fetches all trading pairs, subsequent calls use the cache.

#### Get Pyth Feed ID

```java
// Used for oracle price updates in on-chain operations
String feedId = marketClient.getPythFeedId("BTC-PERP");
System.out.println("Pyth Feed ID: " + feedId);
```

#### Get Ticker

```java
import io.dipcoin.sui.perp.model.request.SymbolRequest;
import io.dipcoin.sui.perp.model.response.TickerResponse;

SymbolRequest request = new SymbolRequest().setSymbol("BTC-PERP");
TickerResponse ticker = marketClient.ticker(request);
System.out.println("Last price: " + ticker.getLastPrice());
System.out.println("24h high: " + ticker.getHigh24h());
System.out.println("24h low: " + ticker.getLow24h());
System.out.println("24h volume: " + ticker.getVolume24h());
System.out.println("Price change 24h: " + ticker.getPriceChange24h());
```

#### Get Order Book

```java
import io.dipcoin.sui.perp.model.response.OrderBookResponse;

SymbolRequest request = new SymbolRequest().setSymbol("BTC-PERP");
OrderBookResponse orderBook = marketClient.orderBook(request);

System.out.println("Bids (sorted descending):");
orderBook.getBids().forEach(bid -> 
    System.out.println("Price: " + bid.getPrice() + ", Qty: " + bid.getQuantity())
);

System.out.println("Asks (sorted ascending):");
orderBook.getAsks().forEach(ask -> 
    System.out.println("Price: " + ask.getPrice() + ", Qty: " + ask.getQuantity())
);
```

#### Get Oracle Price

```java
import java.math.BigInteger;

SymbolRequest request = new SymbolRequest().setSymbol("BTC-PERP");
BigInteger oraclePrice = marketClient.oracle(request);
System.out.println("Oracle price: " + oraclePrice);
```

---

### PerpHttpClient

**Purpose**: Unified HTTP client that aggregates all off-chain API modules (PerpTradeClient, PerpUserClient, PerpMarketClient). Provides a single entry point for all HTTP API operations with automatic authorization management.

#### Initialization

```java
import io.dipcoin.sui.perp.client.PerpHttpClient;
import io.dipcoin.sui.crypto.SuiKeyPair;
import io.dipcoin.sui.perp.enums.PerpNetwork;

SuiKeyPair mainKeyPair = Ed25519KeyPair.decodeHex("main_private_key");
SuiKeyPair subKeyPair = Ed25519KeyPair.decodeHex("sub_private_key");

// Automatically authorizes both accounts on initialization
PerpHttpClient client = new PerpHttpClient(PerpNetwork.TESTNET, mainKeyPair, subKeyPair);

// Access account information
String mainAddress = client.getMainAddress();
String subAddress = client.getSubAddress();
SuiKeyPair mainAccount = client.getMainAccount();
SuiKeyPair subAccount = client.getSubAccount();
```

#### Features

- **Automatic Authorization**: Both main and sub accounts are authorized during initialization
- **Unified Interface**: Access all API operations through a single client instance
- **Internal Module Management**: Automatically delegates calls to appropriate specialized clients

#### Usage Examples

**Trading Operations** (uses PerpTradeClient internally):
```java
// Place order
String orderId = client.placeOrder(orderRequest);

// Cancel order
CancelOrderResponse response = client.cancelOrder(cancelRequest);
```

**User Data Operations** (uses PerpUserClient internally):
```java
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
```java
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

```java
import io.dipcoin.sui.perp.client.PerpOnSignClient;
import io.dipcoin.sui.perp.enums.PerpNetwork;
import io.dipcoin.sui.protocol.SuiClient;
import io.dipcoin.sui.protocol.http.HttpService;

// Basic initialization
PerpOnSignClient onChainClient = new PerpOnSignClient(PerpNetwork.TESTNET);

// With custom SuiClient
SuiClient suiClient = SuiClient.build(new HttpService("https://fullnode.testnet.sui.io:443"));
PerpOnSignClient onChainClient = new PerpOnSignClient(suiClient, PerpNetwork.TESTNET);

// With custom market client
PerpMarketClient marketClient = new PerpMarketClient(PerpNetwork.TESTNET);
PerpOnSignClient onChainClient = new PerpOnSignClient(suiClient, PerpNetwork.TESTNET, marketClient);
```

#### Set Sub Account

Bind sub account to main account on-chain (one-time setup required before trading with sub account).

```java
import io.dipcoin.sui.model.transaction.SuiTransactionBlockResponse;
import io.dipcoin.sui.perp.util.DecimalUtil;

import java.math.BigDecimal;

String subAddress = subKeyPair.address();
long gasPrice = 1000L;
BigDecimal gasBudget = DecimalUtil.toSui(new BigDecimal("0.1")); // 0.1 SUI

SuiTransactionBlockResponse response = onChainClient.setSubAccount(
    mainKeyPair,
    subAddress,
    gasPrice,
    gasBudget
);

System.out.println("Transaction digest: " + response.getDigest());
System.out.println("Status: " + response.getEffects().getStatus().getStatus());
```

#### Deposit

Deposit USDC into trading account. All numerical values use 18 decimal precision.

```java
import java.math.BigDecimal;

// Deposit 1000 USDC
BigDecimal depositAmount = new BigDecimal("1000").multiply(BigInteger.TEN.pow(6));
long gasPrice = 1000L;
BigDecimal gasBudget = DecimalUtil.toSui(new BigDecimal("0.1"));

SuiTransactionBlockResponse response = onChainClient.deposit(
    mainKeyPair,
    depositAmount,
    gasPrice,
    gasBudget
);

System.out.println("Deposit transaction: " + response.getDigest());
```

#### Withdraw

Withdraw USDC from trading account.

```java
// Withdraw 500 USDC
BigDecimal withdrawAmount = new BigDecimal("500").multiply(BigInteger.TEN.pow(6));
long gasPrice = 1000L;
BigDecimal gasBudget = DecimalUtil.toSui(new BigDecimal("0.1"));

SuiTransactionBlockResponse response = onChainClient.withdraw(
    mainKeyPair,
    withdrawAmount,
    gasPrice,
    DecimalUtil.toSui(gasBudget)
);

System.out.println("Withdraw transaction: " + response.getDigest());
```

#### Add Margin

Add margin to an existing position.

```java
String symbol = "BTC-PERP";
String subAddress = subKeyPair.address();
BigDecimal marginAmount = new BigDecimal("100").multiply(BigInteger.TEN.pow(6)); // 100 USDC
long gasPrice = 1000L;
BigDecimal gasBudget = DecimalUtil.toSui(new BigDecimal("0.1"));

SuiTransactionBlockResponse response = onChainClient.addMargin(
    mainKeyPair,
    subAddress,
    symbol,
    marginAmount,
    gasPrice,
    gasBudget
);

System.out.println("Add margin transaction: " + response.getDigest());
```

---

### PerpOffSignClient

**Purpose**: Handles on-chain operations with external wallet integration. Designed for scenarios where private keys are managed by external wallet systems (hardware wallets, wallet SDKs, custody solutions). Requires implementing the `WalletService` interface.

#### WalletService Interface

You must implement this interface to integrate with your wallet system:

```java
import io.dipcoin.sui.perp.client.chain.WalletService;

public class MyWalletService implements WalletService {
    
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

```java
import io.dipcoin.sui.perp.client.PerpOffSignClient;
import io.dipcoin.sui.perp.client.PerpMarketClient;
import io.dipcoin.sui.perp.client.chain.WalletService;
import io.dipcoin.sui.perp.enums.PerpNetwork;

// Implement wallet service
WalletService walletService = new MyWalletService();

// Initialize market client
PerpMarketClient marketClient = new PerpMarketClient(PerpNetwork.TESTNET);

// Create off-sign client
PerpOffSignClient offSignClient = new PerpOffSignClient(
    PerpNetwork.TESTNET,
    marketClient,
    walletService
);

// With custom SuiClient
SuiClient suiClient = SuiClient.build(new HttpService("https://fullnode.testnet.sui.io:443"));
PerpOffSignClient offSignClient = new PerpOffSignClient(
    suiClient,
    PerpNetwork.TESTNET,
    marketClient,
    walletService
);
```

#### Set Sub Account

```java
String sender = "0x..."; // Main account address
String subAddress = "0x..."; // Sub account address
long gasPrice = 1000L;
BigDecimal gasBudget = new BigDecimal("0.1");

SuiTransactionBlockResponse response = offSignClient.setSubAccount(
    sender,
    subAddress,
    gasPrice,
    DecimalUtil.toBaseUnit(gasBudget)
);
```

#### Deposit

```java
String sender = "0x..."; // Main account address
BigDecimal depositAmount = new BigDecimal("1000");

SuiTransactionBlockResponse response = offSignClient.deposit(
    sender,
    DecimalUtil.toBaseUnit(depositAmount),
    gasPrice,
    DecimalUtil.toBaseUnit(gasBudget)
);
```

#### Withdraw

```java
String sender = "0x..."; // Main account address
BigDecimal withdrawAmount = new BigDecimal("500");

SuiTransactionBlockResponse response = offSignClient.withdraw(
    sender,
    DecimalUtil.toBaseUnit(withdrawAmount),
    gasPrice,
    DecimalUtil.toBaseUnit(gasBudget)
);
```

#### Add Margin

```java
String sender = "0x..."; // Main account address
String subAddress = "0x..."; // Sub account address
String symbol = "BTC-PERP";
BigDecimal marginAmount = new BigDecimal("100");

SuiTransactionBlockResponse response = offSignClient.addMargin(
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

```java
public class PlaceOrderRequest {
    private String symbol;           // Trading pair (e.g., "BTC-PERP")
    private String market;           // Market perp ID from getMarketPerpId()
    private BigInteger price;        // Order price (18 decimals)
    private BigInteger quantity;     // Order quantity (18 decimals)
    private String side;             // OrderSide: BUY, SELL
    private String orderType;        // OrderType: LIMIT, MARKET
    private BigInteger leverage;     // Leverage multiplier
    private Boolean reduceOnly;      // Reduce-only flag
    private String salt;             // Random salt
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
    private String walletBalance;              // Available balance (18 decimals)
    private String totalPositionMargin;        // Total position margin (18 decimals)
    private String totalUnrealizedProfit;      // Total unrealized P&L (18 decimals)
    private String freeCollateral;             // Available margin (18 decimals)
    private String accountValue;               // Total account value (18 decimals)
    private String feeTier;                    // Fee tier
    private List<AccountDataByMarketResponse> accountDataByMarket;
}
```

### PositionResponse

```java
public class PositionResponse {
    private String symbol;              // Trading pair
    private String side;                // Position side (BUY/SELL)
    private String quantity;            // Position size (18 decimals)
    private String avgEntryPrice;       // Average entry price (18 decimals)
    private String margin;              // Position margin (18 decimals)
    private String leverage;            // Current leverage
    private String positionValue;       // Position value (18 decimals)
    private String unrealizedProfit;    // Unrealized P&L (18 decimals)
    private String roe;                 // Return on equity (%)
    private String liquidationPrice;    // Liquidation price (18 decimals)
    private String oraclePrice;         // Oracle price (18 decimals)
    private String fundingDue;          // Pending funding (18 decimals)
}
```

### TradingPairResponse

```java
public class TradingPairResponse {
    private String perpId;              // Perpetual market ID
    private String symbol;              // Trading pair symbol
    private String coinName;            // Base coin name
    private Integer status;             // Market status
    private String initialMargin;       // Initial margin requirement (18 decimals)
    private String maintenanceMargin;   // Maintenance margin requirement (18 decimals)
    private String makerFee;            // Maker fee rate (18 decimals)
    private String takerFee;            // Taker fee rate (18 decimals)
    private String stepSize;            // Quantity step size (18 decimals)
    private String tickSize;            // Price tick size (18 decimals)
    private String maxQtyLimit;         // Max quantity for limit orders (18 decimals)
    private String maxQtyMarket;        // Max quantity for market orders (18 decimals)
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
    LIMIT("LIMIT", 1),      // Limit order
    MARKET("MARKET", 2),    // Market order
    LIQ("Liquidation", 3),  // Liquidation order
    ADL("ADL", 4);          // Auto-deleveraging order
}
```

#### PerpNetwork
```java
public enum PerpNetwork {
    MAINNET,  // Production environment
    TESTNET;  // Test environment
}
```

---

## Utilities

### DecimalUtil

All numerical values in the Dipcoin Perpetual system use **18 decimal places precision**. The `DecimalUtil` class provides conversion methods between human-readable values and base unit values.

#### Convert to Base Unit (18 decimals)

```java
import io.dipcoin.sui.perp.util.DecimalUtil;
import java.math.BigDecimal;
import java.math.BigInteger;

// From String
BigInteger amount1 = DecimalUtil.toBaseUnit("1000.5");

// From BigDecimal
BigDecimal value = new BigDecimal("1000.5");
BigInteger amount2 = DecimalUtil.toBaseUnit(value);

// From BigInteger (multiplies by 10^18)
BigInteger value3 = new BigInteger("1000");
BigInteger amount3 = DecimalUtil.toBaseUnit(value3);

// Get base unit constants
BigInteger baseUnit = DecimalUtil.getBaseUintInteger();  // 10^18
BigDecimal baseUnitDecimal = DecimalUtil.getBaseUintDecimal();  // 10^18
BigInteger halfBaseUnit = DecimalUtil.getHalfBaseUint();  // 0.5 * 10^18
```

#### Convert from Base Unit

```java
// Convert 18 decimal base unit to human-readable value
BigInteger baseUnitValue = new BigInteger("1500000000000000000000"); // 1500 * 10^18
BigDecimal readableValue = DecimalUtil.fromBaseUnit(baseUnitValue);  // 1500.000000000000000000
```

#### Arithmetic Operations

```java
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

```java
import io.dipcoin.sui.perp.util.OrderUtil;

byte[] salt = OrderUtil.getSalt();
String saltString = new String(salt);
```

#### Serialize Order

```java
PlaceOrderRequest request = new PlaceOrderRequest()
        .setSymbol("BTC-PERP")
        .setMarket(perpId)
        .setPrice(DecimalUtil.toBaseUnit(new BigDecimal("50000")))
        .setQuantity(DecimalUtil.toBaseUnit(new BigDecimal("1")))
        .setSide(OrderSide.BUY.getCode())
        .setOrderType(OrderType.LIMIT.getCode())
        .setLeverage(new BigInteger("10"))
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

---

## Examples

### Complete Trading Flow

```java
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

public class TradingFlowExample {
    public static void main(String[] args) {
        // Initialize
        SuiKeyPair mainKeyPair = Ed25519KeyPair.decodeHex("main_key");
        SuiKeyPair subKeyPair = Ed25519KeyPair.decodeHex("sub_key");
        
        PerpHttpClient httpClient = new PerpHttpClient(PerpNetwork.TESTNET, mainKeyPair, subKeyPair);
        PerpOnSignClient onChainClient = new PerpOnSignClient(PerpNetwork.TESTNET);
        
        // Setup (one-time)
        onChainClient.setSubAccount(
            mainKeyPair,
            subKeyPair.address(),
            1000L,
            DecimalUtil.toBaseUnit(new BigDecimal("0.1"))
        );
        
        // Deposit funds
        onChainClient.deposit(
            mainKeyPair,
            new BigDecimal("10000").multiply(BigInteger.TEN.pow(6)),
            1000L,
            DecimalUtil.toBaseUnit(new BigDecimal("0.1"))
        );
        
        // Check balance
        AccountResponse account = httpClient.account();
        BigDecimal freeCollateral = DecimalUtil.fromBaseUnit(new BigInteger(account.getFreeCollateral()));
        System.out.println("Free collateral: " + freeCollateral);
        
        // Get market info
        String perpId = httpClient.getMarketPerpId("BTC-PERP");
        
        // Place buy order
        PlaceOrderRequest buyOrder = new PlaceOrderRequest()
                .setSymbol("BTC-PERP")
                .setMarket(perpId)
                .setPrice(DecimalUtil.toBaseUnit(new BigDecimal("45000")))
                .setQuantity(DecimalUtil.toBaseUnit(new BigDecimal("2")))
                .setSide(OrderSide.BUY.getCode())
                .setOrderType(OrderType.LIMIT.getCode())
                .setLeverage(DecimalUtil.toBaseUnit(new BigInteger("5")))
                .setReduceOnly(false)
                .setCreator(mainKeyPair.address())
                .setClientId("buy_001");
        
        String salt = new String(OrderUtil.getSalt());
        buyOrder.setSalt(salt);
        String signature = OrderUtil.getSignature(OrderUtil.getSerializedOrder(buyOrder), subKeyPair);
        buyOrder.setOrderSignature(signature);
        
        String orderId = httpClient.placeOrder(buyOrder);
        System.out.println("Buy order placed: " + orderId);
        
        // Monitor positions
        List<PositionResponse> positions = httpClient.positions();
        positions.forEach(pos -> {
            BigDecimal pnl = DecimalUtil.fromBaseUnit(new BigInteger(pos.getUnrealizedProfit()));
            System.out.println("Position: " + pos.getSymbol() + " " + pos.getSide());
            System.out.println("P&L: " + pnl);
        });
        
        // Close position with market order
        PlaceOrderRequest sellOrder = new PlaceOrderRequest()
                .setSymbol("BTC-PERP")
                .setMarket(perpId)
                .setQuantity(DecimalUtil.toBaseUnit(new BigDecimal("2")))
                .setSide(OrderSide.SELL.getCode())
                .setOrderType(OrderType.MARKET.getCode())
                .setLeverage(DecimalUtil.toBaseUnit(new BigInteger("5")))
                .setReduceOnly(true)
                .setCreator(mainKeyPair.address())
                .setClientId("sell_001");
        
        String sellSalt = new String(OrderUtil.getSalt());
        sellOrder.setSalt(sellSalt);
        sellOrder.setOrderSignature(OrderUtil.getSignature(OrderUtil.getSerializedOrder(sellOrder), subKeyPair));
        
        String sellOrderId = httpClient.placeOrder(sellOrder);
        System.out.println("Sell order placed: " + sellOrderId);
        
        // Withdraw profits
        BigDecimal withdrawAmount = new BigDecimal("1000");
        onChainClient.withdraw(
            mainKeyPair,
            DecimalUtil.toBaseUnit(withdrawAmount),
            1000L,
            DecimalUtil.toBaseUnit(new BigDecimal("0.1"))
        );
    }
}
```

### Market Making Example

```java
public class MarketMakingExample {
    public static void main(String[] args) {
        PerpHttpClient client = new PerpHttpClient(PerpNetwork.TESTNET, mainKeyPair, subKeyPair);
        String perpId = client.getMarketPerpId("BTC-PERP");
        
        // Get current market price
        TickerResponse ticker = client.ticker(new SymbolRequest().setSymbol("BTC-PERP"));
        BigDecimal lastPrice = DecimalUtil.fromBaseUnit(new BigInteger(ticker.getLastPrice()));
        
        // Calculate bid/ask prices (0.1% spread)
        BigDecimal bidPrice = lastPrice.multiply(new BigDecimal("0.9995"));
        BigDecimal askPrice = lastPrice.multiply(new BigDecimal("1.0005"));
        
        BigDecimal quantity = new BigDecimal("1");
        
        // Place bid order
        PlaceOrderRequest bidOrder = createOrder(
            perpId,
            DecimalUtil.toBaseUnit(bidPrice),
            DecimalUtil.toBaseUnit(quantity),
            OrderSide.BUY,
            mainKeyPair.address()
        );
        String bidSalt = new String(OrderUtil.getSalt());
        bidOrder.setSalt(bidSalt);
        bidOrder.setOrderSignature(OrderUtil.getSignature(OrderUtil.getSerializedOrder(bidOrder), subKeyPair));
        client.placeOrder(bidOrder);
        
        // Place ask order
        PlaceOrderRequest askOrder = createOrder(
            perpId,
            DecimalUtil.toBaseUnit(askPrice),
            DecimalUtil.toBaseUnit(quantity),
            OrderSide.SELL,
            mainKeyPair.address()
        );
        String askSalt = new String(OrderUtil.getSalt());
        askOrder.setSalt(askSalt);
        askOrder.setOrderSignature(OrderUtil.getSignature(OrderUtil.getSerializedOrder(askOrder), subKeyPair));
        client.placeOrder(askOrder);
    }
    
    private static PlaceOrderRequest createOrder(
        String perpId, BigInteger price, BigInteger quantity,
        OrderSide side, String creator
    ) {
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

### Using Modular Clients

```java
public class ModularClientExample {
    public static void main(String[] args) {
        // Initialize separate clients for different purposes
        PerpNetwork network = PerpNetwork.TESTNET;
        
        // 1. Market data client (no auth required)
        PerpMarketClient marketClient = new PerpMarketClient(network);
        List<TradingPairResponse> pairs = marketClient.tradingPair();
        
        // 2. User data client (main account auth)
        PerpAuthorization perpAuth = new PerpAuthorization(network);
        AuthSession mainAuth = perpAuth.authorize(mainKeyPair);
        PerpUserClient userClient = new PerpUserClient(network, mainAuth);
        AccountResponse account = userClient.account();
        
        // 3. Trade client (sub account auth)
        AuthSession subAuth = perpAuth.authorize(subKeyPair);
        PerpTradeClient tradeClient = new PerpTradeClient(network, subAuth);
        String orderId = tradeClient.placeOrder(orderRequest);
        
        // OR use unified client for convenience
        PerpHttpClient unifiedClient = new PerpHttpClient(network, mainKeyPair, subKeyPair);
        String orderId2 = unifiedClient.placeOrder(orderRequest);
    }
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

```java
// ALWAYS use DecimalUtil for value conversions
import io.dipcoin.sui.perp.util.DecimalUtil;
import java.math.BigDecimal;

// Convert human-readable values to base unit (18 decimals)
BigDecimal userInput = new BigDecimal("1000.5");
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
   ```java
   AccountResponse account = client.account();
   BigDecimal freeCollateral = DecimalUtil.fromBaseUnit(new BigInteger(account.getFreeCollateral()));
   if (freeCollateral.compareTo(requiredMargin) >= 0) {
       // Place order
   }
   ```

2. **Use Reduce-Only for Closing**: Set `reduceOnly=true` when closing positions
   ```java
   request.setReduceOnly(true);
   ```

3. **Monitor Funding**: Track funding rates and settlements
   ```java
   PageResponse<FundingSettlementsResponse> settlements = client.fundingSettlements(pageRequest);
   ```

4. **Use Appropriate Leverage**: Conservative leverage reduces liquidation risk
   ```java
   request.setLeverage(new BigInteger("5")); // 5x leverage
   ```

5. **Implement Client IDs**: Use unique client IDs for order tracking
   ```java
   request.setClientId("strategy1_" + System.currentTimeMillis());
   ```

### Error Handling

```java
import io.dipcoin.sui.perp.exception.*;

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

### Gas Management

```java
// Get dynamic gas price
SuiClient suiClient = SuiClient.build(new HttpService(networkConfig.suiRpc()));
Long gasPrice = suiClient.getReferenceGasPrice();

// Use appropriate gas budget (in 18 decimals)
BigDecimal normalGasBudget = new BigDecimal("0.1");  // 0.1 SUI
BigDecimal largeGasBudget = new BigDecimal("1.0");   // 1 SUI for complex operations

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

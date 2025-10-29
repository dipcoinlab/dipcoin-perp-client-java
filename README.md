# dipcoin-perp-client-java
Java Implementation of the dipcoin Perp Client Library

## 📋 Table of Contents

- [Quick Start](#quick-start)
  - [Maven Dependency](#maven-dependency)
  - [Gradle Dependency](#gradle-dependency)
  - [Initialize SDK](#initialize-sdk)
    - [Option 1: Main Account Mode (Single Account)](#option-1-main-account-mode-single-account)
    - [Option 2: Main Account + Sub Account Mode (Dual Account)](#option-2-main-account--sub-account-mode-dual-account)
    - [Account Mode Comparison](#account-mode-comparison)
- [Core Features](#core-features)
  - [Authorization](#authorization)
  - [Account Management](#account-management)
    - [Set Sub Account](#set-sub-account)
    - [Deposit](#deposit)
    - [Withdraw](#withdraw)
    - [Add Margin](#add-margin)
  - [Order Operations](#order-operations)
    - [Place Order](#place-order)
    - [Cancel Order](#cancel-order)
  - [Query Functions](#query-functions)
    - [Account Queries](#account-queries)
    - [Order Queries](#order-queries)
    - [Market Queries](#market-queries)
- [Types](#types)
- [Enums](#enums)
- [Constants](#constants)
- [License](#license)

## Quick Start

### Quick Example

A complete example demonstrating how to initialize the client, deposit funds, place orders, and query data:

```java
import io.dipcoin.sui.crypto.Ed25519KeyPair;
import io.dipcoin.sui.crypto.SuiKeyPair;
import io.dipcoin.sui.perp.client.PerpHttpClient;
import io.dipcoin.sui.perp.enums.OrderSide;
import io.dipcoin.sui.perp.enums.OrderType;
import io.dipcoin.sui.perp.enums.PerpNetwork;
import io.dipcoin.sui.perp.model.request.PlaceOrderRequest;
import io.dipcoin.sui.perp.model.response.AccountResponse;
import io.dipcoin.sui.perp.model.response.TradingPairResponse;
import io.dipcoin.sui.perp.util.OrderUtil;
import io.dipcoin.sui.protocol.SuiClient;
import io.dipcoin.sui.protocol.http.HttpService;

import java.math.BigInteger;

public class PerpExample {
    public static void main(String[] args) {
        // 1. Initialize keypairs
        SuiKeyPair mainKeyPair = Ed25519KeyPair.decodeHex("your_main_private_key");
        SuiKeyPair subKeyPair = Ed25519KeyPair.decodeHex("your_sub_private_key");

        // 2. Create client (dual account mode for production)
        PerpNetwork perpNetwork = PerpNetwork.TESTNET;
        SuiClient suiClient = SuiClient.build(new HttpService(perpNetwork.getConfig().suiRpc()));
        PerpHttpClient perpHttpClient = new PerpHttpClient(perpNetwork, suiClient, mainKeyPair, subKeyPair);

        // 3. Set sub account (only once, on-chain operation)
        perpHttpClient.setSubAccount(subKeyPair.address(), 1000L, BigInteger.TEN.pow(8));

        // 4. Deposit funds
        BigInteger depositAmount = new BigInteger("1000000000"); // 1000 USDC
        perpHttpClient.deposit(depositAmount, 1000L, BigInteger.TEN.pow(8));

        // 5. Check account balance
        AccountResponse account = perpHttpClient.account();
        System.out.println("Available margin: " + account.getFreeCollateral());

        // 6. Place an order
        TradingPairResponse tradingPair = perpHttpClient.getMarketPerpId("BTC-USDC");
        PlaceOrderRequest orderRequest = new PlaceOrderRequest()
                .setSymbol("BTC-USDC")
                .setMarket(tradingPair.getPerpId())
                .setPrice(new BigInteger("50000000000"))
                .setQuantity(new BigInteger("1000000000"))
                .setSide(OrderSide.BUY.getCode())
                .setOrderType(OrderType.LIMIT.getCode())
                .setLeverage(new BigInteger("10"))
                .setReduceOnly(false)
                .setCreator(mainKeyPair.address())
                .setClientId("order_001");

        String salt = OrderUtil.generateSalt();
        orderRequest.setSalt(salt);
        orderRequest.setOrderSignature(OrderUtil.signOrder(orderRequest, subKeyPair));

        String orderId = perpHttpClient.placeOrder(orderRequest);
        System.out.println("Order placed: " + orderId);
    }
}
```

### Maven Dependency

```xml
<dependency>
    <groupId>io.dipcoin</groupId>
    <artifactId>sui-perp-client-java</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle Dependency

```gradle
implementation 'io.dipcoin:sui-perp-client-java:1.0.0'
```

### Initialize SDK

PerpHttpClient provides two initialization modes. Choose based on your security requirements:

#### Option 1: Main Account Mode (Single Account)

All operations (including fund operations and trading operations) are performed by the main account. Suitable for simple scenarios or testing environments.

```java
import io.dipcoin.sui.crypto.Ed25519KeyPair;
import io.dipcoin.sui.crypto.SuiKeyPair;
import io.dipcoin.sui.perp.client.PerpHttpClient;
import io.dipcoin.sui.perp.enums.PerpNetwork;
import io.dipcoin.sui.protocol.SuiClient;
import io.dipcoin.sui.protocol.http.HttpService;

public class Test {
    public static void main(String[] args) {
        // Create keypair from different sources
        SuiKeyPair mainKeyPair = Ed25519KeyPair.deriveKeypair("xxx yyy zzz ...", null); // from mnemonics
        // or
        SuiKeyPair mainKeyPair = Ed25519KeyPair.decodeSuiPrivateKey("suiprivkeyxxxx..."); // from sui privateKey
        // or
        SuiKeyPair mainKeyPair = Ed25519KeyPair.decodeHex("01234..."); // from privateKey of hex

        // Initialize for mainnet (single account mode)
        PerpNetwork perpNetwork = PerpNetwork.MAINNET;
        HttpService suiService = new HttpService(perpNetwork.getConfig().suiRpc()); // Optional custom RPC
        SuiClient suiClient = SuiClient.build(suiService);
        
        // All operations (deposit/withdraw/trade/orders) use mainAccount
        PerpHttpClient perpHttpClient = new PerpHttpClient(perpNetwork, suiClient, mainKeyPair);
        
        // PerpHttpClient will automatically:
        // 1. Call authorize() with mainAccount during initialization
        // 2. Set authentication headers for all subsequent requests
    }
}
```

#### Option 2: Main Account + Sub Account Mode (Dual Account)

**Recommended for production environments**. The main account handles fund operations (deposit, withdraw, addMargin), while the sub account handles trading operations (placeOrder, cancelOrder). The sub account holds no funds, so even if the private key is compromised, there is no risk of fund loss.

```java
import io.dipcoin.sui.crypto.Ed25519KeyPair;
import io.dipcoin.sui.crypto.SuiKeyPair;
import io.dipcoin.sui.perp.client.PerpHttpClient;
import io.dipcoin.sui.perp.enums.PerpNetwork;
import io.dipcoin.sui.protocol.SuiClient;
import io.dipcoin.sui.protocol.http.HttpService;

public class Test {
    public static void main(String[] args) {
        // Main account: holds funds, used for deposit/withdraw/margin operations
        SuiKeyPair mainKeyPair = Ed25519KeyPair.decodeHex("01234..."); 
        
        // Sub account: no funds required, used for trading operations only
        SuiKeyPair subKeyPair = Ed25519KeyPair.decodeHex("56789..."); 

        // Initialize for testnet (dual account mode)
        PerpNetwork perpNetwork = PerpNetwork.TESTNET;
        HttpService suiService = new HttpService(perpNetwork.getConfig().suiRpc()); // Optional custom RPC
        SuiClient suiClient = SuiClient.build(suiService);
        
        // Main account for fund operations, sub account for trading operations
        PerpHttpClient perpHttpClient = new PerpHttpClient(perpNetwork, suiClient, mainKeyPair, subKeyPair);
        
        // PerpHttpClient will automatically:
        // 1. Call authorize() for both mainAccount and subAccount during initialization
        // 2. Set separate authentication headers for main and sub accounts
        // 3. Use mainAccount auth for: deposit, withdraw, addMargin, account queries
        // 4. Use subAccount auth for: placeOrder, cancelOrder
    }
}
```

#### Account Mode Comparison

| Feature | Main Account Mode | Main + Sub Account Mode |
|---------|-------------------|-------------------------|
| **Security** | Lower | High ✓ |
| **Fund Risk** | Main account private key leak causes fund loss | Sub account private key leak does not cause fund loss |
| **Use Cases** | Testing environment, personal use | Production environment, quantitative trading ✓ |
| **Complexity** | Simple | Requires managing two accounts |
| **Fund Operations** | mainAccount | mainAccount |
| **Trading Operations** | mainAccount | subAccount ✓ |

**Best Practice Recommendations**:
- Use main account + sub account mode for production environments
- Store main account private key offline, use only for fund operations
- Sub account private key can be deployed on trading servers
- Must bind main and sub account relationship on-chain via `setSubAccount()` first

## Core Features

### Authorization

#### Automatic Authorization Mechanism

PerpHttpClient **automatically completes authorization** during construction, no need to manually call authorization interface:

- **Single Account Mode**: Automatically calls `authorize()` for the main account and sets the authentication token in all HTTP request headers
- **Dual Account Mode**: Calls `authorize()` for both main and sub accounts separately, and sets their respective authentication tokens

All subsequent API requests automatically carry the correct authentication information.

#### Manual Authorization (Optional)

In some special scenarios, you may need to manually re-authorize (e.g., token expiration):

```java
import io.dipcoin.sui.perp.model.response.AuthorizationResponse;

public class Test {
    public static void main(String[] args) {
        PerpNetwork perpNetwork = PerpNetwork.TESTNET;
        SuiKeyPair mainKeyPair = Ed25519KeyPair.decodeHex("01234...");
        SuiClient suiClient = SuiClient.build(new HttpService(perpNetwork.getConfig().suiRpc()));
        PerpHttpClient perpHttpClient = new PerpHttpClient(perpNetwork, suiClient, mainKeyPair);
        
        // Manually re-authorize if needed (e.g., token expired)
        AuthorizationResponse response = perpHttpClient.authorize(mainKeyPair, mainKeyPair.address());
        log.info("Authorization token: {}", response.getToken());
    }
}
```

### Account Management

#### Set Sub Account

**Only required when using dual account mode**. Binds the relationship between main account and sub account on-chain. After this operation, the sub account can perform trading operations on behalf of the main account.

**Important Notes**:
- This is an **on-chain transaction** and requires Gas fees
- Must initialize PerpHttpClient in dual account mode before using sub account for trading
- Each main account can only bind one sub account
- After binding, sub account can perform trading operations like placing/canceling orders, but cannot perform fund operations

```java
import io.dipcoin.sui.model.transaction.SuiTransactionBlockResponse;
import java.math.BigInteger;

public class Test {
    public static void main(String[] args) {
        PerpNetwork perpNetwork = PerpNetwork.TESTNET;
        SuiKeyPair mainKeyPair = Ed25519KeyPair.decodeHex("01234...");
        SuiKeyPair subKeyPair = Ed25519KeyPair.decodeHex("56789...");
        SuiClient suiClient = SuiClient.build(new HttpService(perpNetwork.getConfig().suiRpc()));
        
        // Initialize with dual account mode
        PerpHttpClient perpHttpClient = new PerpHttpClient(perpNetwork, suiClient, mainKeyPair, subKeyPair);
        
        // Bind sub account to main account on-chain
        String subAddress = subKeyPair.address(); // sub account address
        long gasPrice = 1000L; // gas price in MIST
        BigInteger gasBudget = BigInteger.TEN.pow(8); // 0.1 SUI
        
        SuiTransactionBlockResponse response = perpHttpClient.setSubAccount(subAddress, gasPrice, gasBudget);
        log.info("Set sub account response: {}", response);
        log.info("Transaction digest: {}", response.getDigest());
        
        // After binding, the sub account can be used for trading operations
    }
}
```

#### Deposit

Deposit USDC into your perpetual trading account:

```java
public class Test {
    public static void main(String[] args) {
        PerpNetwork perpNetwork = PerpNetwork.TESTNET;
        SuiKeyPair mainKeyPair = Ed25519KeyPair.decodeHex("01234...");
        PerpHttpClient perpHttpClient = new PerpHttpClient(perpNetwork, mainKeyPair);
        
        BigInteger amount = new BigInteger("1000000000"); // 1000 USDC (6 decimals)
        long gasPrice = 1000L;
        BigInteger gasBudget = BigInteger.TEN.pow(8); // 0.1 SUI
        
        SuiTransactionBlockResponse response = perpHttpClient.deposit(amount, gasPrice, gasBudget);
        log.info("Deposit response: {}", response);
    }
}
```

#### Withdraw

Withdraw USDC from your perpetual trading account:

```java
public class Test {
    public static void main(String[] args) {
        PerpNetwork perpNetwork = PerpNetwork.TESTNET;
        SuiKeyPair mainKeyPair = Ed25519KeyPair.decodeHex("01234...");
        PerpHttpClient perpHttpClient = new PerpHttpClient(perpNetwork, mainKeyPair);
        
        BigInteger amount = new BigInteger("500000000"); // 500 USDC
        long gasPrice = 1000L;
        BigInteger gasBudget = BigInteger.TEN.pow(8); // 0.1 SUI
        
        SuiTransactionBlockResponse response = perpHttpClient.withdraw(amount, gasPrice, gasBudget);
        log.info("Withdraw response: {}", response);
    }
}
```

#### Add Margin

Add margin to an existing position:

```java
public class Test {
    public static void main(String[] args) {
        PerpNetwork perpNetwork = PerpNetwork.TESTNET;
        SuiKeyPair mainKeyPair = Ed25519KeyPair.decodeHex("01234...");
        PerpHttpClient perpHttpClient = new PerpHttpClient(perpNetwork, mainKeyPair);
        
        String symbol = "BTC-USDC";
        BigInteger amount = new BigInteger("100000000"); // 100 USDC
        long gasPrice = 1000L;
        BigInteger gasBudget = BigInteger.TEN.pow(8); // 0.1 SUI
        
        SuiTransactionBlockResponse response = perpHttpClient.addMargin(symbol, amount, gasPrice, gasBudget);
        log.info("Add margin response: {}", response);
    }
}
```

### Order Operations

#### Place Order

Place a limit or market order. Order signature varies depending on account mode:
- **Single Account Mode**: Sign with main account
- **Dual Account Mode**: Sign with sub account

##### Dual Account Mode Example (Recommended)

```java
import io.dipcoin.sui.perp.enums.OrderSide;
import io.dipcoin.sui.perp.enums.OrderType;
import io.dipcoin.sui.perp.model.request.PlaceOrderRequest;
import io.dipcoin.sui.perp.model.response.TradingPairResponse;
import io.dipcoin.sui.perp.util.OrderUtil;

public class Test {
    public static void main(String[] args) {
        PerpNetwork perpNetwork = PerpNetwork.TESTNET;
        SuiKeyPair mainKeyPair = Ed25519KeyPair.decodeHex("01234...");
        SuiKeyPair subKeyPair = Ed25519KeyPair.decodeHex("56789...");
        SuiClient suiClient = SuiClient.build(new HttpService(perpNetwork.getConfig().suiRpc()));
        
        // Initialize with dual account mode
        PerpHttpClient perpHttpClient = new PerpHttpClient(perpNetwork, suiClient, mainKeyPair, subKeyPair);
        
        // Get trading pair info
        TradingPairResponse tradingPair = perpHttpClient.getTradingPair("BTC-USDC");
        
        // Place a limit buy order
        PlaceOrderRequest request = new PlaceOrderRequest()
                .setSymbol("BTC-USDC")
                .setMarket(tradingPair.getPerpId())
                .setPrice(new BigInteger("50000000000")) // 50000 USDC (6 decimals)
                .setQuantity(new BigInteger("1000000000")) // 1 BTC (9 decimals)
                .setSide(OrderSide.BUY.getCode())
                .setOrderType(OrderType.LIMIT.getCode())
                .setLeverage(new BigInteger("10")) // 10x leverage
                .setReduceOnly(false)
                .setCreator(mainKeyPair.address())
                .setClientId("client_order_001");
        
        // Generate order signature with SUB account (dual mode)
        String salt = OrderUtil.generateSalt();
        request.setSalt(salt);
        String orderSignature = OrderUtil.signOrder(request, subKeyPair); // Use subKeyPair
        request.setOrderSignature(orderSignature);
        
        String orderId = perpHttpClient.placeOrder(request);
        log.info("Order placed with ID: {}", orderId);
        
        // Place a market order
        PlaceOrderRequest marketOrder = new PlaceOrderRequest()
                .setSymbol("BTC-USDC")
                .setMarket(tradingPair.getPerpId())
                .setQuantity(new BigInteger("500000000")) // 0.5 BTC
                .setSide(OrderSide.SELL.getCode())
                .setOrderType(OrderType.MARKET.getCode())
                .setLeverage(new BigInteger("5"))
                .setReduceOnly(false)
                .setCreator(mainKeyPair.address())
                .setClientId("client_order_002");
        
        String saltMarket = OrderUtil.generateSalt();
        marketOrder.setSalt(saltMarket);
        String marketOrderSignature = OrderUtil.signOrder(marketOrder, subKeyPair); // Use subKeyPair
        marketOrder.setOrderSignature(marketOrderSignature);
        
        String marketOrderId = perpHttpClient.placeOrder(marketOrder);
        log.info("Market order placed with ID: {}", marketOrderId);
    }
}
```

##### Single Account Mode Example

```java
public class Test {
    public static void main(String[] args) {
        PerpNetwork perpNetwork = PerpNetwork.TESTNET;
        SuiKeyPair mainKeyPair = Ed25519KeyPair.decodeHex("01234...");
        SuiClient suiClient = SuiClient.build(new HttpService(perpNetwork.getConfig().suiRpc()));
        
        // Initialize with single account mode
        PerpHttpClient perpHttpClient = new PerpHttpClient(perpNetwork, suiClient, mainKeyPair);
        
        TradingPairResponse tradingPair = perpHttpClient.getTradingPair("BTC-USDC");
        
        PlaceOrderRequest request = new PlaceOrderRequest()
                .setSymbol("BTC-USDC")
                .setMarket(tradingPair.getPerpId())
                .setPrice(new BigInteger("50000000000"))
                .setQuantity(new BigInteger("1000000000"))
                .setSide(OrderSide.BUY.getCode())
                .setOrderType(OrderType.LIMIT.getCode())
                .setLeverage(new BigInteger("10"))
                .setReduceOnly(false)
                .setCreator(mainKeyPair.address())
                .setClientId("client_order_001");
        
        // Generate order signature with MAIN account (single mode)
        String salt = OrderUtil.generateSalt();
        request.setSalt(salt);
        String orderSignature = OrderUtil.signOrder(request, mainKeyPair); // Use mainKeyPair
        request.setOrderSignature(orderSignature);
        
        String orderId = perpHttpClient.placeOrder(request);
        log.info("Order placed with ID: {}", orderId);
    }
}
```

#### Cancel Order

Cancel an existing order. Cancel operation uses different authentication methods in different account modes:
- **Single Account Mode**: Uses main account authentication
- **Dual Account Mode**: Uses sub account authentication

```java
import io.dipcoin.sui.perp.model.request.CancelOrderRequest;
import io.dipcoin.sui.perp.model.response.CancelOrderResponse;

public class Test {
    public static void main(String[] args) {
        PerpNetwork perpNetwork = PerpNetwork.TESTNET;
        SuiKeyPair mainKeyPair = Ed25519KeyPair.decodeHex("01234...");
        SuiKeyPair subKeyPair = Ed25519KeyPair.decodeHex("56789...");
        SuiClient suiClient = SuiClient.build(new HttpService(perpNetwork.getConfig().suiRpc()));
        
        // Dual account mode (recommended)
        PerpHttpClient perpHttpClient = new PerpHttpClient(perpNetwork, suiClient, mainKeyPair, subKeyPair);
        
        CancelOrderRequest request = new CancelOrderRequest()
                .setOrderId("order_id_to_cancel")
                .setSymbol("BTC-USDC");
        
        // In dual mode, uses sub account authentication automatically
        CancelOrderResponse response = perpHttpClient.cancelOrder(request);
        log.info("Cancel order response: {}", response);
        log.info("Cancelled order ID: {}", response.getOrderId());
        
        // For single account mode:
        // PerpHttpClient perpHttpClient = new PerpHttpClient(perpNetwork, suiClient, mainKeyPair);
        // Uses main account authentication automatically
    }
}
```

### Query Functions

#### Account Queries

##### Get Account Information

Get comprehensive account information including balances, margins, and positions:

```java
import io.dipcoin.sui.perp.model.response.AccountResponse;

public class Test {
    public static void main(String[] args) {
        PerpNetwork perpNetwork = PerpNetwork.TESTNET;
        SuiKeyPair mainKeyPair = Ed25519KeyPair.decodeHex("01234...");
        PerpHttpClient perpHttpClient = new PerpHttpClient(perpNetwork, mainKeyPair);
        
        AccountResponse account = perpHttpClient.account();
        log.info("Wallet balance: {}", account.getWalletBalance());
        log.info("Free collateral: {}", account.getFreeCollateral());
        log.info("Total position margin: {}", account.getTotalPositionMargin());
        log.info("Total unrealized profit: {}", account.getTotalUnrealizedProfit());
        log.info("Account value: {}", account.getAccountValue());
    }
}
```

##### Get Positions

Get all current positions:

```java
import io.dipcoin.sui.perp.model.response.PositionResponse;
import java.util.List;

public class Test {
    public static void main(String[] args) {
        PerpNetwork perpNetwork = PerpNetwork.TESTNET;
        SuiKeyPair mainKeyPair = Ed25519KeyPair.decodeHex("01234...");
        PerpHttpClient perpHttpClient = new PerpHttpClient(perpNetwork, mainKeyPair);
        
        List<PositionResponse> positions = perpHttpClient.positions();
        for (PositionResponse position : positions) {
            log.info("Symbol: {}, Side: {}, Quantity: {}, Avg Entry Price: {}, Unrealized Profit: {}",
                    position.getSymbol(),
                    position.getSide(),
                    position.getQuantity(),
                    position.getAvgEntryPrice(),
                    position.getUnrealizedProfit());
        }
    }
}
```

##### Get Balance Changes

Get historical balance changes with pagination:

```java
import io.dipcoin.sui.perp.model.PageResponse;
import io.dipcoin.sui.perp.model.request.PageRequest;
import io.dipcoin.sui.perp.model.response.BalanceChangesResponse;

public class Test {
    public static void main(String[] args) {
        PerpNetwork perpNetwork = PerpNetwork.TESTNET;
        SuiKeyPair mainKeyPair = Ed25519KeyPair.decodeHex("01234...");
        PerpHttpClient perpHttpClient = new PerpHttpClient(perpNetwork, mainKeyPair);
        
        PageRequest request = new PageRequest()
                .setPage(1)
                .setPageSize(20);
        
        PageResponse<BalanceChangesResponse> balanceChanges = perpHttpClient.balanceChanges(request);
        log.info("Total records: {}", balanceChanges.getTotal());
        for (BalanceChangesResponse change : balanceChanges.getData()) {
            log.info("Change type: {}, Amount: {}, Balance: {}",
                    change.getType(),
                    change.getAmount(),
                    change.getBalance());
        }
    }
}
```

##### Get Funding Settlements

Get funding rate settlement history:

```java
import io.dipcoin.sui.perp.model.PageResponse;
import io.dipcoin.sui.perp.model.request.PageRequest;
import io.dipcoin.sui.perp.model.response.FundingSettlementsResponse;

public class Test {
    public static void main(String[] args) {
        PerpNetwork perpNetwork = PerpNetwork.TESTNET;
        SuiKeyPair mainKeyPair = Ed25519KeyPair.decodeHex("01234...");
        PerpHttpClient perpHttpClient = new PerpHttpClient(perpNetwork, mainKeyPair);
        
        PageRequest request = new PageRequest()
                .setPage(1)
                .setPageSize(20);
        
        PageResponse<FundingSettlementsResponse> fundingSettlements = perpHttpClient.fundingSettlements(request);
        log.info("Total settlements: {}", fundingSettlements.getTotal());
        for (FundingSettlementsResponse settlement : fundingSettlements.getData()) {
            log.info("Symbol: {}, Funding fee: {}, Funding rate: {}",
                    settlement.getSymbol(),
                    settlement.getFundingFee(),
                    settlement.getFundingRate());
        }
    }
}
```

#### Order Queries

##### Get Current Orders

Get current active orders with optional filtering:

```java
import io.dipcoin.sui.perp.model.PageResponse;
import io.dipcoin.sui.perp.model.request.OrdersRequest;
import io.dipcoin.sui.perp.model.response.OrdersResponse;

public class Test {
    public static void main(String[] args) {
        PerpNetwork perpNetwork = PerpNetwork.TESTNET;
        SuiKeyPair mainKeyPair = Ed25519KeyPair.decodeHex("01234...");
        PerpHttpClient perpHttpClient = new PerpHttpClient(perpNetwork, mainKeyPair);
        
        OrdersRequest request = new OrdersRequest()
                .setSymbol("BTC-USDC") // Optional: filter by symbol
                .setPage(1)
                .setPageSize(20);
        
        PageResponse<OrdersResponse> orders = perpHttpClient.orders(request);
        log.info("Total active orders: {}", orders.getTotal());
        for (OrdersResponse order : orders.getData()) {
            log.info("Order ID: {}, Symbol: {}, Side: {}, Type: {}, Price: {}, Quantity: {}, Status: {}",
                    order.getOrderId(),
                    order.getSymbol(),
                    order.getSide(),
                    order.getOrderType(),
                    order.getPrice(),
                    order.getQuantity(),
                    order.getStatus());
        }
    }
}
```

##### Get History Orders

Get historical orders (filled, cancelled, etc.):

```java
import io.dipcoin.sui.perp.model.PageResponse;
import io.dipcoin.sui.perp.model.request.HistoryOrdersRequest;
import io.dipcoin.sui.perp.model.response.HistoryOrdersResponse;

public class Test {
    public static void main(String[] args) {
        PerpNetwork perpNetwork = PerpNetwork.TESTNET;
        SuiKeyPair mainKeyPair = Ed25519KeyPair.decodeHex("01234...");
        PerpHttpClient perpHttpClient = new PerpHttpClient(perpNetwork, mainKeyPair);
        
        HistoryOrdersRequest request = new HistoryOrdersRequest()
                .setSymbol("BTC-USDC") // Optional: filter by symbol
                .setPage(1)
                .setPageSize(20);
        
        PageResponse<HistoryOrdersResponse> historyOrders = perpHttpClient.historyOrders(request);
        log.info("Total history orders: {}", historyOrders.getTotal());
        for (HistoryOrdersResponse order : historyOrders.getData()) {
            log.info("Order ID: {}, Symbol: {}, Side: {}, Filled: {}/{}, Avg Fill Price: {}",
                    order.getOrderId(),
                    order.getSymbol(),
                    order.getSide(),
                    order.getFilledQuantity(),
                    order.getQuantity(),
                    order.getAvgFillPrice());
        }
    }
}
```

#### Market Queries

##### Get All Trading Pairs

Get all available trading pairs:

```java
import io.dipcoin.sui.perp.model.response.TradingPairResponse;
import java.util.List;

public class Test {
    public static void main(String[] args) {
        PerpNetwork perpNetwork = PerpNetwork.TESTNET;
        SuiKeyPair mainKeyPair = Ed25519KeyPair.decodeHex("01234...");
        PerpHttpClient perpHttpClient = new PerpHttpClient(perpNetwork, mainKeyPair);
        
        List<TradingPairResponse> tradingPairs = perpHttpClient.tradingPair();
        for (TradingPairResponse pair : tradingPairs) {
            log.info("Symbol: {}, Max Leverage: {}, Maker Fee: {}, Taker Fee: {}",
                    pair.getSymbol(),
                    pair.getMaxLeverage(),
                    pair.getMakerFee(),
                    pair.getTakerFee());
        }
    }
}
```

##### Get Trading Pair Information

Get specific trading pair details:

```java
import io.dipcoin.sui.perp.model.response.TradingPairResponse;

public class Test {
    public static void main(String[] args) {
        PerpNetwork perpNetwork = PerpNetwork.TESTNET;
        SuiKeyPair mainKeyPair = Ed25519KeyPair.decodeHex("01234...");
        PerpHttpClient perpHttpClient = new PerpHttpClient(perpNetwork, mainKeyPair);
        
        TradingPairResponse tradingPair = perpHttpClient.getTradingPair("BTC-USDC");
        log.info("Perp ID: {}", tradingPair.getPerpId());
        log.info("Step size: {}", tradingPair.getStepSize());
        log.info("Tick size: {}", tradingPair.getTickSize());
        log.info("Max leverage: {}", tradingPair.getMaxLeverage());
    }
}
```

##### Get Ticker Information

Get real-time ticker data for a trading pair:

```java
import io.dipcoin.sui.perp.model.request.SymbolRequest;
import io.dipcoin.sui.perp.model.response.TickerResponse;

public class Test {
    public static void main(String[] args) {
        PerpNetwork perpNetwork = PerpNetwork.TESTNET;
        SuiKeyPair mainKeyPair = Ed25519KeyPair.decodeHex("01234...");
        PerpHttpClient perpHttpClient = new PerpHttpClient(perpNetwork, mainKeyPair);
        
        SymbolRequest request = new SymbolRequest().setSymbol("BTC-USDC");
        TickerResponse ticker = perpHttpClient.ticker(request);
        log.info("Last price: {}", ticker.getLastPrice());
        log.info("24h high: {}", ticker.getHigh24h());
        log.info("24h low: {}", ticker.getLow24h());
        log.info("24h volume: {}", ticker.getVolume24h());
        log.info("Price change 24h: {}", ticker.getPriceChange24h());
    }
}
```

##### Get Order Book

Get the order book with bids and asks:

```java
import io.dipcoin.sui.perp.model.request.SymbolRequest;
import io.dipcoin.sui.perp.model.response.OrderBookResponse;

public class Test {
    public static void main(String[] args) {
        PerpNetwork perpNetwork = PerpNetwork.TESTNET;
        SuiKeyPair mainKeyPair = Ed25519KeyPair.decodeHex("01234...");
        PerpHttpClient perpHttpClient = new PerpHttpClient(perpNetwork, mainKeyPair);
        
        SymbolRequest request = new SymbolRequest().setSymbol("BTC-USDC");
        OrderBookResponse orderBook = perpHttpClient.orderBook(request);
        
        log.info("Bids (sorted descending):");
        orderBook.getBids().forEach(bid -> 
            log.info("Price: {}, Quantity: {}", bid.getPrice(), bid.getQuantity())
        );
        
        log.info("Asks (sorted ascending):");
        orderBook.getAsks().forEach(ask -> 
            log.info("Price: {}, Quantity: {}", ask.getPrice(), ask.getQuantity())
        );
    }
}
```

##### Get Oracle Price

Get the current oracle price for a trading pair:

```java
import io.dipcoin.sui.perp.model.request.SymbolRequest;
import java.math.BigInteger;

public class Test {
    public static void main(String[] args) {
        PerpNetwork perpNetwork = PerpNetwork.TESTNET;
        SuiKeyPair mainKeyPair = Ed25519KeyPair.decodeHex("01234...");
        PerpHttpClient perpHttpClient = new PerpHttpClient(perpNetwork, mainKeyPair);
        
        SymbolRequest request = new SymbolRequest().setSymbol("BTC-USDC");
        BigInteger oraclePrice = perpHttpClient.oracle(request);
        log.info("Oracle price: {}", oraclePrice);
    }
}
```

## Types

### PlaceOrderRequest

```java
public class PlaceOrderRequest {
    /** trading pair */
    private String symbol;
    
    /** perp id */
    private String market;
    
    /** price (for limit orders) */
    private BigInteger price;
    
    /** quantity */
    private BigInteger quantity;
    
    /** trade direction: BUY / SELL */
    private String side;
    
    /** order types: LIMIT, MARKET */
    private String orderType;
    
    /** leverage multiplier */
    private BigInteger leverage;
    
    /** whether to reduce position only */
    private Boolean reduceOnly;
    
    /** signature salt */
    private String salt;
    
    /** order belongs to the master account */
    private String creator;
    
    /** client ID */
    private String clientId;
    
    /** order signature */
    private String orderSignature;
}
```

### PositionResponse

```java
public class PositionResponse {
    /** user address */
    private String userAddress;
    
    /** trading pair */
    private String symbol;
    
    /** average opening price */
    private String avgEntryPrice;
    
    /** margin */
    private String margin;
    
    /** leverage */
    private String leverage;
    
    /** quantity */
    private String quantity;
    
    /** trade direction: BUY / SELL */
    private String side;
    
    /** position value */
    private String positionValue;
    
    /** unrealized profit/loss */
    private String unrealizedProfit;
    
    /** unrealized profit/loss percentage (ROE) */
    private String roe;
    
    /** liquidation price */
    private String liquidationPrice;
    
    /** oracle price */
    private String oraclePrice;
    
    /** mid-market price */
    private String midMarketPrice;
    
    /** unsettled funding rate */
    private String fundingDue;
    
    /** estimated next funding rate */
    private String fundingFeeNext;
}
```

### AccountResponse

```java
public class AccountResponse {
    /** address */
    private String address;
    
    /** whether trading is allowed */
    private Boolean canTrade;
    
    /** wallet balance */
    private String walletBalance;
    
    /** total position margin */
    private String totalPositionMargin;
    
    /** total unrealized profit */
    private String totalUnrealizedProfit;
    
    /** available margin */
    private String freeCollateral;
    
    /** account value */
    private String accountValue;
    
    /** fee tier */
    private String feeTier;
    
    /** account data for each market */
    private List<AccountDataByMarketResponse> accountDataByMarket;
}
```

### TradingPairResponse

```java
public class TradingPairResponse {
    /** contract perp id */
    private String perpId;
    
    /** trading pair */
    private String symbol;
    
    /** coin name */
    private String coinName;
    
    /** initial margin */
    private String initialMargin;
    
    /** maintenance margin */
    private String maintenanceMargin;
    
    /** maker fee */
    private String makerFee;
    
    /** taker fee */
    private String takerFee;
    
    /** step size (quantity precision) */
    private String stepSize;
    
    /** tick size (price precision) */
    private String tickSize;
    
    /** max quantity for limit orders */
    private String maxQtyLimit;
    
    /** max quantity for market orders */
    private String maxQtyMarket;
    
    /** max leverage */
    private Integer maxLeverage;
    
    /** perp oi limit list */
    private List<PerpOiLimitResponse> perpOiLimitVOList;
}
```

## Enums

### OrderType

```java
public enum OrderType {
    LIMIT("LIMIT", 1, "Limit"),
    MARKET("MARKET", 2, "Market"),
    LIQ("Liquidation", 3, "Liquidation"),
    ADL("ADL", 4, "ADL");
}
```

### OrderSide

```java
public enum OrderSide {
    BUY("BUY", 1),
    SELL("SELL", 2);
}
```

### PerpNetwork

```java
public enum PerpNetwork {
    MAINNET,  // Production environment
    TESTNET;  // Test environment
}
```

## Constants

### Default Values

- Default gas price: 1000 MIST
- Default gas budget: 0.1 SUI `BigInteger.TEN.pow(8)`
- USDC decimals: 6
- Position quantity decimals: 9

### Important Notes

1. **Account Mode Selection**:
   - **Dual account mode is recommended for production environments**, keep main account offline, deploy sub account on trading servers
   - Single account mode is suitable for testing environments or simple personal scenarios
   - Dual account mode requires calling `setSubAccount()` first to bind account relationship on-chain

2. **Automatic Authorization**: PerpHttpClient automatically calls `authorize()` and sets authentication headers during construction, all subsequent requests automatically carry authentication information

3. **Order Signature**: 
   - Single Account Mode: Sign with main account `OrderUtil.signOrder(request, mainKeyPair)`
   - Dual Account Mode: Sign with sub account `OrderUtil.signOrder(request, subKeyPair)`

4. **Gas Management**: For dynamic gas price queries, use `SuiClient.getReferenceGasPrice()` method

5. **Precision Notes**: 
   - All numbers use unified 18 decimal places precision
   - Use `stepSize` and `tickSize` from trading pair information to ensure valid order parameters

6. **Rate Limiting**: API has rate limits, please handle rate limit errors properly

7. **Error Handling**: All API methods may throw `PerpHttpException`, implement proper error handling logic

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

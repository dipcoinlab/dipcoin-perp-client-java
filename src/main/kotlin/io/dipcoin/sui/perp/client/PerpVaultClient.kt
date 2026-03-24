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

package io.dipcoin.sui.perp.client

import io.dipcoin.sui.crypto.SuiKeyPair
import io.dipcoin.sui.model.transaction.SuiTransactionBlockResponse
import io.dipcoin.sui.perp.client.auth.AuthSession
import io.dipcoin.sui.perp.client.chain.WalletService
import io.dipcoin.sui.perp.client.core.AbstractHttpClient
import io.dipcoin.sui.perp.client.core.PerpAuthorization
import io.dipcoin.sui.perp.client.core.PerpClient
import io.dipcoin.sui.perp.enums.PerpNetwork
import io.dipcoin.sui.perp.model.PageResponse
import io.dipcoin.sui.perp.model.request.*
import io.dipcoin.sui.perp.model.response.*
import org.bouncycastle.util.encoders.Base64
import java.math.BigInteger

/**
 * Full-featured DipCoin perpetual client with vault (parent-address) support.
 *
 * Combines the HTTP API surface of [PerpHttpClient] (trading, account queries, market data)
 * with on-chain vault operations from [PerpOffSignClient] (deposit, withdraw, margin management).
 *
 * **Vault mode**: When [parentAddress] is set, all account/position/order queries automatically
 * include the parent address so that the sub-account operates under the vault owner's funds.
 *
 * ### Usage with key pairs (simple)
 * ```kotlin
 * val client = PerpVaultClient.create(PerpNetwork.MAINNET, mainKeyPair, subKeyPair)
 * // with explicit vault owner:
 * val client = PerpVaultClient.create(PerpNetwork.MAINNET, mainKeyPair, subKeyPair, parentAddress = "0xabc...")
 * ```
 *
 * ### Usage with external wallet (e.g. hardware wallet, MPC)
 * ```kotlin
 * val client = PerpVaultClient.createWithWallet(
 *     perpNetwork = PerpNetwork.MAINNET,
 *     walletService = myHardwareWallet,
 *     mainAuth = AuthSession("0xmain...", "token1"),
 *     subAuth  = AuthSession("0xsub...", "token2"),
 *     mainAddress = "0xmain...",
 *     subAddress  = "0xsub...",
 * )
 * ```
 */
class PerpVaultClient private constructor(
    private val perpAuthorization: PerpAuthorization,
    private val perpMarketClient: PerpMarketClient,
    private val perpUserClient: PerpUserClient,
    private val perpTradeClient: PerpTradeClient,
    private val offSignClient: PerpOffSignClient,
    //parent address
    /** Vault owner address. Auto-injected into account/position/order queries. */
    private val vaultAddress: String,
    private val subAuth: AuthSession,
    private val subAccount: SuiKeyPair<*>
) : AbstractHttpClient(), PerpClient {

    private val _subAddress: String = subAccount.address()

    /** Gas price for on-chain transactions (in MIST). Adjustable at runtime. */
    var gasPrice: Long = 1000L

    /** Gas budget for on-chain transactions. Adjustable at runtime. */
    var gasBudget: BigInteger = BigInteger.valueOf(50_000_000L)

    // ═══════════════════════════════════════════════════════════════════════
    // PerpClient — auth
    // ═══════════════════════════════════════════════════════════════════════

    override fun authorize(request: AuthorizationRequest): AuthorizationResponse =
        perpAuthorization.authorize(request)

    override fun authorize(suiKeyPair: SuiKeyPair<*>): AuthSession =
        perpAuthorization.authorize(suiKeyPair)

    // ═══════════════════════════════════════════════════════════════════════
    // PerpClient — trading (sub-account session)
    // ═══════════════════════════════════════════════════════════════════════

    override fun placeOrder(request: PlaceOrderRequest): String? =
        perpTradeClient.placeOrder(request)

    override fun cancelOrder(request: CancelOrderRequest): CancelOrderResponse? {
        if (request.parentAddress == null) request.parentAddress = vaultAddress
        return perpTradeClient.cancelOrder(request)
    }

    override fun queryTpslPlan(request: QueryTpslPlanRequest): OrdersResponse? =
        perpTradeClient.queryTpslPlan(request)

    override fun planCloseOrder(request: TpslPlanOrderRequest): String? =
        perpTradeClient.planCloseOrder(request)

    // ═══════════════════════════════════════════════════════════════════════
    // PerpClient — account & positions (main session, auto-fill parentAddress)
    // ═══════════════════════════════════════════════════════════════════════

    override fun positions(request: PositionRequest): List<PositionResponse>? {
        if (request.parentAddress == null) request.parentAddress = vaultAddress
        return perpUserClient.positions(request)
    }

    /** Convenience: query positions for a symbol under the vault. */
    fun positions(symbol: String? = null): List<PositionResponse>? =
        positions(PositionRequest(parentAddress = vaultAddress, symbol = symbol))

    override fun orders(request: OrdersRequest): PageResponse<OrdersResponse>? {
        if (request.parentAddress == null) request.parentAddress = vaultAddress
        return perpUserClient.orders(request)
    }

    override fun account(request: AccountRequest): AccountResponse? {
        if (request.parentAddress == null) request.parentAddress = vaultAddress
        return perpUserClient.account(request)
    }

    /** Convenience: query the vault account. */
    fun account(): AccountResponse? =
        account(AccountRequest(parentAddress = vaultAddress))

    override fun historyOrders(request: HistoryOrdersRequest): PageResponse<HistoryOrdersResponse>? {
        if (request.parentAddress == null) request.parentAddress = vaultAddress
        return perpUserClient.historyOrders(request)
    }

    override fun fundingSettlements(request: FundingPageRequest): PageResponse<FundingSettlementsResponse>? {
        if (request.parentAddress == null) request.parentAddress = vaultAddress
        return perpUserClient.fundingSettlements(request)
    }

    override fun balanceChanges(request: BalancePageRequest): PageResponse<BalanceChangesResponse>? {
        if (request.parentAddress == null) request.parentAddress = vaultAddress
        return perpUserClient.balanceChanges(request)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // PerpClient — market data (no auth)
    // ═══════════════════════════════════════════════════════════════════════

    override fun ticker(request: SymbolRequest): TickerResponse? =
        perpMarketClient.ticker(request)

    override fun orderBook(request: SymbolRequest): OrderBookResponse? =
        perpMarketClient.orderBook(request)

    override fun oracle(request: SymbolRequest): BigInteger? =
        perpMarketClient.oracle(request)

    override fun tradingPair(): List<TradingPairResponse>? =
        perpMarketClient.tradingPair()

    override fun getMarketPerpId(symbol: String): String? =
        perpMarketClient.getMarketPerpId(symbol)

    override fun getPythFeedId(symbol: String): String? =
        perpMarketClient.getPythFeedId(symbol)

    // ═══════════════════════════════════════════════════════════════════════
    // PerpClient — key/address accessors
    // ═══════════════════════════════════════════════════════════════════════

    override fun getMainAccount(): SuiKeyPair<*> =
        throw UnsupportedOperationException(
            "Key pair not available (constructed with WalletService)")

    override fun getSubAccount(): SuiKeyPair<*> =
        subAccount

    override fun getMainAddress(): String = throw UnsupportedOperationException(
        "Key pair not available (constructed with WalletService)")
    override fun getSubAddress(): String = _subAddress

    // ═══════════════════════════════════════════════════════════════════════
    // Vault on-chain operations (via PerpOffSignClient)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Register [_subAddress] as a sub-account under [parentAddress] on-chain.
     * Only needs to be called once per sub-account.
     */
    fun vaultSetSubAccount(): SuiTransactionBlockResponse =
        offSignClient.setSubAccount(vaultAddress, _subAddress, gasPrice, gasBudget)

    /**
     * Deposit [amount] (in token base units) into the vault bank.
     * The sender is [parentAddress].
     */
    fun vaultDeposit(amount: BigInteger): SuiTransactionBlockResponse =
        offSignClient.deposit(vaultAddress, amount, gasPrice, gasBudget)

    /**
     * Withdraw [amount] from the vault bank back to [parentAddress].
     */
    fun vaultWithdraw(amount: BigInteger): SuiTransactionBlockResponse =
        offSignClient.withdraw(vaultAddress, amount, gasPrice, gasBudget)

    /**
     * Add [amount] of margin to the position of [symbol] for the sub-account.
     */
    fun vaultAddMargin(symbol: String, amount: BigInteger): SuiTransactionBlockResponse =
        offSignClient.addMargin(vaultAddress, _subAddress, symbol, amount, gasPrice, gasBudget)

    /**
     * Remove [amount] of margin from the position of [symbol] for the sub-account.
     */
    fun vaultRemoveMargin(symbol: String, amount: BigInteger): SuiTransactionBlockResponse =
        offSignClient.removeMargin(vaultAddress, _subAddress, symbol, amount, gasPrice, gasBudget)

    // ═══════════════════════════════════════════════════════════════════════
    // Internal — KeyPair-based WalletService adapter
    // ═══════════════════════════════════════════════════════════════════════

    private class KeyPairWalletService(private val keyPair: SuiKeyPair<*>) : WalletService {
        override fun sign(address: String, txData: ByteArray): String =
            keyPair.signTransactionDataBase64(Base64.toBase64String(txData))
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Factory methods
    // ═══════════════════════════════════════════════════════════════════════

    companion object {
        /**
         * Create with key pairs — the simplest way, mirrors [PerpHttpClient] constructor.
         *
         * @param parentAddress vault owner address; defaults to `main.address()` if null
         */
        @JvmStatic
        @JvmOverloads
        fun create(
            perpNetwork: PerpNetwork,
            sub: SuiKeyPair<*>,
            parentAddress: String,
        ): PerpVaultClient {
            val auth = PerpAuthorization(perpNetwork)
            val subAuth = auth.authorize(sub)
            val marketClient = PerpMarketClient(perpNetwork)
            val subAddr = sub.address()

            return PerpVaultClient(
                perpAuthorization = auth,
                perpMarketClient = marketClient,
                perpUserClient = PerpUserClient(perpNetwork, subAuth),
                perpTradeClient = PerpTradeClient(perpNetwork, subAuth),
                offSignClient = PerpOffSignClient(perpNetwork, marketClient, KeyPairWalletService(sub)),
                vaultAddress = parentAddress,
                subAuth = subAuth,
                subAccount = sub
            )
        }
    }
}

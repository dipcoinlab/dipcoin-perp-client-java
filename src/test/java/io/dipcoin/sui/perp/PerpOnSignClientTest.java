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
import io.dipcoin.sui.model.transaction.SuiTransactionBlockResponse;
import io.dipcoin.sui.perp.client.PerpMarketClient;
import io.dipcoin.sui.perp.client.PerpOnSignClient;
import io.dipcoin.sui.perp.enums.PerpNetwork;
import io.dipcoin.sui.perp.wallet.WalletKey;
import io.dipcoin.sui.protocol.SuiClient;
import io.dipcoin.sui.protocol.http.HttpService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

/**
 * @author : Same
 * @datetime : 2025/10/29 17:24
 * @Description : pass in the SuiKeyPair-signed transaction to the on-chain client test
 */
@Slf4j
public class PerpOnSignClientTest {

    protected SuiClient suiClient;

    protected PerpMarketClient perpMarketClient;

    protected PerpOnSignClient perpOnSignClient;

    @BeforeEach
    protected void setUp() {
        PerpNetwork perpNetwork = PerpNetwork.TESTNET;
        HttpService suiService = new HttpService(perpNetwork.getConfig().suiRpc());
        this.suiClient = SuiClient.build(suiService);
        this.perpMarketClient = new PerpMarketClient(perpNetwork);
        this.perpOnSignClient = new PerpOnSignClient(suiClient, perpNetwork, perpMarketClient);
    }

    @Test
    @Tag("suite")
    void testSetSubAccount() {
        // set subAccount if u need
        SuiKeyPair subAccountKeyPair = WalletKey.subKeyPair;
        String subAccount = subAccountKeyPair.address();
        // gas price 1000 (For dynamic queries, please refer to the `getReferenceGasPrice()` method in `SuiClient`)
        // gas limit 0.1 SUI (BigInteger.TEN.pow(8))1000
        SuiTransactionBlockResponse response = perpOnSignClient.setSubAccount(subAccountKeyPair, subAccount, 1000L, BigInteger.TEN.pow(8));
        // https://testnet.suivision.xyz/txblock/3TMtqn7QPRmdtUEhchX48ZVf2HpjNEH229D8qzytJsX5
        log.info("Response: {}", response);
    }

    @Test
    @Tag("suite")
    void testDeposit() {
        SuiKeyPair mainAccountKeyPair = WalletKey.mainKeyPair;
        // deposit 10, 000 testUSDC
        BigInteger amount = new BigInteger("10000").multiply(BigInteger.TEN.pow(6));
        // gas price 1000 (For dynamic queries, please refer to the `getReferenceGasPrice()` method in `SuiClient`)
        // gas limit 0.1 SUI (BigInteger.TEN.pow(8))1000
        SuiTransactionBlockResponse response = perpOnSignClient.deposit(mainAccountKeyPair, amount, 1000L, BigInteger.TEN.pow(8));
        // https://testnet.suivision.xyz/txblock/CvS8oKVvHkjQHMtnatEgNQ1AjKd2upLAs429BimTgX6c
        log.info("Response: {}", response);
    }

    @Test
    @Tag("suite")
    void testWithdraw() {
        SuiKeyPair mainAccountKeyPair = WalletKey.mainKeyPair;
        // withdraw 10, 000 testUSDC
        BigInteger amount = new BigInteger("10000").multiply(BigInteger.TEN.pow(6));
        // gas price 1000 (For dynamic queries, please refer to the `getReferenceGasPrice()` method in `SuiClient`)
        // gas limit 0.1 SUI (BigInteger.TEN.pow(8))1000
        SuiTransactionBlockResponse response = perpOnSignClient.withdraw(mainAccountKeyPair, amount, 1000L, BigInteger.TEN.pow(8));
        // https://testnet.suivision.xyz/txblock/FtyJ1nT4kwC8MDXwqrYVqEDePy2RquWhbf2xuGNJK7q9
        log.info("Response: {}", response);
    }

    @Test
    @Tag("suite")
    void testAddMargin() {
        SuiKeyPair mainAccountKeyPair = WalletKey.mainKeyPair;
        String mainAccount = mainAccountKeyPair.address();
        // addmargin 200 testUSDC
        String symbol = "ETH-PERP";
        BigInteger amount = new BigInteger("200").multiply(BigInteger.TEN.pow(18));
        // gas price 1000 (For dynamic queries, please refer to the `getReferenceGasPrice()` method in `SuiClient`)
        // gas limit 0.1 SUI (BigInteger.TEN.pow(8))1000
        SuiTransactionBlockResponse response = perpOnSignClient.addMargin(mainAccountKeyPair, mainAccount, symbol, amount, 1000L, BigInteger.TEN.pow(8));
        // https://testnet.suivision.xyz/txblock/HfimayLEjWDQkntX1kUxMheiKNoDAG8bvBSBLJA8hhHk
        log.info("Response: {}", response);
    }

}

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

package io.dipcoin.sui.perp.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dipcoin.sui.crypto.SuiKeyPair;
import io.dipcoin.sui.crypto.exceptions.SigningException;
import io.dipcoin.sui.crypto.signature.SignatureScheme;
import io.dipcoin.sui.perp.enums.OrderScheme;
import io.dipcoin.sui.perp.enums.OrderSide;
import io.dipcoin.sui.perp.model.request.PlaceOrderRequest;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.random.RandomGenerator;

/**
 * @author : Same
 * @datetime : 2025/10/23 10:42
 * @Description : order util
 */
@Slf4j
public class OrderUtil {

    private static final RandomGenerator random = RandomGenerator.getDefault();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String CANCEL_KEY = "orderHashes";

    /**
     * serialize Maker or Taker orders into hex strings
     * @param request
     * @return
     */
    public static String getSerializedOrder(PlaceOrderRequest request) {
        boolean reduceOnly = request.getReduceOnly();
        boolean isBuy = request.getSide().equals(OrderSide.BUY.getCode());
        return getSerializedOrder(request.getMarket(),
                isBuy,
                reduceOnly,
                false,
                true,
                false,
                request.getPrice(),
                request.getQuantity(),
                request.getLeverage(),
                new BigInteger(request.getSalt()),
                BigInteger.ZERO,
                request.getCreator(),
                getOrderFlags(false, false, reduceOnly, isBuy, true)
        );
    }

    /**
     * serialize Maker or Taker orders into hex strings
     * @param symbol trading pair
     * @param isLong
     * @param reduceOnly
     * @param postOnly
     * @param orderbookOnly
     * @param ioc
     * @param price
     * @param quantity
     * @param leverage
     * @param salt
     * @param expiration
     * @param creator
     * @param orderFlags
     * @return
     */
    public static String getSerializedOrder(String symbol,
                                            boolean isLong,
                                            boolean reduceOnly,
                                            boolean postOnly,
                                            boolean orderbookOnly,
                                            boolean ioc,
                                            BigInteger price,
                                            BigInteger quantity,
                                            BigInteger leverage,
                                            BigInteger salt,
                                            BigInteger expiration,
                                            String creator,
                                            int orderFlags) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("\"market\":\"").append(symbol).append("\",\n");
        sb.append("\"creator\":\"").append(creator).append("\",\n");
        sb.append("\"isLong\":\"").append(isLong).append("\",\n");
        sb.append("\"reduceOnly\":\"").append(reduceOnly).append("\",\n");
        sb.append("\"postOnly\":\"").append(postOnly).append("\",\n");
        sb.append("\"orderbookOnly\":\"").append(orderbookOnly).append("\",\n");
        sb.append("\"ioc\":\"").append(ioc).append("\",\n");
        sb.append("\"quantity\":\"").append(quantity != null ? quantity.toString() : "0").append("\",\n");
        sb.append("\"price\":\"").append(price != null ? price.toString() : "0").append("\",\n");
        sb.append("\"leverage\":\"").append(leverage != null ? leverage.toString() : "0").append("\",\n");
        sb.append("\"expiration\":\"").append(expiration != null ? expiration.toString() : "0").append("\",\n");
        sb.append("\"salt\":\"").append(salt != null ? salt.toString() : "0").append("\",\n");
        sb.append("\"orderFlag\":\"").append(orderFlags).append("\",\n");
        sb.append("\"domain\":\"dipcoin.io\"\n");
        sb.append("}");

        return sb.toString();
    }

    /**
     * serialize cancel order
     * @param orderHashes
     * @return
     */
    public static String getSerializedCancelOrder(List<String> orderHashes) {
        try {
            Map<String, List<String>> msg = new HashMap();
            msg.put(CANCEL_KEY, orderHashes);
            return objectMapper.writeValueAsString(msg);
        } catch (Exception e) {
            log.error("Failed to encode message", e);
            return "";
        }
    }

    /**
     * get order flag bits
     * 0th bit = ioc
     * 1st bit = postOnly
     * 2nd bit = reduceOnly
     * 3rd bit = isBuy
     * 4th bit = orderbookOnly
     */
    public static int getOrderFlags(boolean ioc, boolean postOnly, boolean reduceOnly, boolean isBuy, boolean orderbookOnly) {
        int flag = 0;
        if (ioc) flag += 1;
        if (postOnly) flag += 2;
        if (reduceOnly) flag += 4;
        if (isBuy) flag += 8;
        if (orderbookOnly) flag += 16;
        return flag;
    }

    /**
     * message sign
     * @param suiKeyPair
     * @return
     */
    public static String getSignature(String msg, SuiKeyPair suiKeyPair) {
        try {
            return OrderUtil.getMessageSignature(msg.getBytes(StandardCharsets.UTF_8), suiKeyPair) + Base64.toBase64String(suiKeyPair.publicKeyBytes());
        } catch (IOException e) {
            throw new SigningException("Failed to generate signature", e);
        }
    }

    /**
     * get random salt
     * @return
     */
    public static byte[] getSalt() {
        long salt = System.currentTimeMillis() +
                getRandomInt(0, 1_000_000) +
                getRandomInt(0, 1_000_000) +
                getRandomInt(0, 1_000_000);
        return longToBytes(salt);
    }

    /**
     * message signature
     * @param msg
     * @param suiKeyPair
     * @return
     * @throws IOException
     */
    private static String getMessageSignature(byte[] msg, SuiKeyPair suiKeyPair) throws IOException {
        SignatureScheme signatureScheme = suiKeyPair.signatureScheme();
        if (signatureScheme.equals(SignatureScheme.ED25519)) {
            // ed25519, sign the hash value (with KP_ED25519 flag appended at the end).
            return Hex.toHexString(suiKeyPair.signSignaturePersonalMessage(msg)) + OrderScheme.KP_ED25519.getType();
        } else if (signatureScheme.equals(SignatureScheme.SECP256K1)) {
            // Secp256k1, sign the original data (with KP_SECP256 flag appended at the end).
            return Hex.toHexString(suiKeyPair.signSignaturePersonalMessage(msg)) + OrderScheme.KP_SECP256.getType();
        }
        throw new IllegalArgumentException("Unsupported signature scheme: " + signatureScheme);
    }

    private static byte[] longToBytes(long value) {
        byte[] result = new byte[8];
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte) (value & 0xFF);
            value >>= 8;
        }
        return result;
    }

    private static int getRandomInt(int min, int max) {
        return random.nextInt(min, max + 1);
    }
}

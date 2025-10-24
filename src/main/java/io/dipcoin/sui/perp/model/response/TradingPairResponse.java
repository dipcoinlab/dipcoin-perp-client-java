package io.dipcoin.sui.perp.model.response;

import lombok.Data;

import java.util.List;

/**
 * @author : Same
 * @datetime : 2025/10/23 17:42
 * @Description : TradingPair response
 */
@Data
public class TradingPairResponse {

    /**
     * contract perp id
     */
    private String perpId;

    /**
     * trading pair
     */
    private String symbol;

    /**
     * coin name
     */
    private String coinName;

    /**
     * trading pair status
     */
    private Integer status;

    /**
     * initial margin
     */
    private String initialMargin;

    /**
     * maintenance margin
     */
    private String maintenanceMargin;

    /**
     * maker fee
     */
    private String makerFee;

    /**
     * taker fee
     */
    private String takerFee;

    /**
     * step size
     */
    private String stepSize;

    /**
     * tick size
     */
    private String tickSize;

    /**
     * max quantity limit
     */
    private String maxQtyLimit;

    /**
     * max quantity market
     */
    private String maxQtyMarket;

    /**
     * fee pool address
     */
    private String feePoolAddress;

    /**
     * long position price deviation limit
     */
    private String mtbLong;

    /**
     * short position price deviation limit
     */
    private String mtbShort;

    /**
     * max funding
     */
    private String maxFunding;

    /**
     * max leverage
     */
    private Integer maxLeverage;

    /**
     * perp oi limit list
     */
    private List<PerpOiLimitResponse> perpOiLimitVOList;

    /**
     * price identifier id
     */
    private String priceIdentifierId;

}

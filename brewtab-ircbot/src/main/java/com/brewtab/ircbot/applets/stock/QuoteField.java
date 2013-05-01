/*
 * Copyright (c) 2013 Christopher Thunes
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.brewtab.ircbot.applets.stock;

import java.util.HashMap;
import java.util.Map;

public enum QuoteField {
    ASK("a", "Ask"),
    BID("b", "Bid"),
    CHANGE_AND_PERCENT_CHANGE("c", "Change & Percent Change"),
    DIVIDEND_PER_SHARE("d", "Dividend/Share"),
    EARNINGS_PER_SHARE("e", "Earnings/Share"),
    DAY_LOW("g", "Day's Low"),
    DAY_HIGH("h", "Day's High"),
    MORE_INFO("i", "More Info"),
    FIFTY_TWO_WEEK_LOW("j", "52-week Low"),
    FIFTY_TWO_WEEK_HIGH("k", "52-week High"),
    LAST_TRADE_WITH_TIME("l", "Last Trade (With Time)"),
    DAYS_RANGE("m", "Day's Range"),
    NAME("n", "Name"),
    OPEN("o", "Open"),
    PREVIOUS_CLOSE("p", "Previous Close"),
    EX_DIVIDEND_DATE("q", "Ex-Dividend Date"),
    PE_RATIO("r", "P/E Ratio"),
    SYMBOL("s", "Symbol"),
    VOLUME("v", "Volume"),
    FIFTY_TWO_WEEK_RANGE("w", "52-week Range"),
    EXCHANGE("x", "Stock Exchange"),
    DIVIDEND_YIELD("y", "Dividend Yield"),
    AVERAGE_DAILY_VOLUME("a2", "Average Daily Volume"),
    ASK_SIZE("a5", "Ask Size"),
    ASK_REAL_TIME("b2", "Ask (Real-time)"),
    BID_REAL_TIME("b3", "Bid (Real-time)"),
    BOOK_VALUE("b4", "Book Value"),
    BID_SIZE("b6", "Bid Size"),
    CHANGE("c1", "Change"),
    COMMISSION("c3", "Commission"),
    CHANGE_REAL_TIME("c6", "Change (Real-time)"),
    AFTER_HOURS_CHANGE_REAL_TIME("c8", "After Hours Change (Real-time)"),
    LAST_TRADE_DATE("d1", "Last Trade Date"),
    TRADE_DATE("d2", "Trade Date"),
    ERROR_INDICATION("e1", "Error Indication (returned for symbol changed / invalid)"),
    EPS_ESTIMATE_CURRENT_YEAR("e7", "EPS Estimate Current Year"),
    EPS_ESTIMATE_NEXT_YEAR("e8", "EPS Estimate Next Year"),
    EPS_ESTIMATE_NEXT_QUARTER("e9", "EPS Estimate Next Quarter"),
    FLOAT_SHARES("f6", "Float Shares"),
    HOLDINGS_GAIN_PERCENT("g1", "Holdings Gain Percent"),
    ANNUALIZED_GAIN("g3", "Annualized Gain"),
    HOLDINGS_GAIN("g4", "Holdings Gain"),
    HOLDINGS_GAIN_PERCENT_REAL_TIME("g5", "Holdings Gain Percent (Real-time)"),
    HOLDINGS_GAIN_REAL_TIME("g6", "Holdings Gain (Real-time)"),
    ORDER_BOOK_REAL_TIME("i5", "Order Book (Real-time)"),
    MARKET_CAP("j1", "Market Cap"),
    MARKET_CAP_REAL_TIME("j3", "Market Cap (Real-time)"),
    EBITDA("j4", "EBITDA"),
    CHANGE_FROM_52_WEEK_LOW("j5", "Change From 52-week Low"),
    PERCENT_CHANGE_FROM_52_WEEK_LOW("j6", "Percent Change From 52-week Low"),
    LAST_TRADE_REAL_TIME_WITH_TIME("k1", "Last Trade (Real-time) With Time"),
    PERCENT_CHANGE_REAL_TIME("k2", "Change Percent (Real-time)"),
    LAST_TRADE_SIZE("k3", "Last Trade Size"),
    CHANGE_FROM_52_WEEK_HIGH("k4", "Change From 52-week High"),
    PERCEBT_CHANGE_FROM_52_WEEK_HIGH("k5", "Percebt Change From 52-week High"),
    LAST_TRADE_PRICE_ONLY("l1", "Last Trade (Price Only)"),
    HIGH_LIMIT("l2", "High Limit"),
    LOW_LIMIT("l3", "Low Limit"),
    DAYS_RANGE_REAL_TIME("m2", "Day's Range (Real-time)"),
    FIFTY_DAY_MOVING_AVERAGE("m3", "50-day Moving Average"),
    TWO_HUNDRED_DAY_MOVING_AVERAGE("m4", "200-day Moving Average"),
    CHANGE_FROM_200_DAY_MOVING_AVERAGE("m5", "Change From 200-day Moving Average"),
    PERCENT_CHANGE_FROM_200_DAY_MOVING_AVERAGE("m6", "Percent Change From 200-day Moving Average"),
    CHANGE_FROM_50_DAY_MOVING_AVERAGE("m7", "Change From 50-day Moving Average"),
    PERCENT_CHANGE_FROM_50_DAY_MOVING_AVERAGE("m8", "Percent Change From 50-day Moving Average"),
    NOTES("n4", "Notes"),
    PRICE_PAID("p1", "Price Paid"),
    PERCENT_CHANGE("p2", "Change in Percent"),
    PRICE_PER_SALES("p5", "Price/Sales"),
    PRICE_PER_BOOK("p6", "Price/Book"),
    DIVIDEND_PAY_DATE("r1", "Dividend Pay Date"),
    PE_RATIO_REAL_TIME("r2", "P/E Ratio (Real-time)"),
    PEG_RATIO("r5", "PEG Ratio"),
    PRICE_EPS_ESTIMATE_CURRENT_YEAR("r6", "Price/EPS Estimate Current Year"),
    PRICE_EPS_ESTIMATE_NEXT_YEAR("r7", "Price/EPS Estimate Next Year"),
    SHARES_OWNED("s1", "Shares Owned"),
    SHORT_RATIO("s7", "Short Ratio"),
    LAST_TRADE_TIME("t1", "Last Trade Time"),
    TRADE_LINKS("t6", "Trade Links"),
    TICKER_TREND("t7", "Ticker Trend"),
    ONE_YEAR_TARGET_PRICE("t8", "1 Year Target Price"),
    HOLDINGS_VALUE("v1", "Holdings Value"),
    HOLDINGS_VALUE_REAL_TIME("v7", "Holdings Value (Real-time)"),
    DAYS_VALUE_CHANGE("w1", "Day's Value Change"),
    DAYS_VALUE_CHANGE_REAL_TIME("w4", "Day's Value Change (Real-time)");

    private static Map<String, QuoteField> byTag = new HashMap<String, QuoteField>();

    static {
        for (QuoteField field : values()) {
            byTag.put(field.getTag(), field);
        }
    }

    private String tag;
    private String description;

    QuoteField(String tag, String description) {
        this.tag = tag;
        this.description = description;
    }

    public String getTag() {
        return tag;
    }

    public String getDescription() {
        return description;
    }

    public static QuoteField fromTag(String tag) {
        return byTag.get(tag);
    }
}

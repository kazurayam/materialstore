package com.kazurayam.materialstore.reduce.differ;

import java.math.BigDecimal;

public final class DifferUtil {
    private DifferUtil() {
    }

    /**
     * @return e.g. "0.23" or "90.00"
     */
    public static String formatDiffRatioAsString(Double diffRatio, String fmt) {
        if (diffRatio >= 0.0d) {
            return String.format(fmt, diffRatio) + "%";
        } else {
            return "no diff";
        }

    }

    /**
     * @return e.g. "0.23" or "90.00"
     */
    public static String formatDiffRatioAsString(Double diffRatio) {
        return DifferUtil.formatDiffRatioAsString(diffRatio, "%1$.2f");
    }

    /**
     * 0.001 -> 0.01
     *
     */
    public static Double roundUpTo2DecimalPlaces(Double diffRatio) {
        BigDecimal bd = new BigDecimal(diffRatio);
        BigDecimal bdUP = bd.setScale(2, BigDecimal.ROUND_UP);// 0.001 -> 0.01
        return bdUP.doubleValue();
    }

}

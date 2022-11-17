package com.kazurayam.materialstore.base.reduce.differ;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class DifferUtil {
    private DifferUtil() {
    }

    /*
     * @return e.g. "0.23" or "90.00"
     */
    public static String formatDiffRatioAsString(Double diffRatio, String fmt) {
        if (diffRatio >= 0.0d) {
            return String.format(fmt, diffRatio) + "%";
        } else {
            return "no diff";
        }

    }

    /*
     * @return e.g. "0.23" or "90.00"
     */
    public static String formatDiffRatioAsString(Double diffRatio) {
        return DifferUtil.formatDiffRatioAsString(diffRatio, "%1$.2f");
    }

    /*
     * 0.001 -> 0.01
     *
     */
    public static Double roundUpTo2DecimalPlaces(Double diffRatio) {
        BigDecimal bd = new BigDecimal(diffRatio);
        BigDecimal bdUP = bd.setScale(2, RoundingMode.HALF_EVEN); // 0.001 -> 0.01
        return bdUP.doubleValue();
    }

}

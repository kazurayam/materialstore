package com.kazurayam.materialstore.reduce.differ

final class DifferUtil {

    private DifferUtil() {}

    /**
     * @return e.g. "0.23" or "90.00"
     */
    static String formatDiffRatioAsString(Double diffRatio, String fmt = '%1$.2f') {
        if (diffRatio >= 0.0d) {
            return String.format(fmt, diffRatio) + "%"
        } else {
            return "no diff"
        }
    }

    /**
     * 0.001 -> 0.01
     *
     * @param input
     * @return
     */
    static Double roundUpTo2DecimalPlaces(Double diffRatio) {
        BigDecimal bd = new BigDecimal(diffRatio)
        BigDecimal bdUP = bd.setScale(2, BigDecimal.ROUND_UP)  // 0.001 -> 0.01
        return bdUP.doubleValue()
    }
}

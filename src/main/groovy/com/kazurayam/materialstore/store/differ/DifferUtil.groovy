package com.kazurayam.materialstore.store.differ

class DifferUtil {

    private DifferUtil() {}

    /**
     * @return e.g. "0.23" or "90.00"
     */
    static String formatDiffRatioAsString(Double diffRatio, String fmt = '%1$.2f') {
        return String.format(fmt, diffRatio)
    }

    /**
     * 0.001 -> 0.01
     *
     * @param input
     * @return
     */
    static Double roundUpTo2DecimalPlaces(Double diffRatio) {
        BigDecimal bd = new BigDecimal(diffRatio)
        BigDecimal bdUP = bd.setScale(2, BigDecimal.ROUND_UP);  // 0.001 -> 0.01
        return bdUP.doubleValue()
    }
}

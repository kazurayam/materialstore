package com.kazurayam.materialstore.store.differ

class DifferUtil {

    private DifferUtil() {}

    /**
     * @return e.g. "0.23" or "90.00"
     */
    static String formatDiffRatioAsString(Double diffRatio, String fmt = '%1$.2f') {
        return String.format(fmt, diffRatio)
    }
}

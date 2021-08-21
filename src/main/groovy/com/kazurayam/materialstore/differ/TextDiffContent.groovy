package com.kazurayam.materialstore.differ

final class TextDiffContent {

    private final String content

    private final int inserted
    private final int deleted
    private final int changed
    private final int equal

    private TextDiffContent(Builder builder) {
        content = builder.content
        inserted = builder.inserted
        deleted = builder.deleted
        changed = builder.changed
        equal = builder.equal
    }
    String getContent() {
        return content
    }
    int getInserted() {
        return inserted
    }
    int getDeleted() {
        return deleted
    }
    int getChanged() {
        return changed
    }
    int getEqual() {
        return equal
    }
    int getTotal() {
        return inserted + deleted + changed + equal
    }

    Double getDiffRatio() {
        Double diffVolume = Double.valueOf(inserted + deleted + changed)
        Double diffRatio = (diffVolume * 100) / getTotal()
        return DifferUtil.roundUpTo2DecimalPlaces(diffRatio)
    }

    static class Builder {
        private String content
        private int inserted
        private int deleted
        private int changed
        private int equal
        Builder(String content) {
            this.content = content
            inserted = deleted = changed = equal = 0
        }
        Builder inserted(int v) {
            inserted = v
            return this
        }
        Builder deleted(int v) {
            deleted = v
            return this
        }
        Builder changed(int v) {
            changed = v
            return this
        }
        Builder equal(int v) {
            equal = v
            return this
        }
        TextDiffContent build() {
            return new TextDiffContent(this)
        }
    }

}

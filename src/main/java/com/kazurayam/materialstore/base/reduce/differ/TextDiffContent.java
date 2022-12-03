package com.kazurayam.materialstore.base.reduce.differ;

public final class TextDiffContent {

    private final String content;
    private final int inserted;
    private final int deleted;
    private final int changed;
    private final int equal;

    private TextDiffContent(Builder builder) {
        content = builder.content;
        inserted = builder.inserted;
        deleted = builder.deleted;
        changed = builder.changed;
        equal = builder.equal;
    }

    public String getContent() {
        return content;
    }

    public int getInserted() {
        return inserted;
    }

    public int getDeleted() {
        return deleted;
    }

    public int getChanged() {
        return changed;
    }

    public int getEqual() {
        return equal;
    }

    public int getTotal() {
        return inserted + deleted + changed + equal;
    }

    public Double getDiffRatio() {
        int diffVolume = inserted + deleted + changed;
        Double diffRatio = (diffVolume * 1.0D) / getTotal() * 100;
        return DifferUtil.roundUpTo2DecimalPlaces(diffRatio);
    }

    /**
     *
     */
    public static class Builder {
        private final String content;
        private int inserted;
        private int deleted;
        private int changed;
        private int equal;

        public Builder(String content) {
            this.content = content;
            inserted = deleted = changed = equal = 0;
        }

        public Builder inserted(int v) {
            inserted = v;
            return this;
        }

        public Builder deleted(int v) {
            deleted = v;
            return this;
        }

        public Builder changed(int v) {
            changed = v;
            return this;
        }

        public Builder equal(int v) {
            equal = v;
            return this;
        }

        public TextDiffContent build() {
            return new TextDiffContent(this);
        }

    }
}

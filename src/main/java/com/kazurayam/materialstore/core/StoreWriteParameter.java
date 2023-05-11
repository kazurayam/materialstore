package com.kazurayam.materialstore.core;

public class StoreWriteParameter {

    public static StoreWriteParameter DEFAULT = new StoreWriteParameter.Builder().build();
    private final Jobber.DuplicationHandling flowControl;
    private final Float jpegCompressionQuality;

    private StoreWriteParameter(Builder builder) {
        this.flowControl = builder.flowControl;
        this.jpegCompressionQuality = builder.jpegCompressionQuality;
    }

    Jobber.DuplicationHandling getFlowControl() {
        return this.flowControl;
    }

    Float getJpegCompressionQuality() {
        return this.jpegCompressionQuality;
    }

    public static class Builder {
        private Jobber.DuplicationHandling flowControl = Jobber.DuplicationHandling.TERMINATE;
        private Float jpegCompressionQuality = 0.9f;
        Builder() {}
        Builder flowControl(Jobber.DuplicationHandling flowControl) {
            this.flowControl = flowControl;
            return this;
        }
        Builder jpegCompressionQuality(Float compressionQuality) {
            if (compressionQuality < 0.1f || 1.0f < compressionQuality) {
                throw new IllegalArgumentException(
                        "compression quality must be in the range of [0.1f, 1.0f]");
            }
            this.jpegCompressionQuality = compressionQuality;
            return this;
        }
        StoreWriteParameter build() {
            return new StoreWriteParameter(this);
        }
    }
}

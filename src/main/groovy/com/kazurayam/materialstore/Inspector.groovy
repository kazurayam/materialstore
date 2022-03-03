package com.kazurayam.materialstore

import com.kazurayam.materialstore.report.MaterialListReporter

class Inspector {

    private Inspector(Builder builder) {

    }

    static Builder builder() {
        return new Builder()
    }

    static class Builder {
        private MaterialListReporter materialsReporter
    }
}

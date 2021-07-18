package com.kazurayam.materialstore.diff

class DifferDriverFactory {

    static DifferDriver newDifferDriver() {
        return new DifferDriverImpl()
    }
}

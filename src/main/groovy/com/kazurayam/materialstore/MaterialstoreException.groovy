package com.kazurayam.materialstore

final class MaterialstoreException extends Exception {

    MaterialstoreException(String message) {
        super(message)
    }

    MaterialstoreException(String message, Throwable throwable) {
        super(message, throwable)
    }

    MaterialstoreException(Throwable throwable) {
        super(throwable)
    }
}

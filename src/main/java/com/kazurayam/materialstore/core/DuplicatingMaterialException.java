package com.kazurayam.materialstore.core;

public class DuplicatingMaterialException extends MaterialstoreException {

    public DuplicatingMaterialException(String message) {
        super(message);
    }

    public DuplicatingMaterialException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public DuplicatingMaterialException(Throwable throwable) {
        super(throwable);
    }
}

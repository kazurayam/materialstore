package com.kazurayam.materialstore.materialize;

import com.kazurayam.materialstore.MaterialstoreException;

import java.io.File;
import java.io.Reader;
import java.nio.file.Path;
import java.util.List;

public abstract class TargetCSVReader {

    static TargetCSVReader newSimpleReader() {
        return new SimpleTargetCSVReader();
    }

    public abstract List<Target> parse(File file) throws MaterialstoreException;

    public abstract List<Target> parse(Path path) throws MaterialstoreException;

    public abstract List<Target> parse(Reader reader) throws MaterialstoreException;

    public abstract List<Target> parse(String string) throws MaterialstoreException;

}

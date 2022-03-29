package com.kazurayam.materialstore.filesystem.metadata;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;

public class SortKeys {

    public static final SortKeys NULL_OBJECT = new SortKeys(new ArrayList<>());

    private final List<String> arguments;

    public SortKeys(String... args) {
        this(Arrays.asList(args));
    }

    public SortKeys(List<String> args) {
        this.arguments = args;
    }

    public Iterator<String> iterator() {
        return arguments.iterator();
    }

    public int size() {
        return arguments.size();
    }

    public String get(int index) {
        return arguments.get(index);
    }

    @Override
    public String toString() {
        final Gson gson = new Gson();
        final StringBuilder sb = new StringBuilder();
        sb.append("[");
        IntStream.range(0, arguments.size()).forEach(index -> {
            if (index > 0) {
                sb.append(", ");
            }
            String arg = arguments.get(index);
            sb.append(gson.toJson(arg));
        });
        sb.append("]");
        return sb.toString();
    }


}

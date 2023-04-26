package com.kazurayam.materialstore.core;

import com.kazurayam.materialstore.util.StringUtils;

import java.util.Objects;

public final class ID implements Comparable<ID>, Jsonifiable {

    public static final ID NULL_OBJECT = new ID(StringUtils.repeat("0", 40));

    private final String sha1_;

    public static boolean isValid(String sha1) {
        if (sha1.length() != 40) {
            throw new IllegalArgumentException("sha1(${sha1}) must be of length=40");
        }
        if (sha1.replaceAll("[0-9a-fA-F]", "").length() != 0) {
            throw new IllegalArgumentException("sha1(${sha1}) must be consists of only hex-decimal characters");
        }
        return true;
    }

    public ID(String sha1) {
        Objects.requireNonNull(sha1);
        isValid(sha1);
        this.sha1_ = sha1;
    }

    public String getSha1() {
        return sha1_;
    }

    public String getShortSha1() {
        return sha1_.substring(0, 7);
    }

    @Override
    public String toJson() {
        return "\"" + sha1_ + "\"";
    }

    @Override
    public String toJson(boolean prettyPrint) {
        return toJson();
    }

    @Override
    public boolean equals(Object obj) {
        if ( !(obj instanceof ID) ) {
            return false;
        }
        ID other = (ID)obj;
        return getSha1().equals(other.getSha1());
    }

    @Override
    public int hashCode() {
        return getSha1().hashCode();
    }

    @Override
    public String toString() {
        return getSha1();
    }

    @Override
    public int compareTo(ID other) {
        return getSha1().compareTo(other.getSha1());
    }
}


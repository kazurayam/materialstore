package com.kazurayam.materialstore.core.filesystem;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * checks if the giving string is valid as a file name
 */
final class Filename {

    static final String PROHIBITED_CHARACTERS = "/\\?*:|\"<>&";
    static final Set<Character> prohibitedChars = new HashSet<>();

    static {
        for (int i = 0; i < PROHIBITED_CHARACTERS.length(); i++) {
            prohibitedChars.add(PROHIBITED_CHARACTERS.charAt(i));
        }
    }

    static boolean isValid(String s) throws IllegalArgumentException {
        Objects.requireNonNull(s, "argument s must not be null");
        if (s.length() > 0) {
            char[] chs = s.toCharArray();
            for (char c : chs) {
                Character ch = c;
                if (prohibitedChars.contains(ch)) {
                    throw new IllegalArgumentException("character " + String.valueOf(ch) + " is prohibited in a File name");
                }
            }
            return true;
        } else {
            return false;
        }
    }
}

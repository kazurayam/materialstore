package com.kazurayam.materialstore

final class Filename {

    static final String PROHIBITED_CHARACTERS = "/\\?*:|\"<>&"

    static boolean isValid(String s) throws IllegalArgumentException {
        Set<Character> prohibitedChars = PROHIBITED_CHARACTERS.toCharArray()
        char[] chs = s.toCharArray()
        for (int i = 0; i < chs.length; i++) {
            Character ch = Character.valueOf(chs[i])
            if (prohibitedChars.contains(ch)) {
                throw new IllegalArgumentException("character " + String.valueOf(ch) + " is prohibited in a File name")
            }
        }
        return true
    }
}

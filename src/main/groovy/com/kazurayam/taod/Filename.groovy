package com.kazurayam.taod

class Filename {

    static final String PROHIBITED_CHARACTERS = "/\\?*:|\"<>&"

    static boolean isValid(String s) {
        Set<Character> prohibitedChars = PROHIBITED_CHARACTERS.toCharArray()
        char[] chs = s.toCharArray()
        for (int i = 0; i < chs.length; i++) {
            Character ch = Character.valueOf(chs[i])
            if (prohibitedChars.contains(ch)) {
                return false
            }
        }
        return true
    }
}

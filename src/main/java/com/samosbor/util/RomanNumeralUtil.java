package com.samosbor.util;

public class RomanNumeralUtil {
    private static final int[] VALUES = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
    private static final String[] SYMBOLS = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};

    public static String toRoman(int number) {
        if (number <= 0) {
            return String.valueOf(number);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < VALUES.length; i++) {
            while (number >= VALUES[i]) {
                sb.append(SYMBOLS[i]);
                number -= VALUES[i];
            }
        }
        return sb.toString();
    }
}

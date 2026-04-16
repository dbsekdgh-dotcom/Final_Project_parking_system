package com.example.demo.domain.user.util;

import org.springframework.stereotype.Component;

@Component
public class JaroWinklerMatcher {

    public double match(String s1, String s2) {
        if (s1 == null || s2 == null) return 0.0;
        if (s1.equals(s2)) return 1.0;

        int[] jaroscore = score(s1, s2);
        double m = jaroscore[0];
        if (m == 0) return 0.0;

        double j = ((m / s1.length() + m / s2.length() + (m -jaroscore[1]) / m)) / 3.0;
        double p = 0.1;
        int l = 0;

        for (int i = 0; i < Math.min(4, Math.min(s1.length(), s2.length())); i++) {
            if (s1.charAt(i) == s2.charAt(i)) l++;
            else break;
        }

        return j + (l * p * (1 - j));


    }


    private int[] score(String s1, String s2) {
        int maxDist = (Math.max(s1.length(), s2.length()) / 2) -1;
        boolean[] s1Matches = new boolean[s1.length()];
        boolean[] s2Matches = new boolean[s2.length()];
        int matches = 0;

        for (int i = 0; i < s1.length(); i++) {
            int start = Math.max(0, i - maxDist);
            int end = Math.min(i + maxDist + 1, s2.length());
            for (int j = start; j < end; j++) {
                if (!s2Matches[j] && s1.charAt(i) == s2.charAt(j)) {
                    s1Matches[i] = true;
                    s2Matches[j] = true;
                    matches++;
                    break;
                }
            }
        }

        if (matches == 0) return new int[]{0, 0};

        int transpositions = 0;
        int k =0;
        for (int i = 0; i < s1.length(); i++) {
            if (s1Matches[i]) {
                while (!s2Matches[k]) k++;
                if (s1.charAt(i) != s2.charAt(k)) transpositions++;
                k++;
            }
        }

        return new int[]{matches, transpositions / 2};

    }
}

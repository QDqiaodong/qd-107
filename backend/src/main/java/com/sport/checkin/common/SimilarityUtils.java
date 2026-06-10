package com.sport.checkin.common;

public class SimilarityUtils {

    private static final double DEFAULT_SIMILARITY_THRESHOLD = 0.8;

    public static double calculateSimilarity(String s1, String s2) {
        if (s1 == null && s2 == null) {
            return 1.0;
        }
        if (s1 == null || s2 == null) {
            return 0.0;
        }
        if (s1.equals(s2)) {
            return 1.0;
        }

        int distance = levenshteinDistance(s1, s2);
        int maxLength = Math.max(s1.length(), s2.length());
        if (maxLength == 0) {
            return 1.0;
        }
        return 1.0 - (double) distance / maxLength;
    }

    public static boolean isSimilar(String s1, String s2) {
        return calculateSimilarity(s1, s2) >= DEFAULT_SIMILARITY_THRESHOLD;
    }

    public static boolean isSimilar(String s1, String s2, double threshold) {
        return calculateSimilarity(s1, s2) >= threshold;
    }

    private static int levenshteinDistance(String s1, String s2) {
        int len1 = s1.length();
        int len2 = s2.length();

        int[][] dp = new int[len1 + 1][len2 + 1];

        for (int i = 0; i <= len1; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= len2; j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= len1; i++) {
            char c1 = s1.charAt(i - 1);
            for (int j = 1; j <= len2; j++) {
                char c2 = s2.charAt(j - 1);
                if (c1 == c2) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = Math.min(Math.min(dp[i - 1][j], dp[i][j - 1]), dp[i - 1][j - 1]) + 1;
                }
            }
        }

        return dp[len1][len2];
    }
}

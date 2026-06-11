package com.sport.checkin.common;

import com.sport.checkin.entity.CheckinRecord;
import com.sport.checkin.entity.SportType;

import java.math.BigDecimal;

public class IntensityClassifier {

    private static final int DURATION_THRESHOLD_LOW = 30;
    private static final int DURATION_THRESHOLD_HIGH = 60;

    private static final BigDecimal CALORIE_THRESHOLD_LOW = new BigDecimal("150");
    private static final BigDecimal CALORIE_THRESHOLD_HIGH = new BigDecimal("400");

    private static final BigDecimal CALORIE_RATE_LOW = new BigDecimal("5");
    private static final BigDecimal CALORIE_RATE_HIGH = new BigDecimal("8");

    private static final BigDecimal DISTANCE_THRESHOLD_LOW = new BigDecimal("3");
    private static final BigDecimal DISTANCE_THRESHOLD_HIGH = new BigDecimal("8");

    private static final double WEIGHT_DURATION_WITH_DIST = 0.25;
    private static final double WEIGHT_CALORIE_WITH_DIST = 0.30;
    private static final double WEIGHT_RATE_WITH_DIST = 0.25;
    private static final double WEIGHT_DISTANCE = 0.20;

    private static final double WEIGHT_DURATION_NO_DIST = 0.30;
    private static final double WEIGHT_CALORIE_NO_DIST = 0.40;
    private static final double WEIGHT_RATE_NO_DIST = 0.30;

    private static final double THRESHOLD_LIGHT = 1.7;
    private static final double THRESHOLD_HIGH = 2.3;

    public static IntensityLevel classify(CheckinRecord record, SportType sportType) {
        int durationScore = scoreDuration(record.getDuration());
        int calorieScore = scoreCalorie(record.getCalorie());
        int rateScore = scoreCalorieRate(sportType != null ? sportType.getCaloriePerMinute() : null);
        int distanceScore = scoreDistance(record.getDistance());

        boolean hasDistance = record.getDistance() != null
                && record.getDistance().compareTo(BigDecimal.ZERO) > 0;

        double weightedScore;
        if (hasDistance) {
            weightedScore = durationScore * WEIGHT_DURATION_WITH_DIST
                    + calorieScore * WEIGHT_CALORIE_WITH_DIST
                    + rateScore * WEIGHT_RATE_WITH_DIST
                    + distanceScore * WEIGHT_DISTANCE;
        } else {
            weightedScore = durationScore * WEIGHT_DURATION_NO_DIST
                    + calorieScore * WEIGHT_CALORIE_NO_DIST
                    + rateScore * WEIGHT_RATE_NO_DIST;
        }

        if (weightedScore < THRESHOLD_LIGHT) {
            return IntensityLevel.LIGHT;
        } else if (weightedScore < THRESHOLD_HIGH) {
            return IntensityLevel.MODERATE;
        } else {
            return IntensityLevel.HIGH;
        }
    }

    private static int scoreDuration(Integer duration) {
        if (duration == null || duration <= 0) {
            return 1;
        }
        if (duration < DURATION_THRESHOLD_LOW) {
            return 1;
        }
        if (duration <= DURATION_THRESHOLD_HIGH) {
            return 2;
        }
        return 3;
    }

    private static int scoreCalorie(BigDecimal calorie) {
        if (calorie == null || calorie.compareTo(BigDecimal.ZERO) <= 0) {
            return 1;
        }
        if (calorie.compareTo(CALORIE_THRESHOLD_LOW) < 0) {
            return 1;
        }
        if (calorie.compareTo(CALORIE_THRESHOLD_HIGH) <= 0) {
            return 2;
        }
        return 3;
    }

    private static int scoreCalorieRate(BigDecimal caloriePerMinute) {
        if (caloriePerMinute == null || caloriePerMinute.compareTo(BigDecimal.ZERO) <= 0) {
            return 2;
        }
        if (caloriePerMinute.compareTo(CALORIE_RATE_LOW) < 0) {
            return 1;
        }
        if (caloriePerMinute.compareTo(CALORIE_RATE_HIGH) <= 0) {
            return 2;
        }
        return 3;
    }

    private static int scoreDistance(BigDecimal distance) {
        if (distance == null || distance.compareTo(BigDecimal.ZERO) <= 0) {
            return 1;
        }
        if (distance.compareTo(DISTANCE_THRESHOLD_LOW) < 0) {
            return 1;
        }
        if (distance.compareTo(DISTANCE_THRESHOLD_HIGH) <= 0) {
            return 2;
        }
        return 3;
    }
}

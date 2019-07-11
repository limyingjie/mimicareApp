package com.project.miniCare.Utils;

import java.util.Random;

public class MockStepGenerator {

    private boolean onGround = true;

    private static final int MIN_RANDOM_PRESSURE = 0;
    private static final int MAX_RANDOM_PRESSURE = 1000;

    private static final int NO_OF_SENSORS = 6;

    private Random random = new Random();

    public int[] nextRandom() {
        int[] data = new int[NO_OF_SENSORS];
        onGround = !onGround;
        for (int i = 0; i < NO_OF_SENSORS; i++) {
            data[i] = onGround ? (random.nextInt(MAX_RANDOM_PRESSURE) - MIN_RANDOM_PRESSURE) : 0;
        }
        return data;
    }

    public int[] next() {
        int[] data = new int[NO_OF_SENSORS];
        onGround = !onGround;
        for (int i = 0; i < NO_OF_SENSORS; i++) {
            data[i] = onGround ? i * 200 : 0;
        }
        return data;
    }
}

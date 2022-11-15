package com.bethibande.web.timings;

import java.util.concurrent.TimeUnit;

public class TimingGenerator {

    private int index = 0;
    private Long[] timings;

    private Long start;

    public void start(int keyframes) {
        timings = new Long[keyframes];
        start = System.nanoTime();
    }

    public void keyframe() {
        timings[index++] = System.nanoTime();
    }

    public void reset() {
        index = 0;
    }

    public long getTiming(int index) {
        if(index == 0) return timings[index] - start;
        return timings[index] - timings[index-1];
    }

    /**
     * Returns time difference between start call and the last keyframe call. Time in nanoseconds, time unit can be changed using {@link #convert(TimeUnit, TimeUnit)}
     */
    public long getTotalTime() {
        return timings[timings.length-1] - start;
    }

    /**
     * Converts all timings to the specified time unit, default source time unit is nanoseconds
     * @param source time unit to convert from, default is nanoseconds
     * @param unit time unit to convert to
     */
    public void convert(TimeUnit source, TimeUnit unit) {
        start = unit.convert(start, source);

        for(int i = 0; i < timings.length; i++) {
            timings[i] = unit.convert(timings[i], source);
        }
    }

    public boolean isComplete() {
        return index == timings.length;
    }
}

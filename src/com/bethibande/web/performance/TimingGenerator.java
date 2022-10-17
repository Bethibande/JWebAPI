package com.bethibande.web.performance;

import java.util.ArrayList;
import java.util.List;

public class TimingGenerator {

    private List<List<Long>> timings = new ArrayList<>();
    private List<Long> active;

    private String[] labels;

    public void setLabel(String... labels) {
        this.labels = labels;
    }

    public synchronized void start() {
        List<Long> list = new ArrayList<>();
        timings.add(list);
        active = list;

        list.add(System.currentTimeMillis());
    }

    public synchronized void keyframe() {
        active.add(System.currentTimeMillis());
    }

    public List<Long> calculateAverage() {
        List<Long> average = new ArrayList<>();

        for(List<Long> values : timings) {
            for(int i = 0; i < values.size(); i++) {
                if(average.size() < values.size()) {
                    average.add(values.get(i));
                    continue;
                }
                average.set(i, average.get(i) + values.get(i));
            }
        }

        average.replaceAll(aLong -> aLong / timings.size());

        return average;
    }

    public List<Long> calculateDeviation() {
        List<Long> average = calculateAverage();
        List<Long> deviation = new ArrayList<>();

        for(List<Long> values : timings) {
            for(int i = 0; i < values.size(); i++) {
                if(deviation.size() < values.size()) {
                    deviation.add(Math.abs(values.get(i) - average.get(0)));
                    continue;
                }
                deviation.set(i, deviation.get(i) + Math.abs(values.get(i) - average.get(0)));
            }
        }

        return deviation;
    }

    public void printDeviation() {
        List<Long> deviation = calculateDeviation();

        for(int i = 0; i < deviation.size(); i++) {
            Long value = deviation.get(i);
            System.out.println(labels[i] + ": " + value);
        }
    }

    public synchronized void stop() {
        long first = active.get(0);
        active.set(0, 0L);

        for(int i = 1; i < active.size(); i++) {
            long v = active.get(i);
            active.set(i, v - first);
        }

        if(timings.size() % 10 == 0) System.out.println(timings.size());
        if(timings.size() >= 100) {
            printDeviation();
            timings.clear();
        }
    }

}

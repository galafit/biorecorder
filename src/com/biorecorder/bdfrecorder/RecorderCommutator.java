package com.biorecorder.bdfrecorder;

import com.biorecorder.ads.Commutator;

/**
 * Created by galafit on 30/3/18.
 */
public enum RecorderCommutator {
    INPUT(Commutator.INPUT),
    INPUT_SHORT(Commutator.INPUT_SHORT),
    TEST_SIGNAL(Commutator.TEST_SIGNAL);

    private Commutator adsCommutator;

    RecorderCommutator(Commutator adsCommutator) {
        this.adsCommutator = adsCommutator;
    }

    public Commutator getAdsCommutator() {
        return adsCommutator;
    }

    public static RecorderCommutator valueOf(Commutator adsCommutator) throws IllegalArgumentException {
        for (RecorderCommutator recorderCommutator : RecorderCommutator.values()) {
            if(recorderCommutator.getAdsCommutator() == adsCommutator) {
                return recorderCommutator;
            }

        }
        String msg = "Invalid commutator: "+ adsCommutator;
        throw new IllegalArgumentException(msg);
    }
}
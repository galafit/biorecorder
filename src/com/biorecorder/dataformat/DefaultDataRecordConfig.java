package com.biorecorder.dataformat;

import java.text.MessageFormat;
import java.util.ArrayList;

/**
 * Default implementation of DataRecordConfig interface
 */
public class DefaultDataRecordConfig implements DataRecordConfig {
    private double durationOfDataRecord = 1; // sec
    private ArrayList<Signal> signals = new ArrayList<Signal>();

    /**
     * Creates a DefaultDataRecordConfig instance
     * with the given number of channels (signals)
     *
     * @param numberOfSignals number of signals
     * @throws IllegalArgumentException if numberOfSignals < 0
     */
    public DefaultDataRecordConfig(int numberOfSignals) throws IllegalArgumentException {
        if (numberOfSignals < 0) {
            String errMsg = MessageFormat.format("Number of signals is invalid: {0}. Expected {1}", numberOfSignals, ">= 0");
            throw new IllegalArgumentException(errMsg);
        }
        for (int i = 0; i < numberOfSignals; i++) {
            addSignal();
        }
    }

    /**
     * Constructor to make a copy of the given DefaultDataRecordConfig
     *
     * @param dataRecordConfig DefaultDataRecordConfig instance that will be copied
     */
    public DefaultDataRecordConfig(DataRecordConfig dataRecordConfig) {
        this(dataRecordConfig.signalsCount());
        durationOfDataRecord = dataRecordConfig.getDurationOfDataRecord();
        for (int i = 0; i < dataRecordConfig.signalsCount(); i++) {
            setNumberOfSamplesInEachDataRecord(i, dataRecordConfig.getNumberOfSamplesInEachDataRecord(i));
            setPrefiltering(i, dataRecordConfig.getPrefiltering(i));
            setTransducer(i, dataRecordConfig.getTransducer(i));
            setLabel(i, dataRecordConfig.getLabel(i));
            setDigitalRange(i, dataRecordConfig.getDigitalMin(i), dataRecordConfig.getDigitalMax(i));
            setPhysicalRange(i, dataRecordConfig.getPhysicalMin(i), dataRecordConfig.getPhysicalMax(i));
            setPhysicalDimension(i, dataRecordConfig.getPhysicalDimension(i));
        }
    }

    @Override
    public double getDurationOfDataRecord() {
        return durationOfDataRecord;
    }

    @Override
    public int signalsCount() {
        return signals.size();
    }

    @Override
    public int getNumberOfSamplesInEachDataRecord(int signalNumber) {
        return signals.get(signalNumber).getNumberOfSamplesInEachDataRecord();
    }

    @Override
    public String getLabel(int signalNumber) {
        return signals.get(signalNumber).getLabel();
    }

    @Override
    public String getTransducer(int signalNumber) {
        return signals.get(signalNumber).getTransducer();
    }

    @Override
    public String getPrefiltering(int signalNumber) {
        return signals.get(signalNumber).getPrefiltering();
    }

    @Override
    public String getPhysicalDimension(int signalNumber) {
        return signals.get(signalNumber).getPhysicalDimension();
    }

    @Override
    public int getDigitalMin(int signalNumber) {
        return signals.get(signalNumber).getDigitalMin();
    }

    @Override
    public int getDigitalMax(int signalNumber) {
        return signals.get(signalNumber).getDigitalMax();
    }

    @Override
    public double getPhysicalMin(int signalNumber) {
        return signals.get(signalNumber).getPhysicalMin();
    }

    @Override
    public double getPhysicalMax(int signalNumber) {
        return signals.get(signalNumber).getPhysicalMax();
    }

    /**
     * Sets duration of DataRecords (data packages) in seconds.
     * Default value = 1 sec.
     *
     * @param durationOfDataRecord duration of DataRecords in seconds
     * @throws IllegalArgumentException if calculateDurationOfDataRecord <= 0.
     */
    public void setDurationOfDataRecord(double durationOfDataRecord) throws IllegalArgumentException {
        if (durationOfDataRecord <= 0) {
            String errMsg = MessageFormat.format("Duration of data record is invalid: {0}. Expected {1}", durationOfDataRecord, "> 0");
            throw new IllegalArgumentException(errMsg);
        }
        this.durationOfDataRecord = durationOfDataRecord;
    }

    /**
     * Sets the number of samples belonging to the signal
     * in each DataRecord (data package).
     *
     * @param signalNumber                    number of the signal(channel). Numeration starts from 0
     * @param numberOfSamplesInEachDataRecord number of samples belonging to the signal with the given sampleNumberToSignalNumber
     *                                        in each DataRecord
     * @throws IllegalArgumentException if the given getNumberOfSamplesInEachDataRecord <= 0
     */
    public void setNumberOfSamplesInEachDataRecord(int signalNumber, int numberOfSamplesInEachDataRecord) throws IllegalArgumentException {
        if (numberOfSamplesInEachDataRecord <= 0) {
            String errMsg = MessageFormat.format("Signal {0}. Number of samples in data record is invalid: {1}. Expected {2}", signalNumber, numberOfSamplesInEachDataRecord, "> 0");
            throw new IllegalArgumentException(errMsg);
        }
        signals.get(signalNumber).setNumberOfSamplesInEachDataRecord(numberOfSamplesInEachDataRecord);
    }

    public void setPrefiltering(int signalNumber, String prefiltering) {
        signals.get(signalNumber).setPrefiltering(prefiltering);
    }

    public void setTransducer(int signalNumber, String transducerType) {
        signals.get(signalNumber).setTransducer(transducerType);
    }

    public void setLabel(int signalNumber, String label) {
        signals.get(signalNumber).setLabel(label);
    }

    public void setPhysicalDimension(int signalNumber, String physicalDimension) {
        signals.get(signalNumber).setPhysicalDimension(physicalDimension);
    }

    /**
     * Sets the digital minimum and maximum values of the signal.
     * Digital min and max must be set for every signal!!!
     * <br>Default digitalMin = Integer.MIN_VALUE,  digitalMax = Integer.MAX_VALUE
     *
     * @param signalNumber number of the signal(channel). Numeration starts from 0
     * @param digitalMin   the minimum digital value of the signal
     * @param digitalMax   the maximum digital value of the signal
     * @throws IllegalArgumentException if: digitalMin >= digitalMax
     */
    public void setDigitalRange(int signalNumber, int digitalMin, int digitalMax) throws IllegalArgumentException {
        if (digitalMax <= digitalMin) {
            String errMsg = MessageFormat.format("Signal {0}. Digital min-max range is invalid. Min = {1}, Max = {2}. Expected: {3}", signalNumber, digitalMin, digitalMax, "max > min");
            throw new IllegalArgumentException(errMsg);

        }
        signals.get(signalNumber).setDigitalRange(digitalMin, digitalMax);
    }

    /**
     * Sets the physical minimum and maximum values of the signal (the values of the in
     * of the ADC when the output equals the value of "digital minimum" and "digital maximum").
     * Usually physicalMin = - physicalMax.
     * <p>
     * Physical min and max must be set for every signal!!!
     * <br>Default physicalMin = Integer.MIN_VALUE,  physicalMax = Integer.MAX_VALUE
     *
     * @param signalNumber number of the signal(channel). Numeration starts from 0
     * @param physicalMin  the minimum physical value of the signal
     * @param physicalMax  the maximum physical value of the signal
     * @throws IllegalArgumentException if physicalMin >= physicalMax
     */
    public void setPhysicalRange(int signalNumber, double physicalMin, double physicalMax) throws IllegalArgumentException {
        if (physicalMax <= physicalMin) {
            String errMsg = MessageFormat.format("Signal {0}. Physical min-max range is invalid. Min = {1}, Max = {2}. Expected: {3}", signalNumber, physicalMin, physicalMax, "max > min");
            throw new IllegalArgumentException(errMsg);
        }
        signals.get(signalNumber).setPhysicalRange(physicalMin, physicalMax);
    }


    /**
     * Add new signal.
     */
    public void addSignal() {
        Signal signal = new Signal();
        signal.setLabel("Channel_" + signals.size());
        signal.setDigitalRange(Integer.MIN_VALUE, Integer.MAX_VALUE);
        signal.setPhysicalRange(Integer.MIN_VALUE, Integer.MAX_VALUE);
        signals.add(signal);
    }

    /**
     * Removes the signal.
     *
     * @param signalNumber number of the signal(channel) to remove. Numeration starts from 0
     */
    public void removeSignal(int signalNumber) {
        signals.remove(signalNumber);
    }


    class Signal {
        private int numberOfSamplesInEachDataRecord;
        private String prefiltering = "";
        private String transducerType = "Unknown";
        private String label = "";
        private int digitalMin;
        private int digitalMax;
        private double physicalMin;
        private double physicalMax;
        private String physicalDimension = "";  // uV or Ohm


        public int getDigitalMin() {
            return digitalMin;
        }

        public int getDigitalMax() {
            return digitalMax;
        }

        public double getPhysicalMin() {
            return physicalMin;
        }

        public double getPhysicalMax() {
            return physicalMax;
        }

        public String getPhysicalDimension() {
            return physicalDimension;
        }

        public int getNumberOfSamplesInEachDataRecord() {
            return numberOfSamplesInEachDataRecord;
        }

        public void setDigitalRange(int digitalMin, int digitalMax) {
            this.digitalMin = digitalMin;
            this.digitalMax = digitalMax;
        }

        public void setPhysicalRange(double physicalMin, double physicalMax) {
            this.physicalMin = physicalMin;
            this.physicalMax = physicalMax;
        }

        public void setPhysicalDimension(String physicalDimension) {
            this.physicalDimension = physicalDimension;
        }


        public void setNumberOfSamplesInEachDataRecord(int numberOfSamplesInEachDataRecord) {
            this.numberOfSamplesInEachDataRecord = numberOfSamplesInEachDataRecord;
        }

        public String getPrefiltering() {
            return prefiltering;
        }

        public void setPrefiltering(String prefiltering) {
            this.prefiltering = prefiltering;
        }

        public String getTransducer() {
            return transducerType;
        }

        public void setTransducer(String transducerType) {
            this.transducerType = transducerType;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

    }

}

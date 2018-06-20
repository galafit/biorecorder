package com.biorecorder.gui;

/**
 * Created by galafit on 17/6/18.
 */
public interface RecorderSettings {
    String getDeviceType();

    void setDeviceType(String recorderType);

    int getChannelsCount();

    int getMaxFrequency();

    void setMaxFrequency(int frequency);

    String getPatientIdentification();

    void setPatientIdentification(String patientIdentification);

    String getRecordingIdentification();

    void setRecordingIdentification(String recordingIdentification);

    boolean is50HzFilterEnabled(int channelNumber);

    void set50HzFilterEnabled(int channelNumber, boolean is50HzFilterEnabled);

    String getComportName();

    void setComportName(String comportName);

    String getDirToSave();

    void setDirToSave(String dirToSave);

    String getFileName();

    void setFileName(String fileName);

    boolean isChannelEnabled(int channelNumber);

    void setChannelEnabled(int channelNumber, boolean enabled);

    String getChannelName(int channelNumber);

    void setChannelName(int channelNumber, String name);

    int getChannelGain(int channelNumber);

    void setChannelGain(int channelNumber, int gainValue);

    String getChannelMode(int channelNumber);

    void setChannelMode(int channelNumber, String mode);

    int getChannelSampleRate(int channelNumber);

    void setChannelFrequency(int channelNumber, int channelFrequency);

    void setAccelerometerEnabled(boolean accelerometerEnabled);

    boolean isAccelerometerEnabled();

    String getAccelerometerName();

    int getAccelerometerFrequency();

    String getAccelerometerMode();

    void setAccelerometerMode(String mode);


    public String[] getAvailableDeviseTypes();

    public  Integer[] getAvailableMaxFrequencies();

    public Integer[] getChannelsAvailableFrequencies();

    public  Integer[] getChannelsAvailableGains();

    public String[] getChannelsAvailableModes();

    public  String[] getAccelerometerAvailableModes();

    public String[] getAvailableComports();


}
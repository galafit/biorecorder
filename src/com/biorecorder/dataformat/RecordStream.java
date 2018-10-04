package com.biorecorder.dataformat;

/**
 * Created by galafit on 28/7/18.
 */
public interface RecordStream {
    void setRecordConfig(RecordConfig recordConfig);
    void writeRecord(int[] dataRecord);
    void close();
}

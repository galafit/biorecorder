package com.biorecorder.filters;

import com.biorecorder.dataformat.*;

/**
 * Permits to join (piece together) given number of incoming DataRecords.
 * out  data records (that will be send to the listener)
 * have the following structure:
 * <br>  number of samples from channel_0 in original DataRecord * numberOfRecordsToJoin ,
 * <br>  number of samples from channel_1 in original DataRecord * numberOfRecordsToJoin,
 * <br>  ...
 * <br>  number of samples from channel_i in original DataRecord * numberOfRecordsToJoin
 * <p>
 *
 * <br>duration of resulting DataRecord = duration of original DataRecord * numberOfRecordsToJoin
 */
public class RecordsJoiner extends RecordFilter {
    private int numberOfRecordsToJoin;
    private int[] outDataRecord;
    private int joinedRecordsCounter;
    private int outRecordSize;

    public RecordsJoiner(RecordConfig inConfig, int numberOfRecordsToJoin) {
        super(inConfig);
        this.numberOfRecordsToJoin = numberOfRecordsToJoin;
        int inRecordSize = 0;
        for (int i = 0; i < inConfig.signalsCount(); i++) {
            inRecordSize += inConfig.getNumberOfSamplesInEachDataRecord(i);
         }
        outRecordSize = inRecordSize * numberOfRecordsToJoin;
        outDataRecord = new int[outRecordSize];
    }



    @Override
    public RecordConfig dataConfig() {
        DefaultRecordConfig outConfig = new DefaultRecordConfig(inConfig);
        outConfig.setDurationOfDataRecord(inConfig.getDurationOfDataRecord() * numberOfRecordsToJoin);
        for (int i = 0; i < outConfig.signalsCount(); i++) {
            outConfig.setNumberOfSamplesInEachDataRecord(i, inConfig.getNumberOfSamplesInEachDataRecord(i) * numberOfRecordsToJoin);
        }
        return outConfig;
    }


    /**
     * Accumulate and join the specified number of incoming samples into one out
     * DataRecord and when it is ready send it to the dataListener
     */
    @Override
    protected void filterData(int[] inputRecord)  {
        int signalNumber = 0;
        int signalStart = 0;
        int signalSamples = inConfig.getNumberOfSamplesInEachDataRecord(signalNumber);
        for (int inSamplePosition = 0; inSamplePosition < inRecordSize; inSamplePosition++) {

            if(inSamplePosition >= signalStart + signalSamples) {
                signalStart += signalSamples;
                signalNumber++;
                signalSamples = inConfig.getNumberOfSamplesInEachDataRecord(signalNumber);
            }

            int outSamplePosition = signalStart * numberOfRecordsToJoin;
            outSamplePosition += joinedRecordsCounter * signalSamples;
            outSamplePosition += inSamplePosition - signalStart;

            outDataRecord[outSamplePosition] = inputRecord[inSamplePosition];
        }

        joinedRecordsCounter++;

        if(joinedRecordsCounter == numberOfRecordsToJoin) {
            outStream.writeRecord(outDataRecord);
            outDataRecord = new int[outRecordSize];
            joinedRecordsCounter = 0;
        }
    }

    /**
     * Unit Test. Usage Example.
     */
    public static void main(String[] args) {

        // 0 channel 3 samples, 1 channel 2 samples, 3 channel 4 samples
        int[] dataRecord = {1,3,8,  2,4,  7,6,8,6};

        DefaultRecordConfig dataConfig = new DefaultRecordConfig(3);
        dataConfig.setNumberOfSamplesInEachDataRecord(0, 3);
        dataConfig.setNumberOfSamplesInEachDataRecord(1, 2);
        dataConfig.setNumberOfSamplesInEachDataRecord(2, 4);


        // join 2 records
        RecordsJoiner recordFilter = new RecordsJoiner(dataConfig, 2);


        // expected dataRecord
        int[] expectedDataRecord = {1,3,8,1,3,8,  2,4,2,4,  7,6,8,6,7,6,8,6};

        recordFilter.setOutStream(new RecordStream() {
            @Override
            public void writeRecord(int[] dataRecord1) {
                boolean isTestOk = true;
                if(expectedDataRecord.length != dataRecord1.length) {
                    System.out.println("Error!!! Resultant record length: "+dataRecord1.length+ " Expected record length : "+expectedDataRecord.length);
                    isTestOk = false;
                }

                for (int i = 0; i < dataRecord1.length; i++) {
                    if(dataRecord1[i] != expectedDataRecord[i]) {
                        System.out.println(i + " resultant data: "+dataRecord1[i]+ " expected data: "+expectedDataRecord[i]);
                        isTestOk = false;
                        break;
                    }
                }

                System.out.println("Is test ok: "+isTestOk);
            }

            @Override
            public void close() {

            }
        });

        // send 4 records and get as result 2 joined records
        recordFilter.writeRecord(dataRecord);
        recordFilter.writeRecord(dataRecord);
        recordFilter.writeRecord(dataRecord);
        recordFilter.writeRecord(dataRecord);
    }
}
package com.biorecorder;

import com.biorecorder.gui.MainFrame;
import com.biorecorder.gui.RecorderView;
import com.biorecorder.gui.RecorderViewModel;

import java.util.Set;

public class Start {
    public static void main(String[] args) {
        System.out.println("JVM Bit size: " + System.getProperty("sun.arch.data.model"));
        JsonPreferences preferences = new JsonPreferences();
        RecorderViewModel bdfRecorder = new RecorderViewModelImpl(new EdfBioRecorderApp(), preferences);
        MainFrame recorderView = new MainFrame(bdfRecorder);
        bdfRecorder.addProgressListener(recorderView);
        bdfRecorder.addAvailableComportsListener(recorderView);
        bdfRecorder.addStateChangeListener(recorderView);

    }
}

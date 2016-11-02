package com.crostec.gui;

import com.crostec.ads.*;
import com.crostec.bdfrecorder.Controller;
import com.crostec.gui.comport_gui.ComportDataProviderMock;
import com.crostec.gui.comport_gui.ComportUI;
import com.crostec.gui.file_gui.FileToSaveUI;
import comport.ComportFacade;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;


/**
 *
 */
public class SettingsWindow extends JFrame implements AdsDataListener {

    private Controller controller;
    private BdfHeaderData bdfHeaderData;
    private int adsDataFrameSize;
    private int adsDataFrameCounter;
    private int adsDataFrameFrequency;
    private String patientIdentificationLabel = "Patient";
    private String recordingIdentificationLabel = "Record";
    private String spsLabel = "Maximum Frequency (Hz)";
    private JComboBox spsField;
    private JComboBox[] channelFrequency;
    private JComboBox[] channelGain;
    private JComboBox[] channelCommutatorState;
    private JCheckBox[] channelEnable;
    private JCheckBox[] channel50Hz;
    private JTextField[] channelName;

    private JComboBox accelerometerFrequency;
    private JTextField accelerometerName;
    private JCheckBox accelerometerEnable;
    private JTextField patientIdentification;
    private JTextField recordingIdentification;

    private String start = "Start";
    private String stop = "Stop";
    private JButton startButton = new JButton(start);

    private ComportUI comportUI;
    private FileToSaveUI fileToSaveUI;

    private Color colorProcess = Color.GREEN;
    private Color colorProblem = Color.RED;
    private Color colorInfo = Color.GRAY;
    private MarkerLabel markerLabel = new MarkerLabel();
    private JLabel reportLabel = new JLabel(" ");

    Icon iconShow = new ImageIcon("img/arrow-open.png");
    Icon iconHide = new ImageIcon("img/arrow-close.png");
    Icon iconConnected = new ImageIcon("img/greenBall.png");
    Icon iconDisconnected = new ImageIcon("img/redBall.png");
    Icon iconDisabled = new ImageIcon("img/grayBall.png");
    private MarkerLabel[] channelLoffStatPositive;
    private MarkerLabel[] channelLoffStatNegative;
    private JCheckBox[] channelLoffEnable;
    private String title = "EDF Recorder";
    private JComponent[] channelsHeaders = {new JLabel("Number"), new JLabel("Enable"), new JLabel("Name"), new JLabel("Frequency (Hz)"),
            new JLabel("Gain"), new JLabel("Commutator State"), new JLabel("Lead Off Detection"), new JLabel(" "),new JLabel("50 Hz Filter")};


    public SettingsWindow(Controller controller, BdfHeaderData bdfHeaderData) {
        this.controller = controller;
        this.bdfHeaderData = bdfHeaderData;
        init();
        arrangeForm();
        setActions();
        loadDataFromModel();
        setVisible(true);
    }

    private void init() {
        int adsChannelsNumber = bdfHeaderData.getAdsConfiguration().getAdsChannels().size();

        spsField = new JComboBox(Sps.values());
        spsField.setSelectedItem(bdfHeaderData.getAdsConfiguration().getSps());

        comportUI = new ComportUI(new ComportFacade(), bdfHeaderData.getAdsConfiguration().getComPortName());
        fileToSaveUI = new FileToSaveUI();

        int textFieldLength = 25;
        patientIdentification = new JTextField(textFieldLength);
        recordingIdentification = new JTextField(textFieldLength);

        channelFrequency = new JComboBox[adsChannelsNumber];
        channelGain = new JComboBox[adsChannelsNumber];
        channelCommutatorState = new JComboBox[adsChannelsNumber];
        channelEnable = new JCheckBox[adsChannelsNumber];
        channel50Hz = new JCheckBox[adsChannelsNumber];
        channelName = new JTextField[adsChannelsNumber];
        channelLoffStatPositive = new MarkerLabel[adsChannelsNumber];
        channelLoffStatNegative = new MarkerLabel[adsChannelsNumber];
        channelLoffEnable = new JCheckBox[adsChannelsNumber];
        textFieldLength = 16;
        for (int i = 0; i < adsChannelsNumber; i++) {
            channelFrequency[i] = new JComboBox();
            channelGain[i] = new JComboBox();
            channelCommutatorState[i] = new JComboBox();
            channelEnable[i] = new JCheckBox();
            channel50Hz[i] = new JCheckBox();
            channelName[i] = new JTextField(textFieldLength);
            channelLoffStatPositive[i] = new MarkerLabel(iconDisabled);
            channelLoffStatNegative[i] = new MarkerLabel(iconDisabled);
            channelLoffEnable[i] = new JCheckBox();
        }
        accelerometerEnable = new JCheckBox();
        accelerometerName = new JTextField(textFieldLength);
        accelerometerFrequency = new JComboBox();
    }

    private void setActions() {

        for (int i = 0; i < bdfHeaderData.getAdsConfiguration().getAdsChannels().size(); i++) {
            channelEnable[i].addActionListener(new AdsChannelEnableListener(i));
        }

        accelerometerEnable.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                JCheckBox checkBox = (JCheckBox) actionEvent.getSource();
                if (checkBox.isSelected()) {
                    enableAccelerometer(true);
                } else {
                    enableAccelerometer(false);
                }
            }
        });


        spsField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                JComboBox comboBox = (JComboBox) actionEvent.getSource();
                Sps sps = (Sps) comboBox.getSelectedItem();
                setChannelsFrequencies(sps);
            }
        });


        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (controller.isRecording()) {
                    controller.stopRecording();
                    startButton.setText(start);
                    enableFields();
                    setProcessReport("Saved to file: " + bdfHeaderData.getFileNameToSave());  //todo enter file name
                } else {
                    if (!isComportAvailable()){
                        String msg = "No ComPort with the name: " + getComPortName()+ " !\n"+
                                "Check if the «USB receiver» is connected\n" +
                                "and choose the correct ComPort";
                        JOptionPane.showMessageDialog(SettingsWindow.this, msg);

                    }
                    else{
                        startButton.setText(stop);
                        disableFields();
                        saveDataToModel();
                        saveComPortData();
                        adsDataFrameSize = AdsUtils.getDecodedFrameSize(bdfHeaderData.getAdsConfiguration());
                        adsDataFrameCounter = 0;
                        adsDataFrameFrequency = bdfHeaderData.getAdsConfiguration().getSps().getValue() / bdfHeaderData.getAdsConfiguration().getDeviceType().getMaxDiv().getValue();
                        setProcessReport("Connecting...");
                        controller.startRecording(bdfHeaderData);
                    }
                }
            }
        });

        patientIdentification.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent focusEvent) {
                patientIdentification.selectAll();
            }
        });


        recordingIdentification.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent focusEvent) {
                recordingIdentification.selectAll();
            }
        });


        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                saveDataToModel();
                controller.closeApplication(bdfHeaderData);
            }
        });
    }


    private void arrangeForm() {
        setTitle(title);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(startButton);

        int hgap = 5;
        int vgap = 0;
        JPanel spsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, hgap, vgap));
        spsPanel.add(new JLabel(spsLabel));
        spsPanel.add(spsField);


        hgap = 60;
        vgap = 15;
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, hgap, vgap));
        topPanel.add(comportUI);
        topPanel.add(spsPanel);
        topPanel.add(buttonPanel);


        hgap = 20;
        vgap = 5;
        JPanel channelsPanel = new JPanel(new TableLayout(channelsHeaders.length, new TableOption(TableOption.CENTRE, TableOption.CENTRE), hgap, vgap));

        for (JComponent component : channelsHeaders) {
            channelsPanel.add(component);
        }

        for (int i = 0; i < bdfHeaderData.getAdsConfiguration().getAdsChannels().size(); i++) {
            channelsPanel.add(new JLabel(" " + (i + 1) + " "));
            channelsPanel.add(channelEnable[i]);
            channelsPanel.add(channelName[i]);
            channelsPanel.add(channelFrequency[i]);
            channelsPanel.add(channelGain[i]);
            channelsPanel.add(channelCommutatorState[i]);
            JPanel loffPanel = new JPanel();
            loffPanel.add(channelLoffEnable[i]);
            loffPanel.add(channelLoffStatPositive[i]);
            loffPanel.add(channelLoffStatNegative[i]);
            channelsPanel.add(loffPanel);
            channelsPanel.add(new JLabel(" "));
                        channelsPanel.add(channel50Hz[i]);
        }

        // Add line of accelerometer
        channelsPanel.add(new JLabel(" " + (1 + bdfHeaderData.getAdsConfiguration().getAdsChannels().size()) + " "));
        channelsPanel.add(accelerometerEnable);
        channelsPanel.add(accelerometerName);
        channelsPanel.add(accelerometerFrequency);

        hgap = 0;
        vgap = 10;
        JPanel channelsBorderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, hgap, vgap));
        channelsBorderPanel.setBorder(BorderFactory.createTitledBorder("Channels"));
        channelsBorderPanel.add(channelsPanel);

        hgap = 5;
        vgap = 0;
        JPanel patientPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, hgap, vgap));
        patientPanel.add(new JLabel(patientIdentificationLabel));
        patientPanel.add(patientIdentification);

        JPanel recordingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, hgap, vgap));
        recordingPanel.add(new JLabel(recordingIdentificationLabel));
        recordingPanel.add(recordingIdentification);

        hgap = 0;
        vgap = 0;
        JPanel identificationPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, hgap, vgap));
        identificationPanel.add(patientPanel);
//        identificationPanel.add(new Label("    "));
        identificationPanel.add(recordingPanel);

        hgap = 15;
        vgap = 5;
        JPanel identificationBorderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, hgap, vgap));
        identificationBorderPanel.setBorder(BorderFactory.createTitledBorder("Identification"));
        identificationBorderPanel.add(identificationPanel);



        hgap = 10;
        vgap = 5;
        JPanel reportPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, hgap, vgap));
        reportPanel.add(markerLabel);
        reportPanel.add(reportLabel);

        hgap = 0;
        vgap = 5;
        JPanel adsPanel = new JPanel(new BorderLayout(hgap, vgap));
        adsPanel.add(channelsBorderPanel, BorderLayout.NORTH);
        adsPanel.add(identificationBorderPanel, BorderLayout.CENTER);
        adsPanel.add(fileToSaveUI, BorderLayout.SOUTH);

        // Root Panel of the SettingsWindow
        add(topPanel, BorderLayout.NORTH);
        add(adsPanel, BorderLayout.CENTER);
        add(reportPanel, BorderLayout.SOUTH);

        // set the same size for identificationPanel and  saveAsPanel
        int height = Math.max(identificationBorderPanel.getPreferredSize().height, fileToSaveUI.getPreferredSize().height);
        int width = Math.max(identificationBorderPanel.getPreferredSize().width, fileToSaveUI.getPreferredSize().width);
        fileToSaveUI.setPreferredSize(new Dimension(width, height));
        identificationBorderPanel.setPreferredSize(new Dimension(width, height));


        pack();
        // place the window to the screen center
        setLocationRelativeTo(null);
    }

    private void disableEnableFields(boolean isEnable) {
        spsField.setEnabled(isEnable);
        patientIdentification.setEnabled(isEnable);
        recordingIdentification.setEnabled(isEnable);
        fileToSaveUI.setEnabled(isEnable);
        comportUI.setEnabled(isEnable);
        accelerometerName.setEnabled(isEnable);
        accelerometerEnable.setEnabled(isEnable);
        accelerometerFrequency.setEnabled(isEnable);

        for (int i = 0; i < bdfHeaderData.getAdsConfiguration().getAdsChannels().size(); i++) {
            channelEnable[i].setEnabled(isEnable);
            channel50Hz[i].setEnabled(isEnable);
            channelName[i].setEnabled(isEnable);
            channelFrequency[i].setEnabled(isEnable);
            channelGain[i].setEnabled(isEnable);
            channelCommutatorState[i].setEnabled(isEnable);
            channelLoffEnable[i].setEnabled(isEnable);
        }
    }


    private void disableFields() {
        boolean isEnable = false;
        disableEnableFields(isEnable);


    }


    private void enableFields() {
        boolean isEnable = true;
        disableEnableFields(isEnable);
        for (int i = 0; i < bdfHeaderData.getAdsConfiguration().getAdsChannels().size(); i++) {
            if (!isChannelEnable(i)) {
                enableAdsChannel(i, false);
            }
        }
        if (!bdfHeaderData.getAdsConfiguration().isAccelerometerEnabled()) {
            enableAccelerometer(false);
        }
    }

    private void setReport(String report, Color markerColor) {
        int rowLength = 100;
        String htmlReport = convertToHtml(report, rowLength);
        reportLabel.setText(htmlReport);
        markerLabel.setColor(markerColor);
    }

    public void setProcessReport(String report) {
        setReport(report, colorProcess);
    }

    private void loadDataFromModel() {
        spsField.setSelectedItem(bdfHeaderData.getAdsConfiguration().getSps());
        fileToSaveUI.setDirectory(bdfHeaderData.getDirectoryToSave());
//        fileToSave.setText(FILENAME_PATTERN);
        patientIdentification.setText(bdfHeaderData.getPatientIdentification());
        recordingIdentification.setText(bdfHeaderData.getRecordingIdentification());
        int numberOfAdsChannels = bdfHeaderData.getAdsConfiguration().getAdsChannels().size();
        for (int i = 0; i < numberOfAdsChannels; i++) {
            AdsChannelConfiguration channel = bdfHeaderData.getAdsConfiguration().getAdsChannels().get(i);
            channelName[i].setText(bdfHeaderData.getAdsChannelNames().get(i));
            channelEnable[i].setSelected(channel.isEnabled());
            if (!channel.isEnabled()) {
                enableAdsChannel(i, false);
            }
            channel50Hz[i].setSelected(channel.is50HzFilterEnabled());
            channelLoffEnable[i].setSelected(channel.isLoffEnable());
        }

        accelerometerName.setText("Accelerometer");
        accelerometerEnable.setSelected(bdfHeaderData.getAdsConfiguration().isAccelerometerEnabled());
        if (!bdfHeaderData.getAdsConfiguration().isAccelerometerEnabled()) {
            enableAccelerometer(false);
        }
        setChannelsFrequencies(bdfHeaderData.getAdsConfiguration().getSps());
        setChannelsGain();
        setChannelsCommutatorState();
    }

    public void updateLoffStatus(int[] dataFrame) {
        if(bdfHeaderData.getAdsConfiguration().getDeviceType() == DeviceType.ADS1298){
            updateLoffStatus8ch(dataFrame);
        }
        if(bdfHeaderData.getAdsConfiguration().getDeviceType() == DeviceType.ADS1292){
            updateLoffStatus2ch(dataFrame);
        }
    }

    private void updateLoffStatus2ch(int[] dataFrame) {
        int loffStatusRegisterValue = dataFrame[dataFrame.length - 2];
        List<AdsChannelConfiguration> channelsList = bdfHeaderData.getAdsConfiguration().getAdsChannels();
        for (int i = 0; i < bdfHeaderData.getAdsConfiguration().getDeviceType().getNumberOfAdsChannels(); i++) {
            AdsChannelConfiguration channelConfiguration = channelsList.get(i);
            if (channelConfiguration.isEnabled() && channelConfiguration.getCommutatorState() == CommutatorState.INPUT &&
                    channelConfiguration.isLoffEnable()) {
                if ((loffStatusRegisterValue & (int) Math.pow(2, i*2)) == 0) {
                    channelLoffStatPositive[i].setIcon(iconConnected);
                } else {
                    channelLoffStatPositive[i].setIcon(iconDisconnected);
                }
                if ((loffStatusRegisterValue & (int) Math.pow(2, (i*2)+1)) == 0) {
                    channelLoffStatNegative[i].setIcon(iconConnected);
                } else {
                    channelLoffStatNegative[i].setIcon(iconDisconnected);
                }
            }else {
                channelLoffStatPositive[i].setIcon(iconDisabled);
                channelLoffStatNegative[i].setIcon(iconDisabled);
            }
        }
    }

    private void updateLoffStatus8ch(int[] dataFrame) {
        List<AdsChannelConfiguration> channelsList = bdfHeaderData.getAdsConfiguration().getAdsChannels();
        for (int i = 0; i < bdfHeaderData.getAdsConfiguration().getDeviceType().getNumberOfAdsChannels(); i++) {
            AdsChannelConfiguration channelConfiguration = channelsList.get(i);
            if (channelConfiguration.isEnabled() && channelConfiguration.getCommutatorState() == CommutatorState.INPUT &&
                    channelConfiguration.isLoffEnable()) {
                if ((dataFrame[dataFrame.length - 2] & (int) Math.pow(2, i)) == 0) {
                    channelLoffStatPositive[i].setIcon(iconConnected);
                } else {
                    channelLoffStatPositive[i].setIcon(iconDisconnected);
                }
                if ((dataFrame[dataFrame.length - 1] & (int) Math.pow(2, i)) == 0) {
                    channelLoffStatNegative[i].setIcon(iconConnected);
                } else {
                    channelLoffStatNegative[i].setIcon(iconDisconnected);
                }
            }else {
                channelLoffStatPositive[i].setIcon(iconDisabled);
                channelLoffStatNegative[i].setIcon(iconDisabled);
            }
        }
    }

    private void saveDataToModel() {
        bdfHeaderData.getAdsConfiguration().setSps(getSps());
        bdfHeaderData.setPatientIdentification(getPatientIdentification());
        bdfHeaderData.setRecordingIdentification(getRecordingIdentification());
        bdfHeaderData.getAdsChannelNames().clear();
        for (int i = 0; i < bdfHeaderData.getAdsConfiguration().getAdsChannels().size(); i++) {
            AdsChannelConfiguration channel = bdfHeaderData.getAdsConfiguration().getAdsChannels().get(i);
            channel.setName(getChannelName(i));
            channel.setDivider(getChannelDivider(i));
            channel.setEnabled(isChannelEnable(i));
            channel.set50HzFilterEnabled(is50HzFilterEnable(i));
            channel.setGain(getChannelGain(i));
            channel.setCommutatorState(getChannelCommutatorState(i));
            channel.setLoffEnable(channelLoffEnable[i].isSelected());
        }
        bdfHeaderData.getAdsConfiguration().setAccelerometerEnabled(isAccelerometerEnable());
        bdfHeaderData.getAdsConfiguration().setAccelerometerDivider(getAccelerometerDivider());
        bdfHeaderData.setFileNameToSave(fileToSaveUI.getFilename());
        bdfHeaderData.setDirectoryToSave(fileToSaveUI.getDirectory());
    }

    private void saveComPortData(){
            bdfHeaderData.getAdsConfiguration().setComPortName(getComPortName());
    }

    private boolean isComportAvailable() {
        return comportUI.isCurrentComportAvailable();
    }

    private void setChannelsFrequencies(Sps sps) {
        int numberOfAdsChannels = bdfHeaderData.getAdsConfiguration().getAdsChannels().size();
        Divider[] adsChannelsDividers = bdfHeaderData.getAdsConfiguration().getDeviceType().getChannelsAvailableDividers();
        // set available frequencies
        for (int i = 0; i < numberOfAdsChannels; i++) {
            channelFrequency[i].removeAllItems();
            for (Divider divider : adsChannelsDividers) {
                channelFrequency[i].addItem(sps.getValue()/divider.getValue());
            }
            // select channel frequency
            AdsChannelConfiguration channel = bdfHeaderData.getAdsConfiguration().getAdsChannels().get(i);
            Integer frequency = sps.getValue() / channel.getDivider().getValue();
            channelFrequency[i].setSelectedItem(frequency);
        }
        Divider[] accelerometerAvailableDividers = bdfHeaderData.getAdsConfiguration().getDeviceType().getGetAccelerometerAvailableDividers();
        accelerometerFrequency.removeAllItems();
        for (Divider divider : accelerometerAvailableDividers) {
            accelerometerFrequency.addItem(sps.getValue()/divider.getValue());
        }
        // select channel frequency
        Integer frequency = sps.getValue() / bdfHeaderData.getAdsConfiguration().getAccelerometerDivider().getValue();
        accelerometerFrequency.setSelectedItem(frequency);
        if (numberOfAdsChannels > 0) {
            // put the size if field   accelerometerFrequency equal to the size of fields  channelFrequency
            accelerometerFrequency.setPreferredSize(channelFrequency[0].getPreferredSize());
        }
    }

    private void setChannelsGain(){
        int numberOfAdsChannels = bdfHeaderData.getAdsConfiguration().getAdsChannels().size();
        for (int i = 0; i < numberOfAdsChannels; i++) {
            channelGain[i].removeAllItems();
            for (Gain gain : Gain.values()) {
                channelGain[i].addItem(gain.getValue());
            }
            AdsChannelConfiguration channel = bdfHeaderData.getAdsConfiguration().getAdsChannels().get(i);
            channelGain[i].setSelectedItem(channel.getGain().getValue());
        }
    }

    private void setChannelsCommutatorState(){
        int numberOfAdsChannels = bdfHeaderData.getAdsConfiguration().getAdsChannels().size();
        for (int i = 0; i < numberOfAdsChannels; i++) {
            channelCommutatorState[i].removeAllItems();
            for (CommutatorState commutatorState : CommutatorState.values()) {
                channelCommutatorState[i].addItem(commutatorState.toString());
            }
            AdsChannelConfiguration channel = bdfHeaderData.getAdsConfiguration().getAdsChannels().get(i);
            channelCommutatorState[i].setSelectedItem(channel.getCommutatorState().toString());
        }
    }

    private void enableAdsChannel(int channelNumber, boolean isEnable) {
        channel50Hz[channelNumber].setEnabled(isEnable);
        channelFrequency[channelNumber].setEnabled(isEnable);
        channelGain[channelNumber].setEnabled(isEnable);
        channelCommutatorState[channelNumber].setEnabled(isEnable);
        channelName[channelNumber].setEnabled(isEnable);
        channelLoffEnable[channelNumber].setEnabled(isEnable);
        channelLoffStatPositive[channelNumber].setIcon(iconDisabled);
        channelLoffStatNegative[channelNumber].setIcon(iconDisabled);
    }


    private void enableAccelerometer(boolean isEnable) {
        accelerometerName.setEnabled(isEnable);
        accelerometerFrequency.setEnabled(isEnable);

    }

    private Divider getChannelDivider(int channelNumber) {
        int divider = bdfHeaderData.getAdsConfiguration().getSps().getValue() / getChannelFrequency(channelNumber);
        return Divider.valueOf(divider);
    }

    private Divider getAccelerometerDivider() {
        int divider = bdfHeaderData.getAdsConfiguration().getSps().getValue() / getAccelerometerFrequency();
        return Divider.valueOf(divider);
    }


    private int getChannelFrequency(int channelNumber) {
        return (Integer) channelFrequency[channelNumber].getSelectedItem();
    }

    private Gain getChannelGain(int channelNumber) {
        return Gain.valueOf(((Integer)channelGain[channelNumber].getSelectedItem()));
    }

    private CommutatorState getChannelCommutatorState(int channelNumber) {
        return CommutatorState.valueOf(((String)channelCommutatorState[channelNumber].getSelectedItem()));
    }

    private boolean isChannelEnable(int channelNumber) {
        return channelEnable[channelNumber].isSelected();
    }

    private boolean is50HzFilterEnable(int channelNumber) {
        return channel50Hz[channelNumber].isSelected();
    }

    private String getChannelName(int channelNumber) {
        return channelName[channelNumber].getText();
    }

    private String getComPortName() {
        return comportUI.getCurrentComport();
    }

    private String getPatientIdentification() {
        return patientIdentification.getText();
    }

    private String getRecordingIdentification() {
        return recordingIdentification.getText();
    }

    private boolean isAccelerometerEnable() {
        return accelerometerEnable.isSelected();
    }

    private int getAccelerometerFrequency() {
        return (Integer) accelerometerFrequency.getSelectedItem();
    }

    private Sps getSps() {
        return (Sps) spsField.getSelectedItem();
    }


    private String convertToHtml(String text, int rowLength) {
        StringBuilder html = new StringBuilder("<html>");
        String[] givenRows = text.split("\n");
        for (String givenRow : givenRows) {
            String[] splitRows = split(givenRow, rowLength);
            for (String row : splitRows) {
                html.append(row);
                html.append("<br>");
            }
        }
        html.append("</html>");
        return html.toString();
    }

    // split input string to the  array of strings with length() <= rowLength
    private String[] split(String text, int rowLength) {
        ArrayList<String> resultRows = new ArrayList<String>();
        StringBuilder row = new StringBuilder();
        String[] words = text.split(" ");
        for (String word : words) {
            if ((row.length() + word.length()) < rowLength) {
                row.append(word);
                row.append(" ");
            } else {
                resultRows.add(row.toString());
                row = new StringBuilder(word);
                row.append(" ");
            }
        }
        resultRows.add(row.toString());
        String[] resultArray = new String[resultRows.size()];
        return resultRows.toArray(resultArray);
    }

    @Override
    public void onAdsDataReceived(final int[] dataFrame) {
        //update GUI every second
        adsDataFrameCounter++;
        if (adsDataFrameCounter % adsDataFrameFrequency == 0) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    updateLoffStatus(dataFrame);
                    setProcessReport("Recording... " + adsDataFrameCounter / adsDataFrameFrequency + " data records");
                }
            });
        }
    }

    @Override
    public void onStopRecording() {
        //To change body of implemented methods use File | Settings | File Templates.
    }


    private class AdsChannelEnableListener implements ActionListener {
        private int channelNumber;

        private AdsChannelEnableListener(int channelNumber) {
            this.channelNumber = channelNumber;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            JCheckBox checkBox = (JCheckBox) actionEvent.getSource();
            if (checkBox.isSelected()) {
                enableAdsChannel(channelNumber, true);
            } else {
                enableAdsChannel(channelNumber, false);
            }
        }
    }
}
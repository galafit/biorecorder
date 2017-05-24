package com.biorecorder.ads;

import java.util.List;
import java.util.TimerTask;

/**
 * Send hello-request and set whether devise is active.
 * Or send hardware-request and set 1)whether devise is active 2) device-type
 */
class HelloTimerTask extends TimerTask  implements MessageListener {
    private boolean isMsgRecived = false;
    private AdsState adsState;
    private ComPort comPort;
    private List<Byte> command;

    public HelloTimerTask(AdsState adsState, ComPort comPort, List<Byte> command) {
        this.adsState = adsState;
        this.comPort = comPort;
        this.command = command;
    }

    @Override
    public void onMessageReceived(AdsMessage adsMessage) {
       if(adsMessage == AdsMessage.HELLO) {
           isMsgRecived = true;
           adsState.setActive(true);
       }
       if(adsMessage == AdsMessage.ADS_2_CHANNELS) {
            isMsgRecived = true;
            adsState.setActive(true);
            adsState.setDeviceType(DeviceType.ADS_2);
       }
       if(adsMessage == AdsMessage.ADS_8_CHANNELS) {
            isMsgRecived = true;
            adsState.setActive(true);
            adsState.setDeviceType(DeviceType.ADS_8);
       }
    }

    @Override
    public void run() {
        if(! isMsgRecived) {
            adsState.setActive(false);
            adsState.setDeviceType(null);

        }
        isMsgRecived = false;
        comPort.writeToPort(command);
    }
}

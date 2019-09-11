package com.project.mimiCare.Data;

public class AppState {
    private static String bleDeviceAddress;

    public static String getBleDeviceAddress() {
        return bleDeviceAddress;
    }

    public static void setBleDeviceAddress(String bleDeviceAddress) {
        AppState.bleDeviceAddress = bleDeviceAddress;
    }
}

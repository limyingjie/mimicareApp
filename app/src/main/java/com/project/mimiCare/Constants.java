package com.project.mimiCare;

import java.util.UUID;

public class Constants {

    // values have to be globally unique
    public static final String INTENT_ACTION_DISCONNECT = BuildConfig.APPLICATION_ID + ".Disconnect";
    public static final String NOTIFICATION_CHANNEL = BuildConfig.APPLICATION_ID + ".Channel";
    public static final String INTENT_CLASS_MAIN_ACTIVITY = BuildConfig.APPLICATION_ID + ".MainActivity";

    // values have to be unique within each app
    public static final int NOTIFY_MANAGER_START_FOREGROUND_SERVICE = 1001;

    // UUIDs
    public static final UUID BLUETOOTH_LE_CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static final UUID FOOT_BLE_SERVICE = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b");
    public static final UUID FOOT_BLE_RW = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8");
    public static final UUID FOOT_BLE_R_NOTIFY = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a9");

    public static final boolean IS_MOCKING = false; //todo unify all is mockings to use this var

    private Constants() {
    }
}

package com.alteredworlds.taptap.service;

import java.util.UUID;

/**
 * Created by twcgilbert on 08/12/2015.
 */
public class TapGattAttributes {
    public final static String ACTION_BLE_SCAN_START = "awACTION_BLE_SCAN_START";
    public final static String ACTION_BLE_SCAN_STOP = "awACTION_BLE_SCAN_STOP";

    public final static String ACTION_GATT_CONNECTED = "awUART.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "awUART.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "awUART.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_GATT_RSSI = "ACTION_GATT_RSSI";
    public final static String ACTION_DATA_AVAILABLE = "awUART.ACTION_DATA_AVAILABLE";
    public final static String DEVICE_DOES_NOT_SUPPORT_UART = "awUART.DEVICE_DOES_NOT_SUPPORT_UART";

    public final static UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
}

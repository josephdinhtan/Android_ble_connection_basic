package com.jdt.ble_peripheral_lib.service

import android.Manifest
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import androidx.core.app.ActivityCompat
import com.jdt.ble_peripheral_lib.definition.UUIDTable
import com.jdt.ble_peripheral_lib.model.BleLifecycleState
import com.jdt.ble_peripheral_lib.utils.BluetoothUtility

internal class BleAdvertiseHelper(
    private val context: Context, private val bleLifecycleStateChange: (BleLifecycleState) -> Unit
) {
    private var isAdvertising = false


    internal fun startAdvertising() {
        isAdvertising = true
        BluetoothUtility.getBleAdvertiser(context)
            .startAdvertising(advertiseSettings, advertiseData, advertiseScanResponse, advertiseCallback)
        bleLifecycleStateChange(BleLifecycleState.Advertising)
    }

    internal fun stopAdvertising() {
        isAdvertising = false
        BluetoothUtility.getBleAdvertiser(context).stopAdvertising(advertiseCallback)
        bleLifecycleStateChange(BleLifecycleState.StoppedAdvertising)
    }

    private val isBluetoothScanPermissionGranted: Boolean
        get() {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.BLUETOOTH_SCAN
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                TODO("VERSION.SDK_INT < S")
            }
        }
    private val isBluetoothConnectPermissionGranted: Boolean
        get() {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                TODO("VERSION.SDK_INT < S")
            }
        }

    private val advertiseSettings =
        AdvertiseSettings.Builder().setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM).setConnectable(true)
            .build()

    private val advertiseScanResponse = AdvertiseData.Builder()
        .setIncludeDeviceName(true)
        .build()

    private val advertiseData = AdvertiseData.Builder()
        .addServiceUuid(ParcelUuid(UUIDTable.GATT_SERVICE_UUID))
        .build()

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            Log.d(TAG, "Advertise start success\n${UUIDTable.GATT_SERVICE_UUID}")
        }

        override fun onStartFailure(errorCode: Int) {
            val desc = when (errorCode) {
                ADVERTISE_FAILED_DATA_TOO_LARGE -> "\nADVERTISE_FAILED_DATA_TOO_LARGE"
                ADVERTISE_FAILED_TOO_MANY_ADVERTISERS -> "\nADVERTISE_FAILED_TOO_MANY_ADVERTISERS"
                ADVERTISE_FAILED_ALREADY_STARTED -> "\nADVERTISE_FAILED_ALREADY_STARTED"
                ADVERTISE_FAILED_INTERNAL_ERROR -> "\nADVERTISE_FAILED_INTERNAL_ERROR"
                ADVERTISE_FAILED_FEATURE_UNSUPPORTED -> "\nADVERTISE_FAILED_FEATURE_UNSUPPORTED"
                else -> ""
            }
            Log.d(TAG, "Advertise start failed: errorCode=$errorCode $desc")
            isAdvertising = false
        }
    }

    companion object {
        private const val TAG = "BleAdvertiseHelper_Jdt"
    }
}
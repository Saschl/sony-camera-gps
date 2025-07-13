/*
 * Copyright 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.saschl.sonygps.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.companion.AssociationInfo
import android.companion.CompanionDeviceService
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.getSystemService
import androidx.core.graphics.drawable.IconCompat
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber
import java.util.Locale
import java.util.TimeZone
import java.util.UUID


class CompanionDeviceSampleService : CompanionDeviceService() {

    // private lateinit var mHandler: Handler
    /*  private lateinit var fusedLocationClient: FusedLocationProviderClient
      private lateinit var locationCallback: LocationCallback*/
    // private var gatt1: BluetoothGatt? = null

    private var characteristic: BluetoothGattCharacteristic? = null

    private var locationResultVar: Location = Location("")

    //private val coroutineScope = CoroutineScope(Job())
    // private lateinit var timerJob: Timer

    private var cancellationToken = CancellationTokenSource()


    companion object {

        // Random UUID for our service known between the client and server to allow communication
        val SERVICE_UUID: UUID = UUID.fromString("8000dd00-dd00-ffff-ffff-ffffffffffff")

        // Same as the service but for the characteristic
        val CHARACTERISTIC_UUID: UUID = UUID.fromString("0000dd11-0000-1000-8000-00805f9b34fb")

        const val PREFS_NAME = "GattPrefs"
        const val KEY_DEVICE_ADDRESS = "device_address"


        const val ACTION_START_ADVERTISING = "start_ad"
        const val ACTION_STOP_ADVERTISING = "stop_ad"

        // Important: this is just for simplicity, there are better ways to communicate between
        // a service and an activity/view
        val serverLogsState: MutableStateFlow<String> = MutableStateFlow("")
        val isServerRunning = MutableStateFlow(false)

        private const val CHANNEL = "gatt_server_channel"
    }


    private val notificationManager: DeviceNotificationManager by lazy {
        DeviceNotificationManager(applicationContext)
    }

    /*    private val bluetoothManager: BluetoothManager by lazy {
        applicationContext.getSystemService()!!
    }

    private val callback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(
            gatt: BluetoothGatt,
            status: Int,
            newState: Int,
        ) {
            super.onConnectionStateChange(gatt, status, newState)

            // state = state.copy(gatt = gatt, connectionState = newState)
            //  currentOnStateChange(state)

            if (status != BluetoothGatt.GATT_SUCCESS) {
                // Here you should handle the error returned in status based on the constants
                // https://developer.android.com/reference/android/bluetooth/BluetoothGatt#summary
                // For example for GATT_INSUFFICIENT_ENCRYPTION or
                // GATT_INSUFFICIENT_AUTHENTICATION you should create a bond.
                // https://developer.android.com/reference/android/bluetooth/BluetoothDevice#createBond()

                Log.e("BLEConnectEffect", "An error happened: $status")
            } else {
                Log.i("BLEConnectEffect", "Connected to device")
                gatt.discoverServices()

            }


        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            //    state = state.copy(gatt = gatt, mtu = mtu)
            //    currentOnStateChange(state)
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            //      state = state.copy(services = gatt.services)
            //     currentOnStateChange(state)
            val service = gatt.services?.find { it.uuid == SERVICE_UUID }

            // If the GATTServerSample service is found, get the characteristic
            characteristic = service?.getCharacteristic(CHARACTERISTIC_UUID)

            sendData(gatt, characteristic)

        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int,
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            //     state = state.copy(messageSent = status == BluetoothGatt.GATT_SUCCESS)
            //     currentOnStateChange(state)
        }

        @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int,
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
            //   if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            doOnRead(characteristic.value)
            //    }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int,
        ) {
            super.onCharacteristicRead(gatt, characteristic, value, status)
            doOnRead(value)
        }

        private fun doOnRead(value: ByteArray) {
            //     state = state.copy(messageReceived = value.decodeToString())
            //      currentOnStateChange(state)
        }
    }*/

    @SuppressLint("MissingPermission")
    override fun onDeviceAppeared(associationInfo: AssociationInfo) {
        super.onDeviceAppeared(associationInfo)
        if (missingPermissions()) {
            Log.e(CompanionDeviceSampleService::class.java.toString(), "aaa");
            return
        }

        notificationManager.onDeviceAppeared("nah", "HERE I AM")


        val address = associationInfo.deviceMacAddress?.toString() ?: return
        /*  var device: BluetoothDevice? = null
        if (Build.VERSION.SDK_INT >= 34) {
            device = associationInfo.associatedDevice?.bleDevice?.device
        }
        if (device == null) {
            device = bluetoothManager.adapter.getRemoteDevice(address)
        }*/

        val serviceIntent = Intent(this, LocationSenderService::class.java)
        serviceIntent.putExtra("address", address.uppercase(Locale.getDefault()))
        Log.i("cdm","WILL STRART THE FOREGROUND BITCH")
        startForegroundService(serviceIntent)

    }

    @SuppressLint("MissingPermission")
    override fun onDeviceDisappeared(associationInfo: AssociationInfo) {
        super.onDeviceDisappeared(associationInfo)
        if (missingPermissions()) {
            return
        }




        /* Timer().schedule(object : TimerTask() {
             override fun run() {


                 mHandler.post {
                     Toast.makeText(applicationContext, "byebye", Toast.LENGTH_SHORT).show()
                 }
                 gatt?.disconnect()

             }

         }, 120000)


         stopSelf()*/

        /* notificationManager.onDeviceDisappeared(
             address = associationInfo.deviceMacAddress?.toString() ?: return,
         )*/
    }


    @SuppressLint("MissingPermission")
    override fun onDestroy() {
        super.onDestroy()

        //   fusedLocationClient.removeLocationUpdates(locationCallback)
        // timerJob.cancel()

        //fusedLocationClient.removeLocationUpdates(locationCallback)

        stopService(Intent(this, LocationSenderService::class.java))
        notificationManager.onDeviceDisappeared("Service gone :)")

        /*   gatt?.disconnect()
           gatt?.close()*/
        Log.e("service", "Destroyed service")
    }

    /*  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
          super.onStartCommand(intent, flags, startId)

          return START_STICKY
      }*/

    @SuppressLint("MissingPermission")
    override fun onCreate() {
        super.onCreate()

      /*  Timber.plant(
            Timber.DebugTree(),
            FileTree()
        )*/

    }

    /**
     * Check BLUETOOTH_CONNECT is granted and POST_NOTIFICATIONS is granted for devices running
     * Android 13 and above.
     */
    private fun missingPermissions(): Boolean = ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.BLUETOOTH_CONNECT,
    ) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED

    /**
     * Utility class to post notification when CDM notifies that a device appears or disappears
     */
    class DeviceNotificationManager(context: Context) {

        companion object {
            private const val CDM_CHANNEL = "cdm_channel"
        }

        private val manager = NotificationManagerCompat.from(context)

        private val notificationBuilder = NotificationCompat.Builder(context, CDM_CHANNEL)
            .setSmallIcon(IconCompat.createWithResource(context, context.applicationInfo.icon))
            .setContentTitle("Companion Device Manager Sample")

        init {
            createNotificationChannel()
        }

        @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
        fun onDeviceAppeared(address: String, status: String) {
            val notification =
                notificationBuilder.setContentText("Device: $address appeared.\nStatus: $status")
            manager.notify(address.hashCode(), notification.build())
        }

        @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
        fun onDeviceDisappeared(address: String) {
            val notification = notificationBuilder.setContentText("Device: $address disappeared")
            manager.notify(address.hashCode(), notification.build())
        }

        private fun createNotificationChannel() {
            val channel =
                NotificationChannelCompat.Builder(CDM_CHANNEL, NotificationManager.IMPORTANCE_HIGH)
                    .setName("CDM Sample")
                    .setDescription("Channel for the CDM sample")
                    .build()
            manager.createNotificationChannel(channel)
        }
    }
}

   /* @SuppressLint("MissingPermission")
    private fun sendData(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?,
    ) {
        val data = ByteArray(95)

        data[0] = 0x00
        data[1] = 0x5D.toByte()


        // bytes 2-4
        val fixedData = "0802FC".chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
        System.arraycopy(fixedData, 0, data, 2, fixedData.size)

        // transmit timezone offset? NO
        data[5] = 0x00.toByte()

        val fixedData2 = "0000101010".chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()

        System.arraycopy(fixedData2, 0, data, 6, fixedData2.size)
        // position information
        //  Log.e("thisThing", locationResultVar!!.latitude.toString());
        val latitude = locationResultVar.latitude
        val longitude = locationResultVar.longitude
        val locationData = set_location(latitude, longitude)
        System.arraycopy(locationData, 0, data, 11, locationData.size)


        // here UTC time must be used
        val dateData = set_date(TimeZone.getTimeZone("UTC").toZoneId())
        System.arraycopy(dateData, 0, data, 19, dateData.size)
        // Set the last offsets
        // timezone offset
        *//*    val calendar = Calendar.getInstance(timezone)
            val offsetMin = calendar.get(Calendar.ZONE_OFFSET) / 60000
            val offsetMinBytes = ByteBuffer.allocate(2).putShort(offsetMin.toShort()).array()
    *//*
        // dst offset
        *//* val offsetDstMin = calendar.get(Calendar.DST_OFFSET) / 60000
         val offsetDstMinBytes = ByteBuffer.allocate(2).putShort(offsetDstMin.toShort()).array()*//*


        // TODO does weird stuff, probably not needed as camera has TZ configured
        *//*   data[91] = offsetMinBytes[0]
           data[92] = offsetMinBytes[1]
           data[93] = offsetDstMinBytes[0]
           data[94] = offsetDstMinBytes[1]*//*

        val hex = data.toHex()
        Log.i("ayup", "Sending data: $hex with location $locationResultVar")

        if (characteristic != null) {
            val result = gatt?.writeCharacteristic(
                characteristic,
                data,
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT,
            )
            Log.i("ayup", "Write result: $result")
        }
    }
}


fun ByteArray.toHex(): String = joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }*/


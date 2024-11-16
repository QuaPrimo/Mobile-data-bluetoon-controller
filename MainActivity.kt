package com.alluvamz.mdbc

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {

    private val bluetoothAdapter: BluetoothAdapter? by lazy { BluetoothAdapter.getDefaultAdapter() }
    private val appUUID: UUID = UUID.fromString("b3c4e2b8-3e30-11ed-b878-0242ac120002")
    private val appName = "MDBC"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPermissions()
    }

    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            requestPermissionsLauncher.launch(missingPermissions.toTypedArray())
        } else {
            // Permissions already granted
            startBluetoothServer()
        }
    }

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            startBluetoothServer()
        } else {
            Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startBluetoothServer() {
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val serverSocket: BluetoothServerSocket? =
                    bluetoothAdapter?.listenUsingRfcommWithServiceRecord(appName, appUUID)
                serverSocket?.let {
                    while (true) {
                        val socket: BluetoothSocket = it.accept()
                        handleClientSocket(socket)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun handleClientSocket(socket: BluetoothSocket) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val inputStream = socket.inputStream
                val buffer = ByteArray(1024)
                val bytesRead = inputStream.read(buffer)
                val command = String(buffer, 0, bytesRead).trim()

                when (command) {
                    "TURN_ON" -> {
                        // Logic to enable mobile data
                        runOnUiThread { Toast.makeText(this@MainActivity, "Turning ON data", Toast.LENGTH_SHORT).show() }
                    }
                    "TURN_OFF" -> {
                        // Logic to disable mobile data
                        runOnUiThread { Toast.makeText(this@MainActivity, "Turning OFF data", Toast.LENGTH_SHORT).show() }
                    }
                    else -> {
                        runOnUiThread { Toast.makeText(this@MainActivity, "Unknown command: $command", Toast.LENGTH_SHORT).show() }
                    }
                }

                socket.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}

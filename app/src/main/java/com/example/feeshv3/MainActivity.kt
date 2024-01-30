package com.example.feeshv3

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.feeshv3.data.Record
import com.example.feeshv3.data.UserViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.util.*

private val TAG = "MyActivity"
private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
private val REQUEST_CODE = 100
private lateinit var mUserViewModel: UserViewModel

suspend fun connectToDevice(device: BluetoothDevice, context: Context) {
    val adapter = BluetoothAdapter.getDefaultAdapter()


    if (adapter == null) {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Device is not Bluetooth compatible.", Toast.LENGTH_LONG).show()
        }
    }

    // Check if we have permission to use Bluetooth
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
        // Permission not granted, request it
        ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.BLUETOOTH), REQUEST_CODE)
        return
    }

    // Check if we have permission to use Bluetooth admin
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
        // Permission not granted, request it
        ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.BLUETOOTH_ADMIN), REQUEST_CODE)
        return
    }

    try {
        // All permissions granted, proceed with connection
        val socket = device.createRfcommSocketToServiceRecord(uuid)
        socket.connect()
        val inputStream: InputStream = socket.inputStream

        Thread {
            while (true) {
                try {
                    val buffer = ByteArray(1024)
                    val bytes: Int = inputStream.read(buffer)
                    val data = String(buffer, 0, bytes)

                    // Split the received data into separate sensor values
                    val sensorValues = data.trim().split(",")

                    // Check that the list has at least one element before trying to access any elements
                    if (sensorValues.size >= 4) {
                        // Parse the sensor values as float values
                        val sensor1 = sensorValues[0].toFloat()
                        val sensor2 = sensorValues[1].toFloat()
                        val sensor3 = sensorValues[2].toFloat()
                        val sensor4 = sensorValues[3].toFloat()

                        (context as MainActivity).runOnUiThread {
                            val salinity = context.findViewById<TextView>(R.id.saline)
                            val temperature = context.findViewById<TextView>(R.id.temperature)
                            val pH = context.findViewById<TextView>(R.id.pH)
                            val dissolved = context.findViewById<TextView>(R.id.dO)

                            // Update the UI with the sensor values
                            salinity.text = sensor3.toString()
                            temperature.text = sensor1.toString()
                            pH.text = sensor2.toString()
                            dissolved.text = sensor4.toString()
                        }
                    }
                } catch (e: Exception) {
                    // Handle the exception by displaying a Toast message
                    e.printStackTrace()
                    val message = "Error reading data from device: " + e.message
                    (context as MainActivity).runOnUiThread {
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }.start()
    } catch (e: Exception) {
        // Handle the exception by displaying a Toast message
        e.printStackTrace()
        val message = "Error connecting to device: " + e.message
        (context as MainActivity).runOnUiThread {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}

class MainActivity : AppCompatActivity() {

    private val REQUEST_CODE = 1
    private val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val salinityTextView = findViewById<TextView>(R.id.saline)
        val temperatureTextView = findViewById<TextView>(R.id.temperature)
        val pHTextView = findViewById<TextView>(R.id.pH)
        val dissolvedTextView = findViewById<TextView>(R.id.dO)

        if (!hasPermissions(this, *permissions)) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)
        } else {
            // Permissions are already granted
        }

        val recordbtn = findViewById<Button>(R.id.record)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Permission not granted, request it
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CODE)
            return
        }

        val connectButton = findViewById<Button>(R.id.connector)
        val device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice("00:22:09:01:9A:70") // Replace with your device's address
        if (device.bluetoothClass.deviceClass == BluetoothClass.Device.PHONE_SMART) {
            // PIN is required, handle the pairing process with PIN
            val pin = "1234" // Replace with the PIN of your Bluetooth device

            // Pair the device with the PIN
            device.setPin(pin.toByteArray())
            device.createBond()
        } else {
            // No PIN required, proceed with connection
            GlobalScope.launch(Dispatchers.Main) {
                connectToDevice(device, this@MainActivity)
            }
        }
        connectButton.setOnClickListener {
            val bluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(bluetoothIntent, REQUEST_CODE)
            GlobalScope.launch(Dispatchers.Main) {
                connectToDevice(device, this@MainActivity)
            }
        }

        recordbtn.setOnClickListener {
            insertDataToDatabase(
                salinityTextView,
                temperatureTextView,
                pHTextView,
                dissolvedTextView
            )
        }

        mUserViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        val viewbtn = findViewById<Button>(R.id.viewbtn)
        viewbtn.setOnClickListener {
            val intent = Intent(this, View::class.java)
            startActivity(intent)
        }
    }

    private fun insertDataToDatabase(salinity: TextView, temperature: TextView, pH: TextView, dissolved: TextView) {
        val calendar = Calendar.getInstance()
        val salinityValue = salinity.text.toString().toDoubleOrNull()
        val temperatureValue = temperature.text.toString().toDoubleOrNull()
        val pHValue = pH.text.toString().toDoubleOrNull()
        val dissolvedValue = dissolved.text.toString().toDoubleOrNull()

        if (salinityValue != null && temperatureValue != null && pHValue != null && dissolvedValue != null) {
            val month = (calendar.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
            val day = calendar.get(Calendar.DATE).toString().padStart(2, '0')
            val hour = calendar.get(Calendar.HOUR_OF_DAY).toString().padStart(2, '0')
            val minute = calendar.get(Calendar.MINUTE).toString().padStart(2, '0')

            val timestamp = "$month/$day\n$hour:$minute"
            val rec = Record(0, temperatureValue, salinityValue, pHValue, dissolvedValue, timestamp)
            mUserViewModel.addRecord(rec)
            Toast.makeText(this, "Done!", Toast.LENGTH_LONG).show()
        }
    }

    private fun hasPermissions(context: Context, vararg permissions: String): Boolean {
        for (permission in permissions) {
            if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    // Permissions granted
                } else {
                    // Permissions not granted
                }
            }
        }
    }
}

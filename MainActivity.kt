import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStream
import java.util.*

class MainActivity : AppCompatActivity() {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val appName = "BluetoothDataControl"
    private val appUuid: UUID = UUID.fromString("12345678-1234-1234-1234-123456789abc") // Custom UUID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Start Bluetooth server
        CoroutineScope(Dispatchers.IO).launch {
            startBluetoothServer()
        }
    }

    private fun startBluetoothServer() {
        val serverSocket: BluetoothServerSocket? = bluetoothAdapter?.listenUsingRfcommWithServiceRecord(appName, appUuid)

        while (true) {
            val socket: BluetoothSocket? = serverSocket?.accept()
            socket?.let {
                handleBluetoothConnection(it)
                serverSocket.close()
                break
            }
        }
    }

    private fun handleBluetoothConnection(socket: BluetoothSocket) {
        val inputStream: InputStream = socket.inputStream

        // Read commands
        val buffer = ByteArray(1024)
        var bytes: Int
        while (true) {
            bytes = inputStream.read(buffer)
            val command = String(buffer, 0, bytes)
            if (command.equals("TURN_ON", ignoreCase = true)) {
                toggleMobileData(true)
            } else if (command.equals("TURN_OFF", ignoreCase = true)) {
                toggleMobileData(false)
            }
        }
    }

    private fun toggleMobileData(enable: Boolean) {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        try {
            val method = ConnectivityManager::class.java.getDeclaredMethod("setMobileDataEnabled", Boolean::class.javaPrimitiveType)
            method.isAccessible = true
            method.invoke(connectivityManager, enable)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

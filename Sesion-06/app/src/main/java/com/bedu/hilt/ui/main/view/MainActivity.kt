package com.bedu.hilt.ui.main.view

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bedu.hilt.R
import com.bedu.hilt.databinding.ActivityMainBinding
import com.bedu.hilt.ui.people.view.PeopleActivity
import com.bedu.hilt.ui.planets.view.PlanetsActivity
import com.bedu.hilt.utils.*
import dagger.hilt.android.AndroidEntryPoint
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    companion object {
        const val PERMISSION_REQUEST_STORAGE = 0
        const val urlApp = "https://github.com/andres2093/apk-s/raw/main/app-release.apk"
        const val urlCode = "https://github.com/andres2093/AndroidAvanzadoS6/raw/main/versionCode.txt"
    }

    private lateinit var downloadController: DownloadController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        downloadController = DownloadController(this, urlApp)

        setupUI()

        checkUpdate()
    }

    private fun setupUI() {
        binding.btnPeople.setOnClickListener {
            val intent = Intent(this, PeopleActivity::class.java)
            startActivity(intent)
        }
        binding.btnPlanets.setOnClickListener {
            val intent = Intent(this, PlanetsActivity::class.java)
            startActivity(intent)
        }
        binding.btnDownload.setOnClickListener {
            checkStoragePermission()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_STORAGE) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                downloadController.enqueueDownload()
            } else {
                Toast.makeText(this, R.string.storage_permission_denied, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun checkStoragePermission() {
        if (checkSelfPermissionCompat(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        ) {
            downloadController.enqueueDownload()
        } else {
            requestStoragePermission()
        }
    }

    private fun requestStoragePermission() {
        if (shouldShowRequestPermissionRationaleCompat(WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(this, R.string.storage_permission_denied, Toast.LENGTH_LONG).show()
            requestPermissionsCompat(
                arrayOf(WRITE_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_STORAGE
            )
        } else {
            requestPermissionsCompat(
                arrayOf(WRITE_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_STORAGE
            )
        }
    }

    private fun checkUpdate(){
        Thread {
            val remoteVersionCode = readUrlFile(urlCode)
            if (remoteVersionCode !== null) {
                val localVersionCode: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    packageManager.getPackageInfo(packageName, 0).longVersionCode.toInt()
                } else {
                    packageManager.getPackageInfo(packageName, 0).versionCode
                }

                Log.e("TAG", "onCreate: $remoteVersionCode, $localVersionCode")
                runOnUiThread {
                    if (remoteVersionCode.toInt() > localVersionCode) {
                        val alertDialog: AlertDialog = buildAlertDialog(
                            this,
                            R.string.new_version,
                            R.string.new_version_msg
                        )
                        alertDialog.setButton(
                            DialogInterface.BUTTON_POSITIVE, getString(R.string.btn_update)
                        ) { dialog: DialogInterface, _: Int ->
                            dialog.dismiss()
                            checkStoragePermission()
                        }
                        alertDialog.setButton(
                            DialogInterface.BUTTON_NEGATIVE, getString(R.string.btn_cancel)
                        ) { dialog: DialogInterface, _: Int ->
                            dialog.dismiss()
                        }
                        alertDialog.show()
                    }
                }
            } else {
                Log.e("TAG", "onCreate: error checking version")
            }
        }.start()
    }

    private fun readUrlFile(url: String): String? {
        var data: String? = null
        var iStream: InputStream? = null
        var urlConnection: HttpURLConnection? = null
        try {
            urlConnection = URL(url).openConnection() as HttpURLConnection?
            urlConnection?.connect()
            iStream = urlConnection?.inputStream
            val br = BufferedReader(InputStreamReader(iStream))
            val sb = StringBuilder()
            var line: String?
            while (br.readLine().also { line = it } != null) {
                sb.append(line)
            }
            data = sb.toString()
            br.close()
        } catch (e: Exception) {
            Log.w("", "Exception while downloading url: $e")
        } finally {
            iStream?.close()
            urlConnection?.disconnect()
        }
        return data
    }


}

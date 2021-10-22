package com.bedu.hilt.utils

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

fun AppCompatActivity.checkSelfPermissionCompat(permission: String) =
    ActivityCompat.checkSelfPermission(this, permission)

fun AppCompatActivity.shouldShowRequestPermissionRationaleCompat(permission: String) =
    ActivityCompat.shouldShowRequestPermissionRationale(this, permission)

fun AppCompatActivity.requestPermissionsCompat(
    permissionsArray: Array<String>,
    requestCode: Int
) {
    ActivityCompat.requestPermissions(this, permissionsArray, requestCode)
}

fun buildAlertDialog(context: Context, resTitle: Int, resMessage: Int): AlertDialog {
    val alertDialog = AlertDialog.Builder(context).create()
    alertDialog.setTitle(context.getString(resTitle))
    alertDialog.setMessage(context.getString(resMessage))
    alertDialog.setCancelable(false)
    return alertDialog
}

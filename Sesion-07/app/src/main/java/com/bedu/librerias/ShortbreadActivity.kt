package com.bedu.librerias

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bedu.librerias.databinding.ActivityShortbreadBinding
import com.bedu.librerias.utils.SUCCESS
import com.bedu.librerias.utils.showToasty
import com.google.android.material.snackbar.Snackbar
import shortbread.Shortcut

@Shortcut(id = "shortBread", icon = R.drawable.ic_add_light, shortLabel = "ShortBread")
class ShortbreadActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShortbreadBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityShortbreadBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.btnSnackBar.setOnClickListener { showSnackBar() }
        binding.btnDialog.setOnClickListener { showDialog() }
    }

    @Shortcut(
        id = "show_snack_bar",
        icon = R.drawable.ic_clear_white_24dp,
        shortLabel = "Show snackbar"
    )
    fun showSnackBar() {
        Snackbar.make(binding.content, "show_snack_bar", Snackbar.LENGTH_SHORT).show()
    }

    @Shortcut(id = "show_dialog", icon = R.drawable.ic_check_white_24dp, shortLabel = "Show dialog")
    fun showDialog() {
        AlertDialog.Builder(this)
            .setTitle("show_dialog")
            .setMessage(R.string.app_name)
            .setNegativeButton("close") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("Ok!") { dialog, _ ->
                dialog.dismiss()
                showToasty(
                    this,
                    SUCCESS,
                    "Ok!",
                    Toast.LENGTH_SHORT,
                    true,
                    null,
                    null
                )
            }.create().show()
    }

}
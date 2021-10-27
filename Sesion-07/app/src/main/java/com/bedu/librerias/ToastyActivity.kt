package com.bedu.librerias

import android.graphics.Typeface
import android.graphics.Typeface.BOLD_ITALIC
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.style.StyleSpan
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bedu.librerias.databinding.ActivityToastyBinding
import com.bedu.librerias.utils.*
import es.dmoral.toasty.Toasty
import android.text.SpannableStringBuilder as SpannableStringBuilder1

class ToastyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityToastyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityToastyBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.btnError.setOnClickListener {
            showToasty(this, ERROR, "This is an error toast", Toast.LENGTH_SHORT, true, null, null)
        }
        binding.btnSuccess.setOnClickListener {
            showToasty(this, SUCCESS, "Success!", Toast.LENGTH_SHORT, true, null, null)
        }
        binding.btnInfo.setOnClickListener {
            showToasty(this, INFO, "Here is some info for you", Toast.LENGTH_SHORT, true, null, null)
        }
        binding.btnInfoFormatted.setOnClickListener {
            showToasty(this, INFO_FORMATTED, "", Toast.LENGTH_SHORT, true, null, getFormattedMessage())
        }
        binding.btnWarning.setOnClickListener {
            showToasty(this, WARNING, "Beware of the dog", Toast.LENGTH_SHORT, true, null, null)
        }
        binding.btnNormal.setOnClickListener {
            showToasty(this, NORMAL, "Normal toast", Toast.LENGTH_SHORT, false, null, null)
        }
        binding.btnNormalWithIcon.setOnClickListener {
            val icon = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                resources.getDrawable(R.drawable.ic_check_white_24dp, theme) else resources.getDrawable(R.drawable.ic_check_white_24dp)
            showToasty(this, NORMAL_WITH_ICON, "Normal toast with icon", Toast.LENGTH_SHORT, true, icon, null)
        }
        binding.btnCustom.setOnClickListener {
            Toasty.Config.getInstance()
                .setToastTypeface(Typeface.createFromAsset(assets, "GreatVibes-Regular.otf"))
                .allowQueue(false)
                .setTextSize(35)
                .apply()

            val icon = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                resources.getDrawable(R.drawable.ic_check_white_24dp, theme) else resources.getDrawable(R.drawable.ic_check_white_24dp)
            showToasty(this, CUSTOM, "Custom message", Toast.LENGTH_SHORT, true, icon, null)

            Toasty.Config.reset()
        }
    }

    private fun getFormattedMessage(): CharSequence? {
        val prefix = "Formatted "
        val highlight = "bold italic"
        val suffix = " text"
        val ssb = SpannableStringBuilder1(prefix).append(highlight).append(suffix)
        val prefixLen = prefix.length
        ssb.setSpan(
            StyleSpan(BOLD_ITALIC),
            prefixLen, prefixLen + highlight.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return ssb
    }
}
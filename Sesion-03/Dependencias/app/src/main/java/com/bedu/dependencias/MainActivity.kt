package com.bedu.dependencias

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    @Named("String1")
    lateinit var testString: String

    @Inject
    @Named("String2")
    lateinit var testString2: String

    @Inject
    @Named("randomNum")
    lateinit var randomNum: String

    @Inject
    @Named("randomNumAct")
    lateinit var randomNumAct: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.button).setOnClickListener {
            startActivity(Intent(this, OtherActivity::class.java))
            finish()
        }

        Log.e("MainActivity", testString)
        Log.e("MainActivity", testString2)
    }

    override fun onResume() {
        super.onResume()
        Log.e("MainActivity", randomNum)
        Log.e("MainActivityAct", randomNumAct)
    }
}

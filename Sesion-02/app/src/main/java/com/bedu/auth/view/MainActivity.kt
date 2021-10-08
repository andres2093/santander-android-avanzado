package com.bedu.auth.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.bedu.auth.R
import com.bedu.auth.databinding.ActivityOptionsBinding
import com.bedu.auth.utils.Utility
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.crashlytics.CustomKeysAndValues
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase

class MainActivity : Activity() {

    private lateinit var binding: ActivityOptionsBinding

    private lateinit var auth: FirebaseAuth

    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOptionsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        FirebaseApp.initializeApp(this)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        auth = Firebase.auth

        handleClick()
    }

    private fun handleClick() {

        binding.btnCrash.setOnClickListener {
            Toast.makeText(this, "Crash", Toast.LENGTH_SHORT).show()

            //1
            try {
                Log.e(TAG, "handleClick: " + 0 / 0)
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
            }
            //2
            throw RuntimeException("Test Crash") // Force a crash
        }
        binding.btnSetInfo.setOnClickListener {
            Toast.makeText(this, "SetInfo", Toast.LENGTH_SHORT).show()
            FirebaseCrashlytics.getInstance().setCustomKey("str_key", "hello")
            FirebaseCrashlytics.getInstance().setCustomKey("bol_key", true)
            FirebaseCrashlytics.getInstance().setCustomKey("int_key", 1)
            FirebaseCrashlytics.getInstance().setCustomKey("long_key", 1L)
            FirebaseCrashlytics.getInstance().setCustomKey("double_key", 1.0)
            FirebaseCrashlytics.getInstance().setCustomKey("float_key", 1.0f)

            FirebaseCrashlytics.getInstance().setCustomKeys(
                CustomKeysAndValues.Builder()
                    .putString("email key", "string value")
                    .putString("mane", "Andres")
                    .putString("string key 2", "string  value 2")
                    .putBoolean("boolean key", true)
                    .putBoolean("boolean key 2", false)
                    .putFloat("float key", 1.01f)
                    .putFloat("float key 2", 2.02f)
                    .build()
            )

            FirebaseCrashlytics.getInstance().log("Log enviado desde crashlytics")

            FirebaseCrashlytics.getInstance().setUserId("132456")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed", e)
                Utility.displaySnackBar(binding.root, "Google sign in failed", this, R.color.red)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user, null)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    updateUI(null, task.exception)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?, exception: Exception?) {
    }

    companion object {
        private const val TAG = "GoogleActivity"
        private const val RC_SIGN_IN = 9001
    }

}
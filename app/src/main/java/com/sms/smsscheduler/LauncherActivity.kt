package com.sms.smsscheduler

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.GravityCompat
import com.sms.smsscheduler.databinding.ActivityLauncherBinding

class LauncherActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLauncherBinding
    private lateinit var tvMode: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLauncherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tvMode = binding.navLayout.tvMode

        val prefs: SharedPreferences = this.getSharedPreferences(
            getString(R.string.uiMode),
            MODE_PRIVATE
        )
        var uiMode = prefs.getBoolean(getString(R.string.uiMode), false)
        changeUiMode(uiMode)
        val editor: SharedPreferences.Editor = prefs.edit()



        Toast.makeText(this, getString(R.string.developedBy), Toast.LENGTH_LONG).show()

        setSupportActionBar(binding.toolbar)

        binding.menuBtn.setOnClickListener {
            binding.root.openDrawer(GravityCompat.START)
        }

        binding.navLayout.tvContact.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SEND)
            emailIntent.type = "message/rfc822"
            emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.emailId)))
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "")
            emailIntent.putExtra(Intent.EXTRA_TEXT, "")
            startActivity(Intent.createChooser(emailIntent, "Choose an e-mail client:"))
        }

        tvMode.setOnClickListener {
            if (uiMode) {
                uiMode = false
                editor.putBoolean(getString(R.string.uiMode), uiMode)
                editor.apply()
                recreate()
            } else {
                uiMode = true
                editor.putBoolean(getString(R.string.uiMode), uiMode)
                editor.apply()
                recreate()
            }
        }
    }

    fun openMainActivity(v: View) {
        startActivity(Intent(this@LauncherActivity, MainActivity::class.java))
        finish()
    }

    private fun changeUiMode(nightMode: Boolean) {
        if (nightMode) {
            AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES
            )
            tvMode.text = getString(R.string.light_mode)
            tvMode.setCompoundDrawablesWithIntrinsicBounds(
                AppCompatResources.getDrawable(
                    applicationContext,
                    R.drawable.ic_baseline_light_mode_24
                ),
                null, null, null
            )
        } else {
            AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_NO
            )
            tvMode.text = getString(R.string.night_mode)
            tvMode.setCompoundDrawablesWithIntrinsicBounds(
                AppCompatResources.getDrawable(
                    applicationContext,
                    R.drawable.ic_baseline_dark_mode_24
                ),
                null, null, null
            )
        }
    }
}
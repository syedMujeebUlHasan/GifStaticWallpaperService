package com.example.gifstaticwallpaperservice

import android.app.Activity
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.gifstaticwallpaperservice.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var serviceLauncher: ActivityResultLauncher<Intent>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnStartService.setOnClickListener {
            callWallpaperService()
        }

        serviceLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    lifecycleScope.launch {
                        delay(500)
                        Toast.makeText(this@MainActivity, "Wallpaper Applied", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }




    }

    private fun callWallpaperService() {
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    WallpaperManager.getInstance(this@MainActivity).clear()
                }

                val component = ComponentName(this@MainActivity, CombinedWallpaperService::class.java)
                val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                    putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, component)
                }
                serviceLauncher?.launch(intent)
            } catch (e: Exception) {
                e.printStackTrace()

            }
        }
    }
}
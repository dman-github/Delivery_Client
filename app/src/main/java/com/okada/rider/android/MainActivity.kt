package com.okada.rider.android

import android.Manifest
import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupWithNavController
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.okada.rider.android.databinding.ActivityMainBinding
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.annotations.AfterPermissionGranted
import com.vmadalin.easypermissions.dialogs.DEFAULT_SETTINGS_REQ_CODE
import com.vmadalin.easypermissions.dialogs.SettingsDialog

class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {

    private lateinit var binding: ActivityMainBinding
    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 0x2233
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setSupportActionBar(binding.appBarHome.toolbar)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        supportActionBar?.hide()

        /* if we do not want the Action bar then we have to use code below */
        //setupWithNavController(navView,navController)
    }

    override fun onStart() {
        super.onStart()
        Log.i("App_info", "onStart")
        appRequiresPermission()
    }

    @AfterPermissionGranted(REQUEST_LOCATION_PERMISSION)
    private fun appRequiresPermission() {
        Log.i("App_info", "appRequiresPermission called")
        if (hasLocationPermission()) {
            // Already have permission
            Log.i("App_info", "ALREADY permissions granted!")
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this,
                getString(R.string.rationale_message_for_location),
                REQUEST_LOCATION_PERMISSION,
                ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.i("App_info", "onRequestPermissionsResult permissions count: ${permissions.size}")
        // EasyPermissions handles the request result.
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == DEFAULT_SETTINGS_REQ_CODE) {
            val yes = getString(R.string.yes)
            val no = getString(R.string.no)
            // Do something after user returned from app settings screen, like showing a Toast.
            Toast.makeText(
                this,
                getString(R.string.returned_from_app_settings_to_activity, if (hasLocationPermission()) yes else no), LENGTH_LONG).show()
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        // Comes here whenever permission has been denied
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            // This will display a dialog directing them to enable the permission in app settings.
            Log.i("App_info", "onPermissionsDenied show settings screen")
            SettingsDialog.Builder(this).build().show()
        } else {
            //Prompt again
            appRequiresPermission()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        Log.i("App_info", "onPermissionsGranted called")
    }

    private fun hasLocationPermission(): Boolean {
        return EasyPermissions.hasPermissions(this, ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)
    }
}
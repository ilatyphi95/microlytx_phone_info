package com.ilatyphi95.microlytxphoneinfo.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.snackbar.Snackbar
import com.ilatyphi95.microlytxphoneinfo.BuildConfig
import com.ilatyphi95.microlytxphoneinfo.R
import com.ilatyphi95.microlytxphoneinfo.data.ItemUtils
import com.ilatyphi95.microlytxphoneinfo.data.Items
import com.ilatyphi95.microlytxphoneinfo.data.PhoneItem
import com.ilatyphi95.microlytxphoneinfo.databinding.ActivityMainBinding
import com.ilatyphi95.microlytxphoneinfo.utils.LocationService
import pub.devrel.easypermissions.EasyPermissions


const val requestPhoneInfo = 109
const val requestReadPhoneState = 209
const val requestAccessFineLocation = 309
const val requestAllAccess = 409
class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {

    private lateinit var binding : ActivityMainBinding
    private lateinit var viewModel : MainActivityViewModel
    private lateinit var locationService : LocationService
    private lateinit var itemUtil: ItemUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationService = LocationService(this)
        itemUtil = ItemUtils(application)

        binding =
            DataBindingUtil.setContentView(this, R.layout.activity_main)

        val viewModelFactory = MainActivityViewModelFactory(application, itemUtil)
        viewModel =
            ViewModelProvider(this, viewModelFactory).get(MainActivityViewModel::class.java)

        val itemAdapter = ItemAdapter { item -> recyclerItemClicked(item) }

        binding.recycler.apply {
            adapter = itemAdapter
            addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
        }

        viewModel.apply {
            infoList.observe(this@MainActivity) {
                itemAdapter.submitList(it)
            }

            receiveLocationUpdate.observe(this@MainActivity) { receiveUpdate ->
                if(receiveUpdate) {
                    if (ActivityCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        locationService.getLocationUpdate(viewModel.locationCallback)
                    }
                }
            }
        }

        askPermission(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            permRational = getString(R.string.all_permission_rational),
            requestCode = requestAllAccess
        )
    }

    private fun recyclerItemClicked(item: PhoneItem) {
        when(item.id) {

            Items.LONGITUDE, Items.LATITUDE ->
                askPermission(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    requestCode = requestAccessFineLocation,
                    permRational = getString(R.string.access_fine_location_rational)
                )

            Items.MOBILE_COUNTRY_CODE, Items.MOBILE_NETWORK_CODE,
            Items.MOBILE_NETWORK_TECHNOLOGY, Items.OPERATOR_NAME ->
                askPermission(
                    Manifest.permission.READ_PHONE_STATE,
                    permRational = getString(R.string.access_fine_location_rational),
                    requestCode = requestReadPhoneState
                )

            Items.LOCAL_AREA_CODE, Items.CELL_IDENTITY,
            Items.CELL_ID, Items.SIGNAL_STRENGTH -> {
                askPermission(
                    Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_FINE_LOCATION,
                    permRational = getString(R.string.read_phone_state_rational),
                    requestCode = requestPhoneInfo
                )
            }

            else -> {
                // Do nothing in this implementation
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.updateLocation()
    }

    override fun onPause() {
        locationService.removeLocationUpdate(viewModel.locationCallback)
        super.onPause()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        viewModel.refreshItems()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        val rationale = getString(R.string.long_permission_rational)
        if(!shouldShowRequestPermissionRationale(perms[0])) {
            val snackbar: Snackbar =
                Snackbar.make(
                findViewById(android.R.id.content),
                    rationale, Snackbar.LENGTH_LONG
            ).setAction(R.string.settings) {
                startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + BuildConfig.APPLICATION_ID)
                    )
                )
            }

            val textView = snackbar.view
                .findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
            textView.maxLines = 5
            snackbar.show()
        }
    }

    private fun askPermission(
        vararg permString: String, requestCode: Int,
        permRational: String) {
        if(EasyPermissions.hasPermissions(this, *permString)) {
            viewModel.refreshItems()
        } else {
            EasyPermissions.requestPermissions(this, permRational, requestCode, *permString)
        }
    }
}
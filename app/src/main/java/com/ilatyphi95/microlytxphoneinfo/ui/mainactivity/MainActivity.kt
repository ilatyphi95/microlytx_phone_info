package com.ilatyphi95.microlytxphoneinfo.ui.mainactivity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.view.Menu
import android.view.MenuItem
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
import com.ilatyphi95.microlytxphoneinfo.ui.preferenceActivity.SettingsActivity
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
    private lateinit var telephonyManager: TelephonyManager
    private lateinit var connectivityManager: ConnectivityManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationService = LocationService(this)
        telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
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

    override fun onResume() {
        super.onResume()
        viewModel.updateLocation()
        telephonyManager.listen(viewModel.phoneStateListener,
            (PhoneStateListener.LISTEN_SIGNAL_STRENGTHS or PhoneStateListener.LISTEN_SERVICE_STATE)
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val networkRequest =
                NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build()
            connectivityManager.registerNetworkCallback(networkRequest, viewModel.connectivityListener)
        }
    }

    override fun onPause() {
        locationService.removeLocationUpdate(viewModel.locationCallback)
        telephonyManager.listen(viewModel.phoneStateListener, PhoneStateListener.LISTEN_NONE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            connectivityManager.unregisterNetworkCallback(viewModel.connectivityListener)
        }
        super.onPause()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
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
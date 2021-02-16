package com.ilatyphi95.microlytxphoneinfo.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telephony.*
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.material.snackbar.Snackbar
import com.ilatyphi95.microlytxphoneinfo.BuildConfig
import com.ilatyphi95.microlytxphoneinfo.R
import com.ilatyphi95.microlytxphoneinfo.data.Items
import com.ilatyphi95.microlytxphoneinfo.data.PhoneItem
import com.ilatyphi95.microlytxphoneinfo.databinding.ActivityMainBinding
import com.ilatyphi95.microlytxphoneinfo.getDefaultDataSubscriptionId
import com.ilatyphi95.microlytxphoneinfo.getNetworkType
import com.ilatyphi95.microlytxphoneinfo.utils.LocationService
import pub.devrel.easypermissions.EasyPermissions


const val requestPhoneInfo = 109
const val requestReadPhoneState = 209
const val requestAccessFineLocation = 309
const val requestAllAccess = 409
class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {

    lateinit var binding : ActivityMainBinding
    lateinit var viewModel : MainActivityViewModel
    private lateinit var locationService : LocationService

    private val locationCallback = object : LocationCallback() {

        override fun onLocationResult(location: LocationResult?) {
            if(location != null) {
                val latLon = location.lastLocation
                viewModel.updateInfo(
                    listOf(
                        Pair(Items.LATITUDE, latLon.latitude.toString()),
                        Pair(Items.LONGITUDE, latLon.longitude.toString())
                    )
                )
            } else {
                viewModel.updateInfoInt(
                    listOf(
                        Pair(Items.LATITUDE, NOT_AVAILABLE),
                        Pair(Items.LONGITUDE, NOT_AVAILABLE)
                    )
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationService = LocationService(this)

        binding =
            DataBindingUtil.setContentView(this, R.layout.activity_main)

        val viewModelFactory = MainActivityViewModelFactory(application)
        viewModel =
            ViewModelProvider(this, viewModelFactory).get(MainActivityViewModel::class.java)

        val itemAdapter = ItemAdapter { item -> recyclerItemClicked(item) }

        binding.recycler.apply {
            adapter = itemAdapter
            addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
        }
        viewModel.infoList.observe(this) {
            itemAdapter.submitList(it)
        }

        askPermission(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            permRational = getString(R.string.all_permission_rational),
            requestCode = requestAllAccess
        )

        refreshPage()
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
                // Do nothing
            }
        }
    }

    private fun updateManufacturerInfo() {
        viewModel.updateInfo(
            listOf(
                Pair(Items.HANDSET_MAKE, Build.MANUFACTURER),
                Pair(Items.ITEM_MODEL, Build.MODEL),
            )
        )
    }

    private fun updateNetworkInfo() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {

            if(viewModel.checkPermission(
                    Manifest.permission.READ_PHONE_STATE,
                    listOf(
                        Items.MOBILE_NETWORK_CODE, Items.MOBILE_COUNTRY_CODE,
                        Items.OPERATOR_NAME, Items.LOCAL_AREA_CODE,
                        Items.CELL_ID, Items.CELL_IDENTITY, Items.SIGNAL_STRENGTH
                    )
                )) return

            val subscriptionManager =
                getSystemService(TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
            val id = getDefaultDataSubscriptionId(subscriptionManager)

            if (subscriptionManager.activeSubscriptionInfoList.size < 1) {
                viewModel.updateNetworkInfo()
                return
            }

            val subInfo = subscriptionManager.getActiveSubscriptionInfo(id) ?: return

            val phoneManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager

            val operatorInfo = mutableListOf<Pair<Items, Int>>()
            val mcc = subInfo.mcc
            val mnc = subInfo.mnc

            val operatorName = subInfo.carrierName
            viewModel.updateInfo(listOf(Pair(Items.OPERATOR_NAME, operatorName.toString())))

            operatorInfo.addAll(
                listOf(
                    Pair(Items.MOBILE_COUNTRY_CODE, mcc),
                    Pair(Items.MOBILE_NETWORK_CODE, mnc),
                )
            )

            if (viewModel.checkPermission(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    listOf(
                        Items.LOCAL_AREA_CODE, Items.CELL_ID, Items.CELL_IDENTITY,
                        Items.SIGNAL_STRENGTH
                    )
                )) {
                viewModel.updateInfoInt(operatorInfo)
                return
                }

                val activeCells = phoneManager.allCellInfo.filter { it.isRegistered }

                activeCells.forEach { cellInfo ->
                    when (cellInfo) {
                        is CellInfoGsm -> {
                            val identity = cellInfo.cellIdentity

                            if (identity.mcc == mcc && identity.mnc == mnc) {
                                viewModel.updateNetworkInfo(
                                    mcc = mcc, mnc = mnc,
                                    signalStrength = cellInfo.cellSignalStrength.level,
                                    cid = identity.cid, lac = identity.lac
                                )
                            }
                        }
                        is CellInfoWcdma -> {
                            val identity = cellInfo.cellIdentity
                            if (identity.mcc == mcc && identity.mnc == mnc) {

                                viewModel.updateNetworkInfo(
                                    mcc = mcc, mnc = mnc,
                                    signalStrength = cellInfo.cellSignalStrength.level,
                                    cid = identity.cid, lac = identity.lac
                                )
                            }
                        }
                        is CellInfoCdma -> {
                            // check this out later
                        }
                        is CellInfoLte -> {
                            val identity = cellInfo.cellIdentity
                            if (identity.mcc == mcc && identity.mnc == mnc) {

                                viewModel.updateNetworkInfo(
                                    mcc = mcc, mnc = mnc, ci = identity.ci,
                                    signalStrength = cellInfo.cellSignalStrength.level
                                )
                            }
                        }

                        else -> {
                            viewModel.updateNetworkInfo()
                        }
                    }
                }

//            viewModel.updateInfoInt(operatorInfo)
    //        } else {
            // work this out later
        }
    }

    override fun onResume() {
        super.onResume()
        refreshPage()
    }

    override fun onPause() {
        locationService.removeLocationUpdate(locationCallback)
        super.onPause()
    }

    private fun updateConnectionStatus() {
        viewModel.updateConnectionStatus()
    }

    private fun updateNetwork() {

        if (viewModel.checkPermission(
                Manifest.permission.READ_PHONE_STATE,
                listOf(Items.MOBILE_NETWORK_TECHNOLOGY)
            )) return


        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        val value = getString(getNetworkType(telephonyManager.networkType))

        viewModel.updateInfo(
            listOf(
                Pair(Items.MOBILE_NETWORK_TECHNOLOGY, value)
            )
        )
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
        refreshPage()
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

    private fun refreshPage() {
        updateManufacturerInfo()
        updateConnectionStatus()
        updateNetwork()
        updateLocation()
        updateNetworkInfo()
    }

    private fun updateLocation() {
        if(viewModel.checkPermission(
                Manifest.permission.ACCESS_FINE_LOCATION,
                listOf(Items.LATITUDE, Items.LONGITUDE)
            )) return

        viewModel.updateInfoInt(
            listOf(
                Pair(Items.LATITUDE, NOT_AVAILABLE),
                Pair(Items.LONGITUDE, NOT_AVAILABLE)
            )
        )

        locationService.getLocationUpdate(locationCallback)
    }

    private fun askPermission(
        vararg permString: String, requestCode: Int,
        permRational: String) {
        if(EasyPermissions.hasPermissions(this, *permString)) {
            refreshPage()
        } else {
            EasyPermissions.requestPermissions(this, permRational, requestCode, *permString)
        }
    }
}
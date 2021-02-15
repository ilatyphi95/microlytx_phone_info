package com.ilatyphi95.microlytxphoneinfo

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telephony.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.material.snackbar.Snackbar
import com.ilatyphi95.microlytxphoneinfo.databinding.ActivityMainBinding
import pub.devrel.easypermissions.EasyPermissions


class MainActivity : AppCompatActivity() {

    lateinit var binding : ActivityMainBinding
    lateinit var viewModel : MainActivityViewModel
    private lateinit var locationService : LocationService
    private val locationRequestCode = 109

    private val locationCallback = object : LocationCallback() {

        override fun onLocationResult(location: LocationResult?) {
            location?.let { locationResult ->
                val latLon = locationResult.lastLocation
                viewModel.updateInfo(
                    listOf(
                        Pair(Items.LATITUDE, latLon.latitude.toString()),
                        Pair(Items.LONGITUDE, latLon.longitude.toString())
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

        val itemAdapter = ItemAdapter { position, itemAtPosition ->
            Snackbar.make(
                    binding.root,
                    "${getString(itemAtPosition.title)} is at position $position",
                    Snackbar.LENGTH_LONG
            ).show()
        }

        binding.recycler.adapter = itemAdapter
        viewModel.infoList.observe(this) {
            itemAdapter.submitList(it)
        }


        viewModel.updateInfo(
                listOf(
                        Pair(Items.HANDSET_MAKE, Build.MANUFACTURER),
                        Pair(Items.ITEM_MODEL, Build.MODEL),
                )
        )

        updateConnectionStatus()
        updateNetwork()
        subscribeForLocationUpdate()

        updateNetworkInfo()
    }

    private fun updateNetworkInfo() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
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

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {

                val activeCells = phoneManager.allCellInfo.filter { it.isRegistered }

                activeCells.forEach { cellInfo ->
                    when (cellInfo) {
                        is CellInfoGsm -> {
                            val identity = cellInfo.cellIdentity

                            if (identity.mcc == mcc && identity.mnc == mnc) {
                                operatorInfo.addAll(
                                    listOf(
                                        Pair(
                                            Items.SIGNAL_STRENGTH,
                                            cellInfo.cellSignalStrength.level
                                        ),
                                        Pair(Items.CELL_IDENTITY, identity.cid),
                                        Pair(Items.LOCAL_AREA_CODE, identity.lac),
                                    )
                                )
                            }
                        }
                        is CellInfoWcdma -> {
                            val identity = cellInfo.cellIdentity
                            if (identity.mcc == mcc && identity.mnc == mnc) {
                                operatorInfo.addAll(
                                    listOf(
                                        Pair(
                                            Items.SIGNAL_STRENGTH,
                                            cellInfo.cellSignalStrength.level
                                        ),
                                        Pair(Items.CELL_IDENTITY, identity.cid),
                                        Pair(Items.LOCAL_AREA_CODE, identity.lac),
                                    )
                                )
                            }
                        }
                        is CellInfoCdma -> {
                            // check this out later
                        }
                        is CellInfoLte -> {
                            val identity = cellInfo.cellIdentity
                            if (identity.mcc == mcc && identity.mnc == mnc) {
                                operatorInfo.addAll(
                                    listOf(
                                        Pair(
                                            Items.SIGNAL_STRENGTH,
                                            cellInfo.cellSignalStrength.level
                                        ),
                                        Pair(Items.CELL_ID, identity.ci),
                                    )
                                )
                            }
                        }

                        else -> {
                            viewModel.updateNetworkInfo()
                        }
                    }
                }
            }

            viewModel.updateInfoInt(operatorInfo)
    //        } else {
            // work this out later
        }
    }

    override fun onResume() {
        super.onResume()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationService.getLocationUpdate(locationCallback)
        }
    }

    override fun onPause() {
        locationService.removeLocationUpdate(locationCallback)
        super.onPause()
    }

    private fun updateConnectionStatus() {
        viewModel.updateConnectionStatus()
    }

    private fun updateNetwork() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        viewModel.updateNetworkType()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    @SuppressLint("MissingPermission")
    fun subscribeForLocationUpdate() {
        if(EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            locationService.getLocationUpdate(locationCallback)
        } else {
            EasyPermissions.requestPermissions(this, "Need Location", locationRequestCode, Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
}
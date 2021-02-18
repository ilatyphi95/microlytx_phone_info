package com.ilatyphi95.microlytxphoneinfo.ui

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.TelephonyManager
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.ilatyphi95.microlytxphoneinfo.R
import com.ilatyphi95.microlytxphoneinfo.data.ItemUtils
import com.ilatyphi95.microlytxphoneinfo.data.Items
import com.ilatyphi95.microlytxphoneinfo.data.NOT_AVAILABLE
import com.ilatyphi95.microlytxphoneinfo.data.PhoneItem
import com.ilatyphi95.microlytxphoneinfo.utils.ConnectionInfo
import com.ilatyphi95.microlytxphoneinfo.utils.NetworkInfo
import com.ilatyphi95.microlytxphoneinfo.utils.getNetworkType

class MainActivityViewModel(application: Application, private val itemUtils: ItemUtils) : AndroidViewModel(application) {

    private val app = application
    private val requirePermission = application.getString(R.string.require_permission)

    private val _infoList = MutableLiveData<List<PhoneItem>>()
    val infoList : LiveData<List<PhoneItem>> = _infoList

    private val _receiveLocationUpdate = MutableLiveData<Boolean>()
    val receiveLocationUpdate : LiveData<Boolean> = _receiveLocationUpdate


    init {
        _infoList.value = itemUtils.getPhoneInfoList()
        updateManufacturerInfo()
        updateConnectionStatus()
    }

    val locationCallback = object : LocationCallback() {

        override fun onLocationResult(location: LocationResult?) {
            if(location != null) {
                val latLon = location.lastLocation
                updateInfo(
                    listOf(
                        Pair(Items.LATITUDE, latLon.latitude.toString()),
                        Pair(Items.LONGITUDE, latLon.longitude.toString())
                    )
                )
            } else {
                updateInfoInt(
                    listOf(
                        Pair(Items.LATITUDE, NOT_AVAILABLE),
                        Pair(Items.LONGITUDE, NOT_AVAILABLE)
                    )
                )
            }
        }
    }

    fun updateInfoInt(newItems: List<Pair<Items, Int>>) {
        updateInfo( itemUtils.convertIntPairToStringPair(newItems) )
    }

    fun updateInfo(newItems: List<Pair<Items, String>>) {
        val items = itemUtils.convertItemsToPhoneItems(newItems)
        updateThreadSafe(items)
    }

    @Synchronized private fun updateThreadSafe(items: List<PhoneItem>) {
        var myList = _infoList.value!!
        items.forEach {
            myList = itemUtils.replaceItems(myList, it)
        }
        _infoList.value = myList
    }

    fun refreshItems() {
        updateConnectionStatus()
        updateSubscriberInfo()
        updateNetworkInfo()
        updateNetworkTypeInfo()
        updateLocation()
    }

    private fun updateConnectionStatus() {
        val connectionInfo = ConnectionInfo(app)
        updateInfo(connectionInfo.getConnectionInfo())
    }

    private fun updateManufacturerInfo() {
        updateInfo(
            listOf(
                Pair(Items.HANDSET_MAKE, Build.MANUFACTURER),
                Pair(Items.ITEM_MODEL, Build.MODEL),
            )
        )
    }

    private fun updateSubscriberInfo() {
        val networkInfo = NetworkInfo(app, itemUtils)

        if(checkPermission(
                Manifest.permission.READ_PHONE_STATE,
                affectedItems = listOf(Items.MOBILE_NETWORK_CODE, Items.MOBILE_COUNTRY_CODE, Items.OPERATOR_NAME)
            )) return

        updateInfo(networkInfo.getSubscriberInfo())
    }

    private fun updateNetworkTypeInfo() {

        if (checkPermission(
                Manifest.permission.READ_PHONE_STATE,
                affectedItems = listOf(Items.MOBILE_NETWORK_TECHNOLOGY)
            )) return

        val telephonyManager = app.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        val value = app.getString(getNetworkType(telephonyManager.networkType))

        updateInfo(
            listOf(
                Pair(Items.MOBILE_NETWORK_TECHNOLOGY, value)
            )
        )
    }

    private fun updateNetworkInfo() {
        val networkInfo = NetworkInfo(app, itemUtils)

        if(checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE,
                affectedItems = listOf(Items.CELL_ID, Items.CELL_IDENTITY, Items.SIGNAL_STRENGTH, Items.LOCAL_AREA_CODE)
            )) return

        updateInfo(networkInfo.getNetworkInfo())
    }


    fun updateLocation() {
        if(checkPermission(
                Manifest.permission.ACCESS_FINE_LOCATION,
                affectedItems = listOf(Items.LATITUDE, Items.LONGITUDE)
            )) {
                _receiveLocationUpdate.value = false
                return
        }

        updateInfoInt(
            listOf(
                Pair(Items.LATITUDE, NOT_AVAILABLE),
                Pair(Items.LONGITUDE, NOT_AVAILABLE)
            )
        )

        _receiveLocationUpdate.value = true
    }

    private fun checkPermission(vararg permString: String, affectedItems: List<Items>): Boolean {

        var notGranted = false

        permString.forEach { perm ->
            if(ActivityCompat.checkSelfPermission(app, perm) != PackageManager.PERMISSION_GRANTED) {
                notGranted = true
            }
        }

        if (notGranted) {
            val itemList = mutableListOf<Pair<Items, String>>()

            affectedItems.forEach{
                itemList.add(Pair(it, requirePermission))
            }
            updateInfo(itemList)
        }
        return notGranted
    }
}
package com.ilatyphi95.microlytxphoneinfo.ui

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ilatyphi95.microlytxphoneinfo.R
import com.ilatyphi95.microlytxphoneinfo.data.ItemUtils
import com.ilatyphi95.microlytxphoneinfo.data.Items
import com.ilatyphi95.microlytxphoneinfo.data.PhoneItem

const val NOT_AVAILABLE = -1
const val REQUIRE_PERMISSION = -101
class MainActivityViewModel(application: Application, private val itemUtils: ItemUtils) : AndroidViewModel(application) {

    private val app = application
    private val notAvailable = application.getString(R.string.not_available)
    private val requirePermission = application.getString(R.string.require_permission)

    private val _infoList = MutableLiveData<List<PhoneItem>>()
    val infoList : LiveData<List<PhoneItem>> = _infoList

    init {
        _infoList.value = itemUtils.getPhoneInfoList(app)
    }

    fun updateInfoInt(newItems: List<Pair<Items, Int>>) {
        updateInfo( itemUtils.convertIntPairToStringPair(newItems) )
    }

    fun updateInfo(newItems: List<Pair<Items, String>>) {
        val items = convertItemsToPhoneItems(newItems)
        updateThreadSafe(items)

    }

    @Synchronized private fun updateThreadSafe(items: List<PhoneItem>) {
        var myList = _infoList.value!!
        items.forEach {
            myList = replaceItems(myList, it)
        }
        _infoList.value = myList
    }

    private fun replaceItems(oldList: List<PhoneItem>, newItem: PhoneItem) : List<PhoneItem> {
        val info = oldList.toMutableList()
        val oldItem = info.find { it.id == newItem.id }
        val index = info.indexOf(oldItem)
        info.removeAt(index)
        info.add(index, newItem)
        return info.toList()
    }

    private fun convertIntPairToStringPair(intItems: List<Pair<Items, Int>>) :
            List<Pair<Items, String>> {

        val stringItems = mutableListOf<Pair<Items, String>>()

        intItems.forEach{ pair ->

            val second = when(pair.second) {
                NOT_AVAILABLE -> notAvailable
                REQUIRE_PERMISSION -> requirePermission
                else -> pair.second.toString()
            }

            stringItems.add(Pair(pair.first, second))
        }

        return stringItems
    }

    private fun convertItemsToPhoneItems(myList: List<Pair<Items, String>>) : List<PhoneItem> {
        val phoneItemList = mutableListOf<PhoneItem>()

        myList.forEach {

            val valueRes = when(it.first) {
                Items.MOBILE_COUNTRY_CODE -> R.string.mobile_country_code
                Items.MOBILE_NETWORK_CODE -> R.string.mobile_network_code
                Items.LOCAL_AREA_CODE -> R.string.local_area_code
                Items.CELL_IDENTITY -> R.string.cell_identity
                Items.CELL_ID -> R.string.cell_id
                Items.MOBILE_NETWORK_TECHNOLOGY -> R.string.mobile_network_technology
                Items.SIGNAL_STRENGTH -> R.string.signal_strength
                Items.OPERATOR_NAME -> R.string.operator_name
                Items.CELL_CONNECTION_STATUS -> R.string.cell_connection_status
                Items.HANDSET_MAKE -> R.string.handset_make
                Items.ITEM_MODEL -> R.string.phone_model
                Items.LONGITUDE -> R.string.longitude
                Items.LATITUDE -> R.string.latitude
            }
            phoneItemList.add(PhoneItem(it.first, valueRes, it.second))
        }

        return phoneItemList
    }

    fun updateConnectionStatus() {
        var result = R.string.no_connection
        val connectivityManager =
                app.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            val networkCapabilities = connectivityManager.activeNetwork
            val actNw = connectivityManager.getNetworkCapabilities(networkCapabilities)
            actNw?.let {

                result = when {
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> R.string.wifi_connection
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> R.string.cellular_connection
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> R.string.ethernet_connection
                    else -> R.string.no_connection
                }
            }
        } else {
            connectivityManager.run {
                connectivityManager.activeNetworkInfo?.run {
                    result = when (type) {
                        ConnectivityManager.TYPE_WIFI -> R.string.wifi_connection
                        ConnectivityManager.TYPE_MOBILE -> R.string.cellular_connection
                        ConnectivityManager.TYPE_ETHERNET -> R.string.ethernet_connection
                        else -> R.string.no_connection
                    }
                }
            }
        }
        updateInfo(listOf(Pair(Items.CELL_CONNECTION_STATUS, app.getString(result))))
    }

    fun checkPermission(vararg permString: String, affectedItems: List<Items>): Boolean {

        var isGranted = true

        permString.forEach { perm ->
            if(ActivityCompat.checkSelfPermission(app, perm) != PackageManager.PERMISSION_GRANTED) {
                isGranted = false
            }
        }

        if (!isGranted) {
            val itemList = mutableListOf<Pair<Items, String>>()

            affectedItems.forEach{
                itemList.add(Pair(it, requirePermission))
            }

            updateInfo(itemList)
        }

        return isGranted
    }
}
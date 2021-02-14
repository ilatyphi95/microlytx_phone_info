package com.ilatyphi95.microlytxphoneinfo

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.telephony.TelephonyManager
import androidx.annotation.RequiresPermission
import androidx.lifecycle.*

const val NOT_AVAILABLE = -1
const val REQUIRE_PERMISSION = -101
class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application
    private val NOT_AVAILABLE_STRING = application.getString(R.string.not_available)
    private val REQUIRE_PERMISSION_STRING = application.getString(R.string.require_permission)

    private val _infoList = MutableLiveData<List<PhoneItem>>()
    val infoList : LiveData<List<PhoneItem>> = _infoList

    init {
        _infoList.value = ItemList().getPhoneInfoList()
    }

    private fun replaceItem(oldList: List<PhoneItem>, newItem: PhoneItem) : List<PhoneItem> {
        val infos = oldList.toMutableList()
        val oldItem = infos.find { it.id == newItem.id }
        val index = infos.indexOf(oldItem)
        infos.removeAt(index)
        infos.add(index, newItem)
        return infos.toList()
    }

    fun updateInfo(items: List<PhoneItem>) {
        var myList = _infoList.value!!
        items.forEach{
            myList = replaceItem(myList, it)
        }
        _infoList.value = myList
    }

    fun addListInt(myList: List<Pair<Items, Int>>) : List<PhoneItem> {
        val list = mutableListOf<Pair<Items, String>>()

        myList.forEach{ pair ->

            val second = when(pair.second) {
                NOT_AVAILABLE -> NOT_AVAILABLE_STRING
                REQUIRE_PERMISSION -> REQUIRE_PERMISSION_STRING
                else -> pair.second.toString()
            }

            list.add(Pair(pair.first, second))
        }

        return addList(list)
    }

    fun addList(myList: List<Pair<Items, String>>) : List<PhoneItem> {
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

    fun addNetworkInfo(
            mcc: Int = NOT_AVAILABLE,
            mnc: Int = NOT_AVAILABLE,
            signalStrength: Int = NOT_AVAILABLE,
            cid: Int = NOT_AVAILABLE,
            ci: Int = NOT_AVAILABLE,
            lac: Int = NOT_AVAILABLE) {

        val infoList = addListInt(listOf(
                Pair(Items.MOBILE_COUNTRY_CODE, mcc),
                Pair(Items.MOBILE_NETWORK_CODE, mnc),
                Pair(Items.SIGNAL_STRENGTH, signalStrength),
                Pair(Items.CELL_ID, cid),
                Pair(Items.CELL_IDENTITY, ci),
                Pair(Items.LOCAL_AREA_CODE, lac)))

        updateInfo(infoList)
    }

    @RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)
    fun connectionStatus() {
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

        updateInfo(addList(listOf(Pair(Items.CELL_CONNECTION_STATUS, app.getString(result)))))
    }


    @RequiresPermission(android.Manifest.permission.READ_PHONE_STATE)
    fun updateNetworkType() {
        val telephonyManager = app.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        val result = getNetworkType(telephonyManager.networkType)

        updateInfo(addList(listOf(Pair(Items.MOBILE_NETWORK_TECHNOLOGY, app.getString(result)))))
    }
}

class MainActivityViewModelFactory(private val application: Application)
    : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T
        = modelClass.getConstructor(Application::class.java).newInstance(application)

    }
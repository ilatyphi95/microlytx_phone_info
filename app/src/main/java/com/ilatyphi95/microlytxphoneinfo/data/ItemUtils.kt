package com.ilatyphi95.microlytxphoneinfo.data

import android.content.Context
import com.ilatyphi95.microlytxphoneinfo.R
import com.ilatyphi95.microlytxphoneinfo.data.Items.*
import java.lang.IllegalArgumentException

const val NOT_AVAILABLE = -1
const val REQUIRE_PERMISSION = -101
class ItemUtils(context: Context) {

    private val notAvailable = context.getString(R.string.not_available)
    private val requirePermission = context.getString(R.string.require_permission)

    fun getPhoneInfoList(): List<PhoneItem> {
        return listOf(
            PhoneItem(MOBILE_COUNTRY_CODE, title = R.string.mobile_country_code, notAvailable),
            PhoneItem(MOBILE_NETWORK_CODE, title = R.string.mobile_network_code, notAvailable),
            PhoneItem(LOCAL_AREA_CODE, title = R.string.local_area_code, notAvailable),
            PhoneItem(CELL_IDENTITY, title = R.string.cell_identity, notAvailable),
            PhoneItem(CELL_ID, title = R.string.cell_id, notAvailable),
            PhoneItem(MOBILE_NETWORK_TECHNOLOGY, title = R.string.mobile_network_technology, notAvailable),
            PhoneItem(SIGNAL_STRENGTH, title = R.string.signal_strength, notAvailable),
            PhoneItem(OPERATOR_NAME, title = R.string.operator_name, notAvailable),
            PhoneItem(CELL_CONNECTION_STATUS, title = R.string.cell_connection_status, notAvailable),
            PhoneItem(HANDSET_MAKE, title = R.string.handset_make, notAvailable),
            PhoneItem(ITEM_MODEL, title = R.string.phone_model, notAvailable),
            PhoneItem(LONGITUDE, title = R.string.longitude, notAvailable),
            PhoneItem(LATITUDE, title = R.string.latitude, notAvailable),
        )
    }

    fun convertIntPairToStringPair(intItems: List<Pair<Items, Int>>) :
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

    fun convertItemsToPhoneItems(myList: List<Pair<Items, String>>) : List<PhoneItem> {
        val phoneItemList = mutableListOf<PhoneItem>()

        myList.forEach {

            val valueRes = when(it.first) {
                MOBILE_COUNTRY_CODE -> R.string.mobile_country_code
                MOBILE_NETWORK_CODE -> R.string.mobile_network_code
                LOCAL_AREA_CODE -> R.string.local_area_code
                CELL_IDENTITY -> R.string.cell_identity
                CELL_ID -> R.string.cell_id
                MOBILE_NETWORK_TECHNOLOGY -> R.string.mobile_network_technology
                SIGNAL_STRENGTH -> R.string.signal_strength
                OPERATOR_NAME -> R.string.operator_name
                CELL_CONNECTION_STATUS -> R.string.cell_connection_status
                HANDSET_MAKE -> R.string.handset_make
                ITEM_MODEL -> R.string.phone_model
                LONGITUDE -> R.string.longitude
                LATITUDE -> R.string.latitude
            }
            phoneItemList.add(PhoneItem(it.first, valueRes, it.second))
        }

        return phoneItemList
    }


    fun replaceItems(oldList: List<PhoneItem>, newItem: PhoneItem) : List<PhoneItem> {
        val info = oldList.toMutableList()
        val oldItem = info.find { it.id == newItem.id }

        if(oldItem == null)
            throw IllegalArgumentException("$oldItem does not exist in list")

        val index = info.indexOf(oldItem)
        info.removeAt(index)
        info.add(index, newItem)
        return info.toList()
    }
}
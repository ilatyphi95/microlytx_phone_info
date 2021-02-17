package com.ilatyphi95.microlytxphoneinfo.data

import android.app.Application
import android.content.Context
import com.ilatyphi95.microlytxphoneinfo.R
import com.ilatyphi95.microlytxphoneinfo.data.Items.*
import com.ilatyphi95.microlytxphoneinfo.ui.NOT_AVAILABLE
import com.ilatyphi95.microlytxphoneinfo.ui.REQUIRE_PERMISSION

class ItemUtils(context: Context) {

    private val notAvailable = context.getString(R.string.not_available)
    private val requirePermission = context.getString(R.string.require_permission)

    fun getPhoneInfoList(app: Application): List<PhoneItem> {
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
}
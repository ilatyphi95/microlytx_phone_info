package com.ilatyphi95.microlytxphoneinfo

import com.ilatyphi95.microlytxphoneinfo.Items.*

class ItemList {

    fun getPhoneInfoList() : List<PhoneItem> {
        return listOf(
            PhoneItem(MOBILE_COUNTRY_CODE, title = R.string.mobile_country_code),
            PhoneItem(MOBILE_NETWORK_CODE, title = R.string.mobile_network_code),
            PhoneItem(LOCAL_AREA_CODE, title = R.string.local_area_code),
            PhoneItem(CELL_IDENTITY, title = R.string.cell_identity),
            PhoneItem(CELL_ID, title = R.string.cell_id),
            PhoneItem(MOBILE_NETWORK_TECHNOLOGY, title = R.string.mobile_network_technology),
            PhoneItem(SIGNAL_STRENGTH, title = R.string.signal_strength),
            PhoneItem(OPERATOR_NAME, title = R.string.operator_name),
            PhoneItem(CELL_CONNECTION_STATUS, title = R.string.cell_connection_status),
            PhoneItem(HANDSET_MAKE, title = R.string.handset_make),
            PhoneItem(ITEM_MODEL, title = R.string.phone_model),
            PhoneItem(LONGITUDE, title = R.string.longitude),
            PhoneItem(LATITUDE, title = R.string.latitude),
        )
    }
}
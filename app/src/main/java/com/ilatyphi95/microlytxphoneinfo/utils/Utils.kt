package com.ilatyphi95.microlytxphoneinfo

import android.os.Build
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import java.lang.reflect.InvocationTargetException

fun getDefaultDataSubscriptionId(subscriptionManager: SubscriptionManager): Int {
    if (Build.VERSION.SDK_INT >= 24) {
        val nDataSubscriptionId = SubscriptionManager.getDefaultDataSubscriptionId()
        if (nDataSubscriptionId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
            return nDataSubscriptionId
        }
    }
    try {
        val subscriptionClass = Class.forName(subscriptionManager.javaClass.name)
        try {
            val getDefaultDataSubscriptionId = subscriptionClass.getMethod("getDefaultDataSubId")
            try {
                return getDefaultDataSubscriptionId.invoke(subscriptionManager) as Int
            } catch (e1: IllegalAccessException) {
                e1.printStackTrace()
            } catch (e1: InvocationTargetException) {
                e1.printStackTrace()
            }
        } catch (e1: NoSuchMethodException) {
            e1.printStackTrace()
        }
    } catch (e1: ClassNotFoundException) {
        e1.printStackTrace()
    }
    return -1
}

fun getNetworkType(networkId: Int) : Int {
    return when (networkId) {
        TelephonyManager.NETWORK_TYPE_UNKNOWN -> R.string.unknown_network

        TelephonyManager.NETWORK_TYPE_GSM -> R.string.gsm_network

        TelephonyManager.NETWORK_TYPE_CDMA, TelephonyManager.NETWORK_TYPE_1xRTT,
        TelephonyManager.NETWORK_TYPE_IDEN -> R.string.network_2g

        TelephonyManager.NETWORK_TYPE_GPRS -> R.string.network_gprs

        TelephonyManager.NETWORK_TYPE_EDGE -> R.string.network_edge

        TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_EVDO_0,
        TelephonyManager.NETWORK_TYPE_EVDO_A, TelephonyManager.NETWORK_TYPE_EVDO_B -> R.string.network_3g

        TelephonyManager.NETWORK_TYPE_HSPA, TelephonyManager.NETWORK_TYPE_HSDPA,
        TelephonyManager.NETWORK_TYPE_HSUPA -> R.string.network_h

        TelephonyManager.NETWORK_TYPE_EHRPD, TelephonyManager.NETWORK_TYPE_HSPAP,
        TelephonyManager.NETWORK_TYPE_TD_SCDMA -> R.string.network_hplus

        TelephonyManager.NETWORK_TYPE_LTE, TelephonyManager.NETWORK_TYPE_IWLAN -> R.string.network_4g

        TelephonyManager.NETWORK_TYPE_NR -> R.string.network_5g
        else -> R.string.unknown_network
    }
}


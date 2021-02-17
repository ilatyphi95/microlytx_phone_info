package com.ilatyphi95.microlytxphoneinfo

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.ilatyphi95.microlytxphoneinfo.data.Items
import java.lang.reflect.Method


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
private fun getDataSimOperatorBeforeN(context: Context?): TelephonyManager? {
    if (context == null) {
        return null
    }
    var dataSubId = -1
    try {
        val getDefaultDataSubId: Method? = SubscriptionManager::class.java.getDeclaredMethod("getDefaultDataSubId")
        if (getDefaultDataSubId != null) {
            getDefaultDataSubId.isAccessible = true
            dataSubId = getDefaultDataSubId.invoke(null) as Int
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    if (dataSubId != -1) {
        val sm = context.getSystemService(AppCompatActivity.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED) {
            val si = sm.getActiveSubscriptionInfo(dataSubId)
            if (si != null) {
                si.carrierName
                // format keep the same with android.telephony.TelephonyManager#getSimOperator
                // MCC + MNC format
            }
        }
    }
    return null
}

@RequiresPermission(allOf = [Manifest.permission.READ_PHONE_STATE,
    Manifest.permission.ACCESS_FINE_LOCATION])
private fun getDataSim(context: Context) {

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
        var dataSubId = -1
        try {
            val getDefaultDataSubId: Method? =
                    SubscriptionManager::class.java.getDeclaredMethod("getDefaultDataSubId")

            if (getDefaultDataSubId != null) {
                getDefaultDataSubId.isAccessible = true
                dataSubId = getDefaultDataSubId.invoke(null) as Int
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (dataSubId != -1) {
            val sm = context.getSystemService(AppCompatActivity.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
            val si = sm.getActiveSubscriptionInfo(dataSubId)
            if (si != null) {
                si.carrierName
                si.displayName
                si.mcc
                si.mnc
                // format keep the same with android.telephony.TelephonyManager#getSimOperator
                // MCC + MNC format
                return
            }
        } else {
            // no data connection available
        }
    } else {
        val tm = context.getSystemService(AppCompatActivity.TELEPHONY_SERVICE) as TelephonyManager
        val registered = tm.allCellInfo.filter { it.isRegistered }

        when(registered.size) {
            0 -> {
                // no data sim card available
            }
            1 -> {
                tm.networkOperatorName
                tm.simOperatorName
                tm.networkOperator

            }
            else -> {
                // multiple sim cards available
            }
        }
    }
}

@RequiresPermission(Manifest.permission.READ_PHONE_STATE)
fun getSimMccData(context: Context) : String? {

    val stringList = mutableListOf<String>()
    var returnedString: String? = null
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
        val subscriptionManager = context.getSystemService(AppCompatActivity.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        var dataSubId = -1
        try {
            val getDefaultDataSubId: Method? =
                    SubscriptionManager::class.java.getDeclaredMethod("getDefaultDataSubId")

            if (getDefaultDataSubId != null) {
                getDefaultDataSubId.isAccessible = true
                dataSubId = getDefaultDataSubId.invoke(subscriptionManager) as Int
                stringList.add("dataSubId${getDefaultDataSubId.invoke(subscriptionManager)}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (dataSubId != -1) {
            val sm = context.getSystemService(AppCompatActivity.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
            val si = sm.getActiveSubscriptionInfo(dataSubId)
            if (si != null) {
                stringList.add(si.carrierName.toString())
                stringList.add(si.displayName.toString())
                stringList.add(si.mcc.toString())
                stringList.add(si.mnc.toString())

                returnedString = stringList.joinToString { "|" }
//                returnedString = "${si.displayName}|${si.mcc}|${si.mnc}"
                // format keep the same with android.telephony.TelephonyManager#getSimOperator
                // MCC + MNC format
            }
        } else {
            // no data connection available
            returnedString = "${context.getString(R.string.not_available)}data|" +
                    "${context.getString(R.string.not_available)}data|" +
                    context.getString(R.string.not_available)
        }
    }

    return returnedString
}

@RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
fun getSimMccDataPreL(context: Context) : String {
    val tm = context.getSystemService(AppCompatActivity.TELEPHONY_SERVICE) as TelephonyManager
    val registered = tm.allCellInfo.filter { it.isRegistered }

    return when(registered.size) {
        0 -> {
            // no data sim card available
            "${context.getString(R.string.not_available)}no sim|" +
                    "${context.getString(R.string.not_available)}|" +
                    context.getString(R.string.not_available)
        }
        1 -> {
            tm.networkOperatorName
            tm.simOperatorName
            tm.networkOperator

        }
        else -> {
            // multiple sim cards available
            "${context.getString(R.string.not_available)}multisim|" +
                    "${context.getString(R.string.not_available)}|" +
                    context.getString(R.string.not_available)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
@RequiresPermission(Manifest.permission.READ_PHONE_STATE)
fun getOtherNetworkInfo(context: Context, subscriptionManager: SubscriptionManager, mcc: Int, mnc: Int) : List<Pair<Items, String>> {
    val info: SubscriptionInfo? = subscriptionManager.activeSubscriptionInfoList.find{ it.mcc == mcc && it.mnc == mnc}
    return if(info != null) {
        listOf(
                Pair(Items.OPERATOR_NAME, info.carrierName.toString()),
//                    Pair(Items., info.)
        )
    } else {
        emptyList()
    }
}

//@TargetApi(22)
//fun getUIText22(telephonyManager: TelephonyManager?): String {
//    val subscriptionManager = getContext().getApplicationContext().getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
//    val nDataSubscriptionId = getDefaultDataSubscriptionId(subscriptionManager)
//    if (nDataSubscriptionId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
//        val si = subscriptionManager.getActiveSubscriptionInfo(nDataSubscriptionId)
//        if (si != null) {
//            return si.carrierName.toString()
//        }
//    }
//}
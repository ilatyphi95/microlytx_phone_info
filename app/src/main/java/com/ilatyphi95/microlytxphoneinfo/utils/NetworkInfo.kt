package com.ilatyphi95.microlytxphoneinfo.utils

import android.Manifest
import android.app.Application
import android.content.Context
import android.os.Build
import android.telephony.*
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import com.ilatyphi95.microlytxphoneinfo.R
import com.ilatyphi95.microlytxphoneinfo.data.ItemUtils
import com.ilatyphi95.microlytxphoneinfo.data.Items
import com.ilatyphi95.microlytxphoneinfo.ui.NOT_AVAILABLE

class NetworkInfo(private val context: Application, private val itemList: ItemUtils) {
    private val notAvailable = context.getString(R.string.not_available)

    @RequiresPermission(allOf = [Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_FINE_LOCATION])
    fun getAllNetworkInfo() : List<Pair<Items, String>> {

        val list = getSubscriberInfo().toMutableList()
            list.addAll(getNetworkInfo())

        return list
    }

    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    fun getSubscriberInfo() : List<Pair<Items, String>> {
        val subInfo = subscriberInfo()
        return if(subInfo != null) {
            listOf(
                Pair(Items.MOBILE_COUNTRY_CODE, subInfo.mcc.toString()),
                Pair(Items.MOBILE_NETWORK_CODE,subInfo.mnc.toString()),
                Pair(Items.OPERATOR_NAME, subInfo.networkOperator),
            )
        } else {
            listOf(
                Pair(Items.MOBILE_COUNTRY_CODE, notAvailable),
                Pair(Items.MOBILE_NETWORK_CODE, notAvailable),
                Pair(Items.OPERATOR_NAME, notAvailable),
            )
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_FINE_LOCATION])
    fun getNetworkInfo() : List<Pair<Items, String>> {

            val phoneManager = context.getSystemService(AppCompatActivity.TELEPHONY_SERVICE) as TelephonyManager

        val subInfo = subscriberInfo() ?: return itemList.convertIntPairToStringPair(networkInfo())

            val activeCells = phoneManager.allCellInfo.filter { it.isRegistered }
        var networkInfoList = networkInfo()

            activeCells.forEach { cellInfo ->
                when (cellInfo) {
                    is CellInfoGsm -> {
                        val identity = cellInfo.cellIdentity

                        if (identity.mcc == subInfo.mcc && identity.mnc == subInfo.mnc) {

                            networkInfoList = networkInfo(
                                signalStrength = cellInfo.cellSignalStrength.level,
                                cid = identity.cid, lac = identity.lac
                            )
                        }
                    }
                    is CellInfoWcdma -> {
                        val identity = cellInfo.cellIdentity
                        if (identity.mcc == subInfo.mcc && identity.mnc == subInfo.mnc) {

                            networkInfoList = networkInfo(
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
                        if (identity.mcc == subInfo.mcc && identity.mnc == subInfo.mnc) {
                            networkInfoList = networkInfo( ci = identity.ci,
                                signalStrength = cellInfo.cellSignalStrength.level
                            )
                        }
                    }
                }
            }
        return itemList.convertIntPairToStringPair(networkInfoList)

    }

    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    private fun subscriberInfo() : SubscriberInfo? {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {

                val subscriptionManager = context.getSystemService(AppCompatActivity.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
                val id = getDefaultDataSubscriptionId(subscriptionManager)

                val subInfo = subscriptionManager.getActiveSubscriptionInfo(id) ?: return null

                SubscriberInfo(subInfo.mcc, subInfo.mnc, subInfo.carrierName.toString())
            } else {
                //Take care ("VERSION.SDK_INT < LOLLIPOP_MR1")
                null
            }
    }

    private fun networkInfo(
        signalStrength: Int = NOT_AVAILABLE,
        cid: Int = NOT_AVAILABLE,
        ci: Int = NOT_AVAILABLE,
        lac: Int = NOT_AVAILABLE
    ) : List<Pair<Items, Int>> {

        return (listOf(
            Pair(Items.SIGNAL_STRENGTH, signalStrength),
            Pair(Items.CELL_ID, cid),
            Pair(Items.CELL_IDENTITY, ci),
            Pair(Items.LOCAL_AREA_CODE, lac)))
    }

    private class SubscriberInfo(val mcc: Int, val mnc: Int, val networkOperator: String)
}
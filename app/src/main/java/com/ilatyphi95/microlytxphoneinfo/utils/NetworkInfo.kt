package com.ilatyphi95.microlytxphoneinfo.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.os.Build
import android.telephony.*
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import com.ilatyphi95.microlytxphoneinfo.R
import com.ilatyphi95.microlytxphoneinfo.data.ItemUtils
import com.ilatyphi95.microlytxphoneinfo.data.Items
import com.ilatyphi95.microlytxphoneinfo.data.NOT_AVAILABLE

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
                Items.MOBILE_COUNTRY_CODE to subInfo.mcc.toString(),
                Items.MOBILE_NETWORK_CODE to subInfo.mnc.toString(),
                Items.OPERATOR_NAME to subInfo.networkOperator
            )
        } else {
            listOf(
                Items.MOBILE_COUNTRY_CODE to notAvailable,
                Items.MOBILE_NETWORK_CODE to notAvailable,
                Items.OPERATOR_NAME to notAvailable
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

    @SuppressLint("MissingPermission")
    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    private fun subscriberInfo() : SubscriberInfo? {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {

                val subscriptionManager = context.getSystemService(AppCompatActivity.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
                val id = getDefaultDataSubscriptionId(subscriptionManager)

                val subInfo = subscriptionManager.getActiveSubscriptionInfo(id) ?: return null

                SubscriberInfo(subInfo.mcc, subInfo.mnc, subInfo.carrierName.toString())
            } else {
                //Take care ("VERSION.SDK_INT < LOLLIPOP_MR1")
                val telephonyManager =
                    context.getSystemService(AppCompatActivity.TELEPHONY_SERVICE) as TelephonyManager

                val registeredNetworks = telephonyManager.allCellInfo.filter { it.isRegistered }
                return if( registeredNetworks.size == 1) {

                    val mccMnc = telephonyManager.networkOperator

                    val mcc = mccMnc.substring(0, 3).toInt()
                    val mnc = mccMnc.substring(3).toInt()
                    SubscriberInfo(mcc, mnc, telephonyManager.networkOperatorName)
                } else {
                    null
                }
            }
    }

    private fun networkInfo(
        signalStrength: Int = NOT_AVAILABLE,
        cid: Int = NOT_AVAILABLE,
        ci: Int = NOT_AVAILABLE,
        lac: Int = NOT_AVAILABLE
    ) : List<Pair<Items, Int>> {

        return (listOf(
            Items.SIGNAL_STRENGTH to signalStrength,
            Items.CELL_ID to cid,
            Items.CELL_IDENTITY to ci,
            Items.LOCAL_AREA_CODE to lac))
    }

    private class SubscriberInfo(val mcc: Int, val mnc: Int, val networkOperator: String)
}
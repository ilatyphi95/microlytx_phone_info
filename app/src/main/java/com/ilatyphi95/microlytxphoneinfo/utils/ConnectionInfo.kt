package com.ilatyphi95.microlytxphoneinfo.utils

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.ilatyphi95.microlytxphoneinfo.R
import com.ilatyphi95.microlytxphoneinfo.data.Items

class ConnectionInfo(private val app: Application) {
    fun getConnectionInfo() : List<Pair<Items, String>> {
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
        return listOf(Pair(Items.CELL_CONNECTION_STATUS, app.getString(result)))
    }
}
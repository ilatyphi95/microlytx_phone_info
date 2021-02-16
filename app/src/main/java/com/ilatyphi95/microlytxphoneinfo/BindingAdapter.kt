package com.ilatyphi95.microlytxphoneinfo

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.databinding.BindingAdapter

@BindingAdapter("setTextFromRes")
fun TextView.setTextFromRes(@StringRes id: Int) {
    text = this.context.getString(id)
}


@BindingAdapter("grantButtonState")
fun Button.showGrantButton(text: String) {
    val permissionText = context.getString(R.string.require_permission)
    visibility = if(text == permissionText){
        View.VISIBLE
    } else {
        View.GONE
    }
}
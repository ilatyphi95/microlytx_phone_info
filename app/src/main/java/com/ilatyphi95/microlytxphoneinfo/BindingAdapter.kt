package com.ilatyphi95.microlytxphoneinfo

import android.widget.TextView
import androidx.annotation.StringRes
import androidx.databinding.BindingAdapter

@BindingAdapter("setTextFromRes")
fun TextView.setTextFromRes(@StringRes id: Int) {
    text = this.context.getString(id)
}
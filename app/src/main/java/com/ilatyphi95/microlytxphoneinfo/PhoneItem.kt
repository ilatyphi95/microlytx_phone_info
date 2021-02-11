package com.ilatyphi95.microlytxphoneinfo

import androidx.annotation.StringRes

data class PhoneItem(val id: Items, @StringRes val title: Int, val value: String = "")

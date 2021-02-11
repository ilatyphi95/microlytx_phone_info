package com.ilatyphi95.microlytxphoneinfo

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.TelephonyManager
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.google.android.material.snackbar.Snackbar
import com.ilatyphi95.microlytxphoneinfo.Items.*
import com.ilatyphi95.microlytxphoneinfo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        val itemAdapter = ItemAdapter { position, itemAtPosition ->
            Snackbar.make(binding.root, "${getString(itemAtPosition.title)} is at position $position", Snackbar.LENGTH_LONG).show()

        }
        binding.recycler.adapter = itemAdapter

        itemAdapter.submitList(ItemList().getPhoneInfoList())


        val phoneManager = baseContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    }
}
package com.ilatyphi95.microlytxphoneinfo.ui


import android.os.Build
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObjectNotFoundException
import androidx.test.uiautomator.UiSelector
import com.ilatyphi95.microlytxphoneinfo.R
import com.ilatyphi95.microlytxphoneinfo.atPosition
import com.ilatyphi95.microlytxphoneinfo.clickOnViewChild
import com.ilatyphi95.microlytxphoneinfo.data.ItemUtils
import com.ilatyphi95.microlytxphoneinfo.data.Items
import com.ilatyphi95.microlytxphoneinfo.data.PhoneItem
import com.ilatyphi95.microlytxphoneinfo.ui.mainactivity.MainActivity
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.jvm.Throws

@LargeTest
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    private val appContext = InstrumentationRegistry.getInstrumentation().targetContext
    private val itemUtil: ItemUtils = ItemUtils(appContext)
    private val requirePermission = appContext.getString(R.string.require_permission)
    private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    @Test
    fun allPermissionDenied_shouldShow_requiredFieldsWithGrantButton() {
        // when
        denyPermission()
        denyPermission()

        // then
        val itemList = itemUtil.getPhoneInfoList()
        val noPermissionList = listOf(
            PhoneItem(Items.HANDSET_MAKE, R.string.handset_make, ""),
            PhoneItem(Items.CELL_CONNECTION_STATUS, R.string.cell_connection_status, ""),
            PhoneItem(Items.ITEM_MODEL, R.string.phone_model, "")
        )

        val requirePermissionList = removeList(itemList, noPermissionList)

        noPermissionList.forEach{ phoneItem ->
            val position = itemList.indexOfFirst { it.id == phoneItem.id }
            val title = appContext.getString(phoneItem.title)
            onView(withId(R.id.recycler))
                .check(matches(atPosition(position, hasDescendant(withText(title))),))
                .check(matches(not(
                    allOf(
                    atPosition(position, hasDescendant(withText(requirePermission))),
                    atPosition(position, hasDescendant(withId(R.id.button)))
                ))
                ))
        }

        requirePermissionList.forEach{ phoneItem ->
            val position = itemList.indexOfFirst { it.id == phoneItem.id }
            val title = appContext.getString(phoneItem.title)
            onView(withId(R.id.recycler))
                .perform(scrollToPosition<RecyclerView.ViewHolder>(position))
                .check(matches(allOf(
                    atPosition(position, hasDescendant(withText(title))),
                    atPosition(position, hasDescendant(withText(requirePermission))),
                    atPosition(position, hasDescendant(withId(R.id.button)))
                )))
        }
    }

    @Test
    fun locationPermissionOnly_shouldEnable_LongitudeAndLatitude() {

        // when
        denyPermission()
        allowSpecialPermission()


        // then
        val itemList = itemUtil.getPhoneInfoList()
        val permissionList = listOf(
            PhoneItem(Items.LONGITUDE, R.string.longitude, ""),
            PhoneItem(Items.LATITUDE, R.string.latitude, "")
        )

        permissionList.forEach{ phoneItem ->
            val position = itemList.indexOfFirst { it.id == phoneItem.id }
            val title = appContext.getString(phoneItem.title)
            onView(withId(R.id.recycler))
                .perform(scrollToPosition<RecyclerView.ViewHolder>(position))
                .check(matches(atPosition(position, hasDescendant(withText(title)))))
                .check(matches(not(
                    allOf(
                    atPosition(position, hasDescendant(withText(requirePermission))),
                    atPosition(position, hasDescendant(withId(R.id.button)))
                ))
                ))
        }
    }

    @Test
    fun phoneStatePermissionOnly_shouldEnable_LongitudeAndLatitude() {
        // when
        allowPermission()
        denyPermission()

        // then
        val itemList = itemUtil.getPhoneInfoList()
        val permissionList = listOf(
            PhoneItem(Items.MOBILE_NETWORK_CODE, R.string.mobile_network_code, ""),
            PhoneItem(Items.MOBILE_COUNTRY_CODE, R.string.mobile_country_code, ""),
            PhoneItem(Items.MOBILE_NETWORK_TECHNOLOGY, R.string.mobile_network_technology, ""),
            PhoneItem(Items.OPERATOR_NAME, R.string.operator_name, "")
        )

        permissionList.forEach{ phoneItem ->
            val position = itemList.indexOfFirst { it.id == phoneItem.id }
            val title = appContext.getString(phoneItem.title)
            onView(withId(R.id.recycler))
                .perform(scrollToPosition<RecyclerView.ViewHolder>(position))
                .check(matches(atPosition(position, hasDescendant(withText(title)))))
                .check(matches(not(
                    allOf(
                    atPosition(position, hasDescendant(withText(requirePermission))),
                    atPosition(position, hasDescendant(withId(R.id.button)))
                ))
                ))
        }
    }


    @Test
    fun allPermissionGranted_shouldEnable_allFields() {
        // when
        allowPermission()
        allowSpecialPermission()

        // then
        val itemList = itemUtil.getPhoneInfoList()

        itemList.forEach{ phoneItem ->
            val position = itemList.indexOfFirst { it.id == phoneItem.id }
            val title = appContext.getString(phoneItem.title)
            onView(withId(R.id.recycler))
                .perform(scrollToPosition<RecyclerView.ViewHolder>(position))
                .check(matches(atPosition(position, hasDescendant(withText(title)))))
                .check(matches(not(
                    allOf(
                    atPosition(position, hasDescendant(withText(requirePermission))),
                    atPosition(position, hasDescendant(withId(R.id.button)))
                ))
                ))
        }
    }

    @Test
    fun enableLocationPermission_shouldEnable_longitudeAndLatitude() {
        // when
        denyPermission()
        denyPermission()

        val itemList = itemUtil.getPhoneInfoList()
        val checkList = listOf(
            PhoneItem(Items.LATITUDE, R.string.latitude, ""),
            PhoneItem(Items.LONGITUDE, R.string.longitude, ""),
        )

        val latPos = itemList.indexOfFirst { it.id == Items.LATITUDE }

        onView(withId(R.id.recycler))
            .perform(RecyclerViewActions
                .actionOnItemAtPosition<RecyclerView.ViewHolder>(latPos, clickOnViewChild(R.id.button)))

        onView(withText("OK")).perform(click())
        allowSpecialPermission()

        // then
        checkList.forEach{ phoneItem ->
            val position = itemList.indexOfFirst { it.id == phoneItem.id }
            val title = appContext.getString(phoneItem.title)
            onView(withId(R.id.recycler))
                .perform(scrollToPosition<RecyclerView.ViewHolder>(position))
                .check(matches(atPosition(position, hasDescendant(withText(title)))))
                .check(matches(not(
                    allOf(
                    atPosition(position, hasDescendant(withText(requirePermission))),
                    atPosition(position, hasDescendant(withId(R.id.button)))
                ))
                ))
        }
    }

    @Test
    fun denyDontAskAgain_shouldShow_launchSettingsSnackbar() {

        // when
        denyPermission()
        denyPermission()

        val latPos = itemUtil.getPhoneInfoList().indexOfFirst { it.id == Items.LATITUDE }

        onView(withId(R.id.recycler))
            .perform(RecyclerViewActions
                .actionOnItemAtPosition<RecyclerView.ViewHolder>(latPos, clickOnViewChild(R.id.button)))

        onView(withText("OK")).perform(click())
        denyPermissionDontAskAgain()

        // then
        onView(withText(appContext.getString(R.string.settings))).check(matches(isDisplayed()))
    }

    @Test
    fun enablePhonePermission_shouldEnable_longitudeAndLatitude() {

        // when
        denyPermission()
        denyPermission()

        val itemList = itemUtil.getPhoneInfoList()
        val checkList = listOf(
            PhoneItem(Items.MOBILE_COUNTRY_CODE, R.string.mobile_country_code, ""),
            PhoneItem(Items.MOBILE_NETWORK_CODE, R.string.mobile_network_code, ""),
            PhoneItem(Items.MOBILE_NETWORK_TECHNOLOGY, R.string.mobile_network_technology, ""),
            PhoneItem(Items.OPERATOR_NAME, R.string.operator_name, ""),
        )

        val latPos = itemList.indexOfFirst { it.id == Items.MOBILE_NETWORK_TECHNOLOGY }

        onView(withId(R.id.recycler))
            .perform(RecyclerViewActions
                .actionOnItemAtPosition<RecyclerView.ViewHolder>(latPos, clickOnViewChild(R.id.button)))

        onView(withText("OK")).perform(click())
        allowPermission()

        // then
        checkList.forEach{ phoneItem ->
            val position = itemList.indexOfFirst { it.id == phoneItem.id }
            val title = appContext.getString(phoneItem.title)
            onView(withId(R.id.recycler))
                .perform(scrollToPosition<RecyclerView.ViewHolder>(position))
                .check(matches(atPosition(position, hasDescendant(withText(title)))))
                .check(matches(not(
                    allOf(
                    atPosition(position, hasDescendant(withText(requirePermission))),
                    atPosition(position, hasDescendant(withId(R.id.button)))
                ))
                ))
        }
    }

    @Test
    fun clickingMenuSetting_shouldNavigateTo_settingsScreen() {
        denyPermission()
        denyPermission()
        openActionBarOverflowOrOptionsMenu(appContext)
        onView(
            withText(appContext.getString(R.string.settings))).perform(click())

        onView(withText(appContext.getString(R.string.select_theme))).check(matches(isDisplayed()))
    }

    private fun removeList(universalList: List<PhoneItem>, subList: List<PhoneItem>) : List<PhoneItem> {
        val workingList = universalList.toMutableList()

        subList.forEach{ item ->
            workingList.removeIf{ it.id == item.id }
        }

        return workingList
    }

    @Throws(UiObjectNotFoundException::class)
    fun allowPermission() {
        if(Build.VERSION.SDK_INT >= 23) {
            device.findObject(UiSelector().text("Allow")).click()
        }
    }

    @Throws(UiObjectNotFoundException::class)
    fun allowSpecialPermission() {
        if(Build.VERSION.SDK_INT >= 23) {
            device.findObject(UiSelector().text("Allow only while using the app")).click()
        }
    }

    @Throws(UiObjectNotFoundException::class)
    fun denyPermission() {
        if(Build.VERSION.SDK_INT >= 23) {
            device.findObject(UiSelector().text("Deny")).click()
        }
    }

    @Throws(UiObjectNotFoundException::class)
    fun denyPermissionDontAskAgain() {
        if(Build.VERSION.SDK_INT >= 23) {
            device.findObject(UiSelector().textContains("ask again")).click()
        }
    }
}

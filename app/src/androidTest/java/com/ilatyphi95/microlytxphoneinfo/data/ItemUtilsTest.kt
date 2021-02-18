package com.ilatyphi95.microlytxphoneinfo.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ilatyphi95.microlytxphoneinfo.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.IllegalArgumentException

@RunWith(AndroidJUnit4::class)
class ItemUtilsTest {
    private val appContext = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var itemUtil: ItemUtils

    @Before
    fun setUp() {
        itemUtil = ItemUtils(appContext)
    }

    @Test
    fun convertIntPairToStringPair_shouldReturn_sameSizeAsInput() {
        // given
        val inList = listOf(
            Pair(Items.LOCAL_AREA_CODE, 101),
            Pair(Items.MOBILE_COUNTRY_CODE, 201),
            Pair(Items.MOBILE_NETWORK_CODE, 301)
            )

        // when
        val outList = itemUtil.convertIntPairToStringPair(inList)

        // then
        assertEquals("Correct size is returned", inList.size, outList.size)
    }

    @Test
    fun convertIntPairToStringPair_shouldReturn_correctValue() {
        // given
        val inList = listOf(
            Pair(Items.LOCAL_AREA_CODE, 101),
            Pair(Items.MOBILE_COUNTRY_CODE, 201),
            Pair(Items.MOBILE_NETWORK_CODE, 301)
            )

        // when
        val outList = itemUtil.convertIntPairToStringPair(inList)

        // then
        outList.forEachIndexed { index, item ->
            assertEquals(item.second, inList[index].second.toString())
        }
    }

    @Test
    fun convertIntPairToStringPair_shouldReturn_noAvailable() {
        // given
        val inList = listOf(
            Pair(Items.LOCAL_AREA_CODE, NOT_AVAILABLE)
            )

        // when
        val outList = itemUtil.convertIntPairToStringPair(inList)

        // then
        assertEquals(appContext.getString(R.string.not_available), outList[0].second)
    }

    @Test
    fun convertIntPairToStringPair_shouldReturn_requirePermission() {
        // given
        val inList = listOf(
            Pair(Items.LOCAL_AREA_CODE, REQUIRE_PERMISSION)
            )

        // when
        val outList = itemUtil.convertIntPairToStringPair(inList)

        // then
        assertEquals(appContext.getString(R.string.require_permission), outList[0].second)
    }

    @Test
    fun getPhoneInfoList_shouldNotContain_repetition() {
        // when
        val myList = itemUtil.getPhoneInfoList()

        // then
        assertEquals("Should not contain repetition", myList.size, myList.toSet().size)
    }

    @Test
    fun getPhoneInfoList_shouldContain_allItems() {
        // when
        val myList = itemUtil.getPhoneInfoList()

        // then
        Items.values().forEach { item ->
            assertTrue("List contains $item", (myList.find { it.id == item } != null))
        }
    }

    @Test
    fun getPhoneInfoList_shouldHave_notAvailableAsDefault() {
        // when
        val myList = itemUtil.getPhoneInfoList()

        // then
        myList.forEach { item ->
            assertTrue(
                "$item should have default value",
                (item.value == appContext.getString(R.string.not_available))
            )
        }
    }

    @Test
    fun convertItemsToPhoneItems_shouldReturn_sizeOfInput() {
        // given
        val inList = listOf(
            Pair(Items.MOBILE_NETWORK_CODE, "13"),
            Pair(Items.MOBILE_COUNTRY_CODE, "302"),
            Pair(Items.OPERATOR_NAME, "airtel")
        )

        // when
        val outList = itemUtil.convertItemsToPhoneItems(inList)

        // then
        assertEquals(
            "Output should have same size as input",
            inList.size, outList.size
        )
    }

    @Test
    fun convertItemsToPhoneItems_shouldReturn_haveItemsInOrder() {
        // given
        val inList = listOf(
            Pair(Items.MOBILE_NETWORK_CODE, "13"),
            Pair(Items.MOBILE_COUNTRY_CODE, "302"),
            Pair(Items.OPERATOR_NAME, "airtel")
        )

        // when
        val outList = itemUtil.convertItemsToPhoneItems(inList)

        // then
        outList.forEachIndexed { index, phoneItem ->
            assertEquals(
                "Converter should follow the order of the input",
                inList[index].first, phoneItem.id
            )
        }
    }

    @Test
    fun replaceItems_shouldReturn_modifiedItemAtCorrectPosition() {
        // given
        val inList = listOf(
            PhoneItem(Items.OPERATOR_NAME, R.string.operator_name, "airtel"),
            PhoneItem(Items.MOBILE_COUNTRY_CODE, R.string.mobile_country_code, "130"),
            PhoneItem(Items.MOBILE_NETWORK_CODE, R.string.mobile_network_code, "70")
        )

        // when
        val position = 1
        val newCountryCode = inList[position].copy(value = "260")
        val outList = itemUtil.replaceItems(inList, newCountryCode)

        // then
        assertEquals("Replace Items should be in correct position",
            newCountryCode, outList[position])
    }

    @Test
    fun replaceItems_shouldReturn_inputListSize() {
        // given
        val inList = listOf(
            PhoneItem(Items.OPERATOR_NAME, R.string.operator_name, "airtel"),
            PhoneItem(Items.MOBILE_COUNTRY_CODE, R.string.mobile_country_code, "130"),
            PhoneItem(Items.MOBILE_NETWORK_CODE, R.string.mobile_network_code, "70")
        )

        // when
        val position = 1
        val newCountryCode = inList[position].copy(value = "260")
        val outList = itemUtil.replaceItems(inList, newCountryCode)

        // then
        assertEquals("Replace should not alter list size",
            inList.size, outList.size)
    }

    @Test(expected = IllegalArgumentException::class)
    fun replaceItems_shouldThrowException_whenItemIsNotInList() {
        // given
        val inList = listOf(
            PhoneItem(Items.OPERATOR_NAME, R.string.operator_name, "airtel"),
            PhoneItem(Items.MOBILE_COUNTRY_CODE, R.string.mobile_country_code, "130"),
            PhoneItem(Items.MOBILE_NETWORK_CODE, R.string.mobile_network_code, "70")
        )

        // when
        val newCountryCode =  PhoneItem(Items.SIGNAL_STRENGTH, R.string.signal_strength, "3")
        itemUtil.replaceItems(inList, newCountryCode)
    }
}
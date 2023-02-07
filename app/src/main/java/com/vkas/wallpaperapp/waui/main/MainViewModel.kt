package com.vkas.wallpaperapp.waui.main

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.vkas.wallpaperapp.wabase.BaseViewModel
import com.vkas.wallpaperapp.wabean.WallpaperBean
import com.xuexiang.xui.utils.Utils.isNullOrEmpty

class MainViewModel(application: Application) : BaseViewModel(application) {
    private lateinit var waWallpaperBean: WallpaperBean
    private lateinit var waWallpaperBeanList: MutableList<WallpaperBean>

    // 服务器列表数据
    val liveWallpaperListData: MutableLiveData<MutableList<WallpaperBean>> by lazy {
        MutableLiveData<MutableList<WallpaperBean>>()
    }

    /**
     * 获取服务器列表
     */
    fun getServerListData() {
        waWallpaperBeanList = ArrayList()
        getWallpaperList().forEachIndexed { index, it ->
            waWallpaperBean = WallpaperBean()
            waWallpaperBean.wallpaperPath = getWallpaperList()[index]
            waWallpaperBeanList.add(waWallpaperBean)
        }
        liveWallpaperListData.postValue(waWallpaperBeanList)
    }
    private fun getWallpaperList(): MutableList<String> {
        return listOf(
            "wa_1",
            "wa_2",
            "wa_3",
            "wa_4",
            "wa_5",
            "wa_6",
            "wa_7",
            "wa_8",
            "wa_9",
            "wa_10",
            "wa_11",
            "wa_12",
            "wa_13",
            "wa_14",
            "wa_15",
            "wa_16",
            "wa_17",
            "wa_18",
            "wa_19",
            "wa_20",
            "wa_21",
            "wa_22",
            "wa_23",
            "wa_24",
            "wa_25",
            "wa_26",
            "wa_27",
            "wa_28",
            "wa_29",
            "wa_30",
            "wa_31",
            "wa_32",
            "wa_33",
            "wa_34",
            "wa_35",
            "wa_36",
            "wa_37",
            "wa_38",
            "wa_39",
            "wa_40",
            "wa_41",
            "wa_42",
            "wa_43",
            "wa_44",
            "wa_45",
            "wa_46",
            "wa_47",
            "wa_48",
            "wa_49",
            "wa_50",
            "wa_51",
            "wa_52",
            "wa_53",
            "wa_54",
            "wa_55",
            "wa_56",
            "wa_57",
            "wa_58",
            "wa_59"
        ) as MutableList<String>
    }
}
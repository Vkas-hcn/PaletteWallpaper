package com.vkas.wallpaperapp.wabean

import androidx.annotation.Keep

@Keep
data class WaAdBean(
    var wa_open: MutableList<WaDetailBean> = ArrayList(),
    var wa_back: MutableList<WaDetailBean> = ArrayList(),
    var wa_list: MutableList<WaDetailBean> = ArrayList(),
    var wa_result: MutableList<WaDetailBean> = ArrayList(),
    var wa_click_num: Int = 0,
    var wa_show_num: Int = 0
)

@Keep
data class WaDetailBean(
    val wa_id: String,
    val wa_platform: String,
    val wa_type: String,
    val wa_weight: Int
)
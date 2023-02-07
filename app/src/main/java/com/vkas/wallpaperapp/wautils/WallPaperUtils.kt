package com.vkas.wallpaperapp.wautils

import com.google.gson.reflect.TypeToken
import com.vkas.wallpaperapp.waapp.App.Companion.mmkvWa
import com.vkas.wallpaperapp.wabean.WaAdBean
import com.vkas.wallpaperapp.wabean.WaDetailBean
import com.vkas.wallpaperapp.waevent.Constant
import com.xuexiang.xui.utils.Utils
import com.xuexiang.xutil.net.JsonUtil
import com.xuexiang.xutil.resource.ResourceUtils
import android.content.ContentResolver
import android.content.res.Resources
import android.net.Uri
import com.xuexiang.xutil.resource.ResUtils.getResources


object WallPaperUtils {

    /**
     * 广告排序
     */
    private fun adSortingWa(elAdBean: WaAdBean): WaAdBean {
        val adBean: WaAdBean = WaAdBean()
        val elOpen = elAdBean.wa_open.sortedWith(compareByDescending { it.wa_weight })
        val elBack = elAdBean.wa_back.sortedWith(compareByDescending { it.wa_weight })

        val ufVpn = elAdBean.wa_list.sortedWith(compareByDescending { it.wa_weight })
        val elResult = elAdBean.wa_result.sortedWith(compareByDescending { it.wa_weight })


        adBean.wa_open = elOpen.toMutableList()
        adBean.wa_back = elBack.toMutableList()

        adBean.wa_list = ufVpn.toMutableList()
        adBean.wa_result = elResult.toMutableList()
        adBean.wa_show_num = elAdBean.wa_show_num
        adBean.wa_click_num = elAdBean.wa_click_num
        return adBean
    }

    /**
     * 取出排序后的广告ID
     */
    fun takeSortedAdIDWa(index: Int, elAdDetails: MutableList<WaDetailBean>): String {
        return elAdDetails.getOrNull(index)?.wa_id ?: ""
    }

    /**
     * 获取广告服务器数据
     */
    fun getAdServerDataWa(): WaAdBean {
        val serviceData: WaAdBean =
            if (Utils.isNullOrEmpty(mmkvWa.decodeString(Constant.ADVERTISING_WA_DATA))) {
                JsonUtil.fromJson(
                    ResourceUtils.readStringFromAssert(Constant.AD_LOCAL_FILE_NAME_WA),
                    object : TypeToken<
                            WaAdBean?>() {}.type
                )
            } else {
                JsonUtil.fromJson(
                    mmkvWa.decodeString(Constant.ADVERTISING_WA_DATA),
                    object : TypeToken<WaAdBean?>() {}.type
                )
            }
        return adSortingWa(serviceData)
    }

    /**
     * 是否达到阀值
     */
    fun isThresholdReached(): Boolean {
        val clicksCount = mmkvWa.decodeInt(Constant.CLICKS_WA_COUNT, 0)
        val showCount = mmkvWa.decodeInt(Constant.SHOW_WA_COUNT, 0)
        KLog.e("TAG", "clicksCount=${clicksCount}, showCount=${showCount}")
        KLog.e(
            "TAG",
            "wa_click_num=${getAdServerDataWa().wa_click_num}, getAdServerData().wa_show_num=${getAdServerDataWa().wa_show_num}"
        )
        if (clicksCount >= getAdServerDataWa().wa_click_num || showCount >= getAdServerDataWa().wa_show_num) {
            return true
        }
        return false
    }

    /**
     * 记录广告展示次数
     */
    fun recordNumberOfAdDisplaysWa() {
        var showCount = mmkvWa.decodeInt(Constant.SHOW_WA_COUNT, 0)
        showCount++
        MmkvUtils.set(Constant.SHOW_WA_COUNT, showCount)
    }

    /**
     * 记录广告点击次数
     */
    fun recordNumberOfAdClickWa() {
        var clicksCount = mmkvWa.decodeInt(Constant.CLICKS_WA_COUNT, 0)
        clicksCount++
        MmkvUtils.set(Constant.CLICKS_WA_COUNT, clicksCount)
    }

    /**
     * res/drawable(mipmap)/xxx.png::::uri－－－－>url
     *
     * @return
     */
    public fun imageTranslateUri(resId: Int): String? {
        val r: Resources = getResources()
        val uri: Uri = Uri.parse(
            ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                    + r.getResourcePackageName(resId) + "/"
                    + r.getResourceTypeName(resId) + "/"
                    + r.getResourceEntryName(resId)
        )
        return uri.toString()
    }


}
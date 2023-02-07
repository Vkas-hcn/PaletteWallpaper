package com.vkas.wallpaperapp.waui.main

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseDelegateMultiAdapter
import com.chad.library.adapter.base.delegate.BaseMultiTypeDelegate
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.vkas.wallpaperapp.R
import com.vkas.wallpaperapp.wabean.WallpaperBean
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import com.vkas.wallpaperapp.waad.WaLoadListAd
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive


class WallpaperListAdapter(activity: AppCompatActivity, data: MutableList<WallpaperBean>?) :
    BaseDelegateMultiAdapter<WallpaperBean?, BaseViewHolder>() {
    init {
        setMultiTypeDelegate(object : BaseMultiTypeDelegate<WallpaperBean?>() {
            override fun getItemType(data: List<WallpaperBean?>, position: Int): Int {
                // 根据数据，自己判断应该返回的类型
                return if (data[position]?.isAd == true) {
                    1
                } else {
                    0
                }
            }
        })
        // 第二部，绑定 item 类型
        getMultiTypeDelegate()
            ?.addItemType(0, R.layout.item_wallpaper)
            ?.addItemType(1, R.layout.item_wallpaper_ad)
    }

    private var jobBackWa: Job? = null
    var activityList = activity

    @SuppressLint("ResourceType")
    override fun convert(holder: BaseViewHolder, item: WallpaperBean?) {
        if (holder.itemViewType == 0) {
            val goodsIcon: Int = activityList.resources
                .getIdentifier(item?.wallpaperPath, "mipmap", activityList.packageName)
            Glide
                .with(context)
                .load(goodsIcon)
                .thumbnail(0.1f)
                .into(holder.getView( R.id.img_wallpaper) as ImageView)
        } else {
            val adView = holder.getView<FrameLayout>(R.id.wa_item_ad)
            val adViewImg = holder.getView<ImageView>(R.id.img_wa_item_ad)
            WaLoadListAd.getInstance().advertisementLoadingWa(activityList)
            initBackAds(adView, adViewImg)
        }
    }

    private fun initBackAds(adView: ViewGroup, adViewImg: ImageView) {
        jobBackWa = activityList.lifecycleScope.launch {
            while (isActive) {
                WaLoadListAd.getInstance().setDisplayBackNativeAdWa(activityList, adView, adViewImg)
                if (WaLoadListAd.getInstance().whetherToShowWa) {
                    jobBackWa?.cancel()
                    jobBackWa = null
                }
                delay(1000L)
            }
        }
    }
}
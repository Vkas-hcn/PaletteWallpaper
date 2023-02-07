package com.vkas.wallpaperapp.waad

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.vkas.wallpaperapp.waapp.App
import com.vkas.wallpaperapp.wabean.WaAdBean
import com.vkas.wallpaperapp.waevent.Constant.logTagWa
import com.vkas.wallpaperapp.wautils.KLog
import com.vkas.wallpaperapp.wautils.WallPaperUtils
import com.vkas.wallpaperapp.wautils.WallPaperUtils.getAdServerDataWa
import com.vkas.wallpaperapp.wautils.WallPaperUtils.recordNumberOfAdClickWa
import com.vkas.wallpaperapp.wautils.WallPaperUtils.recordNumberOfAdDisplaysWa
import com.vkas.wallpaperapp.wautils.WallPaperUtils.takeSortedAdIDWa
import java.util.*
import com.vkas.wallpaperapp.R
import com.vkas.wallpaperapp.wautils.RoundCornerOutlineProvider

class WaLoadListAd {
    companion object {
        fun getInstance() = InstanceHelper.openLoadWa
    }

    object InstanceHelper {
        val openLoadWa = WaLoadListAd()
    }
    var appAdDataWa: NativeAd? = null

    // 是否正在加载中
    private var isLoadingWa = false

    //加载时间
    private var loadTimeWa: Long = Date().time

    // 是否展示
    var whetherToShowWa = false

    // openIndex
    var adIndexWa = 0


    /**
     * 广告加载前判断
     */
    fun advertisementLoadingWa(context: Context) {
        App.isAppOpenSameDayWa()
        if (WallPaperUtils.isThresholdReached()) {
            KLog.d(logTagWa, "广告达到上线")
            return
        }
        KLog.d(logTagWa, "list--isLoading=${isLoadingWa}")

        if (isLoadingWa) {
            KLog.d(logTagWa, "广告加载中，不能再次加载")
            return
        }

        if(appAdDataWa == null){
            isLoadingWa = true
            loadBackAdvertisementWa(context,getAdServerDataWa())
        }
        if (appAdDataWa != null && !whetherAdExceedsOneHour(loadTimeWa)) {
            isLoadingWa = true
            appAdDataWa =null
            loadBackAdvertisementWa(context,getAdServerDataWa())
        }
    }

    /**
     * 广告是否超过过期（false:过期；true：未过期）
     */
    private fun whetherAdExceedsOneHour(loadTime: Long): Boolean {
        val dateDifference: Long = Date().time - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour
    }

    /**
     * 加载list原生广告
     */
    private fun loadBackAdvertisementWa(context: Context,adData: WaAdBean) {
        val id = takeSortedAdIDWa(adIndexWa, adData.wa_list)
        KLog.d(logTagWa, "list---原生广告id=$id;权重=${adData.wa_list.getOrNull(adIndexWa)?.wa_weight}")

        val homeNativeAds = AdLoader.Builder(
            context.applicationContext,
            id
        )
        val videoOptions = VideoOptions.Builder()
            .setStartMuted(true)
            .build()

        val adOptions = NativeAdOptions.Builder()
            .setVideoOptions(videoOptions)
            .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
            .setMediaAspectRatio(NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_PORTRAIT)
            .build()

        homeNativeAds.withNativeAdOptions(adOptions)
        homeNativeAds.forNativeAd {
            appAdDataWa = it
        }
        homeNativeAds.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                val error =
                    """
           domain: ${loadAdError.domain}, code: ${loadAdError.code}, message: ${loadAdError.message}
          """"
                isLoadingWa = false
                appAdDataWa = null
                KLog.d(logTagWa, "list---加载list原生加载失败: $error")

                if (adIndexWa < adData.wa_list.size - 1) {
                    adIndexWa++
                    loadBackAdvertisementWa(context,adData)
                }else{
                    adIndexWa = 0
                }
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                KLog.d(logTagWa, "list---加载list原生广告成功")
                loadTimeWa = Date().time
                isLoadingWa = false
                adIndexWa = 0
            }

            override fun onAdOpened() {
                super.onAdOpened()
                KLog.d(logTagWa, "list---点击list原生广告")
                recordNumberOfAdClickWa()
            }
        }).build().loadAd(AdRequest.Builder().build())
    }

    /**
     * 设置展示list原生广告
     */
    fun setDisplayBackNativeAdWa(activity: AppCompatActivity, view: ViewGroup, img: ImageView) {
        activity.runOnUiThread {
            appAdDataWa.let {
                if (it != null && !whetherToShowWa) {
                    val activityDestroyed: Boolean = activity.isDestroyed
                    if (activityDestroyed || activity.isFinishing || activity.isChangingConfigurations) {
                        it.destroy()
                        return@let
                    }
                    val adView = activity.layoutInflater
                        .inflate(R.layout.layout_list_native_wa, null) as NativeAdView
                    // 对应原生组件
                    setCorrespondingNativeComponentWa(it, adView)
                    view.removeAllViews()
                    view.addView(adView)
                    view.visibility = View.VISIBLE
                    img.visibility =View.GONE
                    recordNumberOfAdDisplaysWa()
                    whetherToShowWa = true
                    App.nativeAdRefreshWa = false
                    appAdDataWa = null
                    KLog.d(logTagWa, "list--原生广告--展示")
                    //重新缓存
                    advertisementLoadingWa(activity)
                }
            }
        }
    }

    private fun setCorrespondingNativeComponentWa(nativeAd: NativeAd, adView: NativeAdView) {
        adView.mediaView = adView.findViewById(R.id.ad_media)
        // Set other ad assets.
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        (adView.headlineView as TextView).text = nativeAd.headline
        adView.bodyView = adView.findViewById(R.id.ad_body)
        (adView.headlineView as TextView).text = nativeAd.headline

        nativeAd.mediaContent?.let {
            adView.mediaView?.apply { setImageScaleType(ImageView.ScaleType.CENTER_CROP) }
                ?.setMediaContent(it)
        }
        adView.mediaView.clipToOutline=true
        adView.mediaView.outlineProvider= RoundCornerOutlineProvider(6f)
        if (nativeAd.body == null) {
            adView.bodyView?.visibility = View.INVISIBLE
        } else {
            adView.bodyView?.visibility = View.VISIBLE
            (adView.bodyView as TextView).text = nativeAd.body
        }
        if (nativeAd.callToAction == null) {
            adView.callToActionView?.visibility = View.INVISIBLE
        } else {
            adView.callToActionView?.visibility = View.VISIBLE
            (adView.callToActionView as TextView).text = nativeAd.callToAction
        }
        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        adView.setNativeAd(nativeAd)
    }
}
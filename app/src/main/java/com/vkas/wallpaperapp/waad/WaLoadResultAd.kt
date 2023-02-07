package com.vkas.wallpaperapp.waad

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.vkas.wallpaperapp.databinding.ActivityResultBinding
import com.vkas.wallpaperapp.waapp.App
import com.vkas.wallpaperapp.wabean.WaAdBean
import com.vkas.wallpaperapp.waevent.Constant.logTagWa
import com.vkas.wallpaperapp.wautils.KLog
import com.vkas.wallpaperapp.wautils.WallPaperUtils
import com.vkas.wallpaperapp.wautils.WallPaperUtils.getAdServerDataWa
import com.vkas.wallpaperapp.wautils.WallPaperUtils.recordNumberOfAdClickWa
import com.vkas.wallpaperapp.wautils.WallPaperUtils.takeSortedAdIDWa
import java.util.*
import com.vkas.wallpaperapp.R
import com.vkas.wallpaperapp.wautils.WallPaperUtils.recordNumberOfAdDisplaysWa

class WaLoadResultAd {
    companion object {
        fun getInstance() = InstanceHelper.openLoadWa
    }

    object InstanceHelper {
        val openLoadWa = WaLoadResultAd()
    }

    var appAdDataWa: NativeAd? = null

    // 是否正在加载中
    var isLoadingWa = false

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
        KLog.d(logTagWa, "result--isLoading=${isLoadingWa}")

        if (isLoadingWa) {
            KLog.d(logTagWa, "result--广告加载中，不能再次加载")
            return
        }
        if (appAdDataWa == null) {
            isLoadingWa = true
            loadResultAdvertisementWa(context, getAdServerDataWa())
        }
        if (appAdDataWa != null && !whetherAdExceedsOneHour(loadTimeWa)) {
            isLoadingWa = true
            appAdDataWa = null
            loadResultAdvertisementWa(context, getAdServerDataWa())
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
     * 加载result原生广告
     */
    private fun loadResultAdvertisementWa(context: Context, adData: WaAdBean) {
        val id = takeSortedAdIDWa(adIndexWa, adData.wa_result)
        KLog.d(
            logTagWa,
            "result---原生广告id=$id;权重=${adData.wa_result.getOrNull(adIndexWa)?.wa_weight}"
        )

        val homeNativeAds = AdLoader.Builder(
            context.applicationContext,
            id
        )
        val videoOptions = VideoOptions.Builder()
            .setStartMuted(true)
            .build()

        val adOelions = NativeAdOptions.Builder()
            .setVideoOptions(videoOptions)
            .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_LEFT)
            .setMediaAspectRatio(NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_PORTRAIT)
            .build()

        homeNativeAds.withNativeAdOptions(adOelions)
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
                KLog.d(logTagWa, "result---加载result原生加载失败: $error")

                if (adIndexWa < adData.wa_result.size - 1) {
                    adIndexWa++
                    loadResultAdvertisementWa(context, adData)
                } else {
                    adIndexWa = 0
                }
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                KLog.d(logTagWa, "result---加载result原生广告成功")
                loadTimeWa = Date().time
                isLoadingWa = false
                adIndexWa = 0
            }

            override fun onAdOpened() {
                super.onAdOpened()
                KLog.d(logTagWa, "result---点击result原生广告")
                recordNumberOfAdClickWa()
            }
        }).build().loadAd(AdRequest.Builder().build())
    }

    /**
     * 设置展示home原生广告
     */
    fun setDisplayResultNativeAd(activity: AppCompatActivity, binding: ActivityResultBinding) {
        activity.runOnUiThread {
            appAdDataWa.let {
                if (it != null && !whetherToShowWa && activity.lifecycle.currentState == Lifecycle.State.RESUMED) {
                    val activityDestroyed: Boolean = activity.isDestroyed
                    if (activityDestroyed || activity.isFinishing || activity.isChangingConfigurations) {
                        it.destroy()
                        return@let
                    }
                    val adView = activity.layoutInflater
                        .inflate(R.layout.layout_result_native_wa, null) as NativeAdView
                    // 对应原生组件
                    setResultNativeComponent(it, adView)
                    binding.waAdFrame.removeAllViews()
                    binding.waAdFrame.addView(adView)
                    binding.resultAdWa = true
                    recordNumberOfAdDisplaysWa()
                    whetherToShowWa = true
                    App.nativeAdRefreshWa = false
                    appAdDataWa = null
                    KLog.d(logTagWa, "result--原生广告--展示")
                    //重新缓存
                    advertisementLoadingWa(activity)
                }
            }

        }
    }

    private fun setResultNativeComponent(nativeAd: NativeAd, adView: NativeAdView) {
        adView.mediaView = adView.findViewById(R.id.ad_media)
        // Set other ad assets.
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_app_icon)
        (adView.headlineView as TextView).text = nativeAd.headline
        nativeAd.mediaContent?.let {
            adView.mediaView?.apply { setImageScaleType(ImageView.ScaleType.CENTER_CROP) }
                ?.setMediaContent(it)
        }
        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
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

        if (nativeAd.icon == null) {
            adView.iconView?.visibility = View.GONE
        } else {
            (adView.iconView as ImageView).setImageDrawable(
                nativeAd.icon?.drawable
            )
            adView.iconView?.visibility = View.VISIBLE
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        adView.setNativeAd(nativeAd)
    }
}
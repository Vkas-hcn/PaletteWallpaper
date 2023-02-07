package com.vkas.wallpaperapp.waad

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.jeremyliao.liveeventbus.LiveEventBus
import com.vkas.wallpaperapp.waapp.App
import com.vkas.wallpaperapp.wabean.WaAdBean
import com.vkas.wallpaperapp.waevent.Constant
import com.vkas.wallpaperapp.waevent.Constant.logTagWa
import com.vkas.wallpaperapp.wautils.KLog
import com.vkas.wallpaperapp.wautils.WallPaperUtils
import com.vkas.wallpaperapp.wautils.WallPaperUtils.getAdServerDataWa
import com.vkas.wallpaperapp.wautils.WallPaperUtils.recordNumberOfAdClickWa
import com.vkas.wallpaperapp.wautils.WallPaperUtils.recordNumberOfAdDisplaysWa
import com.vkas.wallpaperapp.wautils.WallPaperUtils.takeSortedAdIDWa
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
class WaLoadBackAd {
    companion object {
        fun getInstance() = InstanceHelper.backLoadWa
    }

    object InstanceHelper {
        val backLoadWa = WaLoadBackAd()
    }
    var appAdDataWa: InterstitialAd? = null

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
        KLog.d(logTagWa, "back--isLoading=${isLoadingWa}")

        if (isLoadingWa) {
            KLog.d(logTagWa, "back--广告加载中，不能再次加载")
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
     * 加载首页插屏广告
     */
    private fun loadBackAdvertisementWa(context: Context, adData: WaAdBean) {
        val adRequest = AdRequest.Builder().build()
        val id = takeSortedAdIDWa(adIndexWa, adData.wa_back)
        KLog.d(logTagWa, "back--插屏广告id=$id;权重=${adData.wa_back.getOrNull(adIndexWa)?.wa_weight}")

        InterstitialAd.load(
            context,
            id,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    adError.toString().let {
                        KLog.d(logTagWa, "back---连接插屏加载失败=$it") }
                    isLoadingWa = false
                    appAdDataWa = null
                    if (adIndexWa < adData.wa_back.size - 1) {
                        adIndexWa++
                        loadBackAdvertisementWa(context,adData)
                    }else{
                        adIndexWa = 0
                    }
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    loadTimeWa = Date().time
                    isLoadingWa = false
                    appAdDataWa = interstitialAd
                    adIndexWa = 0
                    KLog.d(logTagWa, "back---返回插屏加载成功")
                }
            })
    }

    /**
     * back插屏广告回调
     */
    private fun backScreenAdCallback() {
        appAdDataWa?.fullScreenContentCallback =
            object : FullScreenContentCallback() {
                override fun onAdClicked() {
                    // Called when a click is recorded for an ad.
                    KLog.d(logTagWa, "back插屏广告点击")
                    recordNumberOfAdClickWa()
                }

                override fun onAdDismissedFullScreenContent() {
                    // Called when ad is dismissed.
                    KLog.d(logTagWa, "关闭back插屏广告${App.isBackDataWa}")
                    LiveEventBus.get<Boolean>(Constant.PLUG_WA_BACK_AD_SHOW)
                        .post(App.isBackDataWa)
                    appAdDataWa = null
                    whetherToShowWa = false
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    // Called when ad fails to show.
                    KLog.d(logTagWa, "Ad failed to show fullscreen content.")
                    appAdDataWa = null
                    whetherToShowWa = false
                }

                override fun onAdImpression() {
                    // Called when an impression is recorded for an ad.
                    KLog.e("TAG", "Ad recorded an impression.")
                }

                override fun onAdShowedFullScreenContent() {
                    appAdDataWa = null
                    recordNumberOfAdDisplaysWa()
                    // Called when ad is shown.
                    whetherToShowWa = true
                    KLog.d(logTagWa, "back----show")
                }
            }
    }

    /**
     * 展示Connect广告
     */
    fun displayBackAdvertisementWa(activity: AppCompatActivity): Boolean {
        if (appAdDataWa == null) {
            KLog.d(logTagWa, "back--插屏广告加载中。。。")
            return false
        }
        if (whetherToShowWa || activity.lifecycle.currentState != Lifecycle.State.RESUMED) {
            KLog.d(logTagWa, "back--前一个插屏广告展示中或者生命周期不对")
            return false
        }
        backScreenAdCallback()
        activity.lifecycleScope.launch(Dispatchers.Main) {
            (appAdDataWa as InterstitialAd).show(activity)
        }
        return true
    }
}
package com.vkas.wallpaperapp.waad

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
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
import com.xuexiang.xutil.net.JsonUtil
import java.util.*
class WaLoadOpenAd {
    companion object {
        fun getInstance() = InstanceHelper.openLoadWa
    }

    object InstanceHelper {
        val openLoadWa = WaLoadOpenAd()
    }

    var appAdDataWa: Any? = null

    // 是否正在加载中
    var isLoadingWa = false

    //加载时间
    private var loadTimeWa: Long = Date().time

    // 是否展示
    var whetherToShowWa = false

    // openIndex
    var adIndexWa = 0
    // 是否是第一遍轮训
    private var isFirstRotation:Boolean=false
    /**
     * 广告加载前判断
     */
    fun advertisementLoadingWa(context: Context) {
        App.isAppOpenSameDayWa()
        if (WallPaperUtils.isThresholdReached()) {
            KLog.d(logTagWa, "广告达到上线")
            return
        }
        KLog.d(logTagWa, "open--isLoading=${isLoadingWa}")

        if (isLoadingWa) {
            KLog.d(logTagWa, "open--广告加载中，不能再次加载")
            return
        }
        isFirstRotation =false
        if (appAdDataWa == null) {
            isLoadingWa = true
            loadStartupPageAdvertisementWa(context, getAdServerDataWa())
        }
        if (appAdDataWa != null && !whetherAdExceedsOneHour(loadTimeWa)) {
            isLoadingWa = true
            appAdDataWa = null
            loadStartupPageAdvertisementWa(context, getAdServerDataWa())
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
     * 加载启动页广告
     */
    private fun loadStartupPageAdvertisementWa(context: Context, adData: WaAdBean) {
        if (adData.wa_open.getOrNull(adIndexWa)?.wa_type == "screen") {
            loadStartInsertAdWa(context, adData)
        } else {
            loadOpenAdvertisementWa(context, adData)
        }
    }

    /**
     * 加载开屏广告
     */
    private fun loadOpenAdvertisementWa(context: Context, adData: WaAdBean) {
        KLog.e("loadOpenAdvertisementWa", "adData().wa_open=${JsonUtil.toJson(adData.wa_open)}")
        KLog.e(
            "loadOpenAdvertisementWa",
            "id=${JsonUtil.toJson(takeSortedAdIDWa(adIndexWa, adData.wa_open))}"
        )

        val id = takeSortedAdIDWa(adIndexWa, adData.wa_open)

        KLog.d(logTagWa, "open--开屏广告id=$id;权重=${adData.wa_open.getOrNull(adIndexWa)?.wa_weight}")
        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            context,
            id,
            request,
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    loadTimeWa = Date().time
                    isLoadingWa = false
                    appAdDataWa = ad

                    KLog.d(logTagWa, "open--开屏广告加载成功")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isLoadingWa = false
                    appAdDataWa = null
                    if (adIndexWa < adData.wa_open.size - 1) {
                        adIndexWa++
                        loadStartupPageAdvertisementWa(context, adData)
                    } else {
                        adIndexWa = 0
                        if(!isFirstRotation){
                            advertisementLoadingWa(context)
                            isFirstRotation =true
                        }
                    }
                    KLog.d(logTagWa, "open--开屏广告加载失败: " + loadAdError.message)
                }
            }
        )
    }


    /**
     * 开屏广告回调
     */
    private fun advertisingOpenCallbackWa() {
        if (appAdDataWa !is AppOpenAd) {
            return
        }
        (appAdDataWa as AppOpenAd).fullScreenContentCallback =
            object : FullScreenContentCallback() {
                //取消全屏内容
                override fun onAdDismissedFullScreenContent() {
                    KLog.d(logTagWa, "open--关闭开屏内容")
                    whetherToShowWa = false
                    appAdDataWa = null
                    if (!App.whetherBackgroundWa) {
                        LiveEventBus.get<Boolean>(Constant.OPEN_CLOSE_JUMP)
                            .post(true)
                    }
                }

                //全屏内容无法显示时调用
                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    whetherToShowWa = false
                    appAdDataWa = null
                    KLog.d(logTagWa, "open--全屏内容无法显示时调用")
                }

                //显示全屏内容时调用
                override fun onAdShowedFullScreenContent() {
                    appAdDataWa = null
                    whetherToShowWa = true
                    recordNumberOfAdDisplaysWa()
                    adIndexWa = 0
                    KLog.d(logTagWa, "open---开屏广告展示")
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                    KLog.d(logTagWa, "open---点击open广告")
                    recordNumberOfAdClickWa()
                }
            }
    }

    /**
     * 展示Open广告
     */
    fun displayOpenAdvertisementWa(activity: AppCompatActivity): Boolean {

        if (appAdDataWa == null) {
            KLog.d(logTagWa, "open---开屏广告加载中。。。")
            return false
        }
        if (whetherToShowWa || activity.lifecycle.currentState != Lifecycle.State.RESUMED) {
            KLog.d(logTagWa, "open---前一个开屏广告展示中或者生命周期不对")
            return false
        }
        if (appAdDataWa is AppOpenAd) {
            advertisingOpenCallbackWa()
            (appAdDataWa as AppOpenAd).show(activity)
        } else {
            startInsertScreenAdCallbackWa()
            (appAdDataWa as InterstitialAd).show(activity)
        }
        return true
    }

    /**
     * 加载启动页插屏广告
     */
    private fun loadStartInsertAdWa(context: Context, adData: WaAdBean) {
        val adRequest = AdRequest.Builder().build()
        val id = takeSortedAdIDWa(adIndexWa, adData.wa_open)
        KLog.d(
            logTagWa,
            "open--插屏广告id=$id;权重=${adData.wa_open.getOrNull(adIndexWa)?.wa_weight}"
        )

        InterstitialAd.load(
            context,
            id,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    adError.toString().let { KLog.d(logTagWa, "open---连接插屏加载失败=$it") }
                    isLoadingWa = false
                    appAdDataWa = null
                    if (adIndexWa < adData.wa_open.size - 1) {
                        adIndexWa++
                        loadStartupPageAdvertisementWa(context, adData)
                    } else {
                        adIndexWa = 0
                    }
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    loadTimeWa = Date().time
                    isLoadingWa = false
                    appAdDataWa = interstitialAd
                    KLog.d(logTagWa, "open--启动页插屏加载完成")
                }
            })
    }

    /**
     * StartInsert插屏广告回调
     */
    private fun startInsertScreenAdCallbackWa() {
        if (appAdDataWa !is InterstitialAd) {
            return
        }
        (appAdDataWa as InterstitialAd).fullScreenContentCallback =
            object : FullScreenContentCallback() {
                override fun onAdClicked() {
                    // Called when a click is recorded for an ad.
                    KLog.d(logTagWa, "open--插屏广告点击")
                    recordNumberOfAdClickWa()
                }

                override fun onAdDismissedFullScreenContent() {
                    // Called when ad is dismissed.
                    KLog.d(logTagWa, "open--关闭StartInsert插屏广告${App.isBackDataWa}")
                    if (!App.whetherBackgroundWa) {
                        LiveEventBus.get<Boolean>(Constant.OPEN_CLOSE_JUMP)
                            .post(true)
                    }
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
                    adIndexWa = 0
                    KLog.d(logTagWa, "open----插屏show")
                }
            }
    }
}
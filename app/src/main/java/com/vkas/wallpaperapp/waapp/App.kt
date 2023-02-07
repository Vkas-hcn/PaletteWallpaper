package com.vkas.wallpaperapp.waapp

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.MobileAds
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.Job
import com.blankj.utilcode.util.ProcessUtils
import com.google.android.gms.ads.AdActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.jeremyliao.liveeventbus.LiveEventBus
import com.vkas.wallpaperapp.BuildConfig
import com.vkas.wallpaperapp.wabase.AppManagerWaMVVM
import com.vkas.wallpaperapp.waevent.Constant
import com.vkas.wallpaperapp.waui.start.StartWaActivity
import com.vkas.wallpaperapp.wautils.ActivityUtils
import com.vkas.wallpaperapp.wautils.CalendarUtils
import com.vkas.wallpaperapp.wautils.KLog
import com.vkas.wallpaperapp.wautils.MmkvUtils
import com.xuexiang.xui.XUI
import com.xuexiang.xutil.XUtil
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
class App : Application(), LifecycleObserver {
    private var flag = 0
    private var job_pt : Job? =null
    private var ad_activity_pt: Activity? = null
    private var top_activity_pt: Activity? = null
    companion object {
        // app当前是否在后台
        var isBackDataWa = false
        // 是否是跳转设置
        var whetherJumpSetting = false
        // 是否进入后台（三秒后）
        var whetherBackgroundWa = false
        // 原生广告刷新
        var nativeAdRefreshWa = false
        val mmkvWa by lazy {
            //启用mmkv的多进程功能
            MMKV.mmkvWithID("wallpaper", MMKV.MULTI_PROCESS_MODE)
        }
        //当日日期
        var adDateWa = ""
        /**
         * 判断是否是当天打开
         */
        fun isAppOpenSameDayWa() {
            adDateWa = mmkvWa.decodeString(Constant.CURRENT_WA_DATE, "").toString()
            if (adDateWa == "") {
                MmkvUtils.set(Constant.CURRENT_WA_DATE, CalendarUtils.formatDateNow())
            } else {
                if (CalendarUtils.dateAfterDate(adDateWa, CalendarUtils.formatDateNow())) {
                    MmkvUtils.set(Constant.CURRENT_WA_DATE, CalendarUtils.formatDateNow())
                    MmkvUtils.set(Constant.CLICKS_WA_COUNT, 0)
                    MmkvUtils.set(Constant.SHOW_WA_COUNT, 0)
                }
            }
        }

    }
    override fun onCreate() {
        super.onCreate()
        MMKV.initialize(this)
//        initCrash()
        setActivityLifecycleWa(this)
        MobileAds.initialize(this) {}
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        if (ProcessUtils.isMainProcess()) {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
            Firebase.initialize(this)
            FirebaseApp.initializeApp(this)
            XUI.init(this) //初始化UI框架
            XUtil.init(this)
            LiveEventBus
                .config()
                .lifecycleObserverAlwaysActive(true)
            //是否开启打印日志
            KLog.init(BuildConfig.DEBUG)
        }
//        Core.init(this, MainActivity::class)
//        sendTimerInformation()
        isAppOpenSameDayWa()
    }
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onMoveToForeground() {
        nativeAdRefreshWa =true
        job_pt?.cancel()
        job_pt = null
        //从后台切过来，跳转启动页
        if (whetherBackgroundWa&& !isBackDataWa && !whetherJumpSetting) {
            jumpGuidePage()
        }
    }
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStopState(){
        job_pt = GlobalScope.launch {
            whetherBackgroundWa = false
            delay(3000L)
            whetherBackgroundWa = true
            ad_activity_pt?.finish()
            ActivityUtils.getActivity(StartWaActivity::class.java)?.finish()
        }
    }
    /**
     * 跳转引导页
     */
    private fun jumpGuidePage(){
        whetherBackgroundWa = false
        val intent = Intent(top_activity_pt, StartWaActivity::class.java)
        intent.putExtra(Constant.RETURN_WA_CURRENT_PAGE, true)
        top_activity_pt?.startActivity(intent)
    }
    fun setActivityLifecycleWa(application: Application) {
        //注册监听每个activity的生命周期,便于堆栈式管理
        application.registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                AppManagerWaMVVM.get().addActivity(activity)
                if (activity !is AdActivity) {
                    top_activity_pt = activity
                } else {
                    ad_activity_pt = activity
                }
                KLog.v("Lifecycle", "onActivityCreated" + activity.javaClass.name)
            }

            override fun onActivityStarted(activity: Activity) {
                KLog.v("Lifecycle", "onActivityStarted" + activity.javaClass.name)
                if (activity !is AdActivity) {
                    top_activity_pt = activity
                } else {
                    ad_activity_pt = activity
                }
                flag++
                isBackDataWa = false
            }

            override fun onActivityResumed(activity: Activity) {
                KLog.v("Lifecycle", "onActivityResumed=" + activity.javaClass.name)
                if (activity !is AdActivity) {
                    top_activity_pt = activity
                }
            }

            override fun onActivityPaused(activity: Activity) {
                if (activity is AdActivity) {
                    ad_activity_pt = activity
                } else {
                    top_activity_pt = activity
                }
                KLog.v("Lifecycle", "onActivityPaused=" + activity.javaClass.name)
            }

            override fun onActivityStopped(activity: Activity) {
                flag--
                if (flag == 0) {
                    isBackDataWa = true
                }
                KLog.v("Lifecycle", "onActivityStopped=" + activity.javaClass.name)
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
                KLog.v("Lifecycle", "onActivitySaveInstanceState=" + activity.javaClass.name)

            }

            override fun onActivityDestroyed(activity: Activity) {
                AppManagerWaMVVM.get().removeActivity(activity)
                KLog.v("Lifecycle", "onActivityDestroyed" + activity.javaClass.name)
                ad_activity_pt = null
                top_activity_pt = null
            }
        })
    }
}
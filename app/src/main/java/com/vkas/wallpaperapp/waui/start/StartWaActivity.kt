package com.vkas.wallpaperapp.waui.start

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.jeremyliao.liveeventbus.LiveEventBus
import com.vkas.wallpaperapp.BR
import com.vkas.wallpaperapp.BuildConfig
import com.vkas.wallpaperapp.R
import com.vkas.wallpaperapp.databinding.ActivityStartBinding
import com.vkas.wallpaperapp.waad.WaLoadBackAd
import com.vkas.wallpaperapp.waad.WaLoadListAd
import com.vkas.wallpaperapp.waad.WaLoadOpenAd
import com.vkas.wallpaperapp.waad.WaLoadResultAd
import com.vkas.wallpaperapp.waapp.App
import com.vkas.wallpaperapp.wabase.BaseActivity
import com.vkas.wallpaperapp.wabase.BaseViewModel
import com.vkas.wallpaperapp.waevent.Constant
import com.vkas.wallpaperapp.waevent.Constant.logTagWa
import com.vkas.wallpaperapp.waui.main.MainActivity
import com.vkas.wallpaperapp.wautils.KLog
import com.vkas.wallpaperapp.wautils.MmkvUtils
import com.vkas.wallpaperapp.wautils.WallPaperUtils.isThresholdReached
import kotlinx.coroutines.*

class StartWaActivity  : BaseActivity<ActivityStartBinding, BaseViewModel>(){
    override fun initContentView(savedInstanceState: Bundle?): Int {
        return R.layout.activity_start
    }

    override fun initVariableId(): Int {
        return BR._all
    }

    companion object {
        var isCurrentPage: Boolean = false
    }
    private var liveJumpHomePage = MutableLiveData<Boolean>()
    private var liveJumpHomePage2 = MutableLiveData<Boolean>()
    private var jobOpenAdsWa: Job? = null
    override fun initParam() {
        super.initParam()
        isCurrentPage = intent.getBooleanExtra(Constant.RETURN_WA_CURRENT_PAGE, false)

    }
    override fun initToolbar() {
        super.initToolbar()
    }

    override fun initData() {
        super.initData()
        liveEventBusWa()
        getFirebaseDataWa()
        jumpHomePageData()
    }
    private fun liveEventBusWa() {
        LiveEventBus
            .get(Constant.OPEN_CLOSE_JUMP, Boolean::class.java)
            .observeForever {
                KLog.d(logTagWa, "关闭开屏内容-接收==${this.lifecycle.currentState}")
                if (this.lifecycle.currentState == Lifecycle.State.STARTED) {
                    jumpPage()
                }
            }
    }

    private fun getFirebaseDataWa() {
        if (BuildConfig.DEBUG) {
            preloadedAdvertisement()
//            lifecycleScope.launch {
//                delay(1500)
//                MmkvUtils.set(Constant.ADVERTISING_WA_DATA, ResourceUtils.readStringFromAssert("elAdDataFireBase.json"))
//            }
            return
        } else {
            preloadedAdvertisement()
            val auth = Firebase.remoteConfig
            auth.fetchAndActivate().addOnSuccessListener {
                MmkvUtils.set(Constant.PROFILE_WA_DATA, auth.getString("wall_ser"))
                MmkvUtils.set(Constant.PROFILE_WA_DATA_FAST, auth.getString("wall_smar"))
                MmkvUtils.set(Constant.AROUND_WA_FLOW_DATA, auth.getString("WaAroundFlow_Data"))
                MmkvUtils.set(Constant.ADVERTISING_WA_DATA, auth.getString("wall_ad"))

            }
        }
    }

    override fun initViewObservable() {
        super.initViewObservable()
    }

    private fun jumpHomePageData() {
        liveJumpHomePage2.observe(this, {
            lifecycleScope.launch(Dispatchers.Main.immediate) {
                KLog.e("TAG", "isBackDataWa==${App.isBackDataWa}")
                delay(300)
                if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                    jumpPage()
                }
            }
        })
        liveJumpHomePage.observe(this, {
            liveJumpHomePage2.postValue(true)
        })
    }

    /**
     * 跳转页面
     */
    private fun jumpPage() {
        // 不是后台切回来的跳转，是后台切回来的直接finish启动页
        if (!isCurrentPage) {
            val intent = Intent(this@StartWaActivity, MainActivity::class.java)
            startActivity(intent)
        }
        finish()

    }
    /**
     * 加载广告
     */
    private fun loadAdvertisement() {
        // 开屏
        WaLoadOpenAd.getInstance().adIndexWa = 0
        WaLoadOpenAd.getInstance().advertisementLoadingWa(this)
        rotationDisplayOpeningAdWa()
        // 首页原生
        WaLoadListAd.getInstance().adIndexWa = 0
        WaLoadListAd.getInstance().advertisementLoadingWa(this)
        // 结果页原生
        WaLoadResultAd.getInstance().adIndexWa = 0
        WaLoadResultAd.getInstance().advertisementLoadingWa(this)
        // 返回插屏
        WaLoadBackAd.getInstance().adIndexWa = 0
        WaLoadBackAd.getInstance().advertisementLoadingWa(this)
    }
    /**
     * 轮训展示开屏广告
     */
    private fun rotationDisplayOpeningAdWa() {
        jobOpenAdsWa = lifecycleScope.launch {
            try {
                withTimeout(8000L) {
                    delay(1000L)
                    while (isActive) {
                        val showState = WaLoadOpenAd.getInstance()
                            .displayOpenAdvertisementWa(this@StartWaActivity)
                        if (showState) {
                            jobOpenAdsWa?.cancel()
                            jobOpenAdsWa =null
                        }
                        delay(1000L)
                    }
                }
            } catch (e: TimeoutCancellationException) {
                KLog.e("TimeoutCancellationException I'm sleeping $e")
                jumpPage()
            }
        }
    }
    /**
     * 预加载广告
     */
    private fun preloadedAdvertisement() {
        App.isAppOpenSameDayWa()
        if (isThresholdReached()) {
            KLog.d(logTagWa, "广告达到上线")
            lifecycleScope.launch {
                delay(2000L)
                liveJumpHomePage.postValue(true)
            }
        } else {
            loadAdvertisement()
        }
    }
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return keyCode == KeyEvent.KEYCODE_BACK
    }

}
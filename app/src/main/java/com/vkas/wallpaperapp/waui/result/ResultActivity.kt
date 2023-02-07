package com.vkas.wallpaperapp.waui.result

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.jeremyliao.liveeventbus.LiveEventBus
import com.vkas.wallpaperapp.BR
import com.vkas.wallpaperapp.R
import com.vkas.wallpaperapp.databinding.ActivityResultBinding
import com.vkas.wallpaperapp.databinding.ActivityStartBinding
import com.vkas.wallpaperapp.waad.WaLoadBackAd
import com.vkas.wallpaperapp.waad.WaLoadResultAd
import com.vkas.wallpaperapp.waapp.App
import com.vkas.wallpaperapp.waapp.App.Companion.whetherJumpSetting
import com.vkas.wallpaperapp.wabase.BaseActivity
import com.vkas.wallpaperapp.wabase.BaseViewModel
import com.vkas.wallpaperapp.waevent.Constant
import com.vkas.wallpaperapp.waevent.Constant.logTagWa
import com.vkas.wallpaperapp.wautils.KLog
import com.vkas.wallpaperapp.wautils.WallPaperUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ResultActivity : BaseActivity<ActivityResultBinding, BaseViewModel>() {
    // 当前图片地址
    private var currentPictureAddress: String? = null
    private var jobResultWa: Job? = null

    override fun initContentView(savedInstanceState: Bundle?): Int {
        return R.layout.activity_result
    }

    override fun initVariableId(): Int {
        return BR._all
    }

    override fun initParam() {
        super.initParam()
        val bundle = intent.extras
        currentPictureAddress = bundle?.getString(Constant.JUMP_RESULT_PAGE_PATH)!!
//        whetherJumpSetting = false
    }

    override fun initToolbar() {
        super.initToolbar()
        liveEventBusReceive()
        binding.resultTitle.imgBack.setImageResource(R.mipmap.ic_title_back_wa)
        binding.resultTitle.imgBack.setOnClickListener {
            returnToHomePage()
        }
    }

    private fun liveEventBusReceive() {
        //插屏关闭后跳转
        LiveEventBus
            .get(Constant.PLUG_WA_BACK_AD_SHOW, Boolean::class.java)
            .observeForever {
                finish()
            }
    }

    override fun initData() {
        super.initData()
        val goodsIcon: Int = this@ResultActivity.resources
            .getIdentifier(
                currentPictureAddress,
                "mipmap", this@ResultActivity.packageName
            )
        binding.imgResult.setImageResource(goodsIcon)
        WaLoadResultAd.getInstance().whetherToShowWa = false
        WaLoadBackAd.getInstance().advertisementLoadingWa(this)
        initResultAds()

    }

    override fun initViewObservable() {
        super.initViewObservable()
    }

    private fun initResultAds() {
        jobResultWa = lifecycleScope.launch {
            while (isActive) {
                WaLoadResultAd.getInstance().setDisplayResultNativeAd(this@ResultActivity, binding)
                if (WaLoadResultAd.getInstance().whetherToShowWa) {
                    jobResultWa?.cancel()
                    jobResultWa = null
                }
                delay(1000L)
            }
        }
    }

    /**
     * 返回主页
     */
    private fun returnToHomePage() {
        App.isAppOpenSameDayWa()
        if (WallPaperUtils.isThresholdReached()) {
            KLog.d(logTagWa, "广告达到上线")
            finish()
            return
        }
        if (!WaLoadBackAd.getInstance().displayBackAdvertisementWa(this)) {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            delay(300)
            if (lifecycle.currentState != Lifecycle.State.RESUMED) {
                return@launch
            }
            if (App.nativeAdRefreshWa) {
                WaLoadResultAd.getInstance().whetherToShowWa = false
                if (WaLoadResultAd.getInstance().appAdDataWa != null) {
                    WaLoadResultAd.getInstance()
                        .setDisplayResultNativeAd(this@ResultActivity, binding)
                } else {
                    WaLoadResultAd.getInstance().advertisementLoadingWa(this@ResultActivity)
                    initResultAds()
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            returnToHomePage()
        }
        return true
    }
}
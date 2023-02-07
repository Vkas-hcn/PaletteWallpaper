package com.vkas.wallpaperapp.waui.main

import android.Manifest.permission
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.vkas.wallpaperapp.BR
import com.vkas.wallpaperapp.R
import com.vkas.wallpaperapp.databinding.ActivityMainBinding
import com.vkas.wallpaperapp.databinding.ActivityStartBinding
import com.vkas.wallpaperapp.wabase.BaseActivity
import com.vkas.wallpaperapp.wabase.BaseViewModel
import android.Manifest.permission.SET_WALLPAPER
import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.provider.MediaStore
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.vkas.wallpaperapp.waad.WaLoadListAd
import com.vkas.wallpaperapp.waapp.App
import com.vkas.wallpaperapp.wabean.WallpaperBean
import com.vkas.wallpaperapp.waevent.Constant
import com.vkas.wallpaperapp.waui.detail.WallpaperDetailActivity
import com.vkas.wallpaperapp.waui.web.WebWaActivity
import com.vkas.wallpaperapp.wautils.KLog
import com.vkas.wallpaperapp.wautils.WaLoadingDialog
import com.xuexiang.xutil.tip.ToastUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.recyclerview.widget.DefaultItemAnimator

import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.vkas.wallpaperapp.wautils.BottomOffsetDecoration


class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>() {
    private lateinit var selectAdapter: WallpaperListAdapter
    private var elServiceBeanList: MutableList<WallpaperBean> = ArrayList()
    private lateinit var adBean: WallpaperBean
    private lateinit var waLoadingDialog: WaLoadingDialog

    override fun initContentView(savedInstanceState: Bundle?): Int {
        return R.layout.activity_main
    }

    override fun initVariableId(): Int {
        return BR._all
    }

    override fun initParam() {
        super.initParam()
    }

    override fun initToolbar() {
        super.initToolbar()
        binding.presenter = WaClick()
        waLoadingDialog = WaLoadingDialog(this)

        binding.mainTitleWa.imgBack.setOnClickListener {
            binding.sidebarShowsWa=true
//            waLoadingDialog.show()
        }
    }

    override fun initData() {
        super.initData()
        initWallpaperRecyclerView()
        viewModel.getServerListData()
        WaLoadListAd.getInstance().advertisementLoadingWa(this)
        WaLoadListAd.getInstance().whetherToShowWa = false

    }
    override fun initViewObservable() {
        super.initViewObservable()
        getWallPaperListData()
    }

    private fun getWallPaperListData() {
        viewModel.liveWallpaperListData.observe(this, {
            echoWallpaper(it)
        })
    }
    private fun initWallpaperRecyclerView() {
        selectAdapter = WallpaperListAdapter(this, elServiceBeanList)
        val layoutManager =
            GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if(position ==3){
                    3
                }else{
                    1
                }
            }
        }
        val offset = 200f //这里是你要在最后一个item底部留多少空间
        val bottomOffsetDecoration = BottomOffsetDecoration(offset.toInt())
        binding.rcList.addItemDecoration(bottomOffsetDecoration)
        binding.rcList.layoutManager = layoutManager
        binding.rcList.itemAnimator = DefaultItemAnimator()
        binding.rcList.adapter = selectAdapter
        selectAdapter.setOnItemClickListener { _, _, pos ->
            run {
              val pos=  if(pos>2){
                    pos-1
                }else{
                    pos
                }
                val bundle = Bundle()
                bundle.putInt(Constant.JUMP_WALLPAPER_SUBSCRIPT, pos)
                startActivity(WallpaperDetailActivity::class.java, bundle)
            }
        }
    }
    /**
     * 回显壁纸
     */
    private fun echoWallpaper(it: MutableList<WallpaperBean>) {
        elServiceBeanList = it
        adBean = WallpaperBean()
        adBean.isAd = true
        if (elServiceBeanList.size >= 3) {
            elServiceBeanList.add(3, adBean)
        } else {
            elServiceBeanList.add(adBean)
        }
        selectAdapter.setList(elServiceBeanList)
    }


    inner class WaClick {
        fun clickMain() {
            KLog.e("TAG","binding.sidebarShowsWa===>${binding.sidebarShowsWa}")
            if (binding.sidebarShowsWa == true) {
                binding.sidebarShowsWa = false
            }
        }

        fun clickMainMenu() {

        }

        fun toContactUs() {
            val uri = Uri.parse("mailto:${Constant.MAILBOX_WA_ADDRESS}")
            val intent = Intent(Intent.ACTION_SENDTO, uri)
            runCatching {
                startActivity(intent)
            }.onFailure {
                ToastUtils.toast("Please set up a Mail account")
            }
        }

        fun toPrivacyPolicy() {
            startActivity(WebWaActivity::class.java)
        }

        fun toShare() {
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.putExtra(
                Intent.EXTRA_TEXT,
                Constant.SHARE_WA_ADDRESS + this@MainActivity.packageName
            )
            intent.type = "text/plain"
            startActivity(intent)
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            delay(300)
            if(lifecycle.currentState != Lifecycle.State.RESUMED){return@launch}
            if (App.nativeAdRefreshWa) {
                WaLoadListAd.getInstance().whetherToShowWa = false
                if (WaLoadListAd.getInstance().appAdDataWa != null) {
                    selectAdapter.notifyDataSetChanged()
                } else {
                    WaLoadListAd.getInstance().advertisementLoadingWa(this@MainActivity)
                    selectAdapter.notifyDataSetChanged()
                }
            }
        }
    }
}
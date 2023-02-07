package com.vkas.wallpaperapp.waui.detail

import android.Manifest
import android.R.attr
import android.app.AlertDialog
import android.app.WallpaperManager
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import cc.shinichi.wallpaperlib.SetWallpaper
import com.bumptech.glide.Glide
import com.jeremyliao.liveeventbus.LiveEventBus
import com.vkas.wallpaperapp.BR
import com.vkas.wallpaperapp.databinding.ActivityDetailBinding
import com.vkas.wallpaperapp.wabase.BaseActivity
import com.vkas.wallpaperapp.waevent.Constant
import com.vkas.wallpaperapp.wautils.ActivityUtils
import com.vkas.wallpaperapp.wautils.KLog
import com.vkas.wallpaperapp.wautils.WallUtils.saveImageToGallery
import com.xuexiang.xui.XUI.getContext
import com.xuexiang.xutil.tip.ToastUtils
import com.vkas.wallpaperapp.R
import com.vkas.wallpaperapp.waui.main.WallpaperListAdapter
import com.vkas.wallpaperapp.waui.result.ResultActivity
import com.vkas.wallpaperapp.wautils.WallPaperUtils
import com.xuexiang.xui.utils.WidgetUtils
import com.xuexiang.xui.widget.dialog.MiniLoadingDialog
import android.R.attr.bitmap
import android.content.ComponentName

import android.provider.MediaStore
import android.widget.Toast
import cc.shinichi.wallpaperlib.util.ImageUtil
import com.vkas.wallpaperapp.waapp.App
import com.vkas.wallpaperapp.waapp.App.Companion.whetherJumpSetting
import com.vkas.wallpaperapp.wautils.SetWallpaperWa
import com.vkas.wallpaperapp.wautils.WallPaperUtils.imageTranslateUri
import com.yanzhenjie.permission.Action
import com.yanzhenjie.permission.AndPermission
import com.yanzhenjie.permission.Permission
import java.io.IOException

class WallpaperDetailActivity : BaseActivity<ActivityDetailBinding, WallpaperDetailViewModel>() {
    var mRecyclerView: RecyclerView? = null
    var mPagerLayoutManager: LinearLayoutManager? = null
    private var mRecyclerViewAdapter: CommonRecyclerViewAdapter? = null
    private lateinit var mSnapHelper: SnapHelper
    private var pos: Int = 0
    private val APP_AUTHORITY = "com.vkas.easylinkapp.fileprovider"

    override fun initContentView(savedInstanceState: Bundle?): Int {
        return R.layout.activity_detail
    }

    override fun initVariableId(): Int {
        return BR._all
    }

    override fun initParam() {
        super.initParam()
        val bundle = intent.extras
        pos = bundle?.getInt(Constant.JUMP_WALLPAPER_SUBSCRIPT)!!
    }

    override fun initToolbar() {
        super.initToolbar()
        binding.presenter = DetailClick()
        liveEventBusReceive()
        binding.detailTitleWa.imgBack.setImageResource(R.mipmap.ic_title_back_wa)
        binding.detailTitleWa.imgBack.setOnClickListener {
            finish()
        }
    }

    private fun liveEventBusReceive() {
    }

    override fun initData() {
        super.initData()
        mRecyclerView = RecyclerView(getContext())
        mPagerLayoutManager =
            LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
        mRecyclerView!!.layoutManager = mPagerLayoutManager
        mRecyclerViewAdapter = CommonRecyclerViewAdapter(this@WallpaperDetailActivity)
        mRecyclerViewAdapter!!.itemCount = 59
        mRecyclerView!!.adapter = mRecyclerViewAdapter
        mRecyclerView!!.scrollToPosition(pos)
        binding.paperWarp.addView(mRecyclerView)
        // PagerSnapHelper每次只能滚动一个item;用LinearSnapHelper则可以一次滚动多个，并最终保证定位
        // mSnapHelper = new LinearSnapHelper();
        // PagerSnapHelper每次只能滚动一个item;用LinearSnapHelper则可以一次滚动多个，并最终保证定位
        // mSnapHelper = new LinearSnapHelper();
        mSnapHelper = PagerSnapHelper()
        mSnapHelper.attachToRecyclerView(mRecyclerView)
    }

    override fun initViewObservable() {
        super.initViewObservable()
    }

    inner class DetailClick {
        fun dissMiss() {
            binding.waDialog = false
        }

        fun downloadImg() {

//            AndPermission.with(this@WallpaperDetailActivity)
//                .runtime()
//                .permission(Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE)
//                .onDenied {
//                    Toast.makeText(
//                        this@WallpaperDetailActivity,
//                        "Please enable storage permissions",
//                        Toast.LENGTH_SHORT
//                    )
//                        .show()
//                    AndPermission.with(this@WallpaperDetailActivity).runtime().setting().start()
//                }
//                .onGranted { saveImage() }
//                .start()
            val hasWriteStoragePermission: Int =
                ContextCompat.checkSelfPermission(
                    application,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            if (hasWriteStoragePermission == PackageManager.PERMISSION_GRANTED) {
                saveImage()
            } else {
                //没有权限，向用户请求权限
                ActivityCompat.requestPermissions(
                    this@WallpaperDetailActivity,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1
                )
            }

        }

        fun settingImg() {
            binding.waDialog = true
//            AndPermission.with(this@WallpaperDetailActivity)
//                .runtime()
//                .permission(Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE)
//                .onDenied {
//                    Toast.makeText(
//                        this@WallpaperDetailActivity,
//                        "Please enable storage permissions",
//                        Toast.LENGTH_SHORT
//                    )
//                        .show()
//                    AndPermission.with(this@WallpaperDetailActivity).runtime().setting().start()
//                }
//                .onGranted {
//                    binding.waDialog = true
//                }
//                .start()
        }

        fun setAsLockScreen() {
            binding.proLoading.visibility = View.VISIBLE
            setHomeWallpaper()
        }

        fun setAsHomeScreen() {
            binding.proLoading.visibility = View.VISIBLE
            setHomeWallpaper()
        }

        fun setAsBothScreen() {
            binding.proLoading.visibility = View.VISIBLE
            setHomeWallpaper()
        }
    }

    /**
     * Android：实现保存assets图片（或res下的图片：R.drawable.image）到手机相册
     */
    private fun saveImage() {
        val firstPosition =
            (mRecyclerView?.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        val goodsIcon: Int = this@WallpaperDetailActivity.resources
            .getIdentifier(
                mRecyclerViewAdapter?.waWallpaperBeanList?.getOrNull(firstPosition)?.wallpaperPath,
                "mipmap", this@WallpaperDetailActivity.packageName
            )
        val bitmap = BitmapFactory.decodeResource(this.resources, goodsIcon)
        Thread {
            val isSaveeSuccess: Boolean = saveImageToGallery(bitmap, this)
            if (isSaveeSuccess) {
                Looper.prepare()
                ToastUtils.toast("Download successfully")
                Looper.loop()
            } else {
                Looper.prepare()
                ToastUtils.toast("Download failed")
                Looper.loop()
            }
        }.start()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveImage()
            } else {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                ) {
                    denyPermissionPopUp()
                }
            }
        }
    }

    /**
     * 拒绝权限弹框
     */
    private fun denyPermissionPopUp() {
        val dialog: AlertDialog? = AlertDialog.Builder(this)
            .setTitle(getString(R.string.permission_title))
            .setMessage(getString(R.string.permission_message))
            //设置对话框的按钮
            .setNegativeButton("CANCEL") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("SET UP") { dialog, _ ->
                dialog.dismiss()
                goSystemSetting()
            }.create()
        dialog?.show()
        dialog?.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(Color.BLACK)
        dialog?.getButton(DialogInterface.BUTTON_NEGATIVE)?.setTextColor(Color.BLACK)
    }

    private fun goSystemSetting() {
        val intent = Intent().apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            action = "android.settings.APPLICATION_DETAILS_SETTINGS"
            data = Uri.fromParts("package", this@WallpaperDetailActivity.packageName, null)
        }
        startActivity(intent)
    }

    fun setHomeWallpaper() {
        val firstPosition =
            (mRecyclerView?.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        val wallpaperManager = WallpaperManager.getInstance(this@WallpaperDetailActivity)
        val goodsIcon: Int = this@WallpaperDetailActivity.resources
            .getIdentifier(
                mRecyclerViewAdapter?.waWallpaperBeanList?.getOrNull(firstPosition)?.wallpaperPath,
                "mipmap", this@WallpaperDetailActivity.packageName
            )
        try {
            val bitmap =
                ContextCompat.getDrawable(
                    this@WallpaperDetailActivity,
                    goodsIcon
                )
                    ?.toBitmap()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                wallpaperManager.setBitmap(
                    bitmap,
                    null,
                    false,
                    WallpaperManager.FLAG_SYSTEM
                )
            }
            binding.waDialog = false
            jumpToTheResultPage()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setLocKWallpaper() {
        whetherJumpSetting = true
        val firstPosition =
            (mRecyclerView?.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        val goodsIcon: Int = this@WallpaperDetailActivity.resources
            .getIdentifier(
                mRecyclerViewAdapter?.waWallpaperBeanList?.getOrNull(firstPosition)?.wallpaperPath,
                "mipmap", this@WallpaperDetailActivity.packageName
            )
        SetWallpaperWa.setWallpaper(
            this@WallpaperDetailActivity, // 上下文
            goodsIcon, // 图片绝对路径
            APP_AUTHORITY
        )
    }

    fun setBothWallpaper() {
        whetherJumpSetting = true
        val firstPosition =
            (mRecyclerView?.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        val goodsIcon: Int = this@WallpaperDetailActivity.resources
            .getIdentifier(
                mRecyclerViewAdapter?.waWallpaperBeanList?.getOrNull(firstPosition)?.wallpaperPath,
                "mipmap", this@WallpaperDetailActivity.packageName
            )

        SetWallpaperWa.setWallpaper(
            applicationContext, // 上下文
            goodsIcon, // 图片绝对路径
            APP_AUTHORITY
        )
    }

    /**
     * 跳转结果页
     */
    private fun jumpToTheResultPage() {
        binding.proLoading.visibility = View.GONE
        val firstPosition =
            (mRecyclerView?.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        val bundle = Bundle()
        bundle.putString(
            Constant.JUMP_RESULT_PAGE_PATH,
            mRecyclerViewAdapter?.waWallpaperBeanList?.getOrNull(firstPosition)?.wallpaperPath
        )
        startActivity(ResultActivity::class.java, bundle)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        KLog.e("TAG", "requestCode====>$requestCode")
        KLog.e("TAG", "resultCode====>$resultCode")
        KLog.e("TAG", "data====>$data")
        if (requestCode == 0x11) {
            binding.waDialog = false
            jumpToTheResultPage()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        whetherJumpSetting = false
    }
}
package com.vkas.wallpaperapp.waui.web
import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.Color
import android.net.http.SslError
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.SslErrorHandler
import android.webkit.WebView
import android.webkit.WebViewClient
import com.vkas.wallpaperapp.BR
import com.vkas.wallpaperapp.R
import com.vkas.wallpaperapp.databinding.ActivityWebWaBinding
import com.vkas.wallpaperapp.wabase.BaseActivity
import com.vkas.wallpaperapp.wabase.BaseViewModel
import com.vkas.wallpaperapp.waevent.Constant

class WebWaActivity : BaseActivity<ActivityWebWaBinding, BaseViewModel>() {
    override fun initContentView(savedInstanceState: Bundle?): Int {
        return R.layout.activity_web_wa
    }

    override fun initVariableId(): Int {
        return BR._all
    }

    override fun initToolbar() {
        super.initToolbar()
        binding.webTitleWa.imgBack.visibility = View.VISIBLE
        binding.webTitleWa.imgBack.setImageResource(R.mipmap.ic_title_back_wa)

        binding.webTitleWa.imgBack.setOnClickListener {
            finish()
        }
        binding.webTitleWa.tvTitle.visibility = View.GONE
    }

    override fun initData() {
        super.initData()
        binding.ppWebWa.loadUrl(Constant.PRIVACY_WA_AGREEMENT)
        binding.ppWebWa.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            }

            override fun onPageFinished(view: WebView, url: String) {
            }

            override fun onReceivedSslError(
                view: WebView,
                handler: SslErrorHandler,
                error: SslError
            ) {
                handler.proceed()
            }
        }

        binding.ppWebWa.webViewClient = object : WebViewClient() {
            override fun onReceivedSslError(
                view: WebView,
                handler: SslErrorHandler, error: SslError
            ) {
                val dialog: AlertDialog? = AlertDialog.Builder(this@WebWaActivity)
                    .setTitle("SSL authentication failed. Do you want to continue accessing?")
                    //设置对话框的按钮
                    .setNegativeButton("cancel") { dialog, _ ->
                        dialog.dismiss()
                        handler.cancel()
                    }
                    .setPositiveButton("continue") { dialog, _ ->
                        dialog.dismiss()
                        handler.cancel()
                    }.create()

                val params = dialog!!.window!!.attributes
                params.width = 200
                params.height = 200
                dialog.window!!.attributes = params
                dialog.show()
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(Color.BLACK)
                dialog.getButton(DialogInterface.BUTTON_NEGATIVE)?.setTextColor(Color.BLACK)
            }

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                if (Constant.PRIVACY_WA_AGREEMENT == url) {
                    view.loadUrl(url)
                } else {
                    // 系统处理
                    return super.shouldOverrideUrlLoading(view, url)
                }
                return true
            }
        }


    }


    //点击返回上一页面而不是退出浏览器
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && binding.ppWebWa.canGoBack()) {
            binding.ppWebWa.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        binding.ppWebWa.loadDataWithBaseURL(null, "", "text/html", "utf-8", null)
        binding.ppWebWa.clearHistory()
        (binding.ppWebWa.parent as ViewGroup).removeView(binding.ppWebWa)
        binding.ppWebWa.destroy()
        super.onDestroy()
    }
}
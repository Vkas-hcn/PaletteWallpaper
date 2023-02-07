package com.vkas.wallpaperapp.wautils

import android.content.Context
import android.os.Looper

import android.graphics.BitmapFactory

import android.graphics.Bitmap
import com.vkas.wallpaperapp.wautils.ActivityUtils.getActivity
import android.os.Environment

import android.content.Intent
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


object WallUtils {
    fun saveImageToGallery(bitmap: Bitmap, context: Context): Boolean {
        //获取手机相册路径
        val appDir = File(getDCIM<Any>() as String)
        if (!appDir.exists()) {
            appDir.mkdir()
        }
        //获取当前时间，标识不同时间保存的图片
        val fileName = System.currentTimeMillis().toString() + ".png"
        val file = File(appDir, fileName)
        try {
            val fos = FileOutputStream(file)
            val isSuccess = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()
            //通知图库更新，必须添加，否则相册无法更新新添加的图片
            val uri: Uri = Uri.fromFile(file)
            context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri))
            //返回图片保存结果
            return isSuccess
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return false
    }

    fun <string> getDCIM(): string {
        if (Environment.MEDIA_MOUNTED != Environment.getExternalStorageState()) {
            return "" as string
        }
        var path = Environment.getExternalStorageDirectory().path + "/dcim"
        if (File(path).exists()) {
            return path as string
        }
        path = Environment.getExternalStorageDirectory().path + "/DCIM"
        val file = File(path)
        if (!file.exists()) {
            if (!file.mkdir()) {
                return "" as string
            }
        }
        return path as string
    }


     fun getBitmap(context: Context, vectorDrawableId: Int): Bitmap {
        var bitmap: Bitmap? = null
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            val vectorDrawable = context.getDrawable(vectorDrawableId)
            bitmap = Bitmap.createBitmap(
                vectorDrawable!!.intrinsicWidth,
                vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
            vectorDrawable.draw(canvas)
        } else {
            bitmap = BitmapFactory.decodeResource(context.resources, vectorDrawableId)
        }
        return bitmap
    }


}
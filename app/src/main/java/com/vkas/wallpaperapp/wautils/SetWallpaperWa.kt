package com.vkas.wallpaperapp.wautils

import android.text.TextUtils
import cc.shinichi.wallpaperlib.util.FileUtil
import android.content.Intent
import cc.shinichi.wallpaperlib.util.RomUtil
import android.content.ComponentName
import android.app.WallpaperManager
import android.content.Context
import android.os.Build
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import cc.shinichi.wallpaperlib.util.ImageUtil
import java.io.IOException
import java.lang.Exception
import java.lang.IllegalArgumentException
import android.R

import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.xuexiang.xutil.XUtil.getResources
import java.io.InputStream


object SetWallpaperWa {
    fun setWallpaper(context: Context?, path: Int, authority: String?) {
        if (context == null || TextUtils.isEmpty(authority)) {
            return
        }
    KLog.e("TAG","context.resources==${context.resources}")
        val `is`: InputStream = getResources().openRawResource(path)
        val mBitmap = BitmapFactory.decodeStream(`is`)

        val bitmap =
            BitmapFactory.decodeResource(context.resources, path) //自己本地的图片可以是drawabe/mipmap
//        val bitmap = ContextCompat.getDrawable(context, path)?.toBitmap()

        val imageUri = Uri.parse(
            MediaStore.Images.Media.insertImage(
                context.contentResolver, bitmap, "", ""
            )
        )
        val intent: Intent
        if (RomUtil.isHuaweiRom()) {
            try {
                val componentName =
                    ComponentName("com.android.gallery3d", "com.android.gallery3d.app.Wallpaper")
                intent = Intent(Intent.ACTION_VIEW)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.setDataAndType(imageUri, "image/*")
                intent.putExtra("mimeType", "image/*")
                intent.component = componentName
                context.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
                try {
                    WallpaperManager.getInstance(context.applicationContext)
                        .setBitmap(bitmap)
                } catch (e1: IOException) {
                    e1.printStackTrace()
                }
            }
        } else if (RomUtil.isMiuiRom()) {
            try {
                val componentName = ComponentName(
                    "com.android.thememanager",
                    "com.android.thememanager.activity.WallpaperDetailActivity"
                )
                intent = Intent("miui.intent.action.START_WALLPAPER_DETAIL")
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.setDataAndType(imageUri, "image/*")
                intent.putExtra("mimeType", "image/*")
                intent.component = componentName
                context.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
                try {
                    WallpaperManager.getInstance(context.applicationContext)
                        .setBitmap(bitmap)
                } catch (e1: IOException) {
                    e1.printStackTrace()
                }
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                try {
                    intent = WallpaperManager.getInstance(context.applicationContext)
                        .getCropAndSetWallpaperIntent(imageUri)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.applicationContext.startActivity(intent)
                } catch (e: IllegalArgumentException) {
                    var bitmap: Bitmap? = null
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(
                            context.applicationContext.contentResolver,
                            imageUri
                        )
                        if (bitmap != null) {
                            WallpaperManager.getInstance(context.applicationContext)
                                .setBitmap(bitmap)
                        }
                    } catch (e1: IOException) {
                        e1.printStackTrace()
                    }
                }
            } else {
                try {
                    WallpaperManager.getInstance(context.applicationContext)
                        .setBitmap(bitmap)
                } catch (e1: IOException) {
                    e1.printStackTrace()
                }
            }
        }
    }
}
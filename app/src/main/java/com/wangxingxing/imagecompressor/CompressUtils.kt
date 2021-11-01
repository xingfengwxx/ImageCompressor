package com.wangxingxing.imagecompressor

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

/**
 * author : 王星星
 * date : 2021/10/30 14:52
 * email : 1099420259@qq.com
 * description :
 */
object CompressUtils {

    /**
     * 质量压缩
     */
    fun qualityCompress(srcFilePath: String, destFilePath: String, quality: Int) {
        val bitmap = BitmapFactory.decodeFile(srcFilePath, BitmapFactory.Options())
        val fos: FileOutputStream? = null
        try {
            // 压缩之后的数据输出流
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, quality, baos)

            val fos = FileOutputStream(File(destFilePath))
            // 把压缩之后的数据流，通过fos写入磁盘文件
            fos.write(baos.toByteArray())
        } catch (ex: Throwable) {
            ex.printStackTrace()
        } finally {
            fos?.apply {
                flush()
                close()
            }
        }
    }

    /**
     * 尺寸压缩
     */
    fun scaleCompress(srcFilePath: String, destFilePath: String, quality: Int, reqWidth: Int, reqHeight: Int) {
        val imageFile = File(srcFilePath)
        val bitmap = decodeSampledBitmapFromFile(imageFile, reqWidth, reqHeight)
        val fos: FileOutputStream? = null
        try {
            // 压缩之后的数据输出流
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, quality, baos)

            val fos = FileOutputStream(File(destFilePath))
            // 把压缩之后的数据流，通过fos写入磁盘文件
            fos.write(baos.toByteArray())
        } catch (ex: Throwable) {
            ex.printStackTrace()
        } finally {
            fos?.apply {
                flush()
                close()
            }
        }
    }

    /**
     * native无损压缩
     */
    fun nativeCompress(srcFilePath: String, destFilePath: String, quality: Int) {
        val bitmap = BitmapFactory.decodeFile(srcFilePath, BitmapFactory.Options())
        nativeCompress(bitmap, quality, destFilePath, true)
    }

    private fun decodeSampledBitmapFromFile(
        imageFile: File,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap {
        return BitmapFactory.Options().run {
            inJustDecodeBounds = true
            // 先获取原始图片的宽高，不会将Bitmap加载到内存中，返回null
            BitmapFactory.decodeFile(imageFile.absolutePath, this)
            // 缩放值
            inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)
            inJustDecodeBounds = false
            // 对图片进行缩放，返回Bitmap对象
            BitmapFactory.decodeFile(imageFile.absolutePath, this)
        }
    }


    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        // 解构语法，获取原始图片的宽高
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        // 计算最大的inSampleSize值，该值为2的幂次方，并同时保持这两个值高度和宽度大于请求的高度和宽度。
        // 原始图片的宽高要大于要求的宽高
        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    /**
     * Native层通过libjpeg压缩
     */
    private external fun nativeCompress(
        bitmap: Bitmap,
        quality: Int,
        destFilePath: String,
        optimize: Boolean
    ): Int

    init {
        System.loadLibrary("imagecompressor")
    }
}
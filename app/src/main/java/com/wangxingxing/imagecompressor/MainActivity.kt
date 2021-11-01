package com.wangxingxing.imagecompressor

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.ResourceUtils
import com.bumptech.glide.Glide
import com.wangxingxing.imagecompressor.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : AppCompatActivity() {

    val TYPE_QUALITY = 1
    val TYPE_SIZE = 2
    val TYPE_LOSSLESS = 3

    private lateinit var mPathPhoto1: String
    private lateinit var mPathPhoto2: String
    private lateinit var mPathPhoto3: String

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    private fun init() {
        mPathPhoto1 = "${filesDir.absolutePath + File.separator}photo1.png"
        mPathPhoto2 = "${filesDir.absolutePath + File.separator}photo2.png"
        mPathPhoto3 = "${filesDir.absolutePath + File.separator}photo3.png"

        copyFile()

        initEvent()
    }

    private fun initEvent() {
        binding.apply {
            btnQuality.setOnClickListener {
                showLoading()

                lifecycleScope.launch(Dispatchers.IO) {
                    val outFilePath =
                        "${filesDir.absolutePath + File.separator}IMG_QUALITY_COMPRESS_${System.currentTimeMillis()}.png"
                    Log.i(App.TAG, "outFilePath$outFilePath")
                    val quality = 50
                    val srcFilePath = mPathPhoto2

                    val startTime = System.currentTimeMillis()
                    CompressUtils.qualityCompress(srcFilePath, outFilePath, quality)
                    val useTime = System.currentTimeMillis() - startTime

                    runOnUiThread {
                        dismissLoading()

                        Glide.with(this@MainActivity)
                            .load(srcFilePath)
                            .into(ivCompressBefore)


                        Glide.with(this@MainActivity)
                            .load(outFilePath)
                            .into(ivCompressAfter)

                        showLog(TYPE_QUALITY, quality, useTime, srcFilePath, outFilePath)
                    }
                }
            }

            btnSize.setOnClickListener {
                showLoading()
                lifecycleScope.launch(Dispatchers.IO) {
                    val outFilePath =
                        "${filesDir.absolutePath + File.separator}IMG_SCALE_COMPRESS_${System.currentTimeMillis()}.png"
                    Log.i(App.TAG, "outFilePath$outFilePath")
                    val quality = 100
                    val reqWidth = 500
                    val reqHeight = 300
                    val srcFilePath = mPathPhoto3

                    val startTime = System.currentTimeMillis()
                    CompressUtils.scaleCompress(
                        srcFilePath,
                        outFilePath,
                        quality,
                        reqWidth,
                        reqHeight
                    )
                    val useTime = System.currentTimeMillis() - startTime

                    runOnUiThread {
                        dismissLoading()

                        Glide.with(this@MainActivity)
                            .load(srcFilePath)
                            .into(ivCompressBefore)


                        Glide.with(this@MainActivity)
                            .load(outFilePath)
                            .into(ivCompressAfter)

                        showLog(TYPE_SIZE, quality, useTime, srcFilePath, outFilePath, reqWidth, reqHeight)
                    }
                }
            }

            btnLossless.setOnClickListener {
                showLoading()
                lifecycleScope.launch(Dispatchers.IO) {
                    val outFilePath =
                        "${filesDir.absolutePath + File.separator}IMG_SCALE_COMPRESS_${System.currentTimeMillis()}.png"
                    Log.i(App.TAG, "outFilePath$outFilePath")
                    val srcFilePath = mPathPhoto1
                    val quality = 50

                    val startTime = System.currentTimeMillis()
                    CompressUtils.nativeCompress(srcFilePath, outFilePath, quality)
                    val useTime = System.currentTimeMillis() - startTime

                    runOnUiThread {
                        dismissLoading()

                        Glide.with(this@MainActivity)
                            .load(srcFilePath)
                            .into(ivCompressBefore)


                        Glide.with(this@MainActivity)
                            .load(outFilePath)
                            .into(ivCompressAfter)

                        showLog(TYPE_LOSSLESS, quality, useTime, srcFilePath, outFilePath)
                    }
                }
            }
        }
    }

    private fun copyFile() {
        val file = File(filesDir, "photo1.png")
        if (!FileUtils.isFileExists(file)) {
            ResourceUtils.copyFileFromAssets("photo1.png", mPathPhoto1)
            ResourceUtils.copyFileFromAssets("photo2.png", mPathPhoto2)
            ResourceUtils.copyFileFromAssets("photo3.png", mPathPhoto3)
        }
    }

    private fun showLog(
        type: Int,
        quality: Int,
        useTime: Long,
        srcFilePath: String,
        destFilePath: String,
        reqWidth: Int = 0,
        reqHeight: Int = 0
    ) {
        binding.tvLog.apply {
            text = ""

            val sb = StringBuffer()

            val strType = when (type) {
                TYPE_QUALITY -> "质量压缩"
                TYPE_SIZE -> "尺寸压缩\n压缩尺寸(宽x高)：$reqWidth x $reqHeight"
                TYPE_LOSSLESS -> "无损压缩"
                else -> ""
            }

            sb.appendLine("压缩类型：$strType")
            sb.appendLine("压缩质量：$quality")
            sb.appendLine("压缩耗时：$useTime ms")

            text = sb.toString()

            binding.tvCompressBefore.text =
                "压缩前：$srcFilePath 文件大小：${FileUtils.getSize(srcFilePath)}"
            binding.tvCompressAfter.text =
                "压缩后：$destFilePath 文件大小：${FileUtils.getSize(destFilePath)}"
        }
    }

    private fun showLoading() {
        //避免接口请求返回时间太短，出现loading闪烁问题。500ms以内不会显示loading，显示loading后至少显示500ms。
        binding.loadingProgressBar.show()
    }

    private fun dismissLoading() {
        binding.loadingProgressBar.hide()
    }

}
package com.wangxingxing.imagecompressor

import android.app.Application
import com.blankj.utilcode.util.Utils

/**
 * author : 王星星
 * date : 2021/10/30 15:05
 * email : 1099420259@qq.com
 * description :
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()

        Utils.init(this)
    }

    companion object {
        const val TAG = "wxx"
    }
}
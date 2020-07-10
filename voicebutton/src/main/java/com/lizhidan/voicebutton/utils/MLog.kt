package com.lizhidan.voicebutton.utils

import android.util.Log


/**
 * Log统一管理类
 * Created by lizhidan on 2017/5/2.
 */
class MLog private constructor() {
    companion object {
        var isDebug = false // 是否需要打印bug，可以在application的onCreate函数里面初始化
        private const val TAG = "voicebutton"

        // 下面四个是默认tag的函数
        fun i(msg: String?) {
            if (isDebug) Log.i(TAG, msg)
        }

        fun d(msg: String?) {
            if (isDebug) Log.d(TAG, msg)
        }

        fun e(msg: String?) {
            if (isDebug) Log.e(TAG, msg)
        }

        fun v(msg: String?) {
            if (isDebug) Log.v(TAG, msg)
        }

        // 下面是传入自定义tag的函数
        fun i(tag: String?, msg: String?) {
            if (isDebug) Log.i(tag, msg)
        }

        fun d(tag: String?, msg: String?) {
            if (isDebug) Log.d(tag, msg)
        }

        fun e(tag: String?, msg: String?) {
            if (isDebug) Log.e(tag, msg)
        }

        fun v(tag: String?, msg: String?) {
            if (isDebug) Log.v(tag, msg)
        }
    }

    init {
        /* cannot be instantiated */
        throw UnsupportedOperationException("cannot be instantiated")
    }
}
package com.lizhidan.voicebutton.interfaces

/**
 * MediaRecorder准备完毕回调接口
 */
interface MediaRecorderStateListener {
    /**
     * 设备异常
     */
    fun onError(what: Int, extra: Int)

    /**
     * 设备准备好
     */
    fun wellPrepared()

    /**
     * 停止录音
     *
     * @param filePath 保存路径
     */
    fun onStop(filePath: String?)

    /**
     * 到达最大录音时长
     *
     * @param filePath 保存路径
     */
    fun onReachMaxRecordTime(filePath: String?)
}
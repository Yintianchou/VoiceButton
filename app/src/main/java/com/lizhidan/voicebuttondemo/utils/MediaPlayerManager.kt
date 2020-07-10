package com.lizhidan.voicebuttondemo.utils

import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener

object MediaPlayerManager {
    //播放音频API类：MediaPlayer
    private var mMediaPlayer: MediaPlayer? = null

    //是否暂停
    private var isPause = false

    /**
     * @param filePath：文件路径 onCompletionListener：播放完成监听
     * @description 播放声音
     */
    fun playSound(
        filePath: String?,
        onCompletionListener: OnCompletionListener?
    ) {
        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer()
            mMediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
            //设置一个error监听器
            mMediaPlayer!!.setOnErrorListener { _, _, _ ->
                mMediaPlayer!!.reset()
                false
            }
        } else {
            mMediaPlayer!!.reset()
        }
        try {
            mMediaPlayer!!.setOnCompletionListener(onCompletionListener)
            mMediaPlayer!!.setDataSource(filePath)
            mMediaPlayer!!.prepare()
            mMediaPlayer!!.start()
        } catch (e: Exception) {
        }
    }

    /**
     * @param
     * @description 暂停播放
     */
    fun pause() {
        if (mMediaPlayer != null && mMediaPlayer!!.isPlaying) { //正在播放的时候
            mMediaPlayer!!.pause()
            isPause = true
        }
    }

    /**
     * @param
     * @description 重新播放
     */
    fun resume() {
        if (mMediaPlayer != null && isPause) {
            mMediaPlayer!!.start()
            isPause = false
        }
    }

    /**
     * @param
     * @description 是否在播放
     */
    val isPlaying: Boolean
        get() = if (mMediaPlayer != null) {
            mMediaPlayer!!.isPlaying
        } else false

    /**
     * @param
     * @description 停止操作
     */
    fun stop() {
        if (mMediaPlayer != null && mMediaPlayer!!.isPlaying) {
            mMediaPlayer!!.stop()
        }
    }

    /**
     * @param
     * @description 释放操作
     */
    fun release() {
        if (mMediaPlayer != null) {
            mMediaPlayer!!.stop()
            mMediaPlayer!!.release()
            mMediaPlayer = null
        }
    }
}
package com.lizhidan.voicebutton.utils

import android.media.MediaRecorder
import com.lizhidan.voicebutton.interfaces.MediaRecorderStateListener
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.math.log10

class RecordManager(
    //音频文件存储目录
    private val recordFileDir: String
) {

    //音频文件绝对存储路径
    var recordAbsoluteFileDir: String? = null
        private set

    //通过MediaRecorder实现录音功能
    private var mediaRecorder: MediaRecorder? = null

    // 最大录音时长,默认1000*60（一分钟）
    var maxRecordLength = 1000 * 60

    //录音控件初始化状态标志
    private var isPrepared = false

    //音量分级标准
    private val volumeBase: Long = 600
    private var mediaRecorderStateListener: MediaRecorderStateListener? = null
    fun setMediaRecorderStateListener(mediaRecorderStateListener: MediaRecorderStateListener?) {
        this.mediaRecorderStateListener = mediaRecorderStateListener
    }

    /**
     * 准备录音控件
     */
    fun prepareAudio() {
        try {
            val dir = File(recordFileDir)
            if (!dir.exists()) {
                dir.mkdir()
            }
            //生成随机文件名
            val file =
                File(dir, UUID.randomUUID().toString() + ".amr")
            recordAbsoluteFileDir = file.absolutePath
            // 需要每次使用前重新构造，不然在调用setOutputFile()时会报重用异常
            mediaRecorder?.let {
                mediaRecorder!!.release()
                mediaRecorder = null
            }
            mediaRecorder = MediaRecorder()
            //设置输出文件
            mediaRecorder!!.setOutputFile(recordAbsoluteFileDir)
            mediaRecorder!!.setMaxDuration(maxRecordLength)
            //设置MediaRecorder的音频源为麦克风
            mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
            //设置音频格式
            mediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB)
            //设置音频的格式为amr
            mediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            mediaRecorder!!.setOnInfoListener { mr, what, extra ->
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                    mediaRecorderStateListener?.run {
                        //到达最大录音时间
                        onReachMaxRecordTime(recordAbsoluteFileDir)
                    }
                }
            }
            mediaRecorder!!.setOnErrorListener { mr, what, extra ->
                mediaRecorderStateListener?.run {
                    //录音发生错误
                    onError(what, extra)
                }
            }
            mediaRecorder!!.prepare()
            mediaRecorder!!.start()
            //准备结束,可以开始录音了
            isPrepared = true

            mediaRecorderStateListener?.run {
                wellPrepared()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    //正常录音结束释放资源
    fun release() {
        mediaRecorder?.let {
            try {
                mediaRecorder!!.stop()
                mediaRecorder!!.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            mediaRecorder = null
            isPrepared = false
        }
    }

    //中途取消录音,删除音频文件
    fun cancel() {
        release()
        recordAbsoluteFileDir?.let {
            val file = File(it)
            file.delete()
            recordAbsoluteFileDir = null
        }
    }

    /**
     * 根据音量分级更新麦克状态
     */
    fun getVoiceLevel(maxLevel: Int): Int {
        if (isPrepared && mediaRecorder != null) {
            val ratio = mediaRecorder!!.maxAmplitude.toDouble() / volumeBase
            var db = 0.0 // 分贝
            if (ratio > 1) {
                db = 20 * log10(ratio)
                return if (db / 4 > maxLevel) {
                    maxLevel
                } else (db / 4).toInt()
            }
        }
        return 0
    }

}

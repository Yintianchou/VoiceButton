package com.lizhidan.voicebutton

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatButton
import com.lizhidan.voicebutton.interfaces.MediaRecorderStateListener
import com.lizhidan.voicebutton.utils.MLog
import com.lizhidan.voicebutton.utils.RecordDialogManager
import com.lizhidan.voicebutton.utils.RecordManager
import java.io.File


class VoiceButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.buttonStyle
) :
    AppCompatButton(context, attrs, defStyleAttr) {
    //当前状态
    private var currentState = STATE_NORMAL
    private lateinit var recordDialogManager: RecordDialogManager
    private lateinit var recordManager: RecordManager
    private val recorderStateListener: RecorderStateListener
    private var audioManager: AudioManager
    private lateinit var mFocusRequest: AudioFocusRequest
    private lateinit var mAudioAttributes: AudioAttributes
    private var audioFocusChangeListener: AudioManager.OnAudioFocusChangeListener

    //录音中标识
    private var isRecording = false

    //录音时间
    private var recordTime: Long = 0

    fun setMaxRecordLength(time: Int) {
        recordManager?.let {
            it.maxRecordLength = time
        }
    }

    //采样间隔时间
    private val samplingInterval: Long = 200

    //最短录音时长
    private val minRecordTime: Long = 600
    private val mHandler = Handler()

    /**
     * 更新话筒状态
     */
    private val mUpdateMicStatusTimer = Runnable {
        while (isRecording) {
            try {
                Thread.sleep(samplingInterval)
                recordTime += samplingInterval
                mHandler.post(updateDialogRunable)
                if (recordManager.maxRecordLength - recordTime < 10 * 1000) {
                    //剩余录音时间小于10秒时显示剩余录制时间提示
                    if (currentState != STATE_WANT_TO_CANCEL) {
                        mHandler.post(updateRemainingTimeRunable)
                    }
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }
    private val updateDialogRunable = Runnable { //时时更新音量显示
        recordDialogManager.updateVoiceLevel(recordManager.getVoiceLevel(6))
    }
    private val updateRemainingTimeRunable = Runnable { //时时更新剩余录音时间显示
        recordDialogManager.updateRemainingTime((recordManager.maxRecordLength - recordTime).toInt() / 1000)
    }
    private val dismissDialogRunable = Runnable { recordDialogManager.dimissDialog() }

    /**
     * 录音完成后的回调接口
     */
    interface RecorderListener {
        //开始录音
        fun onStart()

        //录音时长和文件保存路径
        fun onFinish(time: Long, filePath: String?)
    }

    private var recorderListener: RecorderListener? = null
    fun setRecorderListener(recorderListener: RecorderListener?) {
        this.recorderListener = recorderListener
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        //获取手势类型及触摸位置坐标
        val action = event.action
        val y = event.y
        when (action) {
            MotionEvent.ACTION_DOWN -> setText(R.string.str_recorder_recording)
            MotionEvent.ACTION_MOVE -> if (isRecording) {
                //根据x,y的坐标判断是否需要取消
                if (wantToCancel(y)) {
                    changeState(STATE_WANT_TO_CANCEL)
                } else {
                    changeState(STATE_RECORDING)
                }
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                //如果longclick(录音)操作没触发
                if (currentState == STATE_NORMAL) {
                    MLog.e(TAG, "未触发录音或者录音达到最大时长已经结束")
                    releaseAudioFocus()
                    setText(R.string.str_recorder_normal)
                    resetRecordState()
                    return super.onTouchEvent(event)
                }
                //触发了onlongclick(录音)操作，但录音控件没有准备好（即prepare()、start()方法未执行完）或者录音时间过短所以删除文件夹
                if (currentState == STATE_PREPAREING || recordTime < minRecordTime) {
                    MLog.e(TAG, "未初始化完成或者录音太短")
                    releaseAudioFocus()
                    recordDialogManager.showRocordTooShortDialog()
                    recordManager.cancel()
                    mHandler.postDelayed(dismissDialogRunable, 1000)
                } else if (currentState == STATE_RECORDING) {
                    MLog.e(TAG, "正常录制结束")
                    releaseAudioFocus()
                    //正常录制结束
                    recordManager.release()
                    recordDialogManager.dimissDialog()
                    if (recorderListener != null) {
                        recorderListener!!.onFinish(
                            recordTime,
                            recordManager.recordAbsoluteFileDir
                        )
                    }
                } else if (currentState == STATE_WANT_TO_CANCEL) {
                    MLog.e(TAG, "滑动取消了录制")
                    releaseAudioFocus()
                    //想要取消录制
                    recordManager.cancel()
                    recordDialogManager.dimissDialog()
                }
                resetRecordState()
            }
        }
        return super.onTouchEvent(event)
    }

    //判断是否想取消录制
    private fun wantToCancel(y: Float): Boolean {
        //如果上下滑出 button  加上我们自定义的距离
        return y < -DISTANCE_Y_CANCEL
    }

    //改变状态
    private fun changeState(state: Int) {
        if (currentState != state) {
            currentState = state
            when (state) {
                STATE_NORMAL -> setText(R.string.str_recorder_normal)
                STATE_PREPAREING -> recordDialogManager.showPrepareDialog()
                STATE_RECORDING -> {
                    setText(R.string.str_recorder_recording)
                    if (isRecording) {
                        recordDialogManager.showRecordingDialog()
                    }
                }
                STATE_WANT_TO_CANCEL -> {
                    setText(R.string.str_recorder_want_cancel)
                    recordDialogManager.showCancelRecording()
                }
            }
        }
    }

    /**
     * 恢复录音状态、标志位
     */
    private fun resetRecordState() {
        changeState(STATE_NORMAL)
        isRecording = false
        recordTime = 0
    }

    private inner class RecorderStateListener : MediaRecorderStateListener {
        override fun onError(what: Int, extra: Int) {
            MLog.e(TAG, "录音出错了=====>what:$what,extra:$extra")
        }

        override fun wellPrepared() {
            recorderListener?.run {
                requestAudioFocus()
                onStart()
            }
            isRecording = true
            changeState(STATE_RECORDING)
            //开启线程实时更新音量
            Thread(mUpdateMicStatusTimer).start()
        }

        override fun onStop(filePath: String?) {
            MLog.e(TAG, "录音停止了=====>filePath:$filePath")
        }

        override fun onReachMaxRecordTime(filePath: String?) {
            //到达最大录制时间录制结束
            recordManager.release()
            recordDialogManager.dimissDialog()
            if (recorderListener != null) {
                recorderListener!!.onFinish(recordTime, filePath)
            }
            resetRecordState()
        }
    }

    companion object {
        private const val TAG = "VoiceButton"

        //手指滑动指定距离后判定取消录音
        private const val DISTANCE_Y_CANCEL = 100f

        //录音状态
        private const val STATE_NORMAL = 1 //默认状态
        private const val STATE_PREPAREING = 2 //录音控件准备状态
        private const val STATE_RECORDING = 3 //录制
        private const val STATE_WANT_TO_CANCEL = 4 //取消录制
    }

    /**
     * 构造函数
     *
     * @param context
     */
    init {
        recordDialogManager = RecordDialogManager(context)
        recorderStateListener = RecorderStateListener()
        audioManager =
            context.applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mAudioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
            mFocusRequest =
                AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
                    .setAudioAttributes(mAudioAttributes)
                    .setOnAudioFocusChangeListener(audioFocusChangeListener)
                    .build();
        }
        var saveFile: File? = null
        /* 判断sd的外部设置状态是否可以读写 */saveFile =
            if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                File(context.getExternalFilesDir(null), "recorder_audios")
            } else {
                File(context.filesDir, "recorder_audios")
            }
        if (!saveFile.exists()) {
            saveFile.mkdirs()
        }
        recordManager = RecordManager(saveFile.path)
        recordManager.setMediaRecorderStateListener(recorderStateListener)
        //按钮长按准备录音包括start
        setOnLongClickListener {
            changeState(STATE_PREPAREING)
            recordManager.prepareAudio()
            false
        }
        setText(R.string.str_recorder_normal)
    }

    fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.requestAudioFocus(mFocusRequest);
        } else {
            audioManager.requestAudioFocus(
                audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE
            );
        }
    }

    fun releaseAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.abandonAudioFocusRequest(mFocusRequest);
        } else {
            audioManager.abandonAudioFocus(audioFocusChangeListener);
        }
    }
}
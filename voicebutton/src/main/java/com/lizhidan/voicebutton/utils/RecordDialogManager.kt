package com.lizhidan.voicebutton.utils

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.lizhidan.voicebutton.R

//录音dialog管理类
class RecordDialogManager(private val mContext: Context) {
    private var mDialog: AlertDialog? = null

    //录音麦克风标志
    private var mIcon: ImageView? = null

    //录音音量标志
    private var mVoice: ImageView? = null

    //录音提示
    private var mLable: TextView? = null

    //准备录音标志
    private var mProgressBar: ProgressBar? = null

    //显示准备录音dialog
    fun showPrepareDialog() {
        if (mDialog == null) {
            val builder =
                AlertDialog.Builder(mContext, R.style.Theme_RecordDialog)
            val inflater = LayoutInflater.from(mContext)
            val view = inflater.inflate(R.layout.dialog_recorder, null)
            mIcon = view.findViewById(R.id.iv_recorder_icon)
            mVoice = view.findViewById(R.id.iv_recorder_voice)
            mLable = view.findViewById(R.id.tv_recorder_label)
            mProgressBar = view.findViewById(R.id.progressBar)
            mIcon!!.visibility = View.GONE
            mVoice!!.visibility = View.GONE
            mLable!!.visibility = View.GONE
            mProgressBar!!.visibility = View.VISIBLE
            builder.setView(view)
            mDialog = builder.create()
            mDialog!!.setCancelable(false)
            mDialog!!.setCanceledOnTouchOutside(false)
            mDialog!!.show()
        }
    }

    //显示录音dialog
    fun showRecordingDialog() {
        if (mDialog != null && mDialog!!.isShowing) {
            mIcon!!.setImageResource(R.drawable.ic_recorder)
            mIcon!!.visibility = View.VISIBLE
            mVoice!!.visibility = View.VISIBLE
            mLable!!.visibility = View.VISIBLE
            mProgressBar!!.visibility = View.INVISIBLE
            mLable!!.setText(R.string.str_recorder_swip_cancel)
            mLable!!.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    //想要取消
    fun showCancelRecording() {
        if (mDialog != null && mDialog!!.isShowing) {
            mIcon!!.setImageResource(R.drawable.record_cancel)
            mIcon!!.visibility = View.VISIBLE
            mVoice!!.visibility = View.GONE
            mLable!!.setText(R.string.str_recorder_want_cancel)
            mLable!!.setBackgroundResource(R.color.colorDarkOrange)
            mLable!!.visibility = View.VISIBLE
            mProgressBar!!.visibility = View.INVISIBLE
        }
    }

    //录音时间太短
    fun showRocordTooShortDialog() {
        if (mDialog != null && mDialog!!.isShowing) {
            mIcon!!.setImageResource(R.drawable.voice_to_short)
            mLable!!.setText(R.string.str_recorder_too_short)
            mIcon!!.visibility = View.VISIBLE
            mVoice!!.visibility = View.GONE
            mLable!!.visibility = View.VISIBLE
        }
    }

    //关闭dialog
    fun dimissDialog() {
        if (mDialog != null && mDialog!!.isShowing) {
            mDialog!!.dismiss()
            mDialog = null
        }
    }

    /**
     * 通过level更新voice上的图片
     *
     * @param level
     */
    fun updateVoiceLevel(level: Int) {
        if (mDialog != null && mDialog!!.isShowing) {
            val resId = mContext.resources
                .getIdentifier("volume_$level", "drawable", mContext.packageName)
            mVoice!!.setImageResource(resId)
        }
    }

    /**
     * 开启倒计时通知
     *
     * @param time
     */
    fun updateRemainingTime(time: Int) {
        if (mDialog != null && mDialog!!.isShowing) {
            mLable!!.visibility = View.VISIBLE
            mLable!!.text = String.format(
                mContext.resources.getString(R.string.time_remaining), time + 1
            )
        }
    }

}

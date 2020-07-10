package com.lizhidan.voicebuttondemo

import android.Manifest
import android.graphics.drawable.AnimationDrawable
import android.media.MediaPlayer.OnCompletionListener
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lizhidan.voicebutton.VoiceButton
import com.lizhidan.voicebutton.VoiceButton.RecorderListener
import com.lizhidan.voicebuttondemo.adapter.RecoderAdapter
import com.lizhidan.voicebuttondemo.bean.RecorderInfo
import com.lizhidan.voicebuttondemo.utils.MediaPlayerManager.isPlaying
import com.lizhidan.voicebuttondemo.utils.MediaPlayerManager.playSound
import com.lizhidan.voicebuttondemo.utils.MediaPlayerManager.release
import com.lizhidan.voicebuttondemo.utils.MediaPlayerManager.stop
import java.util.*
import permissions.dispatcher.PermissionRequest
import permissions.dispatcher.ktx.withPermissionsCheck

class MainActivity : AppCompatActivity() {
    private lateinit var vbRecord: VoiceButton
    private lateinit var rvVoice: RecyclerView
    private var recoderAdapter: RecoderAdapter? = null
    private var lastPlayedPosition = -1
    private var voiceAnim: View? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
        //检查录音权限
        checkRecordAudioPermission()
    }

    private fun checkRecordAudioPermission() = withPermissionsCheck(
        Manifest.permission.RECORD_AUDIO,
        onShowRationale = ::onRecordAudioShowRationale,
        onPermissionDenied = ::onRecordAudioDenied,
        onNeverAskAgain = ::onRecordAudioNeverAskAgain
    ) {
    }

    private fun onRecordAudioDenied() {
        Toast.makeText(this, "拒绝了录音权限", Toast.LENGTH_SHORT).show()
    }

    private fun onRecordAudioShowRationale(request: PermissionRequest) {
        request.proceed()
    }

    private fun onRecordAudioNeverAskAgain() {
        Toast.makeText(this, "拒绝了录音权限并且不再提醒", Toast.LENGTH_SHORT).show()
    }

    private fun initView() {
        vbRecord = findViewById(R.id.vb_record)
        vbRecord.setMaxRecordLength(20 * 1000)
        vbRecord.setRecorderListener(object : RecorderListener {
            override fun onStart() {
                Log.d(TAG, "开始（触发）本次录音，可能会因为录音时间太短取消本次录音")
            }

            override fun onFinish(
                time: Long,
                filePath: String?
            ) {
                Log.d(TAG, "完成了本次录音")
                recoderAdapter!!.addData(RecorderInfo(time, filePath!!))
            }
        })
        rvVoice = findViewById(R.id.rv_voice)
        rvVoice.setLayoutManager(LinearLayoutManager(this@MainActivity))
        recoderAdapter = RecoderAdapter(this@MainActivity, ArrayList())
        recoderAdapter!!.setOnItemClickListener(object :
            RecoderAdapter.OnItemClickListener {
            override fun onItemClick(view: View?, position: Int) {
                if (lastPlayedPosition == position && isPlaying) {
                    //停止播放
                    stop()
                    voiceAnim!!.setBackgroundResource(R.drawable.voice)
                } else {
                    //播放语音
                    // 声音播放动画
                    if (voiceAnim != null) {
                        voiceAnim!!.setBackgroundResource(R.drawable.voice)
                        voiceAnim = null
                    }
                    voiceAnim = view!!.findViewById(R.id.v_voice_anim)
                    voiceAnim?.setBackgroundResource(R.drawable.voice_anim)
                    val animation =
                        voiceAnim?.getBackground() as AnimationDrawable
                    animation.start()
                    // 播放录音
                    playSound(
                        recoderAdapter!!.getItem(position).filePath,
                        OnCompletionListener { //播放完成后修改图片
                            voiceAnim?.setBackgroundResource(R.drawable.voice)
                        })
                    lastPlayedPosition = position
                }
            }
        })
        rvVoice.setAdapter(recoderAdapter)
    }

    override fun onDestroy() {
        super.onDestroy()
        release()
    }

    companion object {
        private const val TAG = "MainActivity"
    }


}
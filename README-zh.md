![](https://upload-images.jianshu.io/upload_images/20262249-7fcb9c3a8b5a1c78.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

一个自定义录制语音的按钮控件，kotlin实现，继承AppCompatButton可使用其所有属性。

#####使用效果图：
![](https://upload-images.jianshu.io/upload_images/20262249-dddbe7911fb6e387.gif?imageMogr2/auto-orient/strip)
#####使用方法
1.引入
```
implementation 'com.lizhidan.voicebutton:voicebutton:1.0.0'
```
2.布局文件中定义控件
```
<com.lizhidan.voicebutton.VoiceButton
        android:id="@+id/vb_record"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="长按录音" />
```
3.代码中使用
```
private lateinit var vbRecord: VoiceButton
...
vbRecord = findViewById(R.id.vb_record)
        vbRecord.setMaxRecordLength(20 * 1000)//自定义录音最大长度，默认60秒
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
```
>具体可参考项目中的[示例](https://github.com/Yintianchou/VoiceButton/tree/master/app/src/main/java/com/lizhidan/voicebuttondemo)
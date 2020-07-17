![](https://upload-images.jianshu.io/upload_images/20262249-7fcb9c3a8b5a1c78.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

A custom button control for recording voice,written by kotlin,extends AppCompatButton can use all its properties.

##Effect
![](https://upload-images.jianshu.io/upload_images/20262249-dddbe7911fb6e387.gif?imageMogr2/auto-orient/strip)
##Background
- androidx

##Install
```
implementation 'com.lizhidan.voicebutton:voicebutton:1.0.0'
```
##Usage
1. Define widget in the layout file
```
<com.lizhidan.voicebutton.VoiceButton
        android:id="@+id/vb_record"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Long press to record" />
```
2. Use in code
```
private lateinit var vbRecord: VoiceButton
...
vbRecord = findViewById(R.id.vb_record)
//Customize the maximum length of the recording, the default is 60 seconds
vbRecord.setMaxRecordLength(20 * 1000)
vbRecord.setRecorderListener(object : RecorderListener {
    override fun onStart() {
        Log.d(TAG, "Start (trigger) this recording, the recording may be canceled because the recording time is too short")
    }

    override fun onFinish(
            time: Long,
            filePath: String?
    ) {
        Log.d(TAG, "Completed this recording")
        recoderAdapter!!.addData(RecorderInfo(time, filePath!!))
    }
})
```
##Example
Please check the [sample code](https://github.com/Yintianchou/VoiceButton/tree/master/app/src/main/java/com/lizhidan/voicebuttondemo)
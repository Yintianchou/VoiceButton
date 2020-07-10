package com.lizhidan.voicebuttondemo.bean

/**
 * @description 录音实体类
 */
class RecorderInfo(
    //时间长度
    var time: Long,
    //文件路径
    var filePath: String
) {

    override fun toString(): String {
        return "RecorderInfo{" +
                "time=" + time +
                ", filePath='" + filePath + '\'' +
                '}'
    }

}

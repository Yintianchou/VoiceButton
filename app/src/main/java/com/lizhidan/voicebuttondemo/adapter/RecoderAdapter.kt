package com.lizhidan.voicebuttondemo.adapter

import android.content.Context
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.lizhidan.voicebuttondemo.R
import com.lizhidan.voicebuttondemo.bean.RecorderInfo
import kotlin.math.roundToInt


/**
 * @description 数据适配器
 */
class RecoderAdapter(
    private val mContext: Context,
    mDatas: MutableList<RecorderInfo>
) :
    RecyclerView.Adapter<RecoderAdapter.RecordHolder>() {
    private val mDatas: MutableList<RecorderInfo>

    //item的最小宽度
    private val mMinWidth: Int

    //item的最大宽度
    private val mMaxWidth: Int
    private val mInflater: LayoutInflater

    interface OnItemClickListener {
        fun onItemClick(view: View?, position: Int)
    }

    private var onItemClickListener: OnItemClickListener? = null
    fun setOnItemClickListener(onItemClickListener: OnItemClickListener?) {
        this.onItemClickListener = onItemClickListener
    }

    inner class RecordHolder(itemView: View) : ViewHolder(itemView) {
        val llRecoderLength: LinearLayout
        val tvVoiceDuration: TextView

        init {
            tvVoiceDuration = itemView.findViewById(R.id.tv_voice_duration)
            llRecoderLength = itemView.findViewById(R.id.ll_recoder_length)
            llRecoderLength.setOnClickListener { v ->
                if (onItemClickListener != null) {
                    onItemClickListener!!.onItemClick(v, adapterPosition)
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecordHolder {
        var view: View? = null
        var holder: RecordHolder? = null
        view = mInflater.inflate(R.layout.item_recoder, parent, false)
        holder = RecordHolder(view!!)
        return holder
    }

    override fun onBindViewHolder(
        holder: RecordHolder,
        position: Int
    ) {
        holder.tvVoiceDuration.text = String.format(
            mContext.resources.getString(R.string.voice_duration),
            (mDatas[position].time.toFloat() / 1000).roundToInt()
        )
        val lp = holder.llRecoderLength.layoutParams
        lp.width =
            (mMinWidth + (mMaxWidth / 60f) * (mDatas[position].time.toFloat() / 1000).roundToInt()).toInt()
    }

    override fun getItemCount(): Int {
        return mDatas.size
    }

    fun getItem(positon: Int): RecorderInfo {
        return mDatas[positon]
    }

    fun updateData(infos: List<RecorderInfo>?) {
        mDatas.clear()
        mDatas.addAll(infos!!)
        notifyDataSetChanged()
    }

    fun addData(info: RecorderInfo) {
        mDatas.add(info)
        notifyDataSetChanged()
    }

    init {
        this.mDatas = mDatas
        mInflater = LayoutInflater.from(mContext)
        //获取屏幕的宽度
        val wm =
            mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val outMetrics = DisplayMetrics()
        wm.defaultDisplay.getMetrics(outMetrics)
        //最大宽度为屏幕宽度的百分之六十
        mMaxWidth = (outMetrics.widthPixels * 0.6f).toInt()
        //最小宽度为屏幕宽度的百分之二十
        mMinWidth = (outMetrics.widthPixels * 0.2f).toInt()
    }
}
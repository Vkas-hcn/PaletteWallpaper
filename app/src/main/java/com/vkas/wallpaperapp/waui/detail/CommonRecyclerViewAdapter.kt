package com.vkas.wallpaperapp.waui.detail

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.jeremyliao.liveeventbus.LiveEventBus
import com.vkas.wallpaperapp.R
import com.vkas.wallpaperapp.wabean.WallpaperBean
import com.vkas.wallpaperapp.waevent.Constant
import java.util.ArrayList
import com.vkas.wallpaperapp.wautils.KLog


class CommonRecyclerViewAdapter(context: Context) :
    RecyclerView.Adapter<CommonRecyclerViewAdapter.ViewHolder>() {
    private var mItems: MutableList<Data>? = ArrayList()
    private var mOnItemClickListener: AdapterView.OnItemClickListener? = null
    private lateinit var waWallpaperBean: WallpaperBean
    lateinit var waWallpaperBeanList: MutableList<WallpaperBean>
    var contexts = context

    fun generateDatas(count: Int): List<Data>? {
        getServerListData()
        val mDatas: MutableList<Data> = ArrayList()
        for (i in 0 until count) {
            mDatas.add(Data(waWallpaperBeanList[i].wallpaperPath!!))
        }
        return mDatas
    }

    fun addItem(position: Int) {
        if (position > mItems!!.size) {
            return
        }
        mItems!!.add(position, Data(position.toString()))
        notifyItemInserted(position)
    }

    fun removeItem(position: Int) {
        if (position >= mItems!!.size) {
            return
        }
        mItems!!.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val inflater = LayoutInflater.from(viewGroup.context)
        val root: View =
            inflater.inflate(R.layout.adapter_item_detail, viewGroup, false)
        return ViewHolder(root, this, contexts)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        val data = mItems!![i]
        viewHolder.setText(data.text)
        LiveEventBus.get<String>(Constant.SLIDE_WALLPAPER_SUBSCRIPT)
            .post(data.text)
        KLog.e("TAG","LiveEventBus--i->${i}")
        KLog.e("TAG","LiveEventBus--text->${data.text}")
    }

    override fun getItemCount(): Int {
        return mItems!!.size
    }

    fun setItemCount(count: Int) {
        mItems!!.clear()
        mItems!!.addAll(generateDatas(count)!!)
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(onItemClickListener: AdapterView.OnItemClickListener?) {
        mOnItemClickListener = onItemClickListener
    }

    private fun onItemHolderClick(itemHolder: RecyclerView.ViewHolder, position: Int) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener!!.onItemClick(
                null, itemHolder.itemView,
                itemHolder.adapterPosition, itemHolder.itemId
            )
        }
    }

    class Data(var text: String)
    class ViewHolder(itemView: View, adapter: CommonRecyclerViewAdapter, contexts: Context) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private val mTextView: ImageView
        private val mAdapter: CommonRecyclerViewAdapter
        private var activity: Context
        fun setText(text: String?) {
            val goodsIcon: Int = activity.resources
                .getIdentifier(text, "mipmap", activity.packageName)
            mTextView.setImageResource(goodsIcon)
        }
        override fun onClick(v: View) {
        }

        init {
            itemView.setOnClickListener(this)
            mAdapter = adapter
            activity = contexts
            mTextView = itemView.findViewById(R.id.img_detail)
        }
    }

    /**
     * 获取服务器列表
     */
    fun getServerListData() {
        waWallpaperBeanList = ArrayList()
        getWallpaperList().forEachIndexed { index, it ->
            waWallpaperBean = WallpaperBean()
            waWallpaperBean.wallpaperPath = getWallpaperList()[index]
            waWallpaperBeanList.add(waWallpaperBean)
        }
    }

    private fun getWallpaperList2(): MutableList<String> {
        return listOf(
            "wa_1",
            "wa_2",
            "wa_3",
            "wa_4",
            "wa_5",
            "wa_6",
            "wa_7",
            "wa_8",
            "wa_9",
            "wa_10"
        ) as MutableList<String>
    }

    private fun getWallpaperList(): MutableList<String> {
        return listOf(
            "wa_1",
            "wa_2",
            "wa_3",
            "wa_4",
            "wa_5",
            "wa_6",
            "wa_7",
            "wa_8",
            "wa_9",
            "wa_10",
            "wa_11",
            "wa_12",
            "wa_13",
            "wa_14",
            "wa_15",
            "wa_16",
            "wa_17",
            "wa_18",
            "wa_19",
            "wa_20",
            "wa_21",
            "wa_22",
            "wa_23",
            "wa_24",
            "wa_25",
            "wa_26",
            "wa_27",
            "wa_28",
            "wa_29",
            "wa_30",
            "wa_31",
            "wa_32",
            "wa_33",
            "wa_34",
            "wa_35",
            "wa_36",
            "wa_37",
            "wa_38",
            "wa_39",
            "wa_40",
            "wa_41",
            "wa_42",
            "wa_43",
            "wa_44",
            "wa_45",
            "wa_46",
            "wa_47",
            "wa_48",
            "wa_49",
            "wa_50",
            "wa_51",
            "wa_52",
            "wa_53",
            "wa_54",
            "wa_55",
            "wa_56",
            "wa_57",
            "wa_58",
            "wa_59"
        ) as MutableList<String>
    }
}
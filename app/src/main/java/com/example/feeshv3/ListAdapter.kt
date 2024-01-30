package com.example.feeshv3

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.feeshv3.data.Record
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer


class ListAdapter: RecyclerView.Adapter<ListAdapter.MyViewHolder>() {

    private var recordList = emptyList<Record>()
    private var onItemLongClickListener: ((Record) -> Unit)? = null

    class MyViewHolder(itemView: android.view.View): RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.custom_roww,parent,false))
    }

    override fun getItemCount(): Int {
        return recordList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = recordList[position]
        holder.itemView.findViewById<TextView>(R.id.tempC).text = currentItem.temperature.toString()
        holder.itemView.findViewById<TextView>(R.id.salineDB).text = currentItem.salinity.toString()
        holder.itemView.findViewById<TextView>(R.id.pHDB).text = currentItem.pH.toString()
        holder.itemView.findViewById<TextView>(R.id.dODB).text = currentItem.dissolved.toString()
        holder.itemView.findViewById<TextView>(R.id.textView9).text = currentItem.time

        holder.itemView.setOnLongClickListener {
            onItemLongClickListener?.let { listener ->
                listener(recordList[position])
            }
            true
        }

    }

    fun setData(records: List<Record>) {
        this.recordList = records
        notifyDataSetChanged()
    }

    fun setOnItemLongClickListener(listener: (Record) -> Unit) {
        onItemLongClickListener = listener
    }
}
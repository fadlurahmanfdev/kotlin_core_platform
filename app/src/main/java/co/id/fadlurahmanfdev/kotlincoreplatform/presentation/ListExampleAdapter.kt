package co.id.fadlurahmanfdev.kotlincoreplatform.presentation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import co.id.fadlurahmanfdev.kotlincoreplatform.R
import co.id.fadlurahmanfdev.kotlincoreplatform.data.FeatureModel

class ListExampleAdapter : RecyclerView.Adapter<ListExampleAdapter.ViewHolder>() {
    val items: ArrayList<FeatureModel> = arrayListOf()
    private lateinit var callback: Callback

    fun setCallback(callback: Callback) {
        this.callback = callback
    }

    fun setList(items: List<FeatureModel>) {
        this.items.clear()
        this.items.addAll(items)
        notifyItemRangeInserted(0, items.size)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tvFeatureTitle)
        val desc: TextView = view.findViewById(R.id.tvFeatureDesc)
        val main: LinearLayout = view.findViewById(R.id.llMain)

        init {
            main.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val clickedItem = items[position]
                    callback.onClicked(clickedItem)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_feature, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.title.text = item.title
        holder.desc.text = item.desc ?: "-"
    }

    interface Callback {
        fun onClicked(item: FeatureModel)
    }
}
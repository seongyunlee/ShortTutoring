package org.softwaremaestro.presenter.question_upload.adapter

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.softwaremaestro.presenter.databinding.ItemQuestionFormImageBinding

class FormImageAdapter() :
    RecyclerView.Adapter<FormImageAdapter.ViewHolder>() {

    private var items: List<Bitmap> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FormImageAdapter.ViewHolder {
        val view =
            ItemQuestionFormImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: FormImageAdapter.ViewHolder, position: Int) {
        if (position < items.size) {
            holder.onBind(items[position])
        }
        holder.onBind(null)
    }

    override fun getItemCount(): Int {
        return items.size + 1
    }

    fun setItem(items: List<Bitmap>) {
        this.items = items
    }

    inner class ViewHolder(private val binding: ItemQuestionFormImageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(item: Bitmap?) {
            if (item != null) {
                binding.ivImage.setImageBitmap(item)
            } else {
                binding.tvAddImageDesc.visibility = ViewGroup.VISIBLE
            }
        }
    }

}
package org.softwaremaestro.presenter.teacher_search.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.softwaremaestro.domain.teacher_get.entity.TeacherVO
import org.softwaremaestro.presenter.databinding.ItemTeacherBinding
import org.softwaremaestro.presenter.util.toRating
import java.lang.Integer.min

class TeacherAdapter(
    private val itemCountLimit: Int? = null,
    private val onItemClickListener: (String) -> Unit
) :
    RecyclerView.Adapter<TeacherAdapter.ViewHolder>() {

    private var items: List<TeacherVO> = emptyList()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TeacherAdapter.ViewHolder {

        val binding = ItemTeacherBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)

    }

    override fun onBindViewHolder(holder: TeacherAdapter.ViewHolder, position: Int) {
        holder.onBind(items[position])
    }

    override fun getItemCount(): Int {
        return if (itemCountLimit == null) items.size
        else min(itemCountLimit, items.size)
    }

    fun setItem(items: List<TeacherVO>) {
        this.items = items
    }

    inner class ViewHolder(private val binding: ItemTeacherBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(item: TeacherVO) {
            with(binding) {
                Glide.with(binding.root).load(item.profileUrl).centerCrop().into(ivTeacherImg)
                item.nickname?.let { tvTeacherName.text = it }
                item.univ?.let { tvTeacherUniv.text = it }
                item.bio?.let { tvTeacherBio.text = it }
                item.rating?.let { tvTeacherRating.text = it.toRating() }
                btnFollow.text = "찜한 학생 ${item.followers?.size ?: 0}"
            }

            itemView.setOnClickListener {
                item.teacherId?.let { onItemClickListener(it) }
            }
        }
    }
}
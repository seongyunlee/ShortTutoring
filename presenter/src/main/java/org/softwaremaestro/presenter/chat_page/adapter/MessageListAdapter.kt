package org.softwaremaestro.presenter.chat_page.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.softwaremaestro.domain.chat.entity.MessageBodyVO
import org.softwaremaestro.domain.chat.entity.MessageVO
import org.softwaremaestro.presenter.R
import org.softwaremaestro.presenter.databinding.ItemChatButtonsBinding
import org.softwaremaestro.presenter.databinding.ItemChatQuestionBinding
import org.softwaremaestro.presenter.databinding.ItemChatTextBinding
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class MessageListAdapter(
    private val isTeacher: Boolean,
    private val onBtn1Click: () -> Unit,
    private val onBtn2Click: () -> Unit,
    private val onImageClick: () -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items: List<MessageVO> = emptyList()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_BUTTONS -> {
                val view = ItemChatButtonsBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ButtonsViewHolder(view)
            }

            TYPE_QUESTION -> {
                val view = ItemChatQuestionBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                QuestionViewHolder(view)
            }

            else -> {
                val view = ItemChatTextBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                TextViewHolder(view)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position].bodyVO) {
            is MessageBodyVO.Text -> TYPE_TEXT
            is MessageBodyVO.ProblemImage -> TYPE_QUESTION
            is MessageBodyVO.AppointRequest -> TYPE_TEXT
            is MessageBodyVO.RequestDecline -> TYPE_TEXT
            is MessageBodyVO.ReserveConfirm -> TYPE_TEXT
            is MessageBodyVO.TutoringFinished -> TYPE_TEXT
            else -> TYPE_TEXT
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder.setIsRecyclable(false)
        when (holder.itemViewType) {
            TYPE_BUTTONS -> {
                (holder as ButtonsViewHolder).onBind(items[position])
            }

            TYPE_QUESTION -> {
                (holder as QuestionViewHolder).onBind(items[position])
            }

            else -> {
                (holder as TextViewHolder).onBind(items[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun setItem(items: List<MessageVO>) {
        this.items = items
        notifyDataSetChanged()
    }

    inner class TextViewHolder(private val binding: ItemChatTextBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(item: MessageVO) {
            Log.d("message adapter", "Item ${item}")
            binding.apply {
                if (item.isMyMsg) {
                    //set colors
                    containerBody.backgroundTintList =
                        root.context.getColorStateList(R.color.primary_blue)
                    tvText.setTextColor(root.context.getColor(R.color.white))
                    //set position to right
                    ConstraintSet().apply {
                        clone(containerRoot)
                        connect(
                            containerBody.id,
                            ConstraintSet.RIGHT,
                            containerRoot.id,
                            ConstraintSet.RIGHT
                        )
                        clear(containerBody.id, ConstraintSet.LEFT)
                        applyTo(containerRoot)
                    }
                    ConstraintSet().apply {
                        clone(root)
                        connect(
                            tvTime.id,
                            ConstraintSet.RIGHT,
                            containerBody.id,
                            ConstraintSet.LEFT
                        )
                        clear(tvTime.id, ConstraintSet.LEFT)
                        applyTo(root)
                    }
                } else {
                    //set colors
                    containerBody.backgroundTintList = root.context.getColorStateList(R.color.white)
                    tvText.setTextColor(root.context.getColor(R.color.black))
                    //set position to left
                    ConstraintSet().apply {
                        clone(root)
                        connect(
                            containerBody.id,
                            ConstraintSet.LEFT,
                            root.id,
                            ConstraintSet.LEFT
                        )
                        clear(containerBody.id, ConstraintSet.RIGHT)
                        applyTo(root)
                    }
                    ConstraintSet().apply {
                        clone(root)
                        connect(
                            tvTime.id,
                            ConstraintSet.LEFT,
                            containerBody.id,
                            ConstraintSet.RIGHT
                        )
                        clear(tvTime.id, ConstraintSet.RIGHT)
                        applyTo(root)
                    }
                }
                tvTime.text = "${item.time.dayOfMonth}일 ${item.time.hour}:${
                    String.format(
                        "%02d",
                        item.time.minute
                    )
                }"
                when (item.bodyVO) {
                    is MessageBodyVO.Text -> {
                        var body = item.bodyVO as MessageBodyVO.Text
                        tvText.text = body.text
                    }

                    is MessageBodyVO.AppointRequest -> {
                        var body = item.bodyVO as MessageBodyVO.AppointRequest
                        var time =
                            body.startDateTime?.parseToLocalDateTime() ?: LocalDateTime.now()
                        tvText.text =
                            "안녕하세요 선생님! ${time.monthValue}월 ${time.dayOfMonth}일 ${time.hour}시 ${time.minute}분에 수업 가능하신가요?"
                    }

                    is MessageBodyVO.ReserveConfirm -> {
                        var body = item.bodyVO as MessageBodyVO.ReserveConfirm
                        var time =
                            body.startTime?.parseToLocalDateTime() ?: LocalDateTime.now()
                        tvText.text =
                            "${time.monthValue}월 ${time.dayOfMonth}일 ${time.hour}시 ${time.minute}분에 수업 예약이 완료되었습니다."
                    }

                    is MessageBodyVO.TutoringFinished -> {
                        var body = item.bodyVO as MessageBodyVO.TutoringFinished
                        var startAt =
                            body.startAt?.parseToLocalDateTime() ?: LocalDateTime.now()
                        var endAt =
                            body.endAt?.parseToLocalDateTime() ?: LocalDateTime.now()
                        tvText.text =
                            "수업이 종료 되었습니다.\n ${startAt.monthValue}월 ${startAt.dayOfMonth}일 ${startAt.hour}시 ${startAt.minute}분 ~.\n" +
                                    " ${endAt.monthValue}월 ${endAt.dayOfMonth}일 ${endAt.hour}시 ${endAt.minute}분"
                    }

                    is MessageBodyVO.RequestDecline -> {
                        tvText.text = "이 수업을 진행 할 수 없습니다."
                    }

                    else -> {}
                }

            }
        }
    }

    inner class ButtonsViewHolder(private val binding: ItemChatButtonsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(item: MessageVO) {
            binding.apply {
                if (item.isMyMsg) {
                    ConstraintSet().apply {
                        clone(containerRoot)
                        connect(
                            containerBody.id,
                            ConstraintSet.RIGHT,
                            containerRoot.id,
                            ConstraintSet.RIGHT
                        )
                        clear(containerBody.id, ConstraintSet.LEFT)
                        applyTo(containerRoot)
                    }
                    ConstraintSet().apply {
                        clone(root)
                        connect(
                            tvTime.id,
                            ConstraintSet.RIGHT,
                            containerBody.id,
                            ConstraintSet.LEFT
                        )
                        clear(tvTime.id, ConstraintSet.LEFT)
                        applyTo(root)
                    }
                } else {
                    //set color
                    containerBody.backgroundTintList = root.context.getColorStateList(R.color.white)
                    tvText.setTextColor(root.context.getColor(R.color.black))

                    //set position to left
                    ConstraintSet().apply {
                        clone(root)
                        connect(
                            containerBody.id,
                            ConstraintSet.LEFT,
                            root.id,
                            ConstraintSet.LEFT
                        )
                        clear(containerBody.id, ConstraintSet.RIGHT)
                        applyTo(root)
                    }
                    ConstraintSet().apply {
                        clone(root)
                        connect(
                            tvTime.id,
                            ConstraintSet.LEFT,
                            containerBody.id,
                            ConstraintSet.RIGHT
                        )
                        clear(tvTime.id, ConstraintSet.RIGHT)
                        applyTo(root)
                    }
                    when (item.bodyVO) {


                        is MessageBodyVO.RequestDecline -> {
                            var body = item.bodyVO as MessageBodyVO.RequestDecline
                            tvText.text = "죄송합니다. 이 수업을 진행 할 수 없습니다."
                            if (!isTeacher) {
                                btn1.visibility = Button.VISIBLE
                                btn2.visibility = Button.VISIBLE
                                btn3.visibility = Button.GONE
                                btn1.text = "다른 선생님께 질문하기"
                                btn2.text = "질문 삭제하기"

                                btn1.setOnClickListener {
                                    onBtn1Click()
                                }

                                btn2.setOnClickListener {
                                    onBtn2Click()
                                }
                            } else {
                                btn1.visibility = Button.GONE
                                btn2.visibility = Button.GONE
                                btn3.visibility = Button.GONE

                            }
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    inner class QuestionViewHolder(private val binding: ItemChatQuestionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(item: MessageVO) {
            binding.apply {
                if (item.isMyMsg) {
                    //set position to left
                    //set position to right
                    ConstraintSet().apply {
                        clone(containerRoot)
                        connect(
                            containerBody.id,
                            ConstraintSet.RIGHT,
                            containerRoot.id,
                            ConstraintSet.RIGHT
                        )
                        clear(containerBody.id, ConstraintSet.LEFT)
                        applyTo(containerRoot)
                    }
//                    ConstraintSet().apply {
//                        clone(root)
//                        connect(
//                            tvTime.id,
//                            ConstraintSet.RIGHT,
//                            containerBody.id,
//                            ConstraintSet.LEFT
//                        )
//                        clear(tvTime.id, ConstraintSet.LEFT)
//                        applyTo(root)
//                    }
                } else {
                    //set position to left
                    ConstraintSet().apply {
                        clone(root)
                        connect(
                            containerBody.id,
                            ConstraintSet.LEFT,
                            root.id,
                            ConstraintSet.LEFT
                        )
                        clear(containerBody.id, ConstraintSet.RIGHT)
                        applyTo(root)
                    }
//                    ConstraintSet().apply {
//                        clone(root)
//                        connect(
//                            tvTime.id,
//                            ConstraintSet.LEFT,
//                            containerBody.id,
//                            ConstraintSet.RIGHT
//                        )
//                        clear(tvTime.id, ConstraintSet.RIGHT)
//                        applyTo(root)
//                    }
                }
//                tvTime.text = "${item.time.dayOfMonth}일 ${item.time.hour}:${
//                    String.format(
//                        "%02d",
//                        item.time.minute
//                    )
//                }"
                when (item.bodyVO) {
                    is MessageBodyVO.ProblemImage -> {
                        var body = item.bodyVO as MessageBodyVO.ProblemImage
                        tvDesciption.text = body.description
                        Glide.with(root.context).load(body.image)
                            .centerCrop()
                            .into(ivImage)
                        root.setOnClickListener {
                            onImageClick()
                        }
                        binding.tvSubject.text = body.subTitle

                    }

                    else -> {}
                }
            }
        }
    }

    fun String.parseToLocalDateTime(): LocalDateTime? {
        try {
            Log.d("parseToLocalDateTime", this)
            val formatter = DateTimeFormatter.ISO_DATE_TIME
            val zonedDateTime = ZonedDateTime.parse(this, formatter)
            val kstZoneId = ZoneId.of("Asia/Seoul")
            return zonedDateTime.withZoneSameInstant(kstZoneId).toLocalDateTime()
        } catch (e: Exception) {
            Log.e("parseToLocalDateTime", e.toString())
            return null
        }
    }

    companion object {
        const val TYPE_TEXT = 0
        const val TYPE_BUTTONS = 1
        const val TYPE_QUESTION = 2
    }
}
package org.softwaremaestro.presenter.util.widget

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.softwaremaestro.presenter.databinding.DialogTimePickerBinding
import org.softwaremaestro.presenter.util.nowInKorea

class TimePickerBottomDialog(private val onReturnClick: ((SpecificTime) -> Unit)) :
    BottomSheetDialogFragment() {

    private lateinit var binding: DialogTimePickerBinding

    private var title: String? = null
    private var btnText: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogTimePickerBinding.inflate(layoutInflater)
        setTimePicker()
        setDialogTitle()
        setDialogBtnText()
        setReturnButton()
        setDefaultTime()
        return binding.root
    }

    private fun setTimePicker() {
        binding.timePicker.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
    }

    fun setTitle(title: String) {
        this.title = title
    }

    fun setBtnText(btnText: String) {
        this.btnText = btnText
    }

    private fun setDefaultTime() {
        with(nowInKorea()) {
            binding.timePicker.hour = hour
            binding.timePicker.minute = minute
        }
    }

    private fun setDialogTitle() {
        if (title != null)
            binding.tvTitle.text = title
    }

    private fun setDialogBtnText() {
        if (btnText != null)
            binding.btnReturn.text = btnText
    }

    private fun setReturnButton() {
        binding.btnReturn.setOnClickListener {
            val hour = binding.timePicker.hour
            val minute = binding.timePicker.minute
            onReturnClick(SpecificTime(hour, minute))
            dismiss()
        }
    }

    override fun show(manager: FragmentManager, tag: String?) {
        try {
            super.show(manager, tag)
        } catch (e: IllegalStateException) {
            Log.w("TimePickerBottomDialog", "Exception", e)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val dialog = super.onCreateDialog(savedInstanceState)
        (dialog as BottomSheetDialog).behavior.isDraggable = false

        return dialog
    }

    inner class SpecificTime(
        val hour: Int,
        val minute: Int
    ) {
        override fun toString(): String {
            return "${hour}시 ${minute}분"
        }
    }

}


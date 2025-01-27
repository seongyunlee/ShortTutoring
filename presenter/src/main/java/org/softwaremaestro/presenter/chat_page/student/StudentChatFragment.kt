package org.softwaremaestro.presenter.chat_page.student

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.softwaremaestro.domain.chat.entity.ChatRoomState
import org.softwaremaestro.domain.chat.entity.ChatRoomVO
import org.softwaremaestro.presenter.R
import org.softwaremaestro.presenter.chat_page.ChatFragment
import org.softwaremaestro.presenter.chat_page.viewmodel.StudentChatViewModel
import org.softwaremaestro.presenter.util.UIState
import org.softwaremaestro.presenter.util.setEnabledAndChangeColor
import org.softwaremaestro.presenter.util.toKoreanString
import org.softwaremaestro.presenter.util.widget.DatePickerBottomDialog
import org.softwaremaestro.presenter.util.widget.NumberPickerBottomDialog
import org.softwaremaestro.presenter.util.widget.SimpleAlertDialog
import org.softwaremaestro.presenter.util.widget.SimpleConfirmDialog
import org.softwaremaestro.presenter.util.widget.TimePickerBottomDialog
import java.time.LocalDateTime


@AndroidEntryPoint
class StudentChatFragment : ChatFragment() {

    private val studentViewModel: StudentChatViewModel by viewModels()
    private lateinit var datePickerDialog: DatePickerBottomDialog
    private lateinit var timePickerDialog: TimePickerBottomDialog
    private lateinit var numberPickerDialog: NumberPickerBottomDialog
    private lateinit var waitingTeacherDialog: SimpleAlertDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        observePickTeacherResultState()
        observeTutoringTimeAndDurationProper()
        observeClassroomInfo() // 강의실 입장하기 버튼을 눌렀을 때의 결과 observe
        observeTutoringInfo() //예약하기 질문의 noti 세팅을 위한 과외 정보 observe
        initDialog()
        return view
    }

    override fun isTeacher(): Boolean {
        return false
    }

    override fun onChatRoomStateChange(chatRoomVO: ChatRoomVO) {
        enableChatting(true)
        if (chatRoomVO.isSelect) {
            when (chatRoomVO.chatRoomState) {
                ChatRoomState.PROPOSED -> {
                    onProposedSelectQuestionSelect()
                }

                ChatRoomState.DECLINED -> {
                    //지정 질문을 선생님이 거절
                    Log.d("onChatRoomStateChange", "onChatRoomStateChange: DECLINED")
                    onDeclinedRoomSelect()
                }

                ChatRoomState.RESERVED -> {
                    onReservedRoomSelect()
                }

                else -> {
                    onProposedSelectQuestionSelect()
                    Log.e("${this@StudentChatFragment::class.java}", "invalid question state")
                }
            }
        } else {
            when (chatRoomVO.chatRoomState) {
                ChatRoomState.PROPOSED -> {
                    onProposedNormalQuestionSelect()
                }

                ChatRoomState.RESERVED -> {
                    onReservedRoomSelect()
                }

                ChatRoomState.DECLINED -> {
                    //지정 질문을 선생님이 거절
                    onDeclinedRoomSelect()
                }

                else -> {
                    Log.e("${this@StudentChatFragment::class.java}", "invalid ChatRoomState ")
                }
            }
        }
    }

    private fun onDeclinedRoomSelect() {
        hideLeftButton()
        setNotiVisible(false)
        hideRightButton()
        disableSendMessage()
    }

    private fun initWaitingTeacherDialog() {
        waitingTeacherDialog = SimpleAlertDialog().apply {
            title = "아직 수업이 시작되지 않았습니다"
            description = "선생님께 문의해보세요."
        }
    }

    private fun onProposedSelectQuestionSelect() {
        hideLeftButton()
        setNotiVisible(false)
        hideRightButton()
        enableSendMessage()
    }

    private fun hideLeftButton() {
        binding.btnChatRoomLeft.visibility = View.GONE
    }

    private fun hideRightButton() {
        binding.btnChatRoomRight.visibility = View.GONE
    }

    fun observeClassroomInfo() {
        chatViewModel.classroomInfo.observe(viewLifecycleOwner) {
            when (it) {
                is UIState.Empty -> return@observe
                is UIState.Loading -> {
                    binding.btnChatRoomRight.setEnabledAndChangeColor(false)
                    loadingDialog.show()
                }

                is UIState.Success -> {
                    loadingDialog.dismiss()
                    if (it._data != null) {
                        SimpleConfirmDialog()
                        {
                            moveToClassRoom(it._data)
                        }.apply {
                            title = "강의실에 입장합니다"
                            description = "강의실에 들어가면 바로 수업이 시작됩니다.\n수업 가능한 환경을 준비해주세요."
                        }.show(parentFragmentManager, "enterClassroomDialog")
                    } else {
                        waitingTeacherDialog.show(parentFragmentManager, "waitingTeacherDialog")
                    }
                    binding.btnChatRoomRight.setEnabledAndChangeColor(true)
                }

                is UIState.Failure -> {
                    loadingDialog.dismiss()
                    Toast.makeText(requireContext(), "강의실 정보를 가져오지 못했습니다.", Toast.LENGTH_SHORT)
                        .show()
                }

                else -> {

                }

            }
            chatViewModel._classroomInfo.value = UIState.Empty


            //액티비티 종료되어 돌아오는 경우에 대비해서 초기화
        }
    }

    private fun observePickTeacherResultState() {
        studentViewModel.pickTeacherResultState.observe(viewLifecycleOwner) {
            when (it) {
                is UIState.Loading -> {
                    loadingDialog.show()
                    with(binding.btnChatRoomRight) {
                        setBackgroundResource(R.drawable.bg_radius_100_background_grey)
                        isEnabled = false
                        setTextColor(resources.getColor(R.color.sub_text_grey, null))
                    }
                }

                is UIState.Success -> {
                    disableChatRoomBtn()
                    loadingDialog.dismiss()
                    // 채팅룸의 상태가 변경됐으므로 서버로부터 roomList를 다시 호출
                    //chatViewModel.getChatRoomList(isTeacher(),currentRoomId?.id
                }

                is UIState.Failure -> {
                    //선생님 선택 실패
                }

                else -> {}
            }
        }
    }

    private fun onProposedNormalQuestionSelect() {
        setNotiVisible(false)
        enableSendMessage()
        enablePickTeacherButton()
    }

    private fun onReservedRoomSelect() {
        setNotiVisible(true)
        currentChatRoom?.questionId?.let {
            chatViewModel.getTutoringInfo(it) //예약하기 질문의 noti 세팅을 위한 과외 정보 api 호출
            observeTutoringInfo()
        }
        hideRightButton()
        enableSendMessage()
        unSetOfferingTeacherMode() // 채팅 보고 있을 때 선택 했을 경우에 대비해서 offeringTeacherMode 해제하고 해당 방으로 이동
    }

    private fun observeTutoringInfo() {
        chatViewModel.tutoringInfo.observe(viewLifecycleOwner) {
            when (it) {
                is UIState.Empty -> return@observe

                is UIState.Success -> {
                    loadingDialog.dismiss()
                    it._data?.let { tutoringInfo ->
                        setChatNoti(tutoringInfo.reservedStart, tutoringInfo.id)
                        tutoringId = tutoringInfo.id
                    }
                }

                is UIState.Failure -> {
                    loadingDialog.dismiss()
                    //setChatNoti(null, null)
                    Toast.makeText(requireContext(), "예약 정보를 가져오지 못했습니다.", Toast.LENGTH_SHORT)
                        .show()
                }

                else -> {}
            }
        }
    }

    private fun initDatePickerDialog() {
        datePickerDialog = DatePickerBottomDialog { date ->
            with(date) {
                studentViewModel.setTutoringTime(
                    LocalDateTime.now()
                        .withYear(year)
                        .withMonth(monthValue)
                        .withDayOfMonth(dayOfMonth)
                )
                Log.d("date picker", "initDatePickerDialog: ${studentViewModel.tutoringTime.value}")
            }
            timePickerDialog.show(parentFragmentManager, "timePicker")
        }.apply {
            setTitle("수업 날짜를 선택해주세요")
            setBtnText("선택하기")
        }
    }

    private fun enablePickTeacherButton() {

        binding.btnChatRoomRight.apply {
            visibility = View.VISIBLE
            setBackgroundResource(R.drawable.bg_radius_100_grad_blue)
            isEnabled = true
            setTextColor(resources.getColor(R.color.white, null))
            setOnClickListener {
                datePickerDialog.show(parentFragmentManager, "datePicker")
            }
        }
    }

    private fun initDialog() {
        initDatePickerDialog()
        initTimePickerDialog()
        initNumberPickerDialog()
        initWaitingTeacherDialog()
    }

    private fun initTimePickerDialog() {
        timePickerDialog = TimePickerBottomDialog { time ->
            studentViewModel.setTutoringTime(
                with(studentViewModel.tutoringTime.value!!) {
                    LocalDateTime.of(
                        year,
                        monthValue,
                        dayOfMonth,
                        time.hour,
                        time.minute
                    )
                }
            )
            Log.d("test date", "initTimePickerDialog: ${studentViewModel.tutoringTime.value}")
            numberPickerDialog.show(parentFragmentManager, "numberPicker")
        }.apply {
            setTitle("수업 시작 시간을 선택해주세요")
            setBtnText("선택하기")
        }
    }

    private fun initNumberPickerDialog() {
        numberPickerDialog = NumberPickerBottomDialog { number ->
            studentViewModel.setTutoringDuration(number)
        }.apply {
            setTitle("수업을 몇 분간 진행할까요?")
            setBtnText("입력하기")
        }
    }

    private fun observeTutoringTimeAndDurationProper() {
        studentViewModel.tutoringTimeAndDurationProper.observe(viewLifecycleOwner) { proper ->
            if (proper) {
                currentChatRoom?.let {
                    studentViewModel.pickTeacher(it.id, it.questionId)
                }
                studentViewModel.setTutoringTime(null)
                studentViewModel.setTutoringDuration(null)
            }
        }
    }

    override fun enablePickStudentBtn() {
        return
    }


    fun setChatNoti(startAt: LocalDateTime?, tutoringId: String?) {
        Log.d("setChatNoti", "setChatNoti: ${startAt} ")
        binding.cnNoti.apply {
            setTvNotiMain("선생님과의 수업이 ${startAt?.toKoreanString()}에 진행됩니다")
            setTvNotiSub("선생님이 수업을 시작하면 강의실에 입장할 수 있어요")
            setBtnNegativeText("닫기")
            setBtnPositiveText("강의실 입장하기")
            setOnClickListenerToBtnNegative {
                setNotiVisible(false)
            }
            setOnClickListenerToBtnPositive {
                if (chatViewModel.tutoringInfo.value?._data?.status == "finished") {
                    Toast.makeText(requireContext(), "이미 종료된 수업입니다.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListenerToBtnPositive
                }
                tutoringId?.let { chatViewModel.getClassroomInfo(it) }
            }
        }

    }

}
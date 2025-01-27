package org.softwaremaestro.presenter.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import dagger.hilt.android.AndroidEntryPoint
import org.softwaremaestro.presenter.R
import org.softwaremaestro.presenter.databinding.FragmentLoginBinding
import org.softwaremaestro.presenter.login.viewmodel.LoginViewModel
import org.softwaremaestro.presenter.student_home.StudentHomeActivity
import org.softwaremaestro.presenter.teacher_home.TeacherHomeActivity
import javax.inject.Inject

// 앱에 들어왔을 때 보이는 첫 화면.
// 로그인 창과 소셜 로그인 버튼이 있다.
@AndroidEntryPoint
class LoginFragment @Inject constructor() :
    Fragment() {

    private lateinit var binding: FragmentLoginBinding

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentLoginBinding.inflate(inflater, container, false)

//        binding.tvLogo.setOnClickListener {
//            val intent = Intent(activity, StudentHomeActivity::class.java)
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            startActivity(intent)
//        }

        setKakaoButton()
//        setGoogleButton()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObserver()
    }

    override fun onStop() {
        Log.d("mymymy", "onStop in frag Login")
        super.onStop()
    }


    private fun checkAutoLogin() {
        // 자동로그인 처리 구현 방법
        // 1. preferenece 에 저장된 이전 로그인 정보 확인
        // 2. 로그인 정보 없으면 소셜 로그인 선택화면 보여주기
        // 3. access token으로 회원정보 받아오기.
        // 3. 회원정보에 따라서 선생님, 학생 분기.
    }

    private fun setKakaoButton() {
        binding.btnLoginByKakao.setOnClickListener {
            viewModel.loginWithKakao(requireContext())
            //findNavController().navigate(R.id.action_loginFragment_to_registerRoleFragment)

        }
    }


    private fun observeLoginResult() {
        viewModel.userRole.observe(viewLifecycleOwner) {

            when (it) {
                "teacher" -> {
                    val intent = Intent(activity, TeacherHomeActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }

                "student" -> {
                    val intent = Intent(activity, StudentHomeActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }

                "not registered" -> {
                    Navigation.findNavController(binding.root)
                        .navigate(R.id.action_loginFragment_to_toSFragment)
                }

                else -> return@observe

            }
            // 로그인 성공 시, 유저의 역할에 따라서 화면 분기
            //다른 프래그먼트로 갔다가 되돌아올경우 observe가 다시 호출되는데
            //userRole이 null이 아니면 다시 이동되는 문제가 있음
            //따라서 네비게이션 될때 userRole이 null로 바꿔줌

            viewModel.clearUserRole()

        }
    }

    private fun observeKakaoLogin() {
        viewModel.isKakaoLoginSuccess.observe(viewLifecycleOwner) {
            if (viewModel.isKakaoLoginSuccess.value == true) {
                //카카오 로그인 성공 //로컬에 idToken이 저장되어있음

            }
        }
    }

    private fun setObserver() {
        observeLoginResult()
        observeKakaoLogin()
    }
}
package org.softwaremaestro.presenter.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import org.softwaremaestro.presenter.R
import org.softwaremaestro.presenter.databinding.FragmentLogoBinding

// 앱에 들어왔을 때 보이는 첫 화면.
// 로그인 창과 소셜 로그인 버튼이 있다.
class LogoFragment : Fragment() {

    private lateinit var binding: FragmentLogoBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentLogoBinding.inflate(inflater, container, false)

        binding.containerLoginByGoogle.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_logoFragment_to_registerRoleFragment)
        }

        binding.containerLoginByKakao.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_logoFragment_to_registerRoleFragment)
        }

        return binding.root
    }
}
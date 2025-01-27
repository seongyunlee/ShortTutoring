package org.softwaremaestro.data.login

import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.softwaremaestro.data.common.utils.SavedToken
import org.softwaremaestro.data.infra.SharedPrefs
import org.softwaremaestro.data.login.model.LoginReqDto
import org.softwaremaestro.data.login.model.RegisterFCMTokenReqDto
import org.softwaremaestro.data.login.remote.FCMApi
import org.softwaremaestro.data.login.remote.LoginApi
import org.softwaremaestro.domain.common.BaseResult
import org.softwaremaestro.domain.login.LoginRepository
import org.softwaremaestro.domain.login.entity.UserVO
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class LoginRepositoryImpl @Inject constructor(
    private val loginApi: LoginApi,
    private val fcmApi: FCMApi,
    private val prefs: SharedPrefs,
    private val savedToken: SavedToken,
) :
    LoginRepository {


    override suspend fun autoLogin(): Flow<BaseResult<String, String>> {
        //JWT 토큰 이용해서 로그인
        return flow {
            val savedToken: String = prefs.getJWT()

            if (savedToken == "") {
                emit(BaseResult.Error("No saved token"))
            } else {
                loginApi.getUserInfo().body()?.data?.let {
                    emit(BaseResult.Success(it.role))
                } ?: emit(BaseResult.Error("Fail to get user Info"))
            }
        }
    }

    override suspend fun login(): Flow<BaseResult<String, String>> {
        return flow {

            val result =
                loginApi.login(
                    LoginReqDto(
                        savedToken.getTokenInfo().vendor!!,
                        savedToken.getTokenInfo().token!!
                    )
                )
            if (result.isSuccessful && result.body()?.success == true) {
                val loginData = result.body()?.data!!
                prefs.saveJWT(loginData.JWT)
                emit(
                    BaseResult.Success(
                        loginData.role
                    )
                )
            } else {
                emit(BaseResult.Error("Fail to login"))
            }
        }
    }

    override fun saveKakaoJWT(token: String) {
        //
    }


    override fun getUserInfo(): Flow<BaseResult<UserVO, String>> {
        return flow {
            val result = loginApi.getUserInfo()
            if (result.isSuccessful) {
                val userInfo = result.body()?.data!!
                emit(
                    BaseResult.Success(
                        UserVO(
                            userInfo.role,
                            userInfo.id,
                            userInfo.bio,
                            userInfo.name,
                            userInfo.profileImage
                        )
                    )
                )
            } else {
                emit(BaseResult.Error("Fail to get user info"))
            }
        }
    }

    override fun getToken(): String {
        return prefs.getJWT()
    }


    override suspend fun registerFCMToken(): Flow<BaseResult<String, String>> {
        return flow {
            val result = fcmApi.registerFCMToken(
                RegisterFCMTokenReqDto(getFCMToken()!!)
            )
            if (result.isSuccessful) {
                emit(BaseResult.Success("success"))
            } else {
                emit(BaseResult.Error("error"))
            }
        }
    }


    private suspend fun getFCMToken(): String? = suspendCoroutine { continuation ->
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                continuation.resume(task.result)
            } else {
                continuation.resume(null)
            }
        }
    }

    override fun clearFCMToken(): String {
        prefs.clearJWT()
        return "clear FCM Token succeeded"
    }

}
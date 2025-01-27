package org.softwaremaestro.presenter.login.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.softwaremaestro.domain.common.BaseResult.Error
import org.softwaremaestro.domain.common.BaseResult.Success
import org.softwaremaestro.domain.login.entity.StudentRegisterVO
import org.softwaremaestro.domain.login.usecase.StudentRegisterUseCase
import org.softwaremaestro.presenter.util.UIState
import javax.inject.Inject

@HiltViewModel
class StudentRegisterViewModel @Inject constructor(
    private val studentRegisterUseCase: StudentRegisterUseCase
) :
    ViewModel() {

    private val _schoolLevel = MutableLiveData<String>()
    val schoolLevel: LiveData<String> get() = _schoolLevel

    private val _schoolGrade = MutableLiveData<Int>()
    val schoolGrade: LiveData<Int> get() = _schoolGrade

    private val _schoolLevelAndGradeProper = MediatorLiveData<Boolean>()
    val schoolLevelAndGradeProper: MediatorLiveData<Boolean> get() = _schoolLevelAndGradeProper

    private val _name = MutableLiveData<String>()
    val name: LiveData<String> get() = _name

    private val _bio = MutableLiveData<String>("student-bio")
    val bio: LiveData<String> get() = _bio

    val _image: MutableLiveData<String> = MutableLiveData()
    val image: LiveData<String> get() = _image

    private val _studentSignupState = MutableLiveData<UIState<String>>()
    val studentSignupState: LiveData<UIState<String>> get() = _studentSignupState

    private val _studentNameAndImageProper = MediatorLiveData<Boolean>()
    val studentNameAndImageProper: MediatorLiveData<Boolean> get() = _studentNameAndImageProper

    init {
        with(_schoolLevelAndGradeProper) {
            addSource(_schoolLevel) {
                postValue(!_schoolLevel.value.isNullOrEmpty() && _schoolGrade.value != null)
            }

            addSource(_schoolGrade) {
                postValue(!_schoolLevel.value.isNullOrEmpty() && _schoolGrade.value != null)
            }
        }

        with(_studentNameAndImageProper) {
            addSource(_name) {
                postValue(
                    !_name.value.isNullOrEmpty() && !_image.value.isNullOrEmpty()
                )
            }

            addSource(_image) {
                postValue(
                    !_name.value.isNullOrEmpty() && !_image.value.isNullOrEmpty()
                )
            }
        }
    }

    fun registerStudent() {
        viewModelScope.launch {
            studentRegisterUseCase.execute(
                StudentRegisterVO(
                    name = name.value!!,
                    bio = bio.value!!,
                    schoolLevel = schoolLevel.value!!,
                    schoolGrade = schoolGrade.value!!,
                    profileImg = image.value!!
                )
            )
                .onStart {
                    _studentSignupState.value = UIState.Loading
                }
                .catch {
                    _studentSignupState.value = UIState.Failure
                }
                .collect { result ->
                    when (result) {
                        is Success -> _studentSignupState.value = UIState.Success(result.data)
                        is Error -> _studentSignupState.value = UIState.Failure
                    }
                }
        }
    }

    fun setName(name: String) {
        _name.value = name
    }

    fun setSchoolLevel(schoolLevel: String) {
        _schoolLevel.value = schoolLevel
    }

    fun setSchoolGrade(schoolGrade: Int) {
        _schoolGrade.value = schoolGrade
    }
}
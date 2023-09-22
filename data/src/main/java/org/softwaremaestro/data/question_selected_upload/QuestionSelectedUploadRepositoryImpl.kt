package org.softwaremaestro.data.question_selected_upload

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.softwaremaestro.data.question_selected_upload.model.asDto
import org.softwaremaestro.data.question_selected_upload.remote.QuestionSelectedUploadApi
import org.softwaremaestro.domain.common.BaseResult
import org.softwaremaestro.domain.question_selected_upload.QuestionSelectedUploadRepository
import org.softwaremaestro.domain.question_selected_upload.entity.QuestionSelectedUploadResultVO
import org.softwaremaestro.domain.question_selected_upload.entity.QuestionSelectedUploadVO
import javax.inject.Inject

class QuestionSelectedUploadRepositoryImpl @Inject constructor(private val questionSelectedUploadApi: QuestionSelectedUploadApi) :
    QuestionSelectedUploadRepository {

    override suspend fun uploadQuestionSelected(questionSelectedUploadVO: QuestionSelectedUploadVO): Flow<BaseResult<QuestionSelectedUploadResultVO, String>> {
        return flow {
            val dto = questionSelectedUploadVO.asDto()
            val response = questionSelectedUploadApi.uploadQuestionSelected(dto)
            if (response.isSuccessful) {
                val body = response.body()
                val data = body?.data
                val resultVO = QuestionSelectedUploadResultVO(
                    data?.id,
                    data?.student,
                    data?.teacherIds,
                    data?.problem,
                    data?.hopeTutorialTime,
                    data?.hopeImmediately
                )
                emit(BaseResult.Success(resultVO))
            } else {
                val errorString = "error"
                emit(BaseResult.Error(errorString))
            }
        }
    }
}
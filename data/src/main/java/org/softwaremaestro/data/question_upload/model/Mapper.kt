package org.softwaremaestro.data.question_upload.model

import org.softwaremaestro.data.common.utils.toStringWithTimeZone
import org.softwaremaestro.domain.question_upload.entity.QuestionUploadVO

object Mapper {

    fun questionUploadVOAsDto(questionUploadVO: QuestionUploadVO): QuestionUploadRequestDto {
        return QuestionUploadRequestDto(
            images = questionUploadVO.images,
            description = questionUploadVO.description,
            schoolLevel = questionUploadVO.schoolLevel,
            schoolSubject = questionUploadVO.schoolSubject,
            hopeImmediate = questionUploadVO.hopeImmediate,
            hopeTutoringTime = questionUploadVO.hopeTutoringTime.map { it.toStringWithTimeZone() },
            mainImageIndex = questionUploadVO.mainImageIndex
        )
    }

}


fun QuestionUploadVO.asDto(): QuestionUploadRequestDto {
    return Mapper.questionUploadVOAsDto(this)
}

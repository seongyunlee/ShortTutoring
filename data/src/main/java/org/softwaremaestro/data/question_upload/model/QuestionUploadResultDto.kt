package org.softwaremaestro.data.question_upload.model

import com.google.gson.annotations.SerializedName


data class QuestionUploadResultDto(
    @SerializedName("question_id") var questionId: Int,
)

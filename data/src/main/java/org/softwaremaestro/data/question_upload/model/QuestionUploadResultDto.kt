package org.softwaremaestro.data.question_upload.model

import com.google.gson.annotations.SerializedName


data class QuestionUploadResultDto(
    @SerializedName("requestId") var questionId: String,
)

package org.softwaremaestro.data.question_upload.model

import com.google.gson.annotations.SerializedName

data class PickTeacherResDto(
    @SerializedName("id") val tutoringId: String,
    @SerializedName("whiteBoardToken") val whiteBoardToken: String,
    @SerializedName("whiteBoardUUID") val whiteBoardUUID: String,
    @SerializedName("whiteBoardAppId") val whiteBoardAppId: String,
    @SerializedName("teacherRTCToken") val teacherRTCToken: String,
    @SerializedName("studentRTCToken") val studentRTCToken: String,
    @SerializedName("RTCAppId") val RTCAppId: String,

    )
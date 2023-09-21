package org.softwaremaestro.data.classroom.remote

import org.softwaremaestro.data.classroom.model.TutoringInfoDto
import org.softwaremaestro.data.classroom.model.TutoringInfoResDto
import org.softwaremaestro.data.common.utils.WrappedResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ClassRoomApi {

    @GET("/tutoring/finish/{tutoringId}")
    suspend fun finishClassroom(@Path("tutoringId") tutoringId: String): Response<WrappedResponse<String>>

    @GET("/tutoring/info/{questionId}")
    suspend fun getClassroomInfo(@Path("questionId") questionId: String): Response<WrappedResponse<TutoringInfoResDto>>
}
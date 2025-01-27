package org.softwaremaestro.domain.teacher_get.entity

data class TeacherVO(
    val profileUrl: String?,
    val nickname: String?,
    val teacherId: String?,
    val bio: String?,
    val univ: String?,
    val major: String? = null,
    val rating: Float?,
    val followers: List<String>?,
    val reservationCnt: Int?
)
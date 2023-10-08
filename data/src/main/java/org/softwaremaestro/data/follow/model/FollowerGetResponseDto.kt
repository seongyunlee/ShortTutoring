package org.softwaremaestro.data.follow.model

import com.google.gson.annotations.SerializedName
import org.softwaremaestro.data.profile.model.SchoolDto

data class FollowerGetResponseDto(
    @SerializedName("id") val id: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("bio") val bio: String?,
    @SerializedName("profileImage") val profileImage: String?,
    @SerializedName("role") val role: String?,
    @SerializedName("school") val school: SchoolDto?,
    @SerializedName("followersCount") val followersCount: Int?,
    @SerializedName("followingCount") val followingCount: Int?
)
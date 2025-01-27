package org.softwaremaestro.data.chat

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.softwaremaestro.data.chat.database.ChatDatabase
import org.softwaremaestro.data.chat.entity.ChatRoomEntity
import org.softwaremaestro.data.chat.entity.ChatRoomType
import org.softwaremaestro.data.chat.entity.MessageEntity
import org.softwaremaestro.data.chat.entity.asEntity
import org.softwaremaestro.data.chat.remote.ChatApi
import org.softwaremaestro.data.question_get.remote.QuestionGetApi
import org.softwaremaestro.domain.chat.ChatRepository
import org.softwaremaestro.domain.chat.entity.ChatRoomListVO
import org.softwaremaestro.domain.chat.entity.ChatRoomVO
import org.softwaremaestro.domain.chat.entity.MessageVO
import org.softwaremaestro.domain.chat.entity.QuestionState
import org.softwaremaestro.domain.chat.entity.QuestionType
import org.softwaremaestro.domain.chat.entity.RoomType
import org.softwaremaestro.domain.common.BaseResult
import javax.inject.Inject
import org.softwaremaestro.data.chat.entity.asDomain as EntityToVO

class ChatRepositoryImpl @Inject constructor(
    private val chatApi: ChatApi,
    private val chatDatabase: ChatDatabase,
    private val questionApi: QuestionGetApi,
) :
    ChatRepository {

    private fun getOneRoomFromDB(roomId: String): ChatRoomVO? {
        return try {
            var result = chatDatabase.chatRoomDao().getChatRoomWithMessages(roomId)
            Log.d("ChatRepositoryImpl", "chatroomId $roomId $result")
            result.EntityToVO()
        } catch (e: Exception) {
            Log.e("ChatRepositoryImpl", e.toString())
            null
        }
    }

    private suspend fun getRoomFromDB(isTeacher: Boolean): ChatRoomListVO? {
        try {
            var proposedNormal =
                chatDatabase.chatRoomDao()
                    .getChatRoomListWithUnReadCnt(ChatRoomType.PROPOSED_NORMAL.type)
                    .map { it.EntityToVO() }
            var proposedSelect =
                chatDatabase.chatRoomDao()
                    .getChatRoomListWithUnReadCnt(ChatRoomType.PROPOSED_SELECT.type)
                    .map { it.EntityToVO() }
            var reservedNormal =
                chatDatabase.chatRoomDao()
                    .getChatRoomListWithUnReadCnt(ChatRoomType.RESERVED_NORMAL.type)
                    .map { it.EntityToVO() }
            var reservedSelect =
                chatDatabase.chatRoomDao()
                    .getChatRoomListWithUnReadCnt(ChatRoomType.RESERVED_SELECT.type)
                    .map { it.EntityToVO() }


            Log.d("ChatRepositoryImpl chat", "일반 ${proposedNormal} 예약 ${reservedNormal}")

            val groups: MutableList<ChatRoomVO> = mutableListOf()
            if (!isTeacher) {
                //학생이면 그룹화
                val group = proposedNormal.groupBy { it.questionId }
                try {
                    val questions = questionApi.getMyQuestionList(
                        QuestionState.PROPOSED.value,
                        QuestionType.NORMAL.value
                    ).body()?.data
                    questions?.forEach {
                        // 해당 question Id로 group이 있는지 확인
                        if (!group.containsKey(it.id)) {
                            val questionRoom = ChatRoomVO(
                                id = it.id!!,
                                roomType = RoomType.QUESTION,
                                roomImage = it.problemDto?.mainImage,
                                title = it.problemDto?.description,
                                schoolLevel = it.problemDto?.schoolLevel,
                                schoolSubject = it.problemDto?.schoolSubject,
                                isSelect = false,
                                questionId = it.id,
                                questionState = QuestionState.PROPOSED,
                                description = it.problemDto?.description ?: "",
                                subTitle = "${it.problemDto?.schoolLevel} ${it.problemDto?.schoolSubject}",
                                teachers = listOf(),
                            )
                            groups.add(questionRoom)
                        }
                    }
                } catch (e: Exception) {
                    Log.d("ChatRepositoryImpl", e.toString())
                }


                group.forEach { group ->
                    val questionInfo = questionApi.getQuestionInfo(group.key).body()?.data
                    questionInfo?.let {
                        val questionRoom = ChatRoomVO(
                            id = group.value[0].questionId,
                            roomType = RoomType.QUESTION,
                            roomImage = questionInfo.problemDto?.mainImage,
                            title = questionInfo.problemDto?.description,
                            schoolLevel = questionInfo.problemDto?.schoolLevel,
                            schoolSubject = questionInfo.problemDto?.schoolSubject,
                            isSelect = false,
                            questionId = group.key,
                            questionState = QuestionState.PROPOSED,
                            description = group.value[0].description,
                            subTitle = "${questionInfo.problemDto?.schoolLevel} ${questionInfo.problemDto?.schoolSubject}",
                            teachers = group.value.filter { it.opponentId != null },
                        )
                        groups.add(questionRoom)
                    }
                }
                //선생님의 응답이 하나도 없는 것들을 위해 빈방 만들기


                Log.d("ChatRepositoryImpl group", groups.toString())
            }
            return ChatRoomListVO(
                if (isTeacher) proposedNormal else groups,
                reservedNormal, proposedSelect, reservedSelect
            )
        } catch (e: Exception) {
            Log.d("ChatRepositoryImpl", e.toString())
            return null
        }

    }

    private suspend fun updateRoomStatus() {
        val result = chatApi.getRoomList()

        Log.d("ChatRepositoryImpl  update", result.body().toString())
        if (result.isSuccessful && result.body()?.success == true) {
            val data = result.body()?.data!!
            data.map {
                val chatRoomVO = it.asEntity()
                insertOrUpdateRoom(chatRoomVO)
                if (!chatRoomVO.isSelect && chatRoomVO.status == ChatRoomType.RESERVED_NORMAL.type) {
                    chatRoomVO.questionId.let { questionId ->
                        Log.d(
                            "ChatRepositoryImpl delete",
                            " ${questionId} ${chatRoomVO.description}"
                        )
                        chatDatabase.chatRoomDao().delete(questionId)
                    }
                }
            }
        }
    }

    private fun insertOrUpdateRoom(room: ChatRoomEntity) {
        if (chatDatabase.chatRoomDao().isIdExist(room.id)) {
            chatDatabase.chatRoomDao().update(room)
        } else {
            Log.d("ChatRepositoryImpl", "insert ${room}")
            chatDatabase.chatRoomDao().insert(room)
        }

    }


    override suspend fun getRoomList(
        isTeacher: Boolean,
        currentRoomId: String?
    ): Flow<BaseResult<ChatRoomListVO, String>> {
        return flow {
            //updateRoomStatus()
            var result = getRoomFromDB(isTeacher)
            currentRoomId?.let { result?.currentRoomVO = getOneRoomFromDB(it) }
            Log.d("ChatRepositoryImpl", result.toString())
            if (result == null) {
                emit(BaseResult.Error("error"))
            } else {
                emit(BaseResult.Success(result))
            }
        }
    }

    override suspend fun getMessages(chatRoomId: String): Flow<BaseResult<List<MessageVO>, String>> {
        return flow {
            var result = chatDatabase.chatRoomDao().getChatRoomWithMessages(chatRoomId)
            Log.d("ChatRepositoryImpl", "chatroomId $chatRoomId $result")
            if (result == null) {
                emit(BaseResult.Error("error"))
            } else {
                emit(BaseResult.Success(result.messages.map { it.EntityToVO() }))
            }
        }
    }

    override suspend fun insertMessage(
        roomId: String,
        body: String,
        format: String,
        sendAt: String,
        isMyMsg: Boolean,
        isRead: Boolean,
    ): Flow<BaseResult<Boolean, String>> {
        return flow {
            try {
                var result = chatApi.getRoom(roomId)
                if (!chatDatabase.chatRoomDao().isIdExist(roomId)) {
                    result.body()?.data?.let { chatDatabase.chatRoomDao().insert(it.asEntity()) }
                } else {
                    result.body()?.data?.let { chatDatabase.chatRoomDao().update(it.asEntity()) }
                }
                val now = java.time.LocalDateTime.now()
                chatDatabase.messageDao().insert(
                    MessageEntity(
                        id = roomId + sendAt,
                        roomId = roomId,
                        body = body,
                        format = format,
                        isRead = isRead,
                        sendAt = now,
                        isMyMsg = isMyMsg
                    )
                )
                chatDatabase.chatRoomDao().updateLastMessageTime(roomId, now)
                emit(BaseResult.Success(true))
            } catch (e: Exception) {
                Log.e("${this@ChatRepositoryImpl::class.java}", e.toString())
                emit(BaseResult.Error("error"))
            }
        }

    }

    override suspend fun getChatRoomInfo(chatRoomId: String): Flow<BaseResult<ChatRoomVO, String>> {
        return flow {
            var result = getOneRoomFromDB(chatRoomId)
            if (result == null) {
                emit(BaseResult.Error("error"))
            } else {
                emit(BaseResult.Success(result))
            }
        }
    }

    override suspend fun markAsRead(chatRoomId: String) {
        chatDatabase.messageDao().markAsRead(chatRoomId)
    }

    override suspend fun hasUnreadMessages(): Boolean {
        return chatDatabase.messageDao().hasUnreadMessages()
    }

}
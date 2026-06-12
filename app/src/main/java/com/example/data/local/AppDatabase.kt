package com.example.data.local

import androidx.room.*
import com.example.data.models.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyDao {
    @Query("SELECT * FROM profiles WHERE id = :id")
    suspend fun getProfile(id: String): Profile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: Profile)

    @Query("SELECT * FROM families WHERE inviteCode = :code")
    suspend fun getFamilyByInviteCode(code: String): Family?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFamily(family: Family)

    @Update
    suspend fun updateProfile(profile: Profile)
}

@Dao
interface SocialDao {
    @Query("SELECT * FROM posts WHERE familyId = :familyId ORDER BY timestamp DESC")
    fun getPosts(familyId: String): Flow<List<FamilyPost>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: FamilyPost)

    @Query("SELECT * FROM comments WHERE postId = :postId")
    fun getComments(postId: String): Flow<List<PostComment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: PostComment)

    @Query("SELECT * FROM messages WHERE familyId = :familyId ORDER BY timestamp ASC")
    fun getMessages(familyId: String): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)
}

@Database(entities = [Profile::class, Family::class, FamilyPost::class, PostComment::class, ChatMessage::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun familyDao(): FamilyDao
    abstract fun socialDao(): SocialDao
}

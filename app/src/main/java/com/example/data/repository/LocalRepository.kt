package com.example.data.repository

import com.example.data.local.FamilyDao
import com.example.data.local.SocialDao
import com.example.data.models.*
import kotlinx.coroutines.flow.Flow
import java.util.*

class LocalRepository(private val familyDao: FamilyDao, private val socialDao: SocialDao) {

    // Auth & Profile
    suspend fun getProfile(id: String) = familyDao.getProfile(id)
    suspend fun saveProfile(profile: Profile) = familyDao.insertProfile(profile)
    suspend fun updateProfile(profile: Profile) = familyDao.updateProfile(profile)

    // Family
    suspend fun createFamily(name: String, adminId: String): Family {
        val family = Family(
            id = UUID.randomUUID().toString(),
            name = name,
            inviteCode = (100000..999999).random().toString(),
            createdAt = java.time.OffsetDateTime.now().toString()
        )
        familyDao.insertFamily(family)
        
        val profile = familyDao.getProfile(adminId)
        if (profile != null) {
            familyDao.updateProfile(profile.copy(familyId = family.id, role = "admin"))
        }
        return family
    }

    suspend fun joinFamily(inviteCode: String, userId: String): Boolean {
        val family = familyDao.getFamilyByInviteCode(inviteCode)
        if (family != null) {
            val profile = familyDao.getProfile(userId)
            if (profile != null) {
                familyDao.updateProfile(profile.copy(familyId = family.id))
                return true
            }
        }
        return false
    }

    // Social
    fun getPosts(familyId: String): Flow<List<FamilyPost>> = socialDao.getPosts(familyId)
    suspend fun createPost(post: FamilyPost) = socialDao.insertPost(post)
    
    fun getMessages(familyId: String): Flow<List<ChatMessage>> = socialDao.getMessages(familyId)
    suspend fun sendMessage(message: ChatMessage) = socialDao.insertMessage(message)
    
    fun getComments(postId: String): Flow<List<PostComment>> = socialDao.getComments(postId)
    suspend fun addComment(comment: PostComment) = socialDao.insertComment(comment)
}

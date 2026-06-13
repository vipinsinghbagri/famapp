package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "profiles")
data class Profile(
    @PrimaryKey val id: String,
    val familyId: String?,
    val nickname: String,
    val avatarIndex: Int,
    val role: String = "member",
    val status: String = "Online"
)

@Entity(tableName = "families")
data class Family(
    @PrimaryKey val id: String,
    val name: String,
    val inviteCode: String,
    val createdAt: String
)

@Entity(tableName = "family_members")
data class FamilyMember(
    @PrimaryKey val id: String,
    val name: String,
    val role: String,
    val avatarIndex: Int,
    val mood: String,
    val colorHex: String
)

@Entity(tableName = "posts")
data class FamilyPost(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val familyId: String,
    val authorId: String,
    val sender: String,
    val avatarIndex: Int,
    val content: String,
    val imageUrl: String? = null,
    val likes: Int = 0,
    val timestamp: String = "Just now"
)

@Entity(tableName = "comments")
data class PostComment(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val postId: String,
    val authorId: String,
    val sender: String,
    val avatarIndex: Int,
    val text: String,
    val timestamp: String = "Just now"
)

@Entity(tableName = "messages")
data class ChatMessage(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val familyId: String,
    val senderId: String,
    val sender: String,
    val avatarIndex: Int,
    val text: String,
    val timestamp: String = "Just now"
)

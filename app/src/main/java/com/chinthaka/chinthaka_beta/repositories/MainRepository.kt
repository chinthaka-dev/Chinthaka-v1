package com.chinthaka.chinthaka_beta.repositories

import android.net.Uri
import com.chinthaka.chinthaka_beta.data.entities.Comment
import com.chinthaka.chinthaka_beta.data.entities.Post
import com.chinthaka.chinthaka_beta.data.entities.ProfileUpdate
import com.chinthaka.chinthaka_beta.data.entities.User
import com.chinthaka.chinthaka_beta.other.Resource


interface MainRepository {

    suspend fun createPost(imageUri : Uri, text: String) : Resource<Any>

    suspend fun getUsers(userIds: List<String>) : Resource<List<User>>

    suspend fun getUser(userId : String) : Resource<User>

    suspend fun getPostsForFollows(): Resource<List<Post>>

    suspend fun toggleLikeForPost(post: Post): Resource<Boolean>

    suspend fun deletePost(post: Post): Resource<Post>

    suspend fun getPostsForProfile(userId: String): Resource<List<Post>>

    suspend fun toggleFollowForUser(userId: String): Resource<Boolean>

    suspend fun searchUser(query: String): Resource<List<User>>

    suspend fun createComment(commentText: String, postId: String): Resource<Comment>

    suspend fun deleteComment(comment: Comment): Resource<Comment>

    suspend fun getCommentsForPost(postId: String): Resource<List<Comment>>

    suspend fun updateProfile(profileUpdate: ProfileUpdate): Resource<Any>

    suspend fun updateProfilePicture(uid: String, imageUri: Uri): Uri?
}
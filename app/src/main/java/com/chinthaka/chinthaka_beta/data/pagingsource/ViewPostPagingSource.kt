package com.chinthaka.chinthaka_beta.data.pagingsource

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.chinthaka.chinthaka_beta.data.entities.Post
import com.chinthaka.chinthaka_beta.data.entities.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await

class ViewPostPagingSource(
    private val db: FirebaseFirestore,
    private val postId: String,
) : PagingSource<QuerySnapshot, Post>() {

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, Post> {
        // Load the posts
        // Everytime the data is paginated

        return try {
            val uid = FirebaseAuth.getInstance().uid!!

            val currentUser = db.collection("users")
                .document(uid)
                .get()
                .await()
                .toObject(User::class.java)

            val resultList = mutableListOf<Post>()

            val post = db.collection("posts").document(postId).get().await().toObject(Post::class.java)

            val user = post?.let { db.collection("users").document(it.authorUId).get().await().toObject(User::class.java) }!!

            post.authorProfilePictureUrl = user.profilePictureUrl
            post.authorUserName = user.userName
            post.isLiked = uid in post.likedBy
            if (currentUser != null) {
                post.isBookmarked = post.id in currentUser.bookmarks
            }

            resultList.add(post)

            LoadResult.Page(
                resultList,
                null,
                null
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    // Look into this function
    override fun getRefreshKey(state: PagingState<QuerySnapshot, Post>): QuerySnapshot? {
        return null
    }
}
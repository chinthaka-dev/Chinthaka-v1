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

class BookmarkPostsPagingSource(
    private val db: FirebaseFirestore,
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

            val chunks = currentUser!!.bookmarks.chunked(10)
            val resultList = mutableListOf<Post>()

            var curPage = params.key
            chunks.forEach { chunk ->
                curPage = params.key ?: db.collection("posts")
                    .whereIn("id", chunk)
                    .orderBy("date", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val userIdToUserMap: MutableMap<String,User> = mutableMapOf()

                val parsedPage = curPage!!.toObjects(Post::class.java).onEach { post ->
                    if(userIdToUserMap.containsKey(post.authorUId)) {
                        val user = userIdToUserMap.get(post.authorUId)
                        post.authorProfilePictureUrl = user!!.profilePictureUrl
                        post.authorUserName = user.userName
                        post.isLiked = uid in post.likedBy
                        post.isBookmarked = post.id in currentUser.bookmarks
                    } else {
                        val user = db.collection("users")
                            .document(post.authorUId).get().await().toObject(User::class.java)!!
                        post.authorProfilePictureUrl = user.profilePictureUrl
                        post.authorUserName = user.userName
                        post.isLiked = uid in post.likedBy
                        post.isBookmarked = post.id in currentUser.bookmarks
                        userIdToUserMap.put(post.authorUId,user)
                    }
                }

                resultList.addAll(parsedPage)
            }

            //Get the last Document of the current page
            val lastDocumentSnapshot = curPage!!.documents[curPage!!.size() - 1]

            // And we load the nextPage using the lastDocument of the previous page
            val nextPage = db.collection("posts")
                .whereIn("authorUId", if (chunks.isNotEmpty()) chunks[0] else listOf())
                .orderBy("date", Query.Direction.DESCENDING)
                .startAfter(lastDocumentSnapshot)
                .get()
                .await()


            LoadResult.Page(
                resultList,
                null,
                nextPage
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
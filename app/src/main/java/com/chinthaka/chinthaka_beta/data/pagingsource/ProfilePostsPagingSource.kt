package com.chinthaka.chinthaka_beta.data.pagingsource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.chinthaka.chinthaka_beta.data.entities.Post
import com.chinthaka.chinthaka_beta.data.entities.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await

class ProfilePostsPagingSource(
    private val db: FirebaseFirestore,
    private val uid: String
) : PagingSource<QuerySnapshot, Post>() {

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, Post> {
        // Load the posts
        // Everytime the data is paginated

        return try {
            val curPage = params.key ?: db.collection("posts")
                .whereEqualTo("authorUId", uid)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .await()

            //Get the last Document of the current page
            val lastDocumentSnapshot = curPage.documents[curPage.size() - 1]

            // And we load the nextPage using the lastDocument of the previous page
            val nextPage = db.collection("posts")
                .whereEqualTo("authorUId", uid)
                .orderBy("date", Query.Direction.DESCENDING)
                .startAfter(lastDocumentSnapshot)
                .get()
                .await()

            LoadResult.Page(
                curPage.toObjects(Post::class.java)
                    .onEach { post ->
                        val user = db.collection("users").document(uid).get().await().toObject(User::class.java)!!
                        post.authorProfilePictureUrl = user.profilePictureUrl
                        post.authorUserName = user.userName
                        post.isLiked = uid in post.likedBy
                    },
                null,
                nextPage
            )
        } catch (e : Exception){
            LoadResult.Error(e)
        }
    }

    // Look into this function
    override fun getRefreshKey(state: PagingState<QuerySnapshot, Post>): QuerySnapshot? {
        return null
    }
}
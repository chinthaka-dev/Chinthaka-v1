package com.chinthaka.chinthaka_beta.data.pagingsource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.chinthaka.chinthaka_beta.data.entities.Post
import com.chinthaka.chinthaka_beta.data.entities.User
import com.chinthaka.chinthaka_beta.other.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await

class FeedPostsPagingSource(
    private val db: FirebaseFirestore
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
                .toObject(User::class.java)!!

            val interestsForCurrentUser = currentUser.selectedInterests
            val resultSet: MutableSet<Post> = HashSet()

            val recentPosts = db.collection("posts")
                .whereIn("category", interestsForCurrentUser)
                .limit(20)
                .get()
                .await()
                .toObjects(Post::class.java)

            val attemptedButUnansweredPostIds = currentUser.postsAttempted

            val top5AttemptedPostIds = mutableListOf<String>()
            var i = 0
            while(i <= 5 && attemptedButUnansweredPostIds.size > i){
                top5AttemptedPostIds.add(attemptedButUnansweredPostIds[i])
                i++
            }

            if(top5AttemptedPostIds.size != 0){
                val attemptedButUnansweredPosts = db.collection("posts")
                    .whereIn("id", top5AttemptedPostIds)
                    .limit(5)
                    .get()
                    .await()
                    .toObjects(Post::class.java)

                resultSet.addAll(attemptedButUnansweredPosts)
            }

            val answeredPosts = mutableListOf<Post>()
            recentPosts.forEach { post ->
                if(post.answerViewedBy.contains(uid) || post.answeredBy.contains(uid)) {
                    answeredPosts.add(post)
                }
            }
            recentPosts.removeAll(answeredPosts)
            resultSet.addAll(recentPosts)

            val parsedPage = resultSet
                .onEach { post ->
                    val user = db.collection("users")
                        .document(post.authorUId).get().await().toObject(User::class.java)!!
                    post.authorProfilePictureUrl = user.profilePictureUrl
                    post.authorUserName = user.userName
                    post.isLiked = uid in post.likedBy
                    post.isBookmarked = post.id in currentUser!!.bookmarks
                }.toList()


            LoadResult.Page(
                parsedPage,
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
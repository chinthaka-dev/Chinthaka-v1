package com.chinthaka.chinthaka_beta.data.pagingsource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.chinthaka.chinthaka_beta.data.entities.Post
import com.chinthaka.chinthaka_beta.data.entities.User
import com.chinthaka.chinthaka_beta.other.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await

class FeedPostsPagingSource(
    private val db: FirebaseFirestore
) : PagingSource<QuerySnapshot, Post>() {

    var firstLoad = true
    lateinit var adminUser: User

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

            if (firstLoad) {
                adminUser = db.collection("users")
                    .document(Constants.ADMIN_USER_ID)
                    .get()
                    .await()
                    .toObject(User::class.java)!!
                firstLoad = false
            }

            val postIdsAnsweredByCurrentUser = currentUser.postsAnswered
            val postIdsAttemptedByCurrentUser = currentUser.postsAttempted
            val interestsForCurrentUser = currentUser.selectedInterests
            val postIdsForWhichAnswerHasAlreadyBeenViewed = currentUser.postsOfWhichAnswerHasBeenSeen

            val attemptedButUnansweredPostIds = postIdsAttemptedByCurrentUser.filter { postId -> postId !in postIdsAnsweredByCurrentUser || postId !in postIdsForWhichAnswerHasAlreadyBeenViewed}

            val popularPostsForTopics = db.collection("posts")
                .whereIn("category", interestsForCurrentUser)
                .orderBy("popularityIndex", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(Post::class.java)

            val recentPostsForTopics =  db.collection("posts")
                .whereIn("category", interestsForCurrentUser)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(Post::class.java)

            // Combining Popular Posts And Recent Posts
            val combinedPostIdsOfPopularAndRecentPosts = sequence {
                val popularPosts = popularPostsForTopics.iterator()
                val recentPosts = recentPostsForTopics.iterator()
                while (popularPosts.hasNext() && recentPosts.hasNext()) {
                    yield(popularPosts.next())
                    yield(recentPosts.next())
                }

                yieldAll(popularPosts)
                yieldAll(recentPosts)
            }.toList()

            val unansweredCombinedPostIds = combinedPostIdsOfPopularAndRecentPosts.filter { post -> uid !in post.answeredBy && uid !in post.answerViewedBy }.map { post -> post.id }

            val finalListOfPostIds = sequence {
                val attemptedButUnansweredPosts = attemptedButUnansweredPostIds.iterator()
                val unansweredCombinedPosts = unansweredCombinedPostIds.iterator()
                while (attemptedButUnansweredPosts.hasNext() && unansweredCombinedPosts.hasNext()) {
                    yield(attemptedButUnansweredPosts.next())
                    yield(unansweredCombinedPosts.next())
                }

                yieldAll(attemptedButUnansweredPosts)
                yieldAll(unansweredCombinedPosts)
            }.toSet()

            val resultList = mutableListOf<Post>()

            val postIdsChunks = finalListOfPostIds.chunked(10)

            var curPage = params.key

            postIdsChunks.forEach { chunk ->
                curPage = params.key ?: db.collection("posts")
                    .whereIn("id", chunk)
                    .get()
                    .await()


                // curPage -> Query Snapshot

                val parsedPage = curPage!!.toObjects(Post::class.java)
                    .onEach { post ->

                        val user = db.collection("users")
                            .document(post.authorUId).get().await().toObject(User::class.java)!!

                        post.authorProfilePictureUrl = user.profilePictureUrl
                        post.authorUserName = user.userName
                        post.isLiked = uid in post.likedBy
                        post.isBookmarked = post.id in currentUser!!.bookmarks
                    }

                val map = parsedPage.associateBy({it.id}, {it})

                chunk.forEach { postId -> map.get(postId)?.let { resultList.add(it) } }

            }

            //Get the last Document of the current page
            val lastDocumentSnapshot = curPage!!.documents[curPage!!.size() - 1]

            // And we load the nextPage using the lastDocument of the previous page
            val nextPage = db.collection("posts")
                .whereIn("id", if(postIdsChunks.isNotEmpty()) postIdsChunks[0] else listOf())
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
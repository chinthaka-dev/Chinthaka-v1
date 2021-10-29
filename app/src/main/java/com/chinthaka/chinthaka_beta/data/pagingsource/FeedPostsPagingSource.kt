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

            val interestsForCurrentUser = currentUser.selectedInterests
            val resultSet: MutableSet<Post> = HashSet()

            val recentPostsForTopics =  db.collection("posts")
                    .whereIn("category", interestsForCurrentUser)
                    //.whereNotIn("answerViewedBy", listOf(uid))
                    //.whereNotIn("answeredBy", listOf(uid))
                    .orderBy("date", Query.Direction.DESCENDING)
                    .limit(15)
                    .get()
                    .await()
                    .toObjects(Post::class.java)

            val attemptedButUnansweredPosts =  db.collection("posts")
                    .whereIn("attemptedBy", listOf(uid))
                    .limit(5)
                    .get()
                    .await()
                    .toObjects(Post::class.java)

                resultSet.addAll(attemptedButUnansweredPosts)
                resultSet.addAll(recentPostsForTopics)

                val answeredPosts = mutableListOf<Post>()

                resultSet.forEach { post ->
                    if(post.answerViewedBy.contains(uid) || post.answeredBy.contains(uid)) {
                        answeredPosts.add(post)
                    }
                }

                resultSet.removeAll(answeredPosts)

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
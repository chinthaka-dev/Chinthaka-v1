package com.chinthaka.chinthaka_beta.repositories

import android.net.Uri
import android.util.Log
import com.chinthaka.chinthaka_beta.data.entities.Comment
import com.chinthaka.chinthaka_beta.data.entities.Post
import com.chinthaka.chinthaka_beta.data.entities.ProfileUpdate
import com.chinthaka.chinthaka_beta.data.entities.User
import com.chinthaka.chinthaka_beta.other.Constants.ANSWERED_BY_WEIGHT
import com.chinthaka.chinthaka_beta.other.Constants.ANSWER_VIEWED_BY_WEIGHT
import com.chinthaka.chinthaka_beta.other.Constants.DEFAULT_PROFILE_PICTURE_URL
import com.chinthaka.chinthaka_beta.other.Constants.LIKED_BY_WEIGHT
import com.chinthaka.chinthaka_beta.other.Constants.RECENTNESS_WEIGHT
import com.chinthaka.chinthaka_beta.other.Resource
import com.chinthaka.chinthaka_beta.other.safeCall
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*


@ActivityScoped     // @ActivityScoped is used to make sure we always pick the single instance always.
class DefaultMainRepository : MainRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = Firebase.storage
    private val users = firestore.collection("users")
    private val posts = firestore.collection("posts")
    private val comments = firestore.collection("comments")

    override suspend fun createPost(imageUri: Uri, text: String) = withContext(Dispatchers.IO) {
        safeCall {
            val userId = auth.uid!!
            val postId = UUID.randomUUID().toString()
            val imageUploadResult = storage.getReference(postId).putFile(imageUri).await()
            val imageUrl = imageUploadResult?.metadata?.reference?.downloadUrl?.await().toString()
            val post = Post(
                id = postId,
                authorUId = userId,
                text = text,
                imageUrl = imageUrl,
                date = System.currentTimeMillis()
            )
            posts.document(postId).set(post).await()
            Resource.Success(Any())
        }
    }

    override suspend fun getUsers(userIds: List<String>) = withContext(Dispatchers.IO) {
        safeCall {

            // Chunks are important as Firebase limits the results of wherein queries to 10.
            val chunks = userIds.chunked(10)
            var resultList = mutableListOf<User>()

            chunks.forEach { chunk ->
                val userList = users.whereIn("userId", userIds)
                    .orderBy("userName")
                    .get()
                    .await()
                    .toObjects(User::class.java)
                resultList.addAll(userList)
            }

            Resource.Success(resultList.toList())
        }
    }

    override suspend fun getUser(userId: String) = withContext(Dispatchers.IO) {
        safeCall {
            val user = users.document(userId).get().await().toObject(User::class.java)
                ?: throw IllegalStateException()
            val currentUserId = FirebaseAuth.getInstance().uid!!
            val currentUser = users.document(currentUserId).get().await().toObject(User::class.java)
                ?: throw IllegalStateException()
            user.isFollowing = userId in currentUser.follows
            Resource.Success(user)
        }
    }

    override suspend fun getPostsForFollows() = withContext(Dispatchers.IO) {
        safeCall {
            val userId = FirebaseAuth.getInstance().uid!!
//            val follows = getUser(userId).data!!.follows
//            val allPosts = posts.whereIn("authorUId", follows)
            val allPosts = posts
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(Post::class.java)
                .onEach { post ->
                    val user = getUser(post.authorUId).data!!
                    post.authorProfilePictureUrl = user.profilePictureUrl
                    post.authorUserName = user.userName
                    post.isLiked = userId in post.likedBy
                }
            Resource.Success(allPosts)
        }
    }

    override suspend fun toggleLikeForPost(post: Post) = withContext(Dispatchers.IO) {
        safeCall {
            var isLiked = false

            // For Reading and Writing Documents
            firestore.runTransaction { transaction ->
                val userId = FirebaseAuth.getInstance().uid!!
                val postResult = transaction.get(posts.document(post.id))
                val currentLikes = postResult.toObject(Post::class.java)?.likedBy ?: listOf()
                transaction.update(
                    posts.document(post.id),
                    "likedBy",
                    if (userId in currentLikes) currentLikes - userId else {
                        isLiked = true
                        currentLikes + userId
                    }
                )
            }.await()

            updatePopularityIndexForPost(post.id)

            Resource.Success(isLiked)
        }
    }

    override suspend fun deletePost(post: Post) = withContext(Dispatchers.IO) {
        safeCall {
            posts.document(post.id).delete().await()
            storage.getReference(post.imageUrl).delete().await()
            Resource.Success(post)
        }
    }

    override suspend fun getPostsForProfile(userId: String) = withContext(Dispatchers.IO) {
        safeCall {
            val profilePosts = posts.whereEqualTo("authorUId", userId)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(Post::class.java)
                .onEach { post ->
                    val user = getUser(post.authorUId).data!!
                    post.authorProfilePictureUrl = user.profilePictureUrl
                    post.authorUserName = user.userName
                    post.isLiked = userId in post.likedBy
                }

            Resource.Success(profilePosts)
        }
    }

    override suspend fun toggleFollowForUser(userId: String) = withContext(Dispatchers.IO) {
        safeCall {
            var isFollowing = false
            firestore.runTransaction { transaction ->
                val currentUserId = auth.uid!!
                val currentUser =
                    transaction.get(users.document(currentUserId)).toObject(User::class.java)!!
                isFollowing = userId in currentUser.follows
                val newFollows =
                    if (isFollowing) currentUser.follows - userId else currentUser.follows + userId
                transaction.update(users.document(currentUserId), "follows", newFollows)
            }.await()
            Resource.Success(isFollowing)
        }
    }

    override suspend fun searchUser(query: String) = withContext(Dispatchers.IO) {
        safeCall {
            val userResults =
                users.whereGreaterThanOrEqualTo("userName", query.uppercase(Locale.ROOT))
                    .get()
                    .await()
                    .toObjects(User::class.java)

            Resource.Success(userResults)
        }
    }

    override suspend fun createComment(commentText: String, postId: String) =
        withContext(Dispatchers.IO) {
            safeCall {
                val userId = auth.uid!!
                val commentId = UUID.randomUUID().toString()
                val user = getUser(userId).data!!
                val comment = Comment(
                    commentId,
                    postId,
                    userId,
                    user.userName,
                    user.profilePictureUrl,
                    commentText
                )

                comments.document(commentId).set(comment).await()
                Resource.Success(comment)
            }
        }

    override suspend fun deleteComment(comment: Comment) = withContext(Dispatchers.IO) {
        safeCall {
            comments.document(comment.commentId).delete().await()
            Resource.Success(comment)
        }
    }

    override suspend fun getCommentsForPost(postId: String) = withContext(Dispatchers.IO) {
        safeCall {
            val commentsForPost = comments
                .whereEqualTo("postId", postId)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(Comment::class.java)
                .onEach { comment ->
                    val user = getUser(comment.userId).data!!
                    comment.userName = user.userName
                    comment.profilePictureUrl = user.profilePictureUrl
                }
            Resource.Success(commentsForPost)
        }
    }

    override suspend fun updateProfile(profileUpdate: ProfileUpdate) = withContext(Dispatchers.IO) {
        safeCall {
            val imageUrl = profileUpdate.profilePictureUri?.let { uri ->
                updateProfilePicture(profileUpdate.uidToUpdate, uri).toString()

            }
            val map = mutableMapOf(
                "userName" to profileUpdate.userName,
                "description" to profileUpdate.description,
            )
            imageUrl?.let { url ->
                map["profilePictureUrl"] = url
            }
            users.document(profileUpdate.uidToUpdate).update(map.toMap()).await()
            Resource.Success(Any())
        }
    }

    override suspend fun updateProfilePicture(uid: String, imageUri: Uri) =
        withContext(Dispatchers.IO) {
            val storageRef = storage.getReference(uid)
            val user = getUser(uid).data!!
            if (user.profilePictureUrl != DEFAULT_PROFILE_PICTURE_URL) {
                storage.getReferenceFromUrl(user.profilePictureUrl).delete().await()
            }

            storageRef.putFile(imageUri).await().metadata?.reference?.downloadUrl?.await()
        }

    override suspend fun submitAnswerForPost(postId: String) = withContext(Dispatchers.IO) {
        safeCall {
            var isAnswered = false
            // For Reading and Writing Documents
            firestore.runTransaction { transaction ->
                val userId = FirebaseAuth.getInstance().uid!!
                val postResult = transaction.get(posts.document(postId))
                val currentAnsweredBy = postResult.toObject(Post::class.java)?.answeredBy ?: listOf()
                if (userId !in currentAnsweredBy) {
                    isAnswered = true
                    transaction.update(
                        posts.document(postId),
                        "answeredBy",
                        currentAnsweredBy + userId
                    )
                }
            }.await()

            updatePopularityIndexForPost(postId)
            Resource.Success(isAnswered)
        }
    }

    override suspend fun toggleBookmarkForPost(postId: String) = withContext(Dispatchers.IO) {
        safeCall {
            var hasBookmarked = false
            Log.i("MAIN_REPOSITORY", "Post : $postId is being bookmarked")
            firestore.runTransaction { transaction ->
                val currentUserId = auth.uid!!
                val currentUser =
                    transaction.get(users.document(currentUserId)).toObject(User::class.java)!!
                hasBookmarked = postId in currentUser.bookmarks
                transaction.update(
                    users.document(currentUserId), "bookmarks",
                    if (hasBookmarked) {
                        hasBookmarked = false
                        currentUser.bookmarks - postId
                    } else {
                        hasBookmarked = true
                        currentUser.bookmarks + postId
                    }
                )
            }.await()
            Resource.Success(hasBookmarked)
        }
    }

    override suspend fun updateAnswerViewedByForPost(post: Post) = withContext(Dispatchers.IO) {
        safeCall {
            var hasViewedAnswer = false
            firestore.runTransaction { transaction ->
                val currentUserId = auth.uid!!
                hasViewedAnswer = currentUserId in post.answerViewedBy
                if (!hasViewedAnswer) {
                    hasViewedAnswer = true
                    transaction.update(
                        posts.document(post.id),
                        "answerViewedBy", post.answerViewedBy + currentUserId
                    )
                }
            }.await()
            updatePopularityIndexForPost(post.id)
            Resource.Success(hasViewedAnswer)
        }
    }

    override suspend fun updateAttemptedByForPost(post: Post) = withContext(Dispatchers.IO) {
        safeCall {
            var hasAttempted = false
            firestore.runTransaction { transaction ->
                val currentUserId = auth.uid!!
                hasAttempted = currentUserId in post.attemptedBy
                if (!hasAttempted) {
                    hasAttempted = true
                    transaction.update(
                        posts.document(post.id),
                        "attemptedBy", post.attemptedBy + currentUserId
                    )
                }
            }.await()
            updatePopularityIndexForPost(post.id)
            Resource.Success(hasAttempted)
        }
    }

    override suspend fun updatePopularityIndexForPost(postId: String) = withContext(Dispatchers.IO) {
        safeCall {
            var isPopularityIndexUpdated = false
            val totalUsers = users.get().await().size()
            firestore.runTransaction { transaction ->
                val post = transaction.get(posts.document(postId)).toObject(Post::class.java)!!
                val recentness = (System.currentTimeMillis() - post.date)/ (System.currentTimeMillis())
                val popularityIndexNumerator = ((RECENTNESS_WEIGHT * recentness) + (LIKED_BY_WEIGHT * post.likedBy.size) +
                        (ANSWERED_BY_WEIGHT * post.answeredBy.size) + (ANSWER_VIEWED_BY_WEIGHT * post.answerViewedBy.size))
                val popularityIndexDenominator = 4 * totalUsers
                val newPopularityIndex = (popularityIndexNumerator / popularityIndexDenominator)
                if(!isPopularityIndexUpdated){
                    isPopularityIndexUpdated = true
                    transaction.update(
                        posts.document(post.id),
                        "popularityIndex", newPopularityIndex.coerceAtLeast(0.5)
                    )
                }
            }.await()
            Resource.Success(isPopularityIndexUpdated)
        }
    }

}
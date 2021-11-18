package com.chinthaka.chinthaka_beta.ui.main.fragments

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.RequestManager
import com.chinthaka.chinthaka_beta.R
import com.chinthaka.chinthaka_beta.adapters.PostAdapter
import com.chinthaka.chinthaka_beta.data.entities.Metric
import com.chinthaka.chinthaka_beta.other.EventObserver
import com.chinthaka.chinthaka_beta.repositories.MetricRepository
import com.chinthaka.chinthaka_beta.ui.main.dialogs.DeletePostDialog
import com.chinthaka.chinthaka_beta.ui.main.dialogs.ViewAnswerDialog
import com.chinthaka.chinthaka_beta.ui.main.viewmodels.BasePostViewModel
import com.chinthaka.chinthaka_beta.ui.snackbar
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

import android.graphics.Bitmap

import android.net.Uri
import android.graphics.BitmapFactory
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import android.provider.MediaStore.Images

import android.content.ContentValues
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.chinthaka.chinthaka_beta.ui.main.MainActivity
import java.io.OutputStream
import java.lang.Exception


abstract class BasePostFragment(
    layoutId: Int
) : Fragment(layoutId) {

    @Inject
    lateinit var glide: RequestManager

    @Inject
    lateinit var postAdapter: PostAdapter

    private lateinit var popupMenu: PopupMenu

    protected abstract val basePostViewModel: BasePostViewModel

    private var curLikedIndex: Int? = null

    private var curAnsweredIndex: Int? = null

    var curBookmarkedIndex: Int? = null

    private val metricRepository = MetricRepository()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToObservers()

        postAdapter.setOnLikeClickListener { post, i ->
            curLikedIndex = i
            post.isLiked = !post.isLiked
            basePostViewModel.toggleLikeForPost(post)
            metricRepository.recordClicksOnMetric(Metric.NUMBER_OF_LIKES)
        }

        postAdapter.setOnExpandClickListener{ post, i ->
            curBookmarkedIndex = i
            post.isBookmarked = !post.isBookmarked
            basePostViewModel.toggleBookmarkForPost(post)
            metricRepository.recordClicksOnMetric(Metric.CLICKS_ON_BOOKMARK)
        }

        postAdapter.setOnAnswerClickListener { post, i ->
            curAnsweredIndex = i
            basePostViewModel.updateAttemptedByForPost(post)
            if(userId in post.answeredBy){
                snackbar("This post has already been answered by you!")
            }
            else if(userId in post.answerViewedBy){
                snackbar("You have already viewed the answer for this post!")
            }
            else {
                findNavController().navigate(
                    R.id.globalActionToSubmitAnswerFragment,
                    Bundle().apply {
                        putString("answer", post.answer.getValue("text"))
                        putString("postId", post.id)
                        putInt("currentIndex", curAnsweredIndex!!)
                        putString("description", post.answer.getValue("description"))
                        putString("imageUrl", post.answer.getValue("answerImageUrl"))
                    },
                )
            }
        }

        postAdapter.setOnViewAnswerClickListener { post, i ->
            curAnsweredIndex = i
            Log.i("BASE_POST_FRAGMENT", "AnswerImageUrl : ${post.answer.getValue("answerImageUrl")}")
            if(userId in post.answerViewedBy){
                findNavController().navigate(
                    R.id.globalActionToViewAnswerFragment,
                    Bundle().apply {
                        putString("answer", post.answer.getValue("text"))
                        putString("description", post.answer.getValue("description"))
                        putString("imageUrl", post.answer.getValue("answerImageUrl"))
                    }
                )
            } else {
                ViewAnswerDialog().apply {
                    setPositiveListener {
                        basePostViewModel.updateAnswerViewedByForPost(post)
                        findNavController().popBackStack()
                        findNavController().navigate(
                            R.id.globalActionToViewAnswerFragment,
                            Bundle().apply {
                                putString("answer", post.answer.getValue("text"))
                                putString("description", post.answer.getValue("description"))
                                putString("imageUrl", post.answer.getValue("answerImageUrl"))
                            }
                        )
                    }
                }.show(childFragmentManager, null)
            }
        }

        postAdapter.setOnShareClickListener { post ->

            if (ContextCompat.checkSelfPermission(activity as MainActivity, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED) {
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_TEXT,
                    "${post.authorUserName} has challenged you to answer this question -> https://www.chinthaka.in/post?id="+post.id)
                metricRepository.recordClicksOnMetric(Metric.CLICKS_ON_SHARE)
                startActivity(Intent.createChooser(intent, "Share using"))
            } else {
                val icon: Bitmap = getBitmapFromURL(post.imageUrl)!!
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "image/jpeg"

                val values = ContentValues()
                values.put(Images.Media.TITLE, "title")
                values.put(Images.Media.MIME_TYPE, "image/jpeg")
                val uri: Uri? = context?.getContentResolver()?.insert(
                    Images.Media.EXTERNAL_CONTENT_URI,
                    values
                )

                val outstream: OutputStream
                try {
                    outstream = uri?.let { context?.getContentResolver()?.openOutputStream(it) }!!
                    icon.compress(Bitmap.CompressFormat.JPEG, 100, outstream)
                    outstream.close()
                } catch (e: Exception) {
                    System.err.println(e.toString())
                }

                intent.putExtra(Intent.EXTRA_STREAM, uri)
                intent.putExtra(Intent.EXTRA_TEXT,
                    "${FirebaseAuth.getInstance().currentUser?.displayName} has challenged you to " +
                            "answer this question on Chinthaka > https://www.chinthaka.in/post?id="+post.id)
                metricRepository.recordClicksOnMetric(Metric.CLICKS_ON_SHARE)
                startActivity(Intent.createChooser(intent, "Share Using"))
            }
        }

        postAdapter.setOnDeletePostClickListener { post ->
            DeletePostDialog().apply {
                setPositiveListener {
                    basePostViewModel.deletePost(post)
                }
            }.show(childFragmentManager, null)
        }

        /*
        TODO : Check if the issue of automatic likedBy Counter is fixed.
        Issue : If we click on LikedBy and come back, Likes automatically increases by 1
        */
        postAdapter.setOnLikedByClickListener { post ->
            findNavController().navigate(
                R.id.globalActionToUsersFragment,
                Bundle().apply {
                    putStringArray("userIds", post.likedBy.toTypedArray())
                }
            )
        }

        postAdapter.setOnAnsweredByClickListener { post ->
            findNavController().navigate(
                R.id.globalActionToUsersFragment,
                Bundle().apply {
                    putStringArray("userIds", post.answeredBy.toTypedArray())
                }
            )
        }


        /*postAdapter.setOnAuthorImageClickListener { post ->
            findNavController().navigate(
                R.id.globalActionToOthersProfileFragment,
                Bundle().apply { putString("userId", post.authorUId) }
            )
        }*/
    }

    private fun subscribeToObservers() {

        basePostViewModel.likePostStatus.observe(viewLifecycleOwner, EventObserver(
            onError = {
                curLikedIndex?.let { index ->
                    postAdapter.posts[index].isLiking = false
                    postAdapter.notifyItemChanged(index)
                }
                snackbar(it)
            },
            onLoading = {
                curLikedIndex?.let { index ->
                    postAdapter.posts[index].isLiking = true
                    postAdapter.notifyItemChanged(index)
                }
            }
        ) { isLiked ->
            curLikedIndex?.let { index ->
                val userId = FirebaseAuth.getInstance().uid!!
                postAdapter.posts[index].apply {
                    this.isLiked = isLiked
                    isLiking = false
                    if (isLiked) {
                        likedBy += userId
                    } else {
                        likedBy -= userId
                    }
                }
                postAdapter.notifyItemChanged(index)
            }
        })

        basePostViewModel.bookmarkPostStatus.observe(viewLifecycleOwner, EventObserver(
            onError = {
                curBookmarkedIndex?.let { index ->
                    postAdapter.posts[index].isBookmarking = false
                    postAdapter.notifyItemChanged(index)
                }
                snackbar(it)
            },
            onLoading = {
                curBookmarkedIndex?.let { index ->
                    postAdapter.posts[index].isBookmarking = true
                    postAdapter.notifyItemChanged(index)
                }
            }
        ) { isBookmarked ->
            curBookmarkedIndex?.let { index ->
                postAdapter.posts[index].apply {
                    this.isBookmarked = isBookmarked
                    isBookmarking = false
                }
                postAdapter.notifyItemChanged(index)
            }
        })

        basePostViewModel.attemptedByUpdateStatus.observe(viewLifecycleOwner, EventObserver(
            onError = {
                curAnsweredIndex?.let { index ->
                    postAdapter.posts[index].isAttempting = false
                    postAdapter.notifyItemChanged(index)
                }
                snackbar(it)
            },
            onLoading = {
                curAnsweredIndex?.let { index ->
                    postAdapter.posts[index]?.isAttempting = true
                    postAdapter.notifyItemChanged(index)
                }
            }
        ) { isAttempted ->
            curAnsweredIndex?.let { index ->
                postAdapter.posts[index].apply {
                    isAttempting = false
                }
                postAdapter.notifyItemChanged(index)
            }
        })

    }

    open fun getBitmapFromURL(src: String?): Bitmap? {
        return try {
            val url = URL(src)
            val connection: HttpURLConnection = url
                .openConnection() as HttpURLConnection
            connection.setDoInput(true)
            connection.connect()
            val input: InputStream = connection.getInputStream()
            BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    open val userId: String
        get() = FirebaseAuth.getInstance().uid!!
}
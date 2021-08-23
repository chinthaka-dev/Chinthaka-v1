package com.chinthaka.chinthaka_beta.ui.main.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.RequestManager
import com.chinthaka.chinthaka_beta.R
import com.chinthaka.chinthaka_beta.adapters.PostAdapter
import com.chinthaka.chinthaka_beta.adapters.UserAdapter
import com.chinthaka.chinthaka_beta.data.entities.Post
import com.chinthaka.chinthaka_beta.other.EventObserver
import com.chinthaka.chinthaka_beta.ui.main.dialogs.AnsweredByDialog
import com.chinthaka.chinthaka_beta.ui.main.dialogs.DeletePostDialog
import com.chinthaka.chinthaka_beta.ui.main.dialogs.LikedByDialog
import com.chinthaka.chinthaka_beta.ui.main.dialogs.ViewAnswerDialog
import com.chinthaka.chinthaka_beta.ui.main.viewmodels.BasePostViewModel
import com.chinthaka.chinthaka_beta.ui.snackbar
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

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

    private var curBookmarkedIndex: Int? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToObservers()

        postAdapter.setOnLikeClickListener { post, i ->
            curLikedIndex = i
            post.isLiked = !post.isLiked
            basePostViewModel.toggleLikeForPost(post)
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
                    R.id.action_homeFragment_to_submitAnswerFragment,
                    Bundle().apply {
                        putString("answer", post.answer.getValue("text"))
                        putString("postId", post.id)
                        putInt("currentIndex", curAnsweredIndex!!)
                    },
                )
            }
        }

        postAdapter.setOnViewAnswerClickListener { post, i ->
            curAnsweredIndex = i
            if(userId in post.answerViewedBy){
                findNavController().navigate(
                    R.id.globalActionToViewAnswerFragment,
                    Bundle().apply {
                        putString("answer", post.answer.getValue("text"))
                        putString("description", post.answer.getValue("description"))
                        putString("imageUrl", post.answer.getValue("imageUrl"))
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
                                putString("imageUrl", post.answer.getValue("imageUrl"))
                            }
                        )
                    }
                }.show(childFragmentManager, null)
            }
        }

        postAdapter.setOnShareClickListener { post ->
            val intent: Intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(
                Intent.EXTRA_TEXT,
                "${post.authorUserName} has challenged you to answer the question."
            )
            intent.putExtra(Intent.EXTRA_TEXT, "http://play.google.com")
            startActivity(Intent.createChooser(intent, "Share using"))
        }

        postAdapter.setOnDeletePostClickListener { post ->
            DeletePostDialog().apply {
                setPositiveListener {
                    basePostViewModel.deletePost(post)
                }
            }.show(childFragmentManager, null)
        }

        postAdapter.setOnLikedByClickListener { post ->
            basePostViewModel.getUsers(post.likedBy)
        }

        postAdapter.setOnAnsweredByClickListener { post ->
            basePostViewModel.getUsers(post.answeredBy)
        }

//        postAdapter.setOnCommentsClickListener { post ->
//            findNavController().navigate(
//                R.id.globalActionToCommentDialog,
//                Bundle().apply { putString("postId", post.id) }
//            )
//        }

        postAdapter.setOnAuthorImageClickListener { post ->
            findNavController().navigate(
                R.id.globalActionToOthersProfileFragment,
                Bundle().apply { putString("userId", post.authorUId) }
            )
        }
    }

    private fun subscribeToObservers() {

        basePostViewModel.likePostStatus.observe(viewLifecycleOwner, EventObserver(
            onError = {
                curLikedIndex?.let { index ->
                    postAdapter.peek(index)?.isLiking = false
                    postAdapter.notifyItemChanged(index)
                }
                snackbar(it)
            },
            onLoading = {
                curLikedIndex?.let { index ->
                    postAdapter.peek(index)?.isLiking = true
                    postAdapter.notifyItemChanged(index)
                }
            }
        ) { isLiked ->
            curLikedIndex?.let { index ->
                val userId = FirebaseAuth.getInstance().uid!!
                postAdapter.peek(index)?.apply {
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
                    postAdapter.peek(index)?.isBookmarking = false
                    postAdapter.notifyItemChanged(index)
                }
                snackbar(it)
            },
            onLoading = {
                curBookmarkedIndex?.let { index ->
                    postAdapter.peek(index)?.isBookmarking = true
                    postAdapter.notifyItemChanged(index)
                }
            }
        ) { isBookmarked ->
            curBookmarkedIndex?.let { index ->
                postAdapter.peek(index)?.apply {
                    this.isBookmarked = isBookmarked
                    isBookmarking = false
                }
                postAdapter.notifyItemChanged(index)
            }
        })

        basePostViewModel.attemptedByUpdateStatus.observe(viewLifecycleOwner, EventObserver(
            onError = {
                curAnsweredIndex?.let { index ->
                    postAdapter.peek(index)?.isAttempting = false
                    postAdapter.notifyItemChanged(index)
                }
                snackbar(it)
            },
            onLoading = {
                curAnsweredIndex?.let { index ->
                    postAdapter.peek(index)?.isAttempting = true
                    postAdapter.notifyItemChanged(index)
                }
            }
        ) { isAttempted ->
            curAnsweredIndex?.let { index ->
                postAdapter.peek(index)?.apply {
                    isAttempting = false
                }
                postAdapter.notifyItemChanged(index)
            }
        })

        basePostViewModel.likedByUsers.observe(viewLifecycleOwner, EventObserver(
            onError = { snackbar(it) }
        ) { users ->
            val userAdapter = UserAdapter(glide)
            userAdapter.users = users
            LikedByDialog(userAdapter).show(childFragmentManager, null)
        })

        basePostViewModel.answeredByUsers.observe(viewLifecycleOwner, EventObserver(
            onError = { snackbar(it) }
        ) { users ->
            val userAdapter = UserAdapter(glide)
            userAdapter.users = users
            AnsweredByDialog(userAdapter).show(childFragmentManager, null)
        })

    }

    open val userId: String
        get() = FirebaseAuth.getInstance().uid!!
}
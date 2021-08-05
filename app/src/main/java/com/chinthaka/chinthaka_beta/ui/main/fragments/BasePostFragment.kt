package com.chinthaka.chinthaka_beta.ui.main.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.RequestManager
import com.chinthaka.chinthaka_beta.AuthActivity
import com.chinthaka.chinthaka_beta.R
import com.chinthaka.chinthaka_beta.adapters.PostAdapter
import com.chinthaka.chinthaka_beta.adapters.UserAdapter
import com.chinthaka.chinthaka_beta.other.EventObserver
import com.chinthaka.chinthaka_beta.ui.main.dialogs.DeletePostDialog
import com.chinthaka.chinthaka_beta.ui.main.dialogs.LikedByDialog
import com.chinthaka.chinthaka_beta.ui.main.viewmodels.BasePostViewModel
import com.chinthaka.chinthaka_beta.ui.snackbar
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

abstract class BasePostFragment(
    layoutId : Int
): Fragment(layoutId) {

    @Inject
    lateinit var glide: RequestManager

    @Inject
    lateinit var postAdapter: PostAdapter

    protected abstract val basePostViewModel: BasePostViewModel

    private var curLikedIndex: Int? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToObservers()

        postAdapter.setOnLikeClickListener{post, i ->
            curLikedIndex = i
            post.isLiked = !post.isLiked
            basePostViewModel.toggleLikeForPost(post)
        }

        postAdapter.setOnViewAnswerClickListener { post, i ->
            findNavController().navigate(
                R.id.globalActionToViewAnswerDialog,
                Bundle().apply { putString("postId", post.id) }
            )
        }

        postAdapter.setOnShareClickListener { post ->
            val intent: Intent = Intent(Intent.ACTION_SEND)
            intent.setType("text/plain")
            intent.putExtra(Intent.EXTRA_TEXT, "${post.authorUserName} has challenged you to answer the question.")
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

        postAdapter.setOnCommentsClickListener { post ->
            findNavController().navigate(
                R.id.globalActionToCommentDialog,
                Bundle().apply { putString("postId", post.id) }
            )
        }
    }

    private fun subscribeToObservers(){

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
            curLikedIndex?.let{index ->
                val userId = FirebaseAuth.getInstance().uid!!
                postAdapter.peek(index)?.apply {
                    this.isLiked = isLiked
                    isLiking = false
                    if(isLiked){
                        likedBy += userId
                    } else{
                        likedBy -= userId
                    }
                }
                postAdapter.notifyItemChanged(index)
            }
        })

        basePostViewModel.likedByUsers.observe(viewLifecycleOwner, EventObserver(
            onError = {snackbar(it)}
        ) { users ->
            val userAdapter = UserAdapter(glide)
            userAdapter.users = users
            LikedByDialog(userAdapter).show(childFragmentManager, null)
        })
    }
}
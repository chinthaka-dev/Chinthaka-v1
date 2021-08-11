package com.chinthaka.chinthaka_beta.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.chinthaka.chinthaka_beta.R
import com.chinthaka.chinthaka_beta.data.entities.Post
import com.chinthaka.chinthaka_beta.databinding.ItemPostBinding
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class PostAdapter @Inject constructor(
    private val glide: RequestManager
) : PagingDataAdapter<Post, PostAdapter.PostViewHolder>(Companion) {

    inner class PostViewHolder(binding: ItemPostBinding): RecyclerView.ViewHolder(binding.root){
        val ivPostImage: ImageView = binding.ivPostImage
        val ivAuthorProfileImage: ImageView = binding.ivAuthorProfileImage
        val tvPostAuthor: TextView = binding.tvPostAuthor
        val tvPostText: TextView = binding.tvPostText
        val tvLikedBy: TextView = binding.tvLikedBy
        val tvAnsweredBy: TextView = binding.tvAnsweredBy
        val iblike: ImageButton = binding.ibLike
        val ibAnswer: ImageButton = binding.ibAnswer
        val ibDeletePost: ImageButton = binding.ibExpandPost
        val ibViewAnswer: ImageButton = binding.ibViewAnswer
        val ibShare: ImageButton = binding.ibShare
    }

    companion object : DiffUtil.ItemCallback<Post>(){

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem.id == newItem.id
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {

        val post = getItem(position) ?: return

        holder.apply {
            glide.load(post.imageUrl).into(ivPostImage)
            glide.load(post.authorProfilePictureUrl).into(ivAuthorProfileImage)
            tvPostAuthor.text = post.authorUserName
            tvPostText.text = post.text
            val likeCount = post.likedBy.size
            val answeredByCount = post.answeredBy.size
//            tvLikedBy.text = when{
//                likeCount <= 0 -> "No Likes"
//                likeCount == 1 -> "Liked by 1 person"
//                else -> "Likey by $likeCount people"
//            }
            tvLikedBy.text = likeCount.toString()
            tvAnsweredBy.text = answeredByCount.toString()
//            ibAnswer.isClickable = post.isAnswered
            val userId = FirebaseAuth.getInstance().uid!!
//            ibDeletePost.isVisible = userId == post.authorUId
            iblike.setImageResource(if(post.isLiked){
                R.drawable.ic_thumbs_up_filled
            } else R.drawable.ic_thumbs_up)

            tvPostAuthor.setOnClickListener{
                onUserClickListener?.let { click ->
                    click(post.authorUId)
                }
            }

            ivAuthorProfileImage.setOnClickListener{
                onUserClickListener?.let { click ->
                    click(post.authorUId)
                }
            }

            tvLikedBy.setOnClickListener{
                onLikedByClickListener?.let { click ->
                    click(post)
                }
            }

            tvAnsweredBy.setOnClickListener {
                onAnsweredByClickListener?.let { click ->
                    click(post)
                }
            }

            iblike.setOnClickListener{
                onLikeClickListener?.let { click ->
                    if(!post.isLiking) click(post, holder.layoutPosition)
                }
            }

            ibAnswer.setOnClickListener {
                onAnswerClickListener?.let { click ->
                    if(!post.isAnswering) click(post, holder.layoutPosition)
                }
            }

//            ibComments.setOnClickListener {
//                onCommentsClickListener?.let { click ->
//                    click(post)
//                }
//            }

            ibViewAnswer.setOnClickListener {
                onViewAnswerClickListener?.let { click ->
                    click(post, holder.layoutPosition)
                }
            }

            ibShare.setOnClickListener {
                onShareClickListener?.let { click ->
                    click(post)
                }
            }

            ibDeletePost.setOnClickListener {
                onDeletePostClickListener?.let { click ->
                    click(post)
                }
            }

            ivAuthorProfileImage.setOnClickListener{
                onAuthorImageClickListener?.let { click ->
                    click(post)
                }
            }

        }

    }

    private var onLikeClickListener: ((Post, Int) -> Unit)? = null
    private var onAnswerClickListener: ((Post, Int) -> Unit)? = null
    private var onViewAnswerClickListener: ((Post, Int) -> Unit)? = null
    private var onShareClickListener: ((Post) -> Unit )? = null
    private var onUserClickListener: ((String) -> Unit)? = null
    private var onDeletePostClickListener: ((Post) -> Unit)? = null
    private var onLikedByClickListener: ((Post) -> Unit)? = null
    private var onAnsweredByClickListener: ((Post) -> Unit)? = null
//    private var onCommentsClickListener: ((Post) -> Unit)? = null
    private var onAuthorImageClickListener: ((Post) -> Unit)? = null

    fun setOnLikeClickListener(listener: (Post, Int) -> Unit){
        onLikeClickListener = listener
    }

    fun setOnAnswerClickListener(listener: (Post, Int) -> Unit){
        onAnswerClickListener = listener
    }

    fun setOnViewAnswerClickListener(listener: (Post, Int) -> Unit){
        onViewAnswerClickListener = listener
    }

    fun setOnShareClickListener(listener:(Post) -> Unit){
        onShareClickListener = listener
    }

    fun setOnUserClickListener(listener: (String) -> Unit){
        onUserClickListener = listener
    }

    fun setOnDeletePostClickListener(listener: (Post) -> Unit){
        onDeletePostClickListener = listener
    }

    fun setOnLikedByClickListener(listener: (Post) -> Unit){
        onLikedByClickListener = listener
    }

    fun setOnAnsweredByClickListener(listener: (Post) -> Unit){
        onAnsweredByClickListener = listener
    }

//    fun setOnCommentsClickListener(listener: (Post) -> Unit){
//        onCommentsClickListener = listener
//    }

    fun setOnAuthorImageClickListener(listener: (Post) -> Unit){
        onAuthorImageClickListener = listener
    }

}
package com.chinthaka.chinthaka_beta.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.chinthaka.chinthaka_beta.data.entities.Comment
import com.chinthaka.chinthaka_beta.databinding.ItemCommentBinding
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class CommentAdapter @Inject constructor(
    private val glide: RequestManager
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(binding: ItemCommentBinding): RecyclerView.ViewHolder(binding.root){
        val tvCommentUserName: TextView = binding.tvCommentUsername
        val tvComment: TextView = binding.tvComment
        val ibDeleteComment: ImageButton = binding.ibDeleteComment
        val ivCommentUserProfilePicture: ImageView = binding.ivCommentUserProfilePicture
    }

    private val diffCallback = object : DiffUtil.ItemCallback<Comment>(){

        override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

        override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem.commentId == newItem.commentId
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    var comments: List<Comment>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return CommentViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return comments.size
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {

        val comment = comments[position]
        holder.apply {
            glide.load(comment.profilePictureUrl).into(ivCommentUserProfilePicture)

            ibDeleteComment.isVisible = comment.userId == FirebaseAuth.getInstance().uid!!

            tvComment.text = comment.comment
            tvCommentUserName.text = comment.userName
            tvCommentUserName.setOnClickListener{
                onUserClickListener?.let{ click ->
                    click(comment)
                }
            }
            ibDeleteComment.setOnClickListener {
                onDeleteClickListener?.let{ click ->
                    click(comment)
                }
            }
        }
    }

    private var onUserClickListener: ((Comment) -> Unit)? = null
    private var onDeleteClickListener: ((Comment) -> Unit)? = null

    fun setOnUserClickListener(listener: (Comment) -> Unit){
        onUserClickListener = listener
    }

    fun setOnDeleteCommentsClickListener(listener: (Comment) -> Unit){
        onDeleteClickListener = listener
    }
}
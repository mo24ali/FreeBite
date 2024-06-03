package com.example.freebite2.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.freebite2.R
import com.example.freebite2.databinding.AdminUserItemBinding
import com.example.freebite2.model.User

class UsersAdapter(private var users: List<User>, private val onUserClickListener: OnUserClickListener) : RecyclerView.Adapter<UsersAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = AdminUserItemBinding.inflate(layoutInflater, parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount(): Int = users.size

    inner class UserViewHolder(private val binding: AdminUserItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.user = user
            binding.executePendingBindings()

            binding.root.setOnClickListener {
                onUserClickListener.onUserClick(user)
            }

            binding.moreBtn.setOnClickListener { view ->
                val popup = PopupMenu(view.context, view)
                popup.menuInflater.inflate(R.menu.user_menu, popup.menu)
                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.edit_user -> {
                            onUserClickListener.onEditUserClick(user)
                            true
                        }
                        R.id.delete_user -> {
                            onUserClickListener.onDeleteUserClick(user)
                            true
                        }
                        else -> false
                    }
                }
                popup.show()
            }
        }
    }

    fun updateList(newUserList: List<User>) {
        users = newUserList
        notifyDataSetChanged()
    }

    interface OnUserClickListener {
        fun onUserClick(user: User)
        fun onEditUserClick(user: User)
        fun onDeleteUserClick(user: User)
    }
}

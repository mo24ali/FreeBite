package com.example.freebite2.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.freebite2.R
import com.example.freebite2.model.Chat
import de.hdodenhof.circleimageview.CircleImageView

class ChatAdapter(private val chatList: List<Chat>) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chatList[position]
        holder.username.text = chat.sender
        holder.lastMessage.text = chat.message

        // Chargement de l'image de profil
        chat.profileImage?.let { imageUri ->
            Glide.with(holder.itemView.context)
                .load(imageUri)
                .placeholder(R.mipmap.ic_launcher) // Placeholder image
                .into(holder.profileImage)
        }

        holder.itemView.setOnClickListener {
            // Gestion du clic sur un élément du RecyclerView
            // À implémenter selon les besoins
        }
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val username: TextView = itemView.findViewById(R.id.username)
        val lastMessage: TextView = itemView.findViewById(R.id.last_message)
        val profileImage: CircleImageView = itemView.findViewById(R.id.profile_image)
    }
}

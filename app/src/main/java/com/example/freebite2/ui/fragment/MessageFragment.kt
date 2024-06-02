package com.example.freebite2.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.freebite2.R
import com.example.freebite2.adapter.ChatAdapter
import com.example.freebite2.model.Chat
import com.example.freebite2.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

import de.hdodenhof.circleimageview.CircleImageView

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MessageFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MessageFragment : Fragment() {

    private lateinit var profileImage: CircleImageView
    private lateinit var username: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var chatList: ArrayList<Chat>
    private lateinit var adapter: ChatAdapter
    private lateinit var fuser: FirebaseUser
    private lateinit var reference: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_message, container, false)

        profileImage = view.findViewById(R.id.profile_image)
        username = view.findViewById(R.id.username)
        recyclerView = view.findViewById(R.id.recycler_view)

        recyclerView.layoutManager = LinearLayoutManager(context)
        chatList = ArrayList()
        adapter = ChatAdapter(chatList)
        recyclerView.adapter = adapter

        fuser = FirebaseAuth.getInstance().currentUser!!

        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.uid)
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                username.text = user?.nom
                if (user?.profilePictureUrl == "default") {
                    profileImage.setImageResource(R.mipmap.ic_launcher)
                } else {
                    Glide.with(requireContext()).load(user?.profilePictureUrl).into(profileImage)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Gérer les erreurs
            }
        })

        loadChats()

        return view
    }

    private fun loadChats() {
        val chatRef = FirebaseDatabase.getInstance().getReference("Chats")
        chatRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatList.clear()
                for (dataSnapshot in snapshot.children) {
                    val chat = dataSnapshot.getValue(Chat::class.java)
                    if (chat != null) {
                        if (chat.sender == fuser.uid || chat.receiver == fuser.uid) {
                            chatList.add(chat)
                        }
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Gérer les erreurs
            }
        })
    }
}

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.freebite2.R
import com.example.freebite2.model.User
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class UserAdapter(private val userList: List<User>, private val context: Context) :
    RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    private lateinit var database: DatabaseReference

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = userList[position]

        holder.nameTextView.text = user.prenom + " " + user.nom
        holder.emailTextView.text = user.email

        holder.deleteButton.setOnClickListener {
            deleteUser(user.uid)
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        val emailTextView: TextView = itemView.findViewById(R.id.emailTextView)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
    }

    private fun deleteUser(userId: String) {
        database = FirebaseDatabase.getInstance().reference.child("users").child(userId)
        database.removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "User deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to delete user", Toast.LENGTH_SHORT).show()
            }
    }
}

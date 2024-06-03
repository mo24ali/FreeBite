import androidx.lifecycle.ViewModel
import com.example.freebite2.model.OffreModel
import com.google.firebase.database.FirebaseDatabase

class UpdateOffreViewModel : ViewModel() {
    private val offreRepository = OffreRepository() // Vous devez implémenter ce repository

    fun updateOffre(offre: OffreModel) {
        offreRepository.updateOffre(offre)
    }
}
class OffreRepository {
    private val database = FirebaseDatabase.getInstance().reference.child("offres")

    fun updateOffre(offre: OffreModel) {
        offre.offerID?.let { database.child(it).setValue(offre) }
    }
}


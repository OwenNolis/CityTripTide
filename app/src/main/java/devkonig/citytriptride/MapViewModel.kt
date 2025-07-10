package devkonig.citytriptride

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MapViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _cities = MutableStateFlow<List<CityWithId>>(emptyList())
    val cities: StateFlow<List<CityWithId>> = _cities

    private val _sights = MutableStateFlow<List<Sight>>(emptyList())
    val sights: StateFlow<List<Sight>> = _sights

    init {
        db.collection("cities")
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(City::class.java)?.let { CityWithId(doc.id, it) }
                } ?: emptyList()
                _cities.value = list
            }

        db.collection("sights")
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.toObjects(Sight::class.java) ?: emptyList()
                _sights.value = list
            }
    }
}
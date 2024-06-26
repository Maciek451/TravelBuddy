package uk.ac.aber.dcs.chm9360.travelbuddy.ui

import com.google.firebase.firestore.FirebaseFirestore
import uk.ac.aber.dcs.chm9360.travelbuddy.model.Phrase

class FirestoreRepository {
    private val fb = FirebaseFirestore.getInstance()
    private val phrasesData = fb.collection("phrases")

    fun addPhrase(phrase: Phrase, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        phrasesData.add(phrase)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun getPhrases(onSuccess: (List<Phrase>) -> Unit, onFailure: (Exception) -> Unit) {
        phrasesData.get()
            .addOnSuccessListener { snapshot ->
                val phrases = snapshot.toObjects(Phrase::class.java)
                onSuccess(phrases)
            }
            .addOnFailureListener { onFailure(it) }
    }
}
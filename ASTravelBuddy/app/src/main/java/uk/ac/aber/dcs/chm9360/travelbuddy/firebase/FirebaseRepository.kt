package uk.ac.aber.dcs.chm9360.travelbuddy.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import uk.ac.aber.dcs.chm9360.travelbuddy.model.Phrase

class FirebaseRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val phrasesData = db.collection("phrases")

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

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun signIn(email: String, password: String, onResult: (Boolean) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true)
                } else {
                    onResult(false)
                }
            }
    }

    fun signUp(email: String, password: String, username: String, onResult: (Boolean) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userId = user?.uid
                    if (userId != null) {
                        val userMap = hashMapOf(
                            "username" to username,
                            "email" to email
                        )
                        db.collection("users").document(userId).set(userMap)
                            .addOnSuccessListener {
                                onResult(true)
                            }
                            .addOnFailureListener {
                                onResult(false)
                            }
                    } else {
                        onResult(false)
                    }
                } else {
                    onResult(false)
                }
            }
    }

    fun signOut() {
        auth.signOut()
    }
}
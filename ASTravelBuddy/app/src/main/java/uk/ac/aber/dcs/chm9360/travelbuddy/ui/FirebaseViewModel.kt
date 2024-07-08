package uk.ac.aber.dcs.chm9360.travelbuddy.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import uk.ac.aber.dcs.chm9360.travelbuddy.model.Phrase

class FirebaseViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val phrasesData = db.collection("phrases")

    private val _phrases = MutableStateFlow<List<Phrase>>(emptyList())
    val phrases: StateFlow<List<Phrase>> get() = _phrases

    private val _authState = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val authState: StateFlow<FirebaseUser?> get() = _authState

    init {
        fetchPhrases()
    }

    fun addPhrase(phrase: Phrase) {
        phrasesData.add(phrase)
            .addOnSuccessListener { fetchPhrases() }
            .addOnFailureListener { }
    }

    fun fetchPhrases() {
        viewModelScope.launch {
            phrasesData.get()
                .addOnSuccessListener { snapshot ->
                    val phrases = snapshot.toObjects(Phrase::class.java)
                    _phrases.value = phrases
                }
                .addOnFailureListener { }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _authState.value = auth.currentUser
                    }
                }
        }
    }

    fun signUp(email: String, password: String, username: String) {
        viewModelScope.launch {
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
                                    _authState.value = auth.currentUser
                                }
                                .addOnFailureListener { }
                        }
                    }
                }
        }
    }

    fun signOut() {
        auth.signOut()
        _authState.value = null
    }
}

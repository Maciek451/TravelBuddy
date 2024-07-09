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
        observeAuthState()
        fetchPhrases()
    }

    private fun observeAuthState() {
        auth.addAuthStateListener { firebaseAuth ->
            _authState.value = firebaseAuth.currentUser
        }
    }

    fun signIn(email: String, password: String, callback: (Boolean, Boolean) -> Unit) {
        viewModelScope.launch {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        val isVerified = user?.isEmailVerified ?: false
                        _authState.value = user

                        callback(true, isVerified)
                    } else {
                        callback(false, false) // Sign-in failed
                    }
                }
        }
    }

    fun signUp(email: String, password: String, username: String, onResult: (Boolean) -> Unit) {
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
                                    sendVerificationEmail(onResult)
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
    }

    fun signOut() {
        auth.signOut()
        _authState.value = null
    }

    fun sendVerificationEmail(onResult: (Boolean) -> Unit) {
        val user = auth.currentUser
        user?.sendEmailVerification()
            ?.addOnCompleteListener { task ->
                onResult(task.isSuccessful)
            }
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
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

    fun addPhrase(phrase: Phrase) {
        phrasesData.add(phrase)
            .addOnSuccessListener { fetchPhrases() }
            .addOnFailureListener { }
    }
}

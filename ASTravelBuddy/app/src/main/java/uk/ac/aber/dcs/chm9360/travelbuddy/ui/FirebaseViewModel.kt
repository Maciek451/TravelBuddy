package uk.ac.aber.dcs.chm9360.travelbuddy.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import uk.ac.aber.dcs.chm9360.travelbuddy.model.Phrase
import uk.ac.aber.dcs.chm9360.travelbuddy.utils.AuthenticationState

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

    fun signIn(email: String, password: String, callback: (Int) -> Unit) {
        viewModelScope.launch {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        val isVerified = user?.isEmailVerified ?: false
                        _authState.value = user

                        if (isVerified) {
                            callback(AuthenticationState.LOGGED_IN_SUCCESSFULLY)
                        } else {
                            callback(AuthenticationState.USER_IS_NOT_VERIFIED)
                        }
                    } else {
                        val exception = task.exception
                        when (exception) {
                            is FirebaseAuthInvalidCredentialsException -> {
                                callback(AuthenticationState.PASSWORD_WRONG)
                            }
                            is FirebaseAuthInvalidUserException -> {
                                callback(AuthenticationState.ACCOUNT_DOES_NOT_EXIST)
                            }
                            else -> {
                                callback(AuthenticationState.OTHER)
                            }
                        }
                    }
                }
        }
    }

    fun signUp(email: String, password: String, username: String, callback: (Int) -> Unit) {
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
                                    sendVerificationEmail { isSuccess ->
                                        if (isSuccess) {
                                            callback(AuthenticationState.SIGNED_UP_SUCCESSFULLY)
                                        } else {
                                            callback(AuthenticationState.OTHER)
                                        }
                                    }
                                }
                                .addOnFailureListener {
                                    callback(AuthenticationState.OTHER)
                                }
                        } else {
                            callback(AuthenticationState.OTHER)
                        }
                    } else {
                        val exception = task.exception
                        when (exception) {
                            is FirebaseAuthUserCollisionException -> {
                                callback(AuthenticationState.USER_ALREADY_EXISTS)
                            }
                            is FirebaseAuthInvalidCredentialsException -> {
                                callback(AuthenticationState.EMAIL_WRONG_FORMAT)
                            }
                            else -> {
                                callback(AuthenticationState.OTHER)
                            }
                        }
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

    fun deleteUserAccount(onResult: (Boolean) -> Unit) {
        val user = auth.currentUser
        val userId = user?.uid
        if (userId != null) {
            val batch = db.batch()

            val userDocRef = db.collection("users").document(userId)
            batch.delete(userDocRef)

            db.collection("phrases")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { snapshot ->
                    for (document in snapshot.documents) {
                        val docRef = db.collection("phrases").document(document.id)
                        batch.delete(docRef)
                    }

                    batch.commit()
                        .addOnSuccessListener {
                            user.delete()
                                .addOnSuccessListener {
                                    auth.signOut()
                                    _authState.value = null
                                    onResult(true)
                                }
                                .addOnFailureListener {
                                    onResult(false)
                                }
                        }
                        .addOnFailureListener {
                            onResult(false)
                        }
                }
                .addOnFailureListener {
                    onResult(false)
                }
        } else {
            onResult(false)
        }
    }

    fun removeAllUserData(onResult: (Boolean) -> Unit) {
        phrasesData.get()
            .addOnSuccessListener { snapshot ->
                val batch = db.batch()
                snapshot.documents.forEach { doc ->
                    val ref = phrasesData.document(doc.id)
                    batch.delete(ref)
                }
                batch.commit()
                    .addOnSuccessListener {
                        onResult(true)
                    }
                    .addOnFailureListener {
                        onResult(false)
                    }
            }
            .addOnFailureListener {
                onResult(false)
            }
    }

    fun fetchPhrases() {
        val user = auth.currentUser
        if (user != null) {
            viewModelScope.launch {
                db.collection("users").document(user.uid)
                    .collection("phrases")
                    .get()
                    .addOnSuccessListener { snapshot ->
                        val phrases = snapshot.toObjects(Phrase::class.java)
                        _phrases.value = phrases
                    }
                    .addOnFailureListener { }
            }
        }
    }

    fun addPhrase(phrase: Phrase) {
        val user = auth.currentUser
        if (user != null) {
            viewModelScope.launch {
                db.collection("users").document(user.uid)
                    .collection("phrases")
                    .add(phrase)
                    .addOnSuccessListener {
                        fetchPhrases()
                    }
                    .addOnFailureListener { }
            }
        }
    }
}

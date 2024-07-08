package uk.ac.aber.dcs.chm9360.travelbuddy.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import uk.ac.aber.dcs.chm9360.travelbuddy.firebase.FirebaseRepository
import uk.ac.aber.dcs.chm9360.travelbuddy.model.Phrase

class FirebaseViewModel : ViewModel() {
    private val repository = FirebaseRepository()

    private val _phrases = MutableStateFlow<List<Phrase>>(emptyList())
    val phrases: StateFlow<List<Phrase>> get() = _phrases

    private val _authState = MutableStateFlow<FirebaseUser?>(repository.getCurrentUser())
    val authState: StateFlow<FirebaseUser?> get() = _authState

    init {
        fetchPhrases()
    }

    fun addPhrase(phrase: Phrase) {
        repository.addPhrase(phrase,
            onSuccess = { fetchPhrases() },
            onFailure = { }
        )
    }

    fun fetchPhrases() {
        viewModelScope.launch {
            repository.getPhrases(
                onSuccess = { _phrases.value = it },
                onFailure = { }
            )
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            repository.signInWithEmailAndPassword(email, password) { isSuccess ->
                if (isSuccess) {
                    _authState.value = repository.getCurrentUser()
                }
            }
        }
    }

    fun signUp(email: String, password: String, username: String) {
        viewModelScope.launch {
            repository.signUpWithEmailAndPassword(email, password, username) { isSuccess ->
                if (isSuccess) {
                    _authState.value = repository.getCurrentUser()
                }
            }
        }
    }

    fun signOut() {
        repository.signOut()
        _authState.value = null
    }
}
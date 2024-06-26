package uk.ac.aber.dcs.chm9360.travelbuddy.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import uk.ac.aber.dcs.chm9360.travelbuddy.model.Phrase

class FirebaseViewModel : ViewModel() {
    private val repository = FirestoreRepository()

    private val _phrases = MutableStateFlow<List<Phrase>>(emptyList())
    val phrases: StateFlow<List<Phrase>> get () = _phrases

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
}
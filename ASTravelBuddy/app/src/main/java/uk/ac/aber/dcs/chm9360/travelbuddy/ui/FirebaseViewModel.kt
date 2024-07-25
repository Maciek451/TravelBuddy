package uk.ac.aber.dcs.chm9360.travelbuddy.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import uk.ac.aber.dcs.chm9360.travelbuddy.model.Phrase
import uk.ac.aber.dcs.chm9360.travelbuddy.model.Trip
import uk.ac.aber.dcs.chm9360.travelbuddy.model.User
import uk.ac.aber.dcs.chm9360.travelbuddy.utils.AuthenticationState

class FirebaseViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _username = MutableStateFlow<String?>(null)
    val username: StateFlow<String?> get() = _username

    private val _phrases = MutableStateFlow<List<Phrase>>(emptyList())
    val phrases: StateFlow<List<Phrase>> get() = _phrases

    private val _trips = MutableStateFlow<List<Trip>>(emptyList())
    val trips: StateFlow<List<Trip>> get() = _trips

    private val _authState = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val authState: StateFlow<FirebaseUser?> get() = _authState

    private val _friends = MutableStateFlow<List<User>>(emptyList())
    val friends: StateFlow<List<User>> get() = _friends

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> get() = _isRefreshing

    init {
        observeAuthState()
        fetchPhrases()
        fetchTrips()
        fetchFriends()
    }

    private fun observeAuthState() {
        auth.addAuthStateListener { firebaseAuth ->
            _authState.value = firebaseAuth.currentUser
        }
    }

    fun signInWithEmailAndPassword(email: String, password: String, callback: (Int) -> Unit) {
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
                        when (val exception = task.exception) {
                            is FirebaseAuthInvalidCredentialsException -> {
                                when (exception.errorCode) {
                                    "ERROR_INVALID_CREDENTIAL" -> callback(AuthenticationState.PASSWORD_WRONG)
                                    "ERROR_INVALID_EMAIL" -> callback(AuthenticationState.EMAIL_WRONG_FORMAT)
                                    else -> callback(AuthenticationState.OTHER)
                                }
                            }

                            else -> {
                                callback(AuthenticationState.OTHER)
                            }
                        }
                    }
                }
        }
    }

    fun signUpWithEmailAndPassword(
        email: String,
        password: String,
        username: String,
        callback: (Int) -> Unit
    ) {
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

    fun sendPasswordResetEmail(email: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    onResult(task.isSuccessful)
                }
        }
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun deleteUserAccount(onResult: (Boolean) -> Unit) {
        val user = auth.currentUser
        if (user != null) {
            removeAllUserData { success ->
                if (success) {
                    val userDocRef = db.collection("users").document(user.uid)
                    userDocRef.delete()
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
                } else {
                    onResult(false)
                }
            }
        } else {
            onResult(false)
        }
    }

    fun removeAllUserData(onResult: (Boolean) -> Unit) {
        val user = auth.currentUser
        if (user != null) {
            val batch = db.batch()
            val userDocRef = db.collection("users").document(user.uid)

            userDocRef.collection("phrases")
                .get()
                .addOnSuccessListener { snapshot ->
                    for (document in snapshot.documents) {
                        val docRef = userDocRef.collection("phrases").document(document.id)
                        batch.delete(docRef)
                    }

                    userDocRef.collection("trips")
                        .get()
                        .addOnSuccessListener { tripSnapshot ->
                            for (document in tripSnapshot.documents) {
                                val docRef = userDocRef.collection("trips").document(document.id)
                                batch.delete(docRef)
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
                .addOnFailureListener {
                    onResult(false)
                }
        } else {
            onResult(false)
        }
    }

    fun fetchUsername() {
        val user = auth.currentUser
        if (user != null) {
            viewModelScope.launch {
                try {
                    val document = db.collection("users").document(user.uid).get().await()
                    val fetchedUsername = document.getString("username")
                    _username.value = fetchedUsername
                } catch (_: Exception) {
                }
            }
        }
    }

    fun updateUsername(newUsername: String, callback: (Boolean) -> Unit) {
        val user = auth.currentUser
        if (user != null) {
            db.collection("users").document(user.uid)
                .update("username", newUsername)
                .addOnSuccessListener {
                    _username.value = newUsername
                    callback(true)
                }
                .addOnFailureListener {
                    callback(false)
                }
        }
    }

    private suspend fun fetchFriendsList(userId: String): List<User> {
        return try {
            val friendsSnapshot = db.collection("users").document(userId)
                .collection("friends")
                .get()
                .await()

            friendsSnapshot.toObjects(User::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun fetchFriends() {
        val user = auth.currentUser
        if (user != null) {
            viewModelScope.launch {
                db.collection("users").document(user.uid)
                    .collection("friends")
                    .get()
                    .addOnSuccessListener { snapshot ->
                        val friends = snapshot.map { document ->
                            User(
                                userId = document.id,
                                username = document.getString("username") ?: "",
                                email = document.getString("email") ?: ""
                            )
                        }
                        _friends.value = friends
                    }
                    .addOnFailureListener { }
            }
        }
    }

    private fun addFriend(currentUserId: String, friendId: String, friendUsername: String, friendEmail: String, callback: (Boolean) -> Unit) {
        val currentUserRef = db.collection("users").document(currentUserId)
        val friendData = mapOf(
            "userId" to friendId,
            "username" to friendUsername,
            "email" to friendEmail
        )

        currentUserRef.collection("friends").document(friendId).set(friendData)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    fun removeFriend(friendId: String, callback: (Boolean) -> Unit) {
        val currentUser = auth.currentUser ?: return callback(false)

        val currentUserRef = db.collection("users").document(currentUser.uid)
        currentUserRef.collection("friends").document(friendId)
            .delete()
            .addOnSuccessListener {
                fetchFriends()
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    fun findAndAddFriend(email: String, callback: (Boolean, String) -> Unit) {
        val currentUser = auth.currentUser ?: return callback(false, "User not authenticated")

        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    val userDoc = snapshot.documents[0]
                    val friendId = userDoc.id
                    val friendUsername = userDoc.getString("username") ?: ""
                    val friendEmail = userDoc.getString("email") ?: ""
                    checkIfFriendExists(currentUser.uid, friendId) { exists ->
                        if (exists) {
                            callback(false, "Friend is already added")
                        } else {
                            addFriend(currentUser.uid, friendId, friendUsername, friendEmail) { success ->
                                if (success) {
                                    callback(true, "Friend added successfully")
                                } else {
                                    callback(false, "Failed to add friend")
                                }
                            }
                        }
                    }
                } else {
                    callback(false, "No user found with this email")
                }
            }
            .addOnFailureListener { callback(false, "Failed to search by email") }
    }

    private fun checkIfFriendExists(currentUserId: String, friendId: String, callback: (Boolean) -> Unit) {
        val currentUserRef = db.collection("users").document(currentUserId)
        currentUserRef.collection("friends").document(friendId)
            .get()
            .addOnSuccessListener { document ->
                callback(document.exists())
            }
            .addOnFailureListener { callback(false) }
    }

    fun fetchPhrases() {
        viewModelScope.launch {
            val user = auth.currentUser
            if (user != null) {
                _isRefreshing.value = true
                try {
                    val userPhrasesSnapshot = db.collection("users").document(user.uid)
                        .collection("phrases")
                        .get()
                        .await()

                    val userPhrases = userPhrasesSnapshot.documents.mapNotNull { document ->
                        document.toObject(Phrase::class.java)
                            ?.copy(username = document.getString("username") ?: "")
                    }

                    val friends = fetchFriendsList(user.uid)
                    val friendPhrases = mutableListOf<Phrase>()

                    for (friend in friends) {
                        val friendPhrasesSnapshot = db.collection("users").document(friend.userId)
                            .collection("phrases")
                            .get()
                            .await()

                        val phrases = friendPhrasesSnapshot.documents.mapNotNull { document ->
                            document.toObject(Phrase::class.java)?.copy(username = friend.username)
                        }

                        friendPhrases.addAll(phrases)
                    }

                    val allPhrases = userPhrases + friendPhrases

                    _phrases.value = allPhrases
                } catch (e: Exception) {
                    Log.e("FirebaseViewModel", "Error fetching phrases", e)
                } finally {
                    _isRefreshing.value = false
                }
            }
        }
    }

    fun addPhrase(phrase: Phrase) {
        val user = auth.currentUser
        if (user != null) {
            viewModelScope.launch {
                try {
                    val userDocument = db.collection("users").document(user.uid).get().await()
                    val username = userDocument.getString("username") ?: "Unknown User"

                    val phraseWithUsername = phrase.copy(username = username)
                    db.collection("users").document(user.uid)
                        .collection("phrases")
                        .add(phraseWithUsername)
                        .await()

                    fetchPhrases()
                } catch (e: Exception) {
                    Log.e("FirebaseViewModel", "Error adding phrase", e)
                }
            }
        }
    }

    fun refreshPhrases() {
        fetchPhrases()
    }

    fun fetchTrips() {
        val user = auth.currentUser
        if (user != null) {
            viewModelScope.launch {
                db.collection("users").document(user.uid)
                    .collection("trips")
                    .get()
                    .addOnSuccessListener { snapshot ->
                        val trips = snapshot.toObjects(Trip::class.java)
                        _trips.value = trips
                    }
                    .addOnFailureListener { }
            }
        }
    }

    fun addTrip(trip: Trip) {
        val user = auth.currentUser
        if (user != null) {
            viewModelScope.launch {
                db.collection("users").document(user.uid)
                    .collection("trips")
                    .add(trip)
                    .addOnSuccessListener {
                        fetchTrips()
                    }
                    .addOnFailureListener { }
            }
        }
    }
}
package uk.ac.aber.dcs.chm9360.travelbuddy.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import uk.ac.aber.dcs.chm9360.travelbuddy.model.ChecklistItem
import uk.ac.aber.dcs.chm9360.travelbuddy.model.FriendRequest
import uk.ac.aber.dcs.chm9360.travelbuddy.model.Phrase
import uk.ac.aber.dcs.chm9360.travelbuddy.model.Trip
import uk.ac.aber.dcs.chm9360.travelbuddy.model.TripPlanItem
import uk.ac.aber.dcs.chm9360.travelbuddy.model.User
import uk.ac.aber.dcs.chm9360.travelbuddy.utils.AuthenticationState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    private val _creationDate = MutableStateFlow<String?>(null)
    val creationDate: StateFlow<String?> get() = _creationDate

    private val _friendRequests = MutableStateFlow<List<FriendRequest>>(emptyList())
    val friendRequests: StateFlow<List<FriendRequest>> get() = _friendRequests

    init {
        observeAuthState()
        fetchPhrases()
        fetchTrips()
        fetchFriends()
        fetchCreationDate()
        fetchFriendRequests()
    }

    private fun observeAuthState() {
        auth.addAuthStateListener { firebaseAuth ->
            _authState.value = firebaseAuth.currentUser
        }
    }

    private fun handleAuthResult(task: Task<AuthResult>, successCallback: () -> Unit, errorCallback: (Exception) -> Unit) {
        task.addOnCompleteListener { result ->
            if (result.isSuccessful) {
                successCallback()
            } else {
                errorCallback(result.exception ?: Exception("Unknown error"))
            }
        }
    }

    fun signInWithEmailAndPassword(email: String, password: String, callback: (Int) -> Unit) {
        viewModelScope.launch {
            handleAuthResult(
                auth.signInWithEmailAndPassword(email, password),
                {
                    val user = auth.currentUser
                    if (user?.isEmailVerified == true) {
                        callback(AuthenticationState.LOGGED_IN_SUCCESSFULLY)
                    } else {
                        callback(AuthenticationState.USER_IS_NOT_VERIFIED)
                    }
                },
                { exception ->
                    val errorCode = (exception as? FirebaseAuthException)?.errorCode
                    when (errorCode) {
                        "ERROR_INVALID_CREDENTIAL" -> callback(AuthenticationState.PASSWORD_WRONG)
                        "ERROR_INVALID_EMAIL" -> callback(AuthenticationState.EMAIL_WRONG_FORMAT)
                        else -> callback(AuthenticationState.OTHER)
                    }
                }
            )
        }
    }

    fun signUpWithEmailAndPassword(email: String, password: String, username: String, callback: (Int) -> Unit) {
        viewModelScope.launch {
            handleAuthResult(
                auth.createUserWithEmailAndPassword(email, password),
                {
                    val user = auth.currentUser
                    user?.uid?.let { userId ->
                        val userMap = mapOf("username" to username, "email" to email)
                        db.collection("users").document(userId).set(userMap)
                            .addOnSuccessListener {
                                sendVerificationEmail { isSuccess ->
                                    callback(if (isSuccess) AuthenticationState.SIGNED_UP_SUCCESSFULLY else AuthenticationState.OTHER)
                                }
                            }
                            .addOnFailureListener { callback(AuthenticationState.OTHER) }
                    } ?: callback(AuthenticationState.OTHER)
                },
                { exception ->
                    val errorCode = (exception as? FirebaseAuthException)?.errorCode
                    when (errorCode) {
                        "ERROR_EMAIL_ALREADY_IN_USE" -> callback(AuthenticationState.USER_ALREADY_EXISTS)
                        "ERROR_INVALID_EMAIL" -> callback(AuthenticationState.EMAIL_WRONG_FORMAT)
                        else -> callback(AuthenticationState.OTHER)
                    }
                }
            )
        }
    }

    fun signOut() {
        auth.signOut()
        _authState.value = null
    }

    fun fetchCreationDate() {
        val user = auth.currentUser
        user?.metadata?.creationTimestamp?.let { timestamp ->
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            _creationDate.value = dateFormat.format(Date(timestamp))
        }
    }

    fun sendVerificationEmail(onResult: (Boolean) -> Unit) {
        auth.currentUser?.sendEmailVerification()
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

    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    fun deleteUserAccount(onResult: (Boolean) -> Unit) {
        val user = auth.currentUser
        user?.let {
            viewModelScope.launch {
                try {
                    val dataRemoved = removeAllUserDataSync()
                    if (dataRemoved) {
                        db.collection("users").document(user.uid).delete().await()
                        user.delete().await()
                        auth.signOut()
                        _authState.value = null
                        onResult(true)
                    } else {
                        onResult(false)
                    }
                } catch (e: Exception) {
                    Log.e("FirebaseViewModel", "Error deleting user account", e)
                    onResult(false)
                }
            }
        } ?: onResult(false)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun removeAllUserDataSync(): Boolean = suspendCancellableCoroutine { continuation ->
        removeAllUserData { success ->
            continuation.resume(success) {
                continuation.cancel()
            }
        }
    }

    fun removeAllUserData(onResult: (Boolean) -> Unit) {
        val user = auth.currentUser
        if (user != null) {
            val batch = db.batch()
            val userDocRef = db.collection("users").document(user.uid)

            viewModelScope.launch {
                try {
                    val phrasesSnapshot = userDocRef.collection("phrases").get().await()
                    for (document in phrasesSnapshot.documents) {
                        batch.delete(document.reference)
                    }
                    val tripsSnapshot = userDocRef.collection("trips").get().await()
                    for (document in tripsSnapshot.documents) {
                        batch.delete(document.reference)
                    }
                    val friendsSnapshot = userDocRef.collection("friends").get().await()
                    for (document in friendsSnapshot.documents) {
                        batch.delete(document.reference)
                    }
                    batch.commit()
                        .addOnSuccessListener { onResult(true) }
                        .addOnFailureListener {
                            Log.e("FirebaseViewModel", "Error committing batch", it)
                            onResult(false)
                        }
                } catch (e: Exception) {
                    Log.e("FirebaseViewModel", "Error removing user data", e)
                    onResult(false)
                }
            }
        } else {
            onResult(false)
        }
    }

    fun fetchUsername() {
        auth.currentUser?.let { user ->
            viewModelScope.launch {
                try {
                    val document = db.collection("users").document(user.uid).get().await()
                    _username.value = document.getString("username")
                } catch (e: Exception) {
                    Log.e("FirebaseViewModel", "Error fetching username", e)
                }
            }
        }
    }

    fun updateUsername(newUsername: String, callback: (Boolean) -> Unit) {
        auth.currentUser?.let { user ->
            db.collection("users").document(user.uid)
                .update("username", newUsername)
                .addOnSuccessListener {
                    _username.value = newUsername
                    callback(true)
                }
                .addOnFailureListener { callback(false) }
        } ?: callback(false)
    }

    fun fetchFriends() {
        auth.currentUser?.let { user ->
            viewModelScope.launch {
                db.collection("users").document(user.uid)
                    .collection("friends")
                    .get()
                    .addOnSuccessListener { snapshot ->
                        val friends = snapshot.documents.mapNotNull { doc ->
                            User(
                                userId = doc.id,
                                username = doc.getString("username") ?: "",
                                email = doc.getString("email") ?: ""
                            )
                        }
                        _friends.value = friends
                    }
                    .addOnFailureListener { Log.e("FirebaseViewModel", "Error fetching friends") }
            }
        }
    }

    fun addFriend(currentUserId: String, friendId: String, friendUsername: String, friendEmail: String, callback: (Boolean) -> Unit) {
        val friendData = mapOf("userId" to friendId, "username" to friendUsername, "email" to friendEmail)
        db.collection("users").document(currentUserId)
            .collection("friends").document(friendId).set(friendData)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    fun removeFriend(friendId: String, callback: (Boolean) -> Unit) {
        auth.currentUser?.let { user ->
            db.collection("users").document(user.uid)
                .collection("friends").document(friendId)
                .delete()
                .addOnSuccessListener {
                    fetchFriends()
                    callback(true)
                }
                .addOnFailureListener { callback(false) }
        } ?: callback(false)
    }

    fun findAndAddFriend(email: String, callback: (Boolean, String) -> Unit) {
        auth.currentUser?.let { currentUser ->
            db.collection("users").whereEqualTo("email", email).get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.isEmpty) {
                        callback(false, "No user found with this email")
                    } else {
                        val userDoc = snapshot.documents.first()
                        val friendId = userDoc.id
                        val friendUsername = userDoc.getString("username") ?: ""
                        val friendEmail = userDoc.getString("email") ?: ""
                        checkIfFriendExists(currentUser.uid, friendId) { exists ->
                            if (exists) {
                                callback(false, "Friend is already added")
                            } else {
                                addFriend(currentUser.uid, friendId, friendUsername, friendEmail) { success ->
                                    callback(success, if (success) "Friend added successfully" else "Failed to add friend")
                                }
                            }
                        }
                    }
                }
                .addOnFailureListener { callback(false, "Failed to search by email") }
        } ?: callback(false, "User not authenticated")
    }

    private fun checkIfFriendExists(currentUserId: String, friendId: String, callback: (Boolean) -> Unit) {
        db.collection("users").document(currentUserId)
            .collection("friends").document(friendId)
            .get()
            .addOnSuccessListener { document -> callback(document.exists()) }
            .addOnFailureListener { callback(false) }
    }

    fun sendFriendRequest(receiverId: String, onResult: (Boolean) -> Unit) {
        auth.currentUser?.let { user ->
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { userDoc ->
                    val senderEmail = userDoc.getString("email") ?: ""
                    val senderUsername = userDoc.getString("username") ?: ""

                    val friendRequest = FriendRequest(
                        senderId = user.uid,
                        receiverId = receiverId,
                        senderEmail = senderEmail,
                        senderUsername = senderUsername
                    )

                    db.collection("friendRequests").add(friendRequest)
                        .addOnSuccessListener { onResult(true) }
                        .addOnFailureListener { onResult(false) }
                }
                .addOnFailureListener { onResult(false) }
        } ?: onResult(false)
    }

    fun fetchFriendRequests() {
        auth.currentUser?.let { user ->
            db.collection("friendRequests")
                .whereEqualTo("receiverId", user.uid)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.e("FirebaseViewModel", "Error fetching friend requests", e)
                        return@addSnapshotListener
                    }
                    val requests = snapshot?.toObjects(FriendRequest::class.java) ?: emptyList()
                    _friendRequests.value = requests
                }
        }
    }

    fun acceptFriendRequest(friendRequest: FriendRequest, onResult: (Boolean) -> Unit) {
        auth.currentUser?.let { user ->
            val batch = db.batch()

            val senderFriendsRef = db.collection("users").document(friendRequest.senderId)
                .collection("friends").document(user.uid)
            val senderData = mapOf("userId" to user.uid)
            batch.set(senderFriendsRef, senderData)

            val receiverFriendsRef = db.collection("users").document(user.uid)
                .collection("friends").document(friendRequest.senderId)
            val receiverData = mapOf("userId" to friendRequest.senderId)
            batch.set(receiverFriendsRef, receiverData)

            db.collection("friendRequests")
                .whereEqualTo("senderId", friendRequest.senderId)
                .whereEqualTo("receiverId", friendRequest.receiverId)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot != null && !snapshot.isEmpty) {
                        for (doc in snapshot.documents) {
                            batch.delete(doc.reference)
                        }
                        batch.commit()
                            .addOnSuccessListener { onResult(true) }
                            .addOnFailureListener { onResult(false) }
                    } else {
                        onResult(false)
                    }
                }
                .addOnFailureListener { onResult(false) }
        }
    }

    fun declineFriendRequest(friendRequest: FriendRequest, onResult: (Boolean) -> Unit) {
        auth.currentUser?.let {
            db.collection("friendRequests")
                .whereEqualTo("senderId", friendRequest.senderId)
                .whereEqualTo("receiverId", friendRequest.receiverId)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot != null && !snapshot.isEmpty) {
                        for (doc in snapshot.documents) {
                            doc.reference.delete()
                                .addOnSuccessListener { onResult(true) }
                                .addOnFailureListener { onResult(false) }
                        }
                    } else {
                        onResult(false)
                    }
                }
                .addOnFailureListener { onResult(false) }
        }
    }

    fun fetchUsers(onResult: (List<User>) -> Unit) {
        db.collection("users")
            .get()
            .addOnSuccessListener { snapshot ->
                val users = snapshot.documents.mapNotNull { doc ->
                    User(
                        userId = doc.id,
                        username = doc.getString("username") ?: "",
                        email = doc.getString("email") ?: ""
                    )
                }
                onResult(users)
            }
            .addOnFailureListener { exception ->
                Log.e("FirebaseViewModel", "Error fetching users", exception)
                onResult(emptyList())
            }
    }

    fun fetchPhrases() {
        viewModelScope.launch {
            val user = auth.currentUser
            if (user != null) {
                _isRefreshing.value = true
                try {
                    val userPhrases = db.collection("users").document(user.uid)
                        .collection("phrases")
                        .get()
                        .await()
                        .documents.mapNotNull { doc ->
                            doc.toObject(Phrase::class.java)?.copy(username = doc.getString("username") ?: "")
                        }

                    val friends = fetchFriendsList(user.uid)
                    val friendPhrases = friends.flatMap { friend ->
                        db.collection("users").document(friend.userId)
                            .collection("phrases")
                            .get()
                            .await()
                            .documents.mapNotNull { doc ->
                                doc.toObject(Phrase::class.java)?.copy(username = friend.username)
                            }
                    }

                    _phrases.value = userPhrases + friendPhrases
                } catch (e: Exception) {
                    Log.e("FirebaseViewModel", "Error fetching phrases", e)
                } finally {
                    _isRefreshing.value = false
                }
            }
        }
    }

    private suspend fun fetchFriendsList(userId: String): List<User> {
        return try {
            val friendsSnapshot = db.collection("users").document(userId)
                .collection("friends")
                .get()
                .await()

            friendsSnapshot.documents.mapNotNull { doc ->
                doc.toObject(User::class.java)
            }
        } catch (e: Exception) {
            Log.e("FirebaseViewModel", "Error fetching friends", e)
            emptyList()
        }
    }

    fun addPhrase(phrase: Phrase) {
        auth.currentUser?.let { user ->
            viewModelScope.launch {
                try {
                    val username = db.collection("users").document(user.uid).get().await().getString("username") ?: "Unknown User"
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
        auth.currentUser?.let { user ->
            viewModelScope.launch {
                db.collection("users").document(user.uid)
                    .collection("trips")
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            Log.e("FirebaseViewModel", "Error fetching trips", e)
                            return@addSnapshotListener
                        }
                        val tripsList = snapshot?.toObjects(Trip::class.java) ?: emptyList()
                        _trips.value = tripsList
                    }
            }
        }
    }

    fun addTrip(trip: Trip) {
        auth.currentUser?.let { user ->
            viewModelScope.launch {
                val tripRef = db.collection("users").document(user.uid)
                    .collection("trips").document()
                val tripWithId = trip.copy(id = tripRef.id)

                tripRef.set(tripWithId)
                    .addOnSuccessListener { fetchTrips() }
                    .addOnFailureListener { Log.e("FirebaseViewModel", "Error adding trip") }
            }
        }
    }

    fun removeTrip(trip: Trip, onComplete: (Boolean) -> Unit) {
        auth.currentUser?.let { user ->
            viewModelScope.launch {
                db.collection("users").document(user.uid)
                    .collection("trips").document(trip.id)
                    .delete()
                    .addOnSuccessListener { onComplete(true) }
                    .addOnFailureListener { onComplete(false) }
            }
        } ?: onComplete(false)
    }

    fun updateTrip(updatedTrip: Trip, onSuccess: () -> Unit = {}) {
        auth.currentUser?.let { user ->
            viewModelScope.launch {
                try {
                    db.collection("users").document(user.uid)
                        .collection("trips").document(updatedTrip.id)
                        .set(updatedTrip)
                        .addOnSuccessListener {
                            fetchTrips()
                            onSuccess()
                        }
                        .addOnFailureListener {
                            Log.e("FirebaseViewModel", "Error updating trip")
                        }
                } catch (e: Exception) {
                    Log.e("FirebaseViewModel", "Error updating trip", e)
                }
            }
        }
    }

    fun fetchChecklist(tripId: String, onComplete: (List<ChecklistItem>) -> Unit) {
        auth.currentUser?.let { user ->
            viewModelScope.launch {
                val tripRef = db.collection("users").document(user.uid)
                    .collection("trips").document(tripId)
                tripRef.get().addOnSuccessListener { document ->
                    if (document.exists()) {
                        val trip = document.toObject(Trip::class.java)
                        Log.d("debug", trip.toString())
                        onComplete(trip?.checklist ?: emptyList())
                    }
                }.addOnFailureListener {
                    Log.e("FirebaseViewModel", "Error fetching checklist", it)
                }
            }
        }
    }

    fun updateChecklistItem(tripId: String, updatedItem: ChecklistItem) {
        auth.currentUser?.let { user ->
            viewModelScope.launch {
                val tripRef = db.collection("users").document(user.uid)
                    .collection("trips").document(tripId)

                tripRef.get().addOnSuccessListener { document ->
                    if (document.exists()) {
                        val trip = document.toObject(Trip::class.java)
                        val updatedChecklist = trip?.checklist?.map {
                            if (it.id == updatedItem.id) updatedItem else it
                        } ?: listOf(updatedItem)
                        tripRef.update("checklist", updatedChecklist)
                    }
                }.addOnFailureListener {
                    Log.e("FirebaseViewModel", "Error updating checklist item", it)
                }
            }
        }
    }

    fun fetchTripPlans(tripId: String, onComplete: (List<TripPlanItem>) -> Unit) {
        auth.currentUser?.let { user ->
            viewModelScope.launch {
                val tripRef = db.collection("users").document(user.uid)
                    .collection("trips").document(tripId)
                tripRef.get().addOnSuccessListener { document ->
                    if (document.exists()) {
                        val trip = document.toObject(Trip::class.java)
                        onComplete(trip?.tripPlans ?: emptyList())
                    } else {
                        onComplete(emptyList())
                    }
                }.addOnFailureListener {
                    Log.e("FirebaseViewModel", "Error fetching trip plans", it)
                    onComplete(emptyList())
                }
            }
        }
    }

    fun removeTripPlan(tripId: String, tripPlan: TripPlanItem, onComplete: (Boolean) -> Unit) {
        auth.currentUser?.let { user ->
            val tripRef = db.collection("users").document(user.uid)
                .collection("trips").document(tripId)

            viewModelScope.launch {
                tripRef.update("tripPlans", FieldValue.arrayRemove(tripPlan))
                    .addOnSuccessListener {
                        onComplete(true)
                    }
                    .addOnFailureListener { e ->
                        Log.e("FirebaseViewModel", "Error removing trip plan", e)
                        onComplete(false)
                    }
            }
        }
    }
}
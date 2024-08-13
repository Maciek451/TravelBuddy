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
import uk.ac.aber.dcs.chm9360.travelbuddy.model.Friend
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

    private val _email = MutableStateFlow<String?>(null)
    val email: StateFlow<String?> get() = _email

    private val _phrases = MutableStateFlow<List<Phrase>>(emptyList())
    val phrases: StateFlow<List<Phrase>> get() = _phrases

    private val _trips = MutableStateFlow<List<Trip>>(emptyList())
    val trips: StateFlow<List<Trip>> get() = _trips

    private val _authState = MutableStateFlow(auth.currentUser)
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
        fetchUsername()
        fetchEmail()
    }

    private fun observeAuthState() {
        auth.addAuthStateListener { firebaseAuth ->
            _authState.value = firebaseAuth.currentUser
        }
    }

    private fun handleAuthResult(
        task: Task<AuthResult>,
        successCallback: () -> Unit,
        errorCallback: (Exception) -> Unit
    ) {
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

    fun signUpWithEmailAndPassword(
        email: String,
        password: String,
        username: String,
        callback: (Int) -> Unit
    ) {
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
    private suspend fun removeAllUserDataSync(): Boolean =
        suspendCancellableCoroutine { continuation ->
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

    fun fetchEmail() {
        auth.currentUser?.let { user ->
            viewModelScope.launch {
                try {
                    val document = db.collection("users").document(user.uid).get().await()
                    _email.value = document.getString("email")
                } catch (e: Exception) {
                    Log.e("FirebaseViewModel", "Error fetching email", e)
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

    fun addFriend(user: User, friend: User, onResult: (Boolean) -> Unit = {}) {
        auth.currentUser?.let { fbUser ->
            val db = FirebaseFirestore.getInstance()

            val friendData = hashMapOf(
                "user_id" to user.userId,
                "user_username" to user.username,
                "user_email" to user.email,
                "friend_id" to friend.userId,
                "friend_username" to friend.username,
                "friend_email" to friend.email,
            )

            val friendsListRef = db.collection("friends").document(fbUser.uid)

            friendsListRef.update("list_of_friends", FieldValue.arrayUnion(friendData))
                .addOnSuccessListener { onResult(true) }
                .addOnFailureListener {
                    friendsListRef.set(hashMapOf("list_of_friends" to arrayListOf(friendData)))
                        .addOnSuccessListener { onResult(true) }
                        .addOnFailureListener { onResult(false) }
                }
        } ?: onResult(false)
    }

    fun getFriendsOfUser(user: String, callback: (List<Friend>) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        val friendsListRef = db.collection("friends").document("friends_list")

        friendsListRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val friendsList = documentSnapshot
                        .get("list_of_friends") as? List<Map<String, String>>

                    val filteredFriends = friendsList?.mapNotNull { friendMap ->
                        val friend =
                            Friend(friendMap["user_id"] ?: "", friendMap["friend_id"] ?: "")
                        if (friend.userId == user) friend else null
                    } ?: emptyList()

                    callback(filteredFriends)
                } else {
                    callback(emptyList())
                }
            }
            .addOnFailureListener {
                callback(emptyList())
            }
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
        auth.currentUser?.let {
            val batch = db.batch()

            db.collection("friendRequests")
                .whereEqualTo("senderId", friendRequest.senderId)
                .whereEqualTo("receiverId", friendRequest.receiverId)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot != null && !snapshot.isEmpty) {
                        for (doc in snapshot.documents) {
                            doc.reference.delete()
                        }
                        batch.commit()
                            .addOnSuccessListener {
                                val friend = User(
                                    friendRequest.senderId,
                                    friendRequest.senderUsername,
                                    friendRequest.senderEmail
                                )
                                val user = User(
                                    friendRequest.receiverId,
                                    username.toString(),
                                    email.toString()
                                )
                                addFriend(user, friend)
                                addFriend(friend, user)
                            }
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
                            doc.toObject(Phrase::class.java)
                                ?.copy(username = doc.getString("username") ?: "")
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
                    val username = db.collection("users").document(user.uid).get().await()
                        .getString("username") ?: "Unknown User"
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
                // Fetch the user's own trips
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

                // Fetch trips shared by friends
                val friends = fetchFriendsList(user.uid)
                val sharedTrips = mutableListOf<Trip>()
                friends.forEach { friend ->
                    db.collection("users").document(friend.userId)
                        .collection("sharedTrips")
                        .get()
                        .addOnSuccessListener { snapshot ->
                            sharedTrips.addAll(snapshot.toObjects(Trip::class.java))
                            _trips.value += sharedTrips
                        }
                        .addOnFailureListener { e ->
                            Log.e("FirebaseViewModel", "Error fetching shared trips", e)
                        }
                }
            }
        }
    }

    fun addTrip(trip: Trip) {
        auth.currentUser?.let { user ->
            viewModelScope.launch {
                try {
                    val username = db.collection("users").document(user.uid).get().await()
                        .getString("username") ?: "Unknown User"
                    val tripRef = db.collection("users").document(user.uid)
                        .collection("trips").document()
                    val tripWithIdAndAuthor = trip.copy(id = tripRef.id, author = username)
                    tripRef.set(tripWithIdAndAuthor).await()
                    fetchTrips()
                } catch (e: Exception) {
                    Log.e("FirebaseViewModel", "Error adding trip", e)
                }
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
                    val tripRef = db.collection("users").document(user.uid)
                        .collection("trips").document(updatedTrip.id)

                    val updates = mapOf(
                        "title" to updatedTrip.title,
                        "destination" to updatedTrip.destination,
                        "startDate" to updatedTrip.startDate,
                        "endDate" to updatedTrip.endDate
                    )

                    tripRef.update(updates)
                        .addOnSuccessListener {
                            fetchTrips()
                            onSuccess()
                        }
                        .addOnFailureListener { exception ->
                            Log.e("FirebaseViewModel", "Error updating trip", exception)
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

    fun addTripPlan(tripId: String, tripPlanItem: TripPlanItem, onComplete: (Boolean) -> Unit) {
        auth.currentUser?.let { user ->
            val tripRef = db.collection("users").document(user.uid)
                .collection("trips").document(tripId)

            viewModelScope.launch {
                try {
                    tripRef.update("tripPlans", FieldValue.arrayUnion(tripPlanItem))
                        .addOnSuccessListener {
                            onComplete(true)
                        }
                        .addOnFailureListener { e ->
                            Log.e("FirebaseViewModel", "Error adding trip plan", e)
                            onComplete(false)
                        }
                } catch (e: Exception) {
                    Log.e("FirebaseViewModel", "Exception adding trip plan", e)
                    onComplete(false)
                }
            }
        }
    }

    fun updateTripPlan(tripId: String, updatedTripPlan: TripPlanItem, onComplete: (Boolean) -> Unit) {
        auth.currentUser?.let { user ->
            val tripRef = db.collection("users").document(user.uid)
                .collection("trips").document(tripId)

            viewModelScope.launch {
                try {
                    val tripSnapshot = tripRef.get().await()
                    val trip = tripSnapshot.toObject(Trip::class.java)

                    if (trip != null) {
                        val updatedTripPlans = trip.tripPlans.map {
                            if (it.id == updatedTripPlan.id) updatedTripPlan else it
                        }

                        tripRef.update("tripPlans", updatedTripPlans)
                            .addOnSuccessListener {
                                onComplete(true)
                            }
                            .addOnFailureListener { e ->
                                Log.e("FirebaseViewModel", "Error updating trip plan", e)
                                onComplete(false)
                            }
                    } else {
                        onComplete(false)
                    }
                } catch (e: Exception) {
                    Log.e("FirebaseViewModel", "Error fetching trip", e)
                    onComplete(false)
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

    fun postTrip(trip: Trip, onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
        auth.currentUser?.let { user ->
            viewModelScope.launch {
                val tripRef = db.collection("users").document(user.uid)
                    .collection("trips").document(trip.id)

                val updatedTrip = trip.copy(shared = true)

                tripRef.set(updatedTrip)
                    .addOnSuccessListener {
                        fetchTrips()
                        onSuccess()
                    }
                    .addOnFailureListener { exception ->
                        Log.e(
                            "FirebaseViewModel",
                            "Error setting trip share status to true",
                            exception
                        )
                        onFailure(exception)
                    }
            }
        }
    }

    fun tripToPrivate(
        trip: Trip,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        auth.currentUser?.let { user ->
            viewModelScope.launch {
                val tripRef = db.collection("users").document(user.uid)
                    .collection("trips").document(trip.id)

                val updatedTrip = trip.copy(shared = false)

                tripRef.set(updatedTrip)
                    .addOnSuccessListener {
                        fetchTrips()
                        onSuccess()
                    }
                    .addOnFailureListener { exception ->
                        Log.e(
                            "FirebaseViewModel",
                            "Error setting trip share status to false",
                            exception
                        )
                        onFailure(exception)
                    }
            }
        }
    }
}
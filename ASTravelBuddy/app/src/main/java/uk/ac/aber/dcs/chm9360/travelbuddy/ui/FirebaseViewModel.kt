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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
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

    fun getUserData(userId: String, onSuccess: (User?) -> Unit) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                onSuccess(user)
            }
            .addOnFailureListener {
                onSuccess(null)
            }
    }

    fun signUpWithEmailAndPassword(
        email: String,
        password: String,
        callback: (Int) -> Unit
    ) {
        viewModelScope.launch {
            handleAuthResult(
                auth.createUserWithEmailAndPassword(email, password),
                {
                    val user = auth.currentUser
                    user?.uid?.let { userId ->
                        val userMap = mapOf("email" to email)
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

    fun removeAllUserData(onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val user = auth.currentUser
            if (user != null) {
                try {
                    val db = FirebaseFirestore.getInstance()

                    val phrasesRef = db.collection("users").document(user.uid).collection("phrases")
                    val phrasesSnapshot = phrasesRef.get().await()
                    for (doc in phrasesSnapshot.documents) {
                        doc.reference.delete().await()
                    }

                    val tripsRef = db.collection("users").document(user.uid).collection("trips")
                    val tripsSnapshot = tripsRef.get().await()
                    for (doc in tripsSnapshot.documents) {
                        doc.reference.delete().await()
                    }

                    val friendRequestsRef = db.collection("friendRequests")
                    val friendRequestsSnapshot =
                        friendRequestsRef.whereEqualTo("receiverId", user.uid).get().await()
                    for (doc in friendRequestsSnapshot.documents) {
                        doc.reference.delete().await()
                    }

                    val friendsListRef = db.collection("friends").document(user.uid)
                    friendsListRef.delete().await()

                    onComplete(true)
                } catch (e: Exception) {
                    Log.e("FirebaseViewModel", "Error removing user data", e)
                    onComplete(false)
                }
            } else {
                onComplete(false)
            }
        }
    }

    fun deleteUserAccount(onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val user = auth.currentUser
            if (user != null) {
                try {
                    removeAllUserData { success ->
                        if (success) {
                            val userDocRef = db.collection("users").document(user.uid)
                            userDocRef.delete()
                                .addOnCompleteListener { deleteUserDocTask ->
                                    if (deleteUserDocTask.isSuccessful) {
                                        user.delete()
                                            .addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    onComplete(true)
                                                } else {
                                                    Log.e(
                                                        "FirebaseViewModel",
                                                        "Error deleting user account",
                                                        task.exception
                                                    )
                                                    onComplete(false)
                                                }
                                            }
                                    } else {
                                        Log.e(
                                            "FirebaseViewModel",
                                            "Error deleting user document",
                                            deleteUserDocTask.exception
                                        )
                                        onComplete(false)
                                    }
                                }
                        } else {
                            onComplete(false)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("FirebaseViewModel", "Error deleting user account", e)
                    onComplete(false)
                }
            } else {
                onComplete(false)
            }
        }
    }

    fun isUsernameTaken(username: String, callback: (Boolean) -> Unit) {
        db.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { documents ->
                callback(!documents.isEmpty)
            }
            .addOnFailureListener {
                Log.e("FirebaseViewModel", "Error checking if username is taken", it)
                callback(false)
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
        isUsernameTaken(newUsername) { isTaken ->
            if (isTaken) {
                callback(false)
            } else {
                auth.currentUser?.let { user ->
                    val userDocRef = db.collection("users").document(user.uid)
                    val tripsRef = db.collection("users").document(user.uid).collection("trips")
                    val phrasesRef = db.collection("users").document(user.uid).collection("phrases")

                    viewModelScope.launch {
                        try {
                            userDocRef.update("username", newUsername).await()
                            _username.value = newUsername

                            val tripsSnapshot = tripsRef.get().await()
                            for (doc in tripsSnapshot.documents) {
                                doc.reference.update("author", newUsername).await()
                            }
                            val phrasesSnapshot = phrasesRef.get().await()
                            for (doc in phrasesSnapshot.documents) {
                                doc.reference.update("username", newUsername).await()
                            }
                            callback(true)
                        } catch (e: Exception) {
                            Log.e("FirebaseViewModel", "Error updating username", e)
                            callback(false)
                        }
                    }
                } ?: callback(false)
            }
        }
    }

    fun checkUsernameAndNavigate(callback: (Boolean) -> Unit) {
        auth.currentUser?.let { user ->
            val userDocRef = db.collection("users").document(user.uid)
            userDocRef.get().addOnSuccessListener { document ->
                val username = document.getString("username")
                callback(username.isNullOrEmpty())
            }
        } ?: callback(false)
    }

    data class FriendRelation(
        val user: User = User(),
        val friend: User = User()
    )

    fun addFriend(user: User, friend: User, onResult: (Boolean) -> Unit = {}) {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        val db = FirebaseFirestore.getInstance()

        if (currentUser != null) {

            val friendRelation = FriendRelation(user, friend)
            val friendsListRef = db.collection("friends").document("friends_list")

            friendsListRef.update("list_of_friends", FieldValue.arrayUnion(friendRelation))
                .addOnSuccessListener { onResult(true) }
                .addOnFailureListener {
                    friendsListRef.set(
                        hashMapOf(
                            "list_of_friends" to arrayListOf(friendRelation)
                        )
                    )
                        .addOnSuccessListener { onResult(true) }
                        .addOnFailureListener { onResult(false) }
                }
        } else {
            onResult(false)
        }
    }

    fun getFriendsOfUser(userId: String) {
        val db = FirebaseFirestore.getInstance()

        val friendsListRef = db.collection("friends").document("friends_list")

        friendsListRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val friendRelations = documentSnapshot.get("list_of_friends") as? List<Map<String, Any>>

                    val friendsList = friendRelations?.mapNotNull { map ->
                        val userMap = map["user"] as? Map<String, Any>
                        val friendMap = map["friend"] as? Map<String, Any>

                        val user = userMap?.let {
                            User(
                                userId = it["userId"] as? String ?: "",
                                username = it["username"] as? String ?: "",
                                email = it["email"] as? String ?: ""
                            )
                        }

                        val friend = friendMap?.let {
                            User(
                                userId = it["userId"] as? String ?: "",
                                username = it["username"] as? String ?: "",
                                email = it["email"] as? String ?: ""
                            )
                        }

                        if (user != null && friend != null && user.userId == userId) {
                            FriendRelation(user, friend)
                        } else {
                            null
                        }
                    }?.flatMap { relation ->
                        when {
                            relation.user.userId == userId -> listOf(relation.friend)
                            relation.friend.userId == userId -> listOf(relation.user)
                            else -> emptyList()
                        }
                    } ?: emptyList()

                    Log.d("FirebaseViewModel", "Parsed friends list: $friendsList")
                    _friends.value = friendsList
                } else {
                    Log.d("FirebaseViewModel", "No friends list found")
                    _friends.value = emptyList()
                }
            }
            .addOnFailureListener {
                Log.e("FirebaseViewModel", "Error fetching friends", it)
                _friends.value = emptyList()
            }
    }

    fun sendFriendRequest(receiverId: String, onResult: (Boolean, String?) -> Unit) {
        auth.currentUser?.let { user ->
            viewModelScope.launch {
                try {
                    val friendsListRef = db.collection("friends").document("friends_list").get().await()
                    val friendRelations = friendsListRef.get("list_of_friends") as? List<Map<String, Any>>

                    val alreadyFriends = friendRelations?.any { map ->
                        val userMap = map["user"] as? Map<String, Any>
                        val friendMap = map["friend"] as? Map<String, Any>

                        (userMap?.get("userId") == user.uid && friendMap?.get("userId") == receiverId) ||
                                (userMap?.get("userId") == receiverId && friendMap?.get("userId") == user.uid)
                    } ?: false

                    if (alreadyFriends) {
                        onResult(false, "already_friends")
                        return@launch
                    }

                    val existingRequest = db.collection("friendRequests")
                        .whereEqualTo("senderId", user.uid)
                        .whereEqualTo("receiverId", receiverId)
                        .get()
                        .await()

                    if (!existingRequest.isEmpty) {
                        onResult(false, "already_sent")
                        return@launch
                    }

                    val userDoc = db.collection("users").document(user.uid).get().await()
                    val senderEmail = userDoc.getString("email") ?: ""
                    val senderUsername = userDoc.getString("username") ?: ""

                    val friendRequest = FriendRequest(
                        senderId = user.uid,
                        receiverId = receiverId,
                        senderEmail = senderEmail,
                        senderUsername = senderUsername
                    )

                    db.collection("friendRequests").add(friendRequest)
                        .addOnSuccessListener { onResult(true, null) }
                        .addOnFailureListener { onResult(false, "error_sending_request") }
                } catch (e: Exception) {
                    Log.e("FirebaseViewModel", "Error sending friend request", e)
                    onResult(false, "exception_occurred")
                }
            }
        } ?: onResult(false, "not_logged_in")
    }

    fun removeFriend(friend: User, onComplete: (Boolean) -> Unit) {
        auth.currentUser?.let { currentUser ->
            viewModelScope.launch {
                try {
                    val friendsListRef = db.collection("friends").document("friends_list")
                    val friendRelationToRemove = FriendRelation(
                        user = User(
                            userId = currentUser.uid,
                            username = _username.value ?: "",
                            email = _email.value ?: ""
                        ),
                        friend = friend
                    )

                    friendsListRef.update("list_of_friends", FieldValue.arrayRemove(friendRelationToRemove))
                        .addOnSuccessListener {
                            val reverseFriendRelation = FriendRelation(
                                user = friend,
                                friend = User(
                                    userId = currentUser.uid,
                                    username = _username.value ?: "",
                                    email = _email.value ?: ""
                                )
                            )

                            friendsListRef.update("list_of_friends", FieldValue.arrayRemove(reverseFriendRelation))
                                .addOnSuccessListener {
                                    getFriendsOfUser(currentUser.uid)
                                    onComplete(true)
                                }
                                .addOnFailureListener { onComplete(false) }
                        }
                        .addOnFailureListener { onComplete(false) }
                } catch (e: Exception) {
                    Log.e("FirebaseViewModel", "Error removing friend", e)
                    onComplete(false)
                }
            }
        } ?: onComplete(false)
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
            db.collection("friendRequests")
                .whereEqualTo("senderId", friendRequest.senderId)
                .whereEqualTo("receiverId", friendRequest.receiverId)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot != null && !snapshot.isEmpty) {
                        for (doc in snapshot.documents) {
                            doc.reference.delete()
                        }

                        val friend = User(
                            friendRequest.senderId,
                            friendRequest.senderUsername,
                            friendRequest.senderEmail
                        )
                        val user = User(
                            friendRequest.receiverId,
                            _username.value ?: "",
                            _email.value ?: ""
                        )

                        addFriend(user, friend) { success1 ->
                            if (success1) {
                                addFriend(friend, user) { success2 ->
                                    onResult(success2)
                                }
                            } else {
                                onResult(false)
                            }
                        }
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

                    getFriendsOfUser(user.uid)
                    val friendPhrases = friends.value.flatMap { friend ->
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

    fun addPhrase(phrase: Phrase) {
        auth.currentUser?.let { user ->
            viewModelScope.launch {
                try {
                    val username = db.collection("users").document(user.uid).get().await()
                        .getString("username") ?: "Unknown User"
                    val phraseRef = db.collection("users").document(user.uid)
                        .collection("phrases").document()
                    val phraseWithIdAndUsername = phrase.copy(id = phraseRef.id, username = username)
                    phraseRef.set(phraseWithIdAndUsername).await()
                    fetchPhrases()
                } catch (e: Exception) {
                    Log.e("FirebaseViewModel", "Error adding phrase", e)
                }
            }
        }
    }

    fun updatePhrase(updatedPhrase: Phrase, onSuccess: () -> Unit = {}) {
        auth.currentUser?.let { user ->
            viewModelScope.launch {
                try {
                    val phraseRef = db.collection("users").document(user.uid)
                        .collection("phrases").document(updatedPhrase.id) // Make sure `updatedPhrase` has an `id` property

                    val updates = mapOf(
                        "language" to updatedPhrase.language,
                        "phrase" to updatedPhrase.phrase,
                        "translation" to updatedPhrase.translation,
                        "username" to updatedPhrase.username
                    )

                    phraseRef.update(updates)
                        .addOnSuccessListener {
                            fetchPhrases()
                            onSuccess()
                        }
                        .addOnFailureListener { exception ->
                            Log.e("FirebaseViewModel", "Error updating phrase", exception)
                        }
                } catch (e: Exception) {
                    Log.e("FirebaseViewModel", "Error updating phrase", e)
                }
            }
        }
    }

    fun removePhrase(phrase: Phrase, onComplete: (Boolean) -> Unit) {
        auth.currentUser?.let { user ->
            viewModelScope.launch {
                db.collection("users").document(user.uid)
                    .collection("phrases").document(phrase.id) // Make sure `phrase` has an `id` property
                    .delete()
                    .addOnSuccessListener { onComplete(true) }
                    .addOnFailureListener { onComplete(false) }
            }
        } ?: onComplete(false)
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

                getFriendsOfUser(user.uid)
                val sharedTrips = mutableListOf<Trip>()
                friends.value.forEach { friend ->
                    db.collection("users").document(friend.userId)
                        .collection("trips")
                        .get()
                        .addOnSuccessListener { snapshot ->
                            val trips = snapshot?.toObjects(Trip::class.java) ?: emptyList()
                            trips.forEach { trip ->
                                if (trip.shared) {
                                    sharedTrips.add(trip)
                                }
                            }
                            _trips.value += sharedTrips
                            Log.d("FirebaseViewModel", "All trips: $trips")
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
                        "endDate" to updatedTrip.endDate,
                        "checklist" to updatedTrip.checklist
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

    fun updateTripPlan(
        tripId: String,
        updatedTripPlan: TripPlanItem,
        onComplete: (Boolean) -> Unit
    ) {
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
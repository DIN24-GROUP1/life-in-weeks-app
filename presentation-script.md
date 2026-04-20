# Demo Script — Firebase CRUD Operations

### Memento: Life in Weeks

**Total estimated length:** 6–7 minutes  
**Screen layout:** Split — emulator on the left, Android Studio (code or Logcat) on the right.  
**Logcat filter to set before recording:**

```
tag:UserProfileRepository tag:LifePhaseRepository tag:AuthRepository
```

---

## Introduction — What is Firebase? (0:00–1:00)

### On screen: Firebase Console homepage (browser)

**Narration:**

> "Firebase is a platform developed by Google that provides a set of backend services for mobile and web applications — so developers can build features like authentication, data storage, and file hosting without managing their own servers."

---

### Core services used in this project

**Narration:**

> "Firebase consists of many products. In our app we use three of them."

Point to each in the Firebase Console left-hand menu as you mention it:

**1. Authentication**

> "Firebase Authentication handles user identity. It supports email and password, Google Sign-In, Apple, GitHub, and more. It also supports anonymous sign-in — the user gets a real UID without providing any credentials. This is how Memento works on first launch."

**2. Firestore (Cloud Firestore)**

> "Cloud Firestore is a NoSQL document database. Data is organised into _collections_ and _documents_ — similar to folders and files. Each document is a set of key-value fields. Firestore is real-time, meaning connected clients receive updates the moment data changes, and it works offline by caching data locally."

**3. Cloud Storage**

> "Firebase Storage is used for binary files — in our case, photos attached to weeks. Files are stored in a bucket and referenced by a path, similar to a file system."

---

### The key principles behind how Firestore is used in this project

**Narration:**

> "Before diving into the demo, here are three design principles that shape how Memento uses Firebase."

**Principle 1 — Local database as cache, Firestore as source of truth**

> "All data is stored in Room — Android's local SQLite database — so the UI is always fast and works offline. Firestore receives a copy of every write. On sign-in, Firestore is read and used to rebuild the local database. Room is the cache; Firestore is what persists across devices."

**Principle 2 — User-scoped data paths**

> "Every piece of data is stored under the authenticated user's UID in Firestore: `users/{uid}/data/profile` for the profile, `users/{uid}/phases/{id}` for phases. This means each user only ever reads and writes their own data."

**Principle 3 — Anonymous-first with optional account linking**

> "The app never forces registration. An anonymous session is created immediately so data can be saved right away. When the user later signs in with Google, the anonymous account is _linked_ — the UID stays the same and no data is lost. This is called account linking."

---

## Scene 1 — App Launch & Anonymous Auth (1:00–1:40)

### On screen: emulator (cold launch)

Cold-launch the app. It opens on the Login screen.

**Narration:**

> "When Memento launches for the first time, it silently signs the user in anonymously via Firebase Authentication — no account needed. All data written from this point is stored in Firestore under that temporary user ID."

---

### Show code — `AuthRepository.kt` line 28–31

Switch focus to Android Studio, open `AuthRepository.kt`, highlight:

```kotlin
private suspend fun ensureUserId(): String {
    auth.currentUser?.let { return it.uid }
    return auth.signInAnonymously().await().user!!.uid
}
```

**Narration:**

> "Every repository shares this helper. If there is already a signed-in user it returns their UID immediately. If not — for example right after install — it calls `signInAnonymously` and returns the new anonymous UID. This guarantees data is always written under a valid identity."

---

### Show Logcat

Switch to Logcat, point out the sign-in log line from `AuthRepository`.

---

## Scene 2 — Profile Setup: CREATE (0:40–1:30)

### On screen: emulator

Tap **"Continue without signing in"** → Start screen appears.

1. Tap the calendar icon — pick a date of birth
2. Move the gender slider
3. Tap **"Country of Origin"** — search and select a country (e.g. Finland)
4. Notice life expectancy auto-fills
5. Tap **"Start my timeline"**

---

### Show code — `UserViewModel.kt` lines 115–121 and 145–156

Open `UserViewModel.kt`, highlight `convertMillisToDate` and `saveProfile`:

```kotlin
fun convertMillisToDate(millis: Long) {
    birthdayText = dateFormatter.format(Date(millis))
    saveProfile()
    ...
}

private fun saveProfile() {
    viewModelScope.launch {
        profileRepository.saveProfile(
            UserProfile(
                birthday = birthdayText,
                lifeExpectancyYears = lifeExpectancyYears,
                genderSliderPosition = genderSliderPosition,
                country = selectedCountry?.name ?: "",
            )
        )
    }
}
```

**Narration:**

> "Every time the user changes any profile field, `saveProfile` is called. It packages the current state into a `UserProfile` object and hands it off to the repository."

---

### Show code — `UserProfileRepository.kt` lines 22–53

Open `UserProfileRepository.kt`, highlight `saveProfile` and `profileRef`:

```kotlin
private suspend fun profileRef() = firestore
    .collection("users")
    .document(ensureUserId())
    .collection("data")
    .document("profile")

suspend fun saveProfile(profile: UserProfile) = runCatching {
    profileRef().set(
        mapOf(
            "birthday" to profile.birthday,
            "lifeExpectancyYears" to profile.lifeExpectancyYears,
            "genderSliderPosition" to profile.genderSliderPosition,
            "country" to profile.country,
        )
    ).await()
}
```

**Narration:**

> "The repository builds the Firestore path — `users / {uid} / data / profile` — and calls `.set()` with a map of the four fields. `await()` suspends the coroutine until Firestore confirms the write."

---

### Show Firebase Console

Open Firestore → `users` → `{uid}` → `data` → `profile`.  
Show the document live with all four fields.

---

## Scene 3 — Default Phases Auto-Created: CREATE (1:30–2:20)

### On screen: emulator

The Life Grid appears with coloured phase bands across the grid.

---

### Show code — `DefaultPhases.kt` lines 10–16

Open `DefaultPhases.kt`:

```kotlin
val defaultLifePhases: List<DefaultPhase> = listOf(
    DefaultPhase("Childhood",    0,   6,  0xFF3B82F6.toInt()),  // blue
    DefaultPhase("School",       6,  18,  0xFF10B981.toInt()),  // emerald
    DefaultPhase("University",  18,  23,  0xFFF59E0B.toInt()),  // amber
    DefaultPhase("Career",      23,  65,  0xFF8B5CF6.toInt()),  // violet
    DefaultPhase("Senior",      65,  90,  0xFF64748B.toInt()),  // slate
)
```

**Narration:**

> "Five default phases are defined as simple data objects with a name, a start and end year relative to the user's birthday, and a colour."

---

### Show code — `LifePhaseRepository.kt` lines 56–64

Open `LifePhaseRepository.kt`, highlight `seedDefaultPhasesIfEmpty`:

```kotlin
suspend fun seedDefaultPhasesIfEmpty(birthday: LocalDate) {
    if (dao.count() > 0) return
    defaultLifePhases.forEach { phase ->
        val start = birthday.plusYears(phase.startYear.toLong()).toEpochDay()
        val end   = birthday.plusYears(phase.endYear.toLong()).minusDays(1).toEpochDay()
        addPhase(LifePhase(name = phase.name, colorArgb = phase.colorArgb,
                           startEpochDay = start, endEpochDay = end))
    }
}
```

**Narration:**

> "When the birthday is first set, this function checks whether the user already has phases. If the database is empty it converts each default's year offsets into absolute epoch days using the birthday, then calls `addPhase` for each one — which writes to both Room and Firestore."

---

### Show Firebase Console

Open Firestore → `users` → `{uid}` → `phases`.  
Show the 5 phase documents, each containing `name`, `colorArgb`, `startEpochDay`, `endEpochDay`.

---

## Scene 4 — Edit a Phase: UPDATE (2:20–3:05)

### On screen: emulator

Navigate to the **Settings** tab → scroll to the Life Phases section.

1. Tap the edit icon on **"Career"**
2. Change the name to **"Work Life"**
3. Pick a different colour
4. Tap **Save**

---

### Show code — `LifePhaseRepository.kt` lines 39–53

Open `LifePhaseRepository.kt`, highlight `updatePhase` and `syncToFirestore`:

```kotlin
suspend fun updatePhase(phase: LifePhase) {
    dao.update(phase)
    syncToFirestore(phase)
}

private suspend fun syncToFirestore(phase: LifePhase) = runCatching {
    phasesRef().document(phase.id.toString()).set(phase.toMap()).await()
}.onFailure { Log.w("LifePhaseRepository", "Failed to sync phase ${phase.id}", it) }
```

**Narration:**

> "An update writes to Room first — keeping the UI reactive — then calls `syncToFirestore`, which overwrites the matching Firestore document using the phase's Room ID as the document key. Failures are logged but never crash the app."

---

### Show Firebase Console

Refresh the `phases` collection. Open the document that was edited — show `name` is now `"Work Life"` and `colorArgb` has changed.

---

## Scene 5 — Add a Custom Phase: CREATE (3:05–3:40)

### On screen: emulator

Still on Settings, scroll to the **"Add phase"** form at the bottom.

1. Enter a name (e.g. **"Retirement"**)
2. Set a start and end date
3. Choose a colour
4. Tap **"Add Phase"**

---

### Show code — `LifePhaseRepository.kt` lines 34–37

Open `LifePhaseRepository.kt`, highlight `addPhase`:

```kotlin
suspend fun addPhase(phase: LifePhase) {
    val roomId = dao.insert(phase)
    syncToFirestore(phase.copy(id = roomId.toInt()))
}
```

**Narration:**

> "Room's auto-generated ID is captured from the insert result and copied onto the phase before syncing. This means the Firestore document key always matches the Room primary key — making it straightforward to update or delete by ID later."

---

### Show Firebase Console

A new document appears in the `phases` collection with the ID matching Room's auto-generated value.

---

## Scene 6 — Delete a Phase: DELETE (3:40–4:10)

### On screen: emulator

Settings screen → tap the delete icon on **"Retirement"** (the phase just added).  
It disappears from the list instantly.

---

### Show code — `LifePhaseRepository.kt` lines 44–49

Open `LifePhaseRepository.kt`, highlight `deletePhase`:

```kotlin
suspend fun deletePhase(phase: LifePhase) {
    dao.delete(phase)
    runCatching {
        phasesRef().document(phase.id.toString()).delete().await()
    }.onFailure { Log.w("LifePhaseRepository", "Failed to delete phase ${phase.id}", it) }
}
```

**Narration:**

> "Deletion follows the same pattern: Room first so the UI updates immediately, then Firestore. The delete is wrapped in `runCatching` — if the network is unavailable the local delete still succeeds and the Firestore cleanup can be retried later."

---

### Show Firebase Console

The document for "Retirement" is gone from the `phases` collection.

---

## Scene 7 — Sign In with Google & Data Restored: READ (4:10–5:15)

### On screen: emulator

Navigate to **Settings** → **Account** section → tap **"Sign in with Google"** → complete the flow.

---

### Show code — `AuthRepository.kt` lines 48–75

Open `AuthRepository.kt`, highlight `signInWithGoogle`:

```kotlin
suspend fun signInWithGoogle(context: Context): Result<Unit> = runCatching {
    ...
    try {
        // Link the anonymous account to the Google credential (data is preserved)
        auth.currentUser?.linkWithCredential(firebaseCredential)?.await()
    } catch (e: FirebaseAuthUserCollisionException) {
        // This Google account already has a Firebase account — sign in normally
        auth.signInWithCredential(firebaseCredential).await()
    }
}
```

**Narration:**

> "When signing in with Google, the app first tries to link the current anonymous account to the Google credential. This preserves the UID and all existing data. If the Google account already exists in Firebase — meaning the user signed in before — it falls back to a normal sign-in."

---

### On screen: emulator

Tap **"Sign out"** → on the Login screen sign back in with Google.

---

### Show code — `UserViewModel.kt` lines 71–99

Open `UserViewModel.kt`, highlight the `authStateListener` and `loadProfileAndSeed`:

```kotlin
private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
    val uid = firebaseAuth.currentUser?.uid
    if (uid != null && uid != currentUserId) {
        currentUserId = uid
        viewModelScope.launch { loadProfileAndSeed() }
    }
}

private suspend fun loadProfileAndSeed() {
    isProfileLoaded = false
    profileRepository.loadProfile()?.let { profile ->
        birthdayText = profile.birthday
        ...
    }
    isProfileLoaded = true
    birthday?.let { phaseRepository.syncFromFirestore(it) }
}
```

**Narration:**

> "The ViewModel listens for auth state changes. Whenever the user ID changes — including after sign-out and sign-in — `loadProfileAndSeed` fires automatically. It reads the profile from Firestore, then calls `syncFromFirestore` to restore phases."

---

### Show code — `LifePhaseRepository.kt` lines 71–95

Open `LifePhaseRepository.kt`, highlight `syncFromFirestore`:

```kotlin
suspend fun syncFromFirestore(birthday: LocalDate) {
    val snapshot = runCatching { phasesRef().get().await() }.getOrNull()

    dao.clearAll()

    if (snapshot == null || snapshot.isEmpty) {
        seedDefaultPhasesIfEmpty(birthday)
        return
    }

    val phases = snapshot.documents.mapNotNull { doc ->
        LifePhase(
            id = doc.id.toInt(),
            name = doc.getString("name") ?: return@mapNotNull null,
            ...
        )
    }
    dao.insertAll(phases)
}
```

**Narration:**

> "On every UID change, the local Room database is cleared and rebuilt from Firestore. If Firestore has phases they are inserted back into Room with their original IDs. If Firestore is empty — a brand new account — the five defaults are seeded instead."

---

### On screen: emulator + Logcat

Show the app returning to the Life Grid with all phases intact.  
Point out the read log line in Logcat from `LifePhaseRepository`.

---

## Scene 8 — Wrap-up (5:15–5:45)

### On screen: Firebase Console

Show the full user document tree:

```
users/
  {uid}/
    data/
      profile          ← birthday, lifeExpectancyYears, genderSliderPosition, country
    phases/
      {id}             ← name, colorArgb, startEpochDay, endEpochDay
```

**Narration:**

> "To summarise: Memento covers all four CRUD operations with Firestore. Profile and phase data is created and updated on every user change, read on every sign-in or UID change, and deleted cleanly when the user removes a phase. Room acts as a local cache for offline access and instant UI updates, while Firestore is the source of truth for cross-device sync."

**End screen / cut.**

---

## File Reference

| Operation                           | File                       | Lines            |
| ----------------------------------- | -------------------------- | ---------------- |
| Anonymous sign-in                   | `AuthRepository.kt`        | 28–31            |
| Google sign-in + account linking    | `AuthRepository.kt`        | 48–75            |
| Save profile (CREATE / UPDATE)      | `UserProfileRepository.kt` | 44–53            |
| Profile Firestore path              | `UserProfileRepository.kt` | 22–26            |
| Trigger save on field change        | `UserViewModel.kt`         | 115–121, 145–156 |
| Auth state listener (triggers READ) | `UserViewModel.kt`         | 71–99            |
| Default phase definitions           | `DefaultPhases.kt`         | 10–16            |
| Seed default phases (CREATE)        | `LifePhaseRepository.kt`   | 56–64            |
| Add phase (CREATE)                  | `LifePhaseRepository.kt`   | 34–37            |
| Update phase (UPDATE)               | `LifePhaseRepository.kt`   | 39–53            |
| Delete phase (DELETE)               | `LifePhaseRepository.kt`   | 44–49            |
| Restore phases on sign-in (READ)    | `LifePhaseRepository.kt`   | 71–95            |

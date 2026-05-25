package com.maku.eyeonsite

import com.maku.eyeonsite.admin.WaitlistDocument
import com.maku.eyeonsite.landing.waitlist.WaitlistEntry
import org.w3c.xhr.XMLHttpRequest

private const val FIRESTORE_BASE =
    "https://firestore.googleapis.com/v1/projects/eyeonsite-3b8e0/databases/(default)/documents"
private const val FIREBASE_API_KEY = "AIzaSyByLoQpoBU0d-ZrYxVP0C1alp7t4ZPNL_s"
private const val ADMIN_EMAIL = "makpalyy@gmail.com"
private val HTTP_OK = 200..299

// ── Waitlist submit ───────────────────────────────────────────────────────────

actual fun submitWaitlistEntry(
    entry: WaitlistEntry,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
) {
    val xhr = XMLHttpRequest()
    xhr.open("POST", "$FIRESTORE_BASE/waitlist?key=$FIREBASE_API_KEY")
    xhr.setRequestHeader("Content-Type", "application/json")
    xhr.asDynamic().onload = {
        if (xhr.status.toInt() in HTTP_OK) {
            onSuccess()
        } else {
            onError("Submission failed (${xhr.status}). Please try again.")
        }
    }
    xhr.asDynamic().onerror = {
        onError("Network error. Please check your connection and try again.")
    }
    xhr.send(buildFirestoreDoc(entry))
}

private fun buildFirestoreDoc(entry: WaitlistEntry): String {
    @Suppress("UnsafeCastFromDynamic")
    val ts: String = js("new Date().toISOString()")
    return buildString {
        append("""{"fields":{""")
        append(""""name":{"stringValue":"${entry.name.esc()}"},""")
        append(""""email":{"stringValue":"${entry.email.esc()}"},""")
        append(""""role":{"stringValue":"${entry.role.apiValue}"},""")
        append(""""location":{"stringValue":"${entry.location.esc()}"},""")
        append(""""message":{"stringValue":"${entry.message.esc()}"},""")
        append(""""createdAt":{"timestampValue":"$ts"},""")
        append(""""platform":{"stringValue":"web"}""")
        append("}}")
    }
}

// ── Routing ───────────────────────────────────────────────────────────────────

@Suppress("UnsafeCastFromDynamic")
actual fun currentPath(): String = js("window.location.pathname")

// ── Admin auth ────────────────────────────────────────────────────────────────

actual fun signInAdmin(
    email: String,
    password: String,
    onSuccess: (idToken: String) -> Unit,
    onError: (String) -> Unit,
) {
    val xhr = XMLHttpRequest()
    xhr.open(
        "POST",
        "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=$FIREBASE_API_KEY",
    )
    xhr.setRequestHeader("Content-Type", "application/json")
    xhr.asDynamic().onload = {
        if (xhr.status.toInt() in HTTP_OK) {
            @Suppress("UnsafeCastFromDynamic")
            val json: dynamic = JSON.parse(xhr.responseText)
            val signedInEmail: String = json.email.toString()
            if (signedInEmail.lowercase() != ADMIN_EMAIL) {
                onError("Access denied — admin only.")
            } else {
                onSuccess(json.idToken.toString())
            }
        } else {
            @Suppress("UnsafeCastFromDynamic")
            val json: dynamic = JSON.parse(xhr.responseText)
            val msg = json.error?.message?.toString() ?: "Sign-in failed"
            onError(msg)
        }
    }
    xhr.asDynamic().onerror = { onError("Network error") }
    xhr.send("""{"email":"${email.trim().esc()}","password":"${password.esc()}","returnSecureToken":true}""")
}

// ── Admin data fetch ──────────────────────────────────────────────────────────

@Suppress("NestedBlockDepth", "TooGenericExceptionCaught", "SwallowedException")
actual fun fetchWaitlist(
    idToken: String,
    onSuccess: (List<WaitlistDocument>) -> Unit,
    onError: (String) -> Unit,
) {
    val xhr = XMLHttpRequest()
    xhr.open("GET", "$FIRESTORE_BASE/waitlist?pageSize=500")
    xhr.setRequestHeader("Authorization", "Bearer $idToken")
    xhr.asDynamic().onload = {
        if (xhr.status.toInt() in HTTP_OK) {
            @Suppress("UnsafeCastFromDynamic")
            val json: dynamic = JSON.parse(xhr.responseText)
            val docs = json.documents
            val list = mutableListOf<WaitlistDocument>()
            if (docs != null) {
                val len = docs.length.toString().toIntOrNull() ?: 0
                for (i in 0 until len) {
                    try {
                        val f = docs[i].fields
                        list.add(
                            WaitlistDocument(
                                name = f.name?.stringValue?.toString() ?: "",
                                email = f.email?.stringValue?.toString() ?: "",
                                role = f.role?.stringValue?.toString() ?: "",
                                location = f.location?.stringValue?.toString() ?: "",
                                message = f.message?.stringValue?.toString() ?: "",
                                createdAt = f.createdAt?.timestampValue?.toString() ?: "",
                            ),
                        )
                    } catch (e: Throwable) {
                        // skip malformed doc
                    }
                }
            }
            onSuccess(list.sortedByDescending { it.createdAt })
        } else {
            onError("Failed to load waitlist (${xhr.status})")
        }
    }
    xhr.asDynamic().onerror = { onError("Network error") }
    xhr.send()
}

// ── Analytics ─────────────────────────────────────────────────────────────────

@Suppress("TooGenericExceptionCaught", "SwallowedException")
actual fun trackAnalyticsEvent(
    name: String,
    params: Map<String, String>,
) {
    try {
        val gtagFn: dynamic = js("window.gtag")
        if (gtagFn != null) {
            val p: dynamic = js("({})")
            params.forEach { (k, v) -> p[k] = v }
            gtagFn.call(null, "event", name, p)
        }
    } catch (e: Throwable) {
        // gtag not available
    }
}

// ── Navigation ────────────────────────────────────────────────────────────────

actual fun navigateTo(path: String) {
    val w: dynamic = js("window")
    w.location.href = path
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun String.esc() = replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").trim()

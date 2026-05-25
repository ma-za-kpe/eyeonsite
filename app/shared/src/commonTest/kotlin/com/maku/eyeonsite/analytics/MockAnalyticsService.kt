/** MockAnalyticsService — in-memory analytics for unit tests. Never use in production. */
package com.maku.eyeonsite.analytics

/**
 * Test double for [AnalyticsService]. Records all calls so tests can assert on them.
 *
 * Usage:
 * ```kotlin
 * val analytics = MockAnalyticsService()
 * // ... exercise code under test ...
 * analytics.assertEventLogged("cta_clicked", mapOf("cta_id" to "hero_start"))
 * ```
 */
class MockAnalyticsService : AnalyticsService {
    data class LoggedEvent(
        val name: String,
        val params: Map<String, String>,
    )

    private val _events = mutableListOf<LoggedEvent>()
    private val _screenViews = mutableListOf<String>()

    /** All events logged since creation or last [reset]. */
    val events: List<LoggedEvent> get() = _events.toList()

    /** All screen names passed to [logScreenView] since creation or last [reset]. */
    val screenViews: List<String> get() = _screenViews.toList()

    override fun logEvent(
        name: String,
        params: Map<String, String>,
    ) {
        _events.add(LoggedEvent(name, params))
    }

    override fun logScreenView(screenName: String) {
        _screenViews.add(screenName)
    }

    /** Clears all recorded events and screen views. */
    fun reset() {
        _events.clear()
        _screenViews.clear()
    }

    /** Asserts that an event with the given name and params was logged. Throws if not found. */
    fun assertEventLogged(
        name: String,
        params: Map<String, String> = emptyMap(),
    ) {
        val found = _events.any { it.name == name && it.params == params }
        check(found) {
            "Expected event '$name' with params $params to be logged.\n" +
                "Actual events: ${_events.joinToString("\n") { "  ${it.name} ${it.params}" }}"
        }
    }

    /** Asserts that a screen view for [screenName] was logged. */
    fun assertScreenViewed(screenName: String) {
        check(_screenViews.contains(screenName)) {
            "Expected screen '$screenName' to be viewed.\nActual: $_screenViews"
        }
    }

    /** Returns true if an event with this name was logged at least once. */
    fun wasEventLogged(name: String): Boolean = _events.any { it.name == name }
}

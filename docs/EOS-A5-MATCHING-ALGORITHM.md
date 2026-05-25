# A5 — Matching Algorithm

**EyeOnSite** · Manti Labs · May 2026 · v0.1-DRAFT

---

## Design Principle

Matching is the core engine. A good match means: the right inspector reaches the right site quickly, delivers quality evidence, and both parties are happy. A bad match means: delays, poor quality, disputes, and churn on both sides.

The algorithm evolves with volume: V1 is simple geo-distance ranking. V3 adds ML scoring. V5 is a full optimization engine.

---

## V1 Matching Algorithm (Simple, Deterministic)

### When Matching Triggers

A matching cycle starts when a task transitions to `ESCROW_FUNDED`. The Cloud Function `onTaskFunded()` fires and runs the matching pipeline.

### Pipeline Steps

```
Step 1: CANDIDATE POOL
    Query inspectors collection:
    WHERE verificationStatus == "VERIFIED"
    AND tier >= task.inspectorTierRequired
    AND activeTaskId == null              // not currently on another task
    AND verticalSpecializations CONTAINS task.verticalType
    AND location within X km of task.location

    X = configurable per city:
      Accra:   15 km (compact city)
      Lagos:   25 km (sprawling)
      Nairobi: 20 km

Step 2: RANK BY DISTANCE
    For each candidate:
      distance = haversine(inspector.location, task.location)
    Sort ascending (nearest first)

Step 3: FILTER BY TRUST SCORE
    Remove candidates with trustScore < 0.40
    (below this threshold, quality risk is too high)

Step 4: APPLY TIE-BREAKERS
    If multiple candidates at similar distance (within 2km):
      Primary: higher trustScore wins
      Secondary: fewer active tasks today wins (freshness)
      Tertiary: higher total completedTasks wins (experience)

Step 5: PING TOP N CANDIDATES
    Send FCM push to top 3 candidates simultaneously:
    "New task available: [title] — [distance] away — GHS [budget]"
    
    First to accept wins.
    Accept window: 5 minutes per ping round.
    
    If no acceptance in 5 minutes:
      Ping next batch of 3 candidates.
      Maximum 3 ping rounds (9 candidates total).
    
    If all 9 decline/ignore:
      Expand radius by 50% and restart pipeline.
      If still no match after expanded search:
        Task status → EXPIRED
        Client notified: "No inspectors available. Full refund issued."
        Escrow auto-refunded.
```

### V1 Matching — Cloud Function Pseudocode

```javascript
// Cloud Function: onTaskFunded (Firestore trigger)
exports.onTaskFunded = onDocumentWritten('tasks/{taskId}', async (event) => {
  const task = event.data.after.data();
  if (task.status !== 'ESCROW_FUNDED') return;

  const candidates = await findCandidates(task);
  
  if (candidates.length === 0) {
    await expandAndRetry(task);
    return;
  }

  // Ping top 3
  const topCandidates = candidates.slice(0, 3);
  await sendMatchPings(task.id, topCandidates);
  
  // Set timeout for next round
  await scheduleNextPingRound(task.id, candidates.slice(3, 6), 5 * 60);
});

async function findCandidates(task) {
  const radiusKm = getCityRadius(task.city); // 15, 20, or 25
  
  // Firestore GeoPoint query (using geohash range query)
  const nearbyInspectors = await db.collection('inspectors')
    .where('verificationStatus', '==', 'VERIFIED')
    .where('activeTaskId', '==', null)
    .get();
  
  // Filter and sort in-memory (Firestore can't do geo-distance sort natively)
  return nearbyInspectors.docs
    .map(doc => ({
      ...doc.data(),
      distance: haversine(doc.data().location, task.location)
    }))
    .filter(i => i.distance <= radiusKm)
    .filter(i => i.tier >= task.inspectorTierRequired)
    .filter(i => i.verticalSpecializations.includes(task.verticalType))
    .filter(i => i.trustScore >= 0.40)
    .sort((a, b) => {
      // Primary: distance
      if (Math.abs(a.distance - b.distance) > 2) return a.distance - b.distance;
      // Tie-breaker: trust score
      return b.trustScore - a.trustScore;
    });
}

function haversine(point1, point2) {
  const R = 6371; // Earth's radius in km
  const dLat = toRad(point2.latitude - point1.latitude);
  const dLon = toRad(point2.longitude - point1.longitude);
  const a = Math.sin(dLat/2) * Math.sin(dLat/2) +
            Math.cos(toRad(point1.latitude)) * Math.cos(toRad(point2.latitude)) *
            Math.sin(dLon/2) * Math.sin(dLon/2);
  return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
}
```

### Inspector Acceptance Flow

```
Inspector receives FCM push → Opens app → Sees task detail
    │
    ├── "Accept" → 
    │   Inspector commits ETA (auto-calculated from distance, or manual override)
    │   Task status → INSPECTOR_MATCHED
    │   Inspector.activeTaskId = task.id
    │   Client notified: "Inspector [Name] accepted. ETA: [time]"
    │   Cancel other pending pings for this task
    │
    ├── "Decline" →
    │   Remove from current round. No penalty.
    │   If all in current round decline → trigger next round
    │
    └── No response (5 min timeout) →
        Treated as decline. Trigger next round.
```

---

## V2 Matching Enhancements

### Inspector Preferences

Allow inspectors to set preferences that influence matching:

```kotlin
data class InspectorPreferences(
    val maxDistanceKm: Int = 15,            // won't see tasks beyond this
    val preferredVerticals: List<VerticalType>,  // ranked preference
    val minimumBudget: Money? = null,       // won't see tasks below this
    val availableHours: AvailableHours,     // Mon-Fri 8am-6pm, etc.
    val preferredAreas: List<GeoArea>? = null,  // neighborhoods they know well
)
```

### Client Preferences

Allow clients to influence inspector selection:

```kotlin
data class ClientMatchPreferences(
    val preferredInspectorId: String? = null,     // request specific inspector
    val excludeInspectorIds: List<String> = emptyList(), // block specific inspectors
    val minimumTrustScore: Float = 0.60,          // higher than platform default
    val requireProfessionalTier: Boolean = false,
    val sameInspectorAsProject: Boolean = false,   // for recurring: same person each time
)
```

### Recurring Task — Same Inspector

For projects (monthly construction checks), matching the same inspector each time provides consistency. The client sees the same person, the inspector knows the site, and progress comparison is easier.

```
If task.projectId != null AND client.preferences.sameInspectorAsProject:
    previousTask = last completed task in same project
    if previousTask.matchedInspectorId is available:
        prioritize that inspector (skip to Step 5, ping only them first)
    else:
        fall back to normal matching
```

---

## V3 Matching — Scored Matching

Replace deterministic ranking with a composite score:

```
match_score = weighted_sum(
    distance_score:     0.30,  // closer = higher (1.0 at 0km, 0.0 at max radius)
    trust_score:        0.25,  // inspector's trust score directly
    vertical_fit:       0.15,  // has the inspector done this vertical before? How many?
    availability:       0.10,  // tasks completed today (fewer = more available = higher)
    response_history:   0.10,  // acceptance rate for similar tasks
    client_preference:  0.10,  // does the client prefer this inspector?
)
```

### ETA Prediction

At V3, predict actual travel time (not just distance):

```
Inputs:
  - Distance (haversine)
  - Time of day (traffic patterns: Lagos 7-9am = 3x travel time)
  - Inspector's transport mode (motorcycle, car, public transit)
  - Historical data: how long did previous inspectors take for this area?

Output:
  - Predicted ETA (minutes)
  - Confidence interval

Use: display to client as "Estimated arrival: 35-50 min"
```

---

## V4+ Matching — Optimization Engine

At scale (1000+ tasks/day), individual matching isn't optimal. Batch optimization:

```
Every 5 minutes, collect all unmatched tasks
  + all available inspectors
  → Run assignment optimization:
     Minimize: total travel distance across all assignments
     Subject to: tier requirements, trust minimums, capacity constraints
  → Output: optimal inspector-task pairs
  → Ping all at once

This is a variant of the Assignment Problem (Hungarian Algorithm or similar).
```

---

## Matching SLAs

| Metric | V1 Target | V2 Target | V3 Target |
|--------|-----------|-----------|-----------|
| Time to first ping | < 30 seconds | < 15 seconds | < 10 seconds |
| Time to match (acceptance) | < 15 minutes | < 10 minutes | < 5 minutes |
| Match success rate (task gets an inspector) | > 80% | > 90% | > 95% |
| Average distance (inspector to task) | < 10 km | < 8 km | < 6 km |
| Inspector acceptance rate (per ping) | > 30% | > 40% | > 50% |

---

## Edge Cases

### No Inspectors Available

```
Scenario: rural area, no inspectors within radius
Action:
  1. Expand radius by 50%, retry
  2. Expand radius by 100%, retry
  3. If still no match → Task status: EXPIRED
  4. Client notified: "No inspectors available in this area yet.
     Full refund issued. We'll notify you when inspectors are available."
  5. Task location added to "demand signal" database
     → informs inspector recruitment (where should we recruit next?)
```

### Inspector Cancels After Accepting

```
Scenario: Inspector accepted but can't complete (emergency, traffic, changed mind)
Action:
  1. Inspector cancels via app (with reason)
  2. Task status reverts to ESCROW_FUNDED
  3. Re-run matching pipeline (excluding this inspector)
  4. Client notified: "Your inspector had to cancel. Finding a replacement..."
  5. Inspector penalty:
     - 1st cancel: No penalty, but tracked
     - 2nd cancel (in 30 days): Warning
     - 3rd cancel (in 30 days): 48hr task restriction
```

### Simultaneous Acceptance

```
Scenario: Two inspectors accept the same task within milliseconds
Action:
  Firestore transaction ensures atomicity:
  - Read task.status + task.matchedInspectorId
  - If status == ESCROW_FUNDED and matchedInspectorId == null:
      Set matchedInspectorId = first inspector
      Set status = INSPECTOR_MATCHED
  - Second inspector's transaction fails → they see "Task already taken"
  - Use Firestore transactions (optimistic locking) to prevent race conditions
```

### Urgent Tasks (Same-Day / 4-Hour)

```
Scenario: Client marks task as urgent (1.5x or 2x pricing)
Action:
  - Expand initial candidate pool (all inspectors within 2x normal radius)
  - Ping top 5 simultaneously (instead of 3)
  - Reduce ping timeout to 3 minutes (instead of 5)
  - Include "URGENT" badge in FCM push
  - Inspector sees urgency surcharge in task detail
```

---

## Monitoring and Alerting

| Alert | Trigger | Action |
|-------|---------|--------|
| Match failure rate > 20% | 5+ tasks expire in 24hrs | Review inspector supply in affected areas. Emergency recruitment push. |
| Average match time > 20 min | Sustained over 24hrs | Check: are inspectors online? FCM delivery issues? App crashes? |
| Acceptance rate < 20% | Sustained over 48hrs | Check: are task budgets too low? Inspector burnout? Better-paying alternatives? |
| Inspector ghost rate > 10% | Accept but don't complete | Investigate: are inspectors accepting to block competitors? Tighten SLAs. |

---

*See [EOS-03-ARCHITECTURE.md](./EOS-03-ARCHITECTURE.md) for event model and Firestore triggers.*
*See [EOS-06-INSPECTOR-SPEC.md](./EOS-06-INSPECTOR-SPEC.md) for inspector tiers and trust score calculation.*

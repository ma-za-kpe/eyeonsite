# Authentication — Requirements

**Spec ref:** EOS-D1 F1/F2 | EOS-02

## Functional Requirements

### FR-1: Phone OTP
- Enter phone (E.164), Firebase sends OTP SMS
- Enter 6-digit code → Firebase UID created
- `users/{uid}` written to Firestore on first sign-in
- Continue button disabled until ≥ 10 chars

### FR-2: Google Sign-In (demo fallback)
- One-tap Google Sign-In as alternative to OTP

### FR-3: Role Selection (first-time only)
- "I'm verifying my project" → CLIENT
- "I'm here to inspect" → INSPECTOR
- Role persisted to `users/{uid}.role`
- Returning users skip directly to their home screen

### FR-4: Post-Auth Navigation
- CLIENT → ClientHomeScreen
- INSPECTOR → InspectorHomeScreen

### FR-5: Sign-Out
- Clears Firebase Auth + local SQLDelight cache

## Non-Functional
- OTP must work in GH (+233), NG (+234), KE (+254), UG (+256)
- Auth state persists across app restarts (Firebase handles)

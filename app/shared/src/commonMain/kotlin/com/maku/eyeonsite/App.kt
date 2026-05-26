package com.maku.eyeonsite

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.maku.eyeonsite.domain.EvidencePhoto
import com.maku.eyeonsite.domain.TaskStatus
import com.maku.eyeonsite.domain.TaskType
import com.maku.eyeonsite.domain.UploadStatus
import com.maku.eyeonsite.domain.UserRole
import com.maku.eyeonsite.domain.VerificationTask
import com.maku.eyeonsite.ui.auth.AuthState
import com.maku.eyeonsite.ui.auth.AuthViewModel
import com.maku.eyeonsite.ui.auth.OtpScreen
import com.maku.eyeonsite.ui.auth.RoleSelectionScreen
import com.maku.eyeonsite.ui.auth.SignInScreen
import com.maku.eyeonsite.ui.dashboard.ClientDashboardScreen
import com.maku.eyeonsite.ui.dashboard.ClientDashboardViewModel
import com.maku.eyeonsite.ui.evidence.EvidenceCaptureScreen
import com.maku.eyeonsite.ui.evidence.EvidenceCaptureViewModel
import com.maku.eyeonsite.ui.home.HomeScreen
import com.maku.eyeonsite.ui.map.InspectorTaskMapScreen
import com.maku.eyeonsite.ui.map.TaskMapViewModel
import com.maku.eyeonsite.ui.review.EvidenceReviewScreen
import com.maku.eyeonsite.ui.review.EvidenceReviewViewModel
import com.maku.eyeonsite.ui.settings.SettingsScreen
import com.maku.eyeonsite.ui.settings.SettingsViewModel
import com.maku.eyeonsite.ui.task.InspectorMatchingScreen
import com.maku.eyeonsite.ui.task.TaskCreationScreen
import com.maku.eyeonsite.ui.theme.AppVariant
import com.maku.eyeonsite.ui.theme.EyeOnSiteTheme
import com.maku.eyeonsite.ui.theme.ThemeMode
import org.koin.core.context.GlobalContext

private val MOCK_REVIEW_TASK =
    VerificationTask(
        id = "mock-review-001",
        type = TaskType.CONSTRUCTION,
        location = "East Legon, Accra",
        description = "Verify floor slab and column placement on floors 3–5",
        status = TaskStatus.EVIDENCE_SUBMITTED,
    )

private val MOCK_REVIEW_PHOTOS =
    listOf(
        EvidencePhoto(
            id = "photo-001",
            taskId = "mock-review-001",
            latitude = 5.6037,
            longitude = -0.1870,
            capturedAtMs = 1_748_000_000_000L,
            uploadStatus = UploadStatus.UPLOADED,
        ),
        EvidencePhoto(
            id = "photo-002",
            taskId = "mock-review-001",
            latitude = 5.6038,
            longitude = -0.1871,
            capturedAtMs = 1_748_000_060_000L,
            uploadStatus = UploadStatus.UPLOADED,
        ),
        EvidencePhoto(
            id = "photo-003",
            taskId = "mock-review-001",
            latitude = 5.6039,
            longitude = -0.1872,
            capturedAtMs = 1_748_000_120_000L,
            uploadStatus = UploadStatus.UPLOADED,
        ),
    )

private sealed class AppScreen {
    data object Home : AppScreen()

    data object CreateTask : AppScreen()

    data class MatchInspectors(
        val task: VerificationTask,
    ) : AppScreen()

    data class CaptureEvidence(
        val task: VerificationTask,
    ) : AppScreen()

    data class ReviewEvidence(
        val task: VerificationTask,
        val photos: List<EvidencePhoto>,
    ) : AppScreen()

    data object ClientDashboard : AppScreen()

    data object Settings : AppScreen()
}

@Composable
fun App() {
    val authVm: AuthViewModel = viewModel { GlobalContext.get().get() }
    val settingsVm: SettingsViewModel = viewModel { GlobalContext.get().get() }
    val evidenceVm: EvidenceCaptureViewModel = viewModel { GlobalContext.get().get() }
    val mapVm: TaskMapViewModel = viewModel { GlobalContext.get().get() }
    val reviewVm: EvidenceReviewViewModel = viewModel { GlobalContext.get().get() }
    val dashboardVm: ClientDashboardViewModel = viewModel { GlobalContext.get().get() }

    val authState by authVm.state.collectAsState()
    val phone by authVm.phone.collectAsState()
    val themeMode by settingsVm.themeMode.collectAsState()

    val systemDark = isSystemInDarkTheme()
    val isDark =
        when (themeMode) {
            ThemeMode.SYSTEM -> systemDark
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
        }

    val variant =
        if (authState is AuthState.Authenticated &&
            (authState as AuthState.Authenticated).role == UserRole.INSPECTOR
        ) {
            AppVariant.INSPECTOR
        } else {
            AppVariant.CLIENT
        }

    EyeOnSiteTheme(variant = variant, darkTheme = isDark) {
        val isAuthenticated = authState is AuthState.Authenticated
        var appScreen by remember(isAuthenticated) { mutableStateOf<AppScreen>(AppScreen.Home) }

        when (val s = authState) {
            is AuthState.Idle,
            is AuthState.SendingOtp,
            ->
                SignInScreen(
                    isSending = s is AuthState.SendingOtp,
                    onContinue = { p -> authVm.sendOtp(p) },
                )

            is AuthState.OtpSent,
            is AuthState.Verifying,
            is AuthState.Error,
            ->
                OtpScreen(
                    phone = phone,
                    isVerifying = s is AuthState.Verifying,
                    errorMessage = (s as? AuthState.Error)?.message,
                    onVerify = { code -> authVm.verifyOtp(code) },
                    onResend = { authVm.sendOtp(phone) },
                    onBack = { authVm.dismissError() },
                )

            is AuthState.NeedsRole ->
                RoleSelectionScreen(
                    uid = s.uid,
                    onRoleSelected = { uid, role -> authVm.selectRole(uid, role) },
                )

            is AuthState.Authenticated ->
                when (val screen = appScreen) {
                    AppScreen.Home ->
                        if (s.role == UserRole.INSPECTOR) {
                            InspectorTaskMapScreen(
                                vm = mapVm,
                                onAcceptTask = { nearbyTask ->
                                    appScreen =
                                        AppScreen.CaptureEvidence(
                                            VerificationTask(
                                                id = nearbyTask.id,
                                                type = nearbyTask.type,
                                                location = nearbyTask.location,
                                                description = nearbyTask.description,
                                            ),
                                        )
                                },
                            )
                        } else {
                            HomeScreen(
                                role = s.role,
                                onCreateTask = { appScreen = AppScreen.CreateTask },
                                onSettings = { appScreen = AppScreen.Settings },
                                onViewDashboard = { appScreen = AppScreen.ClientDashboard },
                                onReviewEvidence = {
                                    appScreen = AppScreen.ReviewEvidence(MOCK_REVIEW_TASK, MOCK_REVIEW_PHOTOS)
                                },
                            )
                        }

                    AppScreen.ClientDashboard ->
                        ClientDashboardScreen(
                            vm = dashboardVm,
                            clientUid = s.uid,
                            onCreateTask = { appScreen = AppScreen.CreateTask },
                            onBack = { appScreen = AppScreen.Home },
                        )

                    AppScreen.CreateTask ->
                        TaskCreationScreen(
                            onBack = { appScreen = AppScreen.Home },
                            onTaskCreated = { task -> appScreen = AppScreen.MatchInspectors(task) },
                        )

                    is AppScreen.MatchInspectors ->
                        InspectorMatchingScreen(
                            task = screen.task,
                            onBack = { appScreen = AppScreen.CreateTask },
                            onAssign = { appScreen = AppScreen.CaptureEvidence(screen.task) },
                        )

                    is AppScreen.CaptureEvidence ->
                        EvidenceCaptureScreen(
                            task = screen.task,
                            vm = evidenceVm,
                            onBack = { appScreen = AppScreen.MatchInspectors(screen.task) },
                            onSubmitted = { appScreen = AppScreen.Home },
                        )

                    is AppScreen.ReviewEvidence ->
                        EvidenceReviewScreen(
                            vm = reviewVm,
                            task = screen.task,
                            photos = screen.photos,
                            onBack = { appScreen = AppScreen.Home },
                            onReviewSubmitted = { appScreen = AppScreen.Home },
                        )

                    AppScreen.Settings ->
                        SettingsScreen(
                            role = s.role,
                            currentMode = themeMode,
                            onThemeChange = { settingsVm.setThemeMode(it) },
                            onBack = { appScreen = AppScreen.Home },
                            onSignOut = {
                                authVm.signOut()
                                appScreen = AppScreen.Home
                            },
                        )
                }
        }
    }
}

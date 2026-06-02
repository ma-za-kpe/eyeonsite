package com.maku.eyeonsite

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.maku.eyeonsite.domain.ChecklistAnswer
import com.maku.eyeonsite.domain.ClientTaskSummary
import com.maku.eyeonsite.domain.EvidencePhoto
import com.maku.eyeonsite.domain.TaskStatus
import com.maku.eyeonsite.domain.TaskType
import com.maku.eyeonsite.domain.UploadStatus
import com.maku.eyeonsite.domain.UserRole
import com.maku.eyeonsite.domain.VerificationTask
import com.maku.eyeonsite.ui.AppBackHandler
import com.maku.eyeonsite.ui.SystemBarAppearance
import com.maku.eyeonsite.ui.auth.AuthState
import com.maku.eyeonsite.ui.auth.AuthViewModel
import com.maku.eyeonsite.ui.auth.OtpScreen
import com.maku.eyeonsite.ui.auth.RoleSelectionScreen
import com.maku.eyeonsite.ui.auth.SignInScreen
import com.maku.eyeonsite.ui.dashboard.ClientDashboardScreen
import com.maku.eyeonsite.ui.dashboard.ClientDashboardViewModel
import com.maku.eyeonsite.ui.dashboard.TaskDetailScreen
import com.maku.eyeonsite.ui.evidence.ChecklistScreen
import com.maku.eyeonsite.ui.evidence.ChecklistViewModel
import com.maku.eyeonsite.ui.evidence.EvidenceCaptureScreen
import com.maku.eyeonsite.ui.evidence.EvidenceCaptureViewModel
import com.maku.eyeonsite.ui.evidence.EvidenceSummaryScreen
import com.maku.eyeonsite.ui.home.HomeScreen
import com.maku.eyeonsite.ui.map.InspectorHomeViewModel
import com.maku.eyeonsite.ui.map.InspectorTaskMapScreen
import com.maku.eyeonsite.ui.map.TaskMapViewModel
import com.maku.eyeonsite.ui.review.EvidenceReviewScreen
import com.maku.eyeonsite.ui.review.EvidenceReviewViewModel
import com.maku.eyeonsite.ui.settings.SettingsScreen
import com.maku.eyeonsite.ui.settings.SettingsViewModel
import com.maku.eyeonsite.ui.task.CreateTaskViewModel
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

    data class Checklist(
        val task: VerificationTask,
        val photos: List<EvidencePhoto>,
    ) : AppScreen()

    data class EvidenceSummary(
        val task: VerificationTask,
        val photos: List<EvidencePhoto>,
        val answers: List<ChecklistAnswer>,
    ) : AppScreen()

    data class TaskDetail(
        val task: ClientTaskSummary,
    ) : AppScreen()

    data object ClientDashboard : AppScreen()

    data object Settings : AppScreen()
}

@Composable
fun App(forcedVariant: AppVariant? = null) {
    val authVm: AuthViewModel = viewModel { GlobalContext.get().get() }
    val settingsVm: SettingsViewModel = viewModel { GlobalContext.get().get() }
    val evidenceVm: EvidenceCaptureViewModel = viewModel { GlobalContext.get().get() }
    val mapVm: TaskMapViewModel = viewModel { GlobalContext.get().get() }
    val homeVm: InspectorHomeViewModel = viewModel { GlobalContext.get().get() }
    val reviewVm: EvidenceReviewViewModel = viewModel { GlobalContext.get().get() }
    val dashboardVm: ClientDashboardViewModel = viewModel { GlobalContext.get().get() }
    val createTaskVm: CreateTaskViewModel = viewModel { GlobalContext.get().get() }
    val checklistVm: ChecklistViewModel = viewModel { GlobalContext.get().get() }

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

    SystemBarAppearance(isDark = isDark)

    val variant =
        forcedVariant ?: if (authState is AuthState.Authenticated &&
            (authState as AuthState.Authenticated).role == UserRole.INSPECTOR
        ) {
            AppVariant.INSPECTOR
        } else {
            AppVariant.CLIENT
        }

    EyeOnSiteTheme(variant = variant, darkTheme = isDark) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .safeDrawingPadding(),
        ) {
            val isAuthenticated = authState is AuthState.Authenticated
            var backStack by remember(isAuthenticated) {
                mutableStateOf(listOf<AppScreen>(AppScreen.Home))
            }
            val appScreen = backStack.last()

            fun navigate(s: AppScreen) {
                backStack = backStack + s
            }

            fun navigateBack() {
                if (backStack.size > 1) backStack = backStack.dropLast(1)
            }

            AppBackHandler(enabled = backStack.size > 1) { navigateBack() }

            when (val s = authState) {
                is AuthState.CheckingSession -> Unit

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
                    if (forcedVariant != null) {
                        // Flavored build — auto-assign the role that matches this APK's purpose.
                        val autoRole =
                            if (forcedVariant == AppVariant.INSPECTOR) UserRole.INSPECTOR else UserRole.CLIENT
                        LaunchedEffect(s.uid) { authVm.selectRole(s.uid, autoRole) }
                    } else {
                        RoleSelectionScreen(
                            uid = s.uid,
                            onRoleSelected = { uid, role -> authVm.selectRole(uid, role) },
                        )
                    }

                is AuthState.Authenticated ->
                    when (val screen = appScreen) {
                        AppScreen.Home ->
                            if (s.role == UserRole.INSPECTOR) {
                                InspectorTaskMapScreen(
                                    vm = mapVm,
                                    homeVm = homeVm,
                                    inspectorUid = s.uid,
                                    onAcceptTask = { nearbyTask ->
                                        navigate(
                                            AppScreen.CaptureEvidence(
                                                VerificationTask(
                                                    id = nearbyTask.id,
                                                    type = nearbyTask.type,
                                                    location = nearbyTask.location,
                                                    description = nearbyTask.description,
                                                ),
                                            ),
                                        )
                                    },
                                )
                            } else {
                                HomeScreen(
                                    role = s.role,
                                    onCreateTask = { navigate(AppScreen.CreateTask) },
                                    onSettings = { navigate(AppScreen.Settings) },
                                    onViewDashboard = { navigate(AppScreen.ClientDashboard) },
                                    onReviewEvidence = {
                                        navigate(AppScreen.ReviewEvidence(MOCK_REVIEW_TASK, MOCK_REVIEW_PHOTOS))
                                    },
                                )
                            }

                        AppScreen.ClientDashboard ->
                            ClientDashboardScreen(
                                vm = dashboardVm,
                                clientUid = s.uid,
                                onCreateTask = { navigate(AppScreen.CreateTask) },
                                onTaskDetail = { task -> navigate(AppScreen.TaskDetail(task)) },
                                onReviewEvidence = { task ->
                                    navigate(
                                        AppScreen.ReviewEvidence(
                                            VerificationTask(
                                                id = task.id,
                                                type = task.type,
                                                location = task.location,
                                                description = task.description,
                                                status = task.status,
                                            ),
                                            MOCK_REVIEW_PHOTOS,
                                        ),
                                    )
                                },
                                onBack = { navigateBack() },
                            )

                        is AppScreen.TaskDetail ->
                            TaskDetailScreen(
                                task = screen.task,
                                onBack = { navigateBack() },
                                onReviewEvidence =
                                    if (screen.task.status in
                                        setOf(
                                            TaskStatus.EVIDENCE_SUBMITTED,
                                            TaskStatus.UNDER_REVIEW,
                                        )
                                    ) {
                                        {
                                            navigate(
                                                AppScreen.ReviewEvidence(
                                                    VerificationTask(
                                                        id = screen.task.id,
                                                        type = screen.task.type,
                                                        location = screen.task.location,
                                                        description = screen.task.description,
                                                        status = screen.task.status,
                                                    ),
                                                    MOCK_REVIEW_PHOTOS,
                                                ),
                                            )
                                        }
                                    } else {
                                        null
                                    },
                            )

                        AppScreen.CreateTask ->
                            TaskCreationScreen(
                                vm = createTaskVm,
                                onBack = { navigateBack() },
                                onTaskCreated = { task -> navigate(AppScreen.MatchInspectors(task)) },
                            )

                        is AppScreen.MatchInspectors ->
                            InspectorMatchingScreen(
                                task = screen.task,
                                onBack = { navigateBack() },
                                onAssign = { navigate(AppScreen.CaptureEvidence(screen.task)) },
                            )

                        is AppScreen.CaptureEvidence ->
                            EvidenceCaptureScreen(
                                task = screen.task,
                                vm = evidenceVm,
                                onBack = { navigateBack() },
                                onSubmitted = { photos -> navigate(AppScreen.Checklist(screen.task, photos)) },
                            )

                        is AppScreen.Checklist ->
                            ChecklistScreen(
                                vm = checklistVm,
                                task = screen.task,
                                onBack = { navigateBack() },
                                onDone = { answers ->
                                    navigate(AppScreen.EvidenceSummary(screen.task, screen.photos, answers))
                                },
                            )

                        is AppScreen.EvidenceSummary ->
                            EvidenceSummaryScreen(
                                task = screen.task,
                                photos = screen.photos,
                                checklistAnswers = screen.answers,
                                onBack = { navigateBack() },
                                onConfirmed = { backStack = listOf(AppScreen.Home) },
                            )

                        is AppScreen.ReviewEvidence ->
                            EvidenceReviewScreen(
                                vm = reviewVm,
                                task = screen.task,
                                photos = screen.photos,
                                onBack = { navigateBack() },
                                onReviewSubmitted = { backStack = listOf(AppScreen.Home) },
                            )

                        AppScreen.Settings ->
                            SettingsScreen(
                                role = s.role,
                                currentMode = themeMode,
                                onThemeChange = { settingsVm.setThemeMode(it) },
                                onBack = { navigateBack() },
                                onSignOut = {
                                    authVm.signOut()
                                    backStack = listOf(AppScreen.Home)
                                },
                            )
                    }
            }
        } // Box
    }
}

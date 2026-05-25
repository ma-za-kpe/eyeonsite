package com.maku.eyeonsite

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.maku.eyeonsite.domain.VerificationTask
import com.maku.eyeonsite.ui.auth.AuthState
import com.maku.eyeonsite.ui.auth.AuthViewModel
import com.maku.eyeonsite.ui.auth.OtpScreen
import com.maku.eyeonsite.ui.auth.RoleSelectionScreen
import com.maku.eyeonsite.ui.auth.SignInScreen
import com.maku.eyeonsite.ui.home.HomeScreen
import com.maku.eyeonsite.ui.task.InspectorMatchingScreen
import com.maku.eyeonsite.ui.task.TaskCreationScreen
import com.maku.eyeonsite.ui.theme.EyeOnSiteTheme
import org.koin.core.context.GlobalContext

private sealed class AppScreen {
    data object Home : AppScreen()

    data object CreateTask : AppScreen()

    data class MatchInspectors(
        val task: VerificationTask,
    ) : AppScreen()
}

@Composable
fun App() {
    EyeOnSiteTheme {
        val vm: AuthViewModel = viewModel { GlobalContext.get().get() }
        val authState by vm.state.collectAsState()
        val phone by vm.phone.collectAsState()

        val isAuthenticated = authState is AuthState.Authenticated
        var appScreen by remember(isAuthenticated) { mutableStateOf<AppScreen>(AppScreen.Home) }

        when (val s = authState) {
            is AuthState.Idle,
            is AuthState.SendingOtp,
            ->
                SignInScreen(
                    isSending = s is AuthState.SendingOtp,
                    onContinue = { p -> vm.sendOtp(p) },
                )

            is AuthState.OtpSent,
            is AuthState.Verifying,
            is AuthState.Error,
            ->
                OtpScreen(
                    phone = phone,
                    isVerifying = s is AuthState.Verifying,
                    errorMessage = (s as? AuthState.Error)?.message,
                    onVerify = { code -> vm.verifyOtp(code) },
                    onResend = { vm.sendOtp(phone) },
                    onBack = { vm.dismissError() },
                )

            is AuthState.NeedsRole ->
                RoleSelectionScreen(
                    uid = s.uid,
                    onRoleSelected = { uid, role -> vm.selectRole(uid, role) },
                )

            is AuthState.Authenticated ->
                when (val screen = appScreen) {
                    AppScreen.Home ->
                        HomeScreen(
                            role = s.role,
                            onCreateTask = { appScreen = AppScreen.CreateTask },
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
                        )
                }
        }
    }
}

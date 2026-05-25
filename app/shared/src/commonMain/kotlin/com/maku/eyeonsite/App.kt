package com.maku.eyeonsite

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.maku.eyeonsite.ui.auth.AuthState
import com.maku.eyeonsite.ui.auth.AuthViewModel
import com.maku.eyeonsite.ui.auth.OtpScreen
import com.maku.eyeonsite.ui.auth.RoleSelectionScreen
import com.maku.eyeonsite.ui.auth.SignInScreen
import com.maku.eyeonsite.ui.home.HomeScreen
import com.maku.eyeonsite.ui.theme.EyeOnSiteTheme
import org.koin.core.context.GlobalContext

@Composable
fun App() {
    EyeOnSiteTheme {
        val vm: AuthViewModel = viewModel { GlobalContext.get().get() }
        val state by vm.state.collectAsState()
        val phone by vm.phone.collectAsState()

        when (val s = state) {
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

            is AuthState.Authenticated -> HomeScreen(role = s.role)
        }
    }
}

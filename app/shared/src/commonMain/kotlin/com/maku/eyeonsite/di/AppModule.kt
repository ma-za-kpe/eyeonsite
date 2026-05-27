package com.maku.eyeonsite.di

import com.maku.eyeonsite.data.MockNearbyTaskRepository
import com.maku.eyeonsite.data.NoOpInspectorAvailabilityRepository
import com.maku.eyeonsite.onboarding.InspectorOnboardingViewModel
import com.maku.eyeonsite.payment.FundTaskViewModel
import com.maku.eyeonsite.ui.auth.AuthViewModel
import com.maku.eyeonsite.ui.auth.FirebaseAuthProvider
import com.maku.eyeonsite.ui.auth.createFirebaseAuthProvider
import com.maku.eyeonsite.ui.dashboard.ClientDashboardViewModel
import com.maku.eyeonsite.ui.evidence.ChecklistViewModel
import com.maku.eyeonsite.ui.evidence.EvidenceCaptureViewModel
import com.maku.eyeonsite.ui.map.InspectorAvailabilityRepository
import com.maku.eyeonsite.ui.map.InspectorHomeViewModel
import com.maku.eyeonsite.ui.map.NearbyTaskRepository
import com.maku.eyeonsite.ui.map.TaskMapViewModel
import com.maku.eyeonsite.ui.review.EvidenceReviewViewModel
import com.maku.eyeonsite.ui.settings.SettingsViewModel
import com.maku.eyeonsite.ui.task.CreateTaskViewModel
import org.koin.core.module.Module
import org.koin.dsl.module

val authModule =
    module {
        factory<FirebaseAuthProvider> { createFirebaseAuthProvider() }
        factory { AuthViewModel(get(), get()) }
        factory { SettingsViewModel() }
        factory { EvidenceCaptureViewModel(get(), get(), get()) }
        factory { ChecklistViewModel(get()) }
        factory<NearbyTaskRepository> { MockNearbyTaskRepository() }
        factory { TaskMapViewModel(get()) }
        factory<InspectorAvailabilityRepository> { NoOpInspectorAvailabilityRepository() }
        factory { InspectorHomeViewModel(get(), get()) }
        factory { EvidenceReviewViewModel(get(), get()) }
        factory { ClientDashboardViewModel(get(), get()) }
        factory { CreateTaskViewModel(get(), get(), get()) }
        factory { InspectorOnboardingViewModel(get(), get()) }
        factory { FundTaskViewModel(get(), get()) }
    }

expect val analyticsModule: Module

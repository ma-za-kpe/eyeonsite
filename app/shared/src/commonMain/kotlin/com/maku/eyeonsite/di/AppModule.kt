package com.maku.eyeonsite.di

import com.maku.eyeonsite.data.MockNearbyTaskRepository
import com.maku.eyeonsite.ui.auth.AuthViewModel
import com.maku.eyeonsite.ui.auth.FirebaseAuthProvider
import com.maku.eyeonsite.ui.auth.createFirebaseAuthProvider
import com.maku.eyeonsite.ui.evidence.EvidenceCaptureViewModel
import com.maku.eyeonsite.ui.evidence.EvidenceUploader
import com.maku.eyeonsite.ui.evidence.NoOpEvidenceUploader
import com.maku.eyeonsite.ui.map.NearbyTaskRepository
import com.maku.eyeonsite.ui.map.TaskMapViewModel
import com.maku.eyeonsite.ui.review.EvidenceReviewViewModel
import com.maku.eyeonsite.ui.review.EvidenceReviewer
import com.maku.eyeonsite.ui.review.NoOpEvidenceReviewer
import com.maku.eyeonsite.ui.settings.SettingsViewModel
import org.koin.core.module.Module
import org.koin.dsl.module

val authModule =
    module {
        factory<FirebaseAuthProvider> { createFirebaseAuthProvider() }
        factory { AuthViewModel(get(), get()) }
        factory { SettingsViewModel() }
        factory<EvidenceUploader> { NoOpEvidenceUploader() }
        factory { EvidenceCaptureViewModel(get()) }
        factory<NearbyTaskRepository> { MockNearbyTaskRepository() }
        factory { TaskMapViewModel(get()) }
        factory<EvidenceReviewer> { NoOpEvidenceReviewer() }
        factory { EvidenceReviewViewModel(get()) }
    }

expect val analyticsModule: Module

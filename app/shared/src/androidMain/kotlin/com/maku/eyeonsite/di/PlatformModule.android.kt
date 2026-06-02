package com.maku.eyeonsite.di

import android.content.pm.ApplicationInfo
import com.maku.eyeonsite.analytics.AnalyticsService
import com.maku.eyeonsite.analytics.FirebaseAnalyticsService
import com.maku.eyeonsite.data.EvidenceRepository
import com.maku.eyeonsite.data.FirestoreClientTaskRepository
import com.maku.eyeonsite.data.FirestoreEvidenceRepository
import com.maku.eyeonsite.data.FirestoreTaskRepository
import com.maku.eyeonsite.data.PendingEvidenceRepository
import com.maku.eyeonsite.data.PendingUploadRepository
import com.maku.eyeonsite.data.SqlDelightPendingEvidenceRepository
import com.maku.eyeonsite.data.SqlDelightPendingUploadRepository
import com.maku.eyeonsite.data.TaskRepository
import com.maku.eyeonsite.db.createDatabase
import com.maku.eyeonsite.onboarding.FirestoreInspectorOnboardingRepository
import com.maku.eyeonsite.onboarding.InspectorOnboardingRepository
import com.maku.eyeonsite.onboarding.MockInspectorOnboardingRepository
import com.maku.eyeonsite.payment.FirebasePaymentRepository
import com.maku.eyeonsite.payment.MockPaymentRepository
import com.maku.eyeonsite.payment.PaymentRepository
import com.maku.eyeonsite.platform.AndroidGeocoderService
import com.maku.eyeonsite.platform.AppCheckAttestationService
import com.maku.eyeonsite.platform.AttestationService
import com.maku.eyeonsite.platform.GeocoderService
import com.maku.eyeonsite.ui.dashboard.ClientTaskRepository
import com.maku.eyeonsite.ui.evidence.EvidenceUploader
import com.maku.eyeonsite.ui.evidence.FirebaseStorageEvidenceUploader
import com.maku.eyeonsite.ui.evidence.LocalPhotoQualityAnalyzer
import com.maku.eyeonsite.ui.evidence.PhotoQualityAnalyzer
import com.maku.eyeonsite.ui.review.EvidenceReviewer
import com.maku.eyeonsite.ui.review.FirestoreEvidenceReviewer
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val analyticsModule: Module =
    module {
        single<AnalyticsService> { FirebaseAnalyticsService(androidContext()) }
        single<GeocoderService> { AndroidGeocoderService(androidContext()) }
        single<AttestationService> {
            val isDebug = (androidContext().applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
            AppCheckAttestationService(isDebug)
        }
        single<TaskRepository> { FirestoreTaskRepository() }
        single<EvidenceRepository> {
            FirestoreEvidenceRepository(
                context = androidContext(),
                pendingUploadRepo = get(),
                pendingEvidenceRepo = get(),
            )
        }
        single<EvidenceUploader> { FirebaseStorageEvidenceUploader() }
        single<EvidenceReviewer> { FirestoreEvidenceReviewer() }
        single { createDatabase(androidContext()) }
        single<ClientTaskRepository> { FirestoreClientTaskRepository(get()) }
        single<PendingUploadRepository> { SqlDelightPendingUploadRepository(get()) }
        single<PendingEvidenceRepository> { SqlDelightPendingEvidenceRepository(get()) }
        single<PhotoQualityAnalyzer> { LocalPhotoQualityAnalyzer() }
        single<InspectorOnboardingRepository> {
            val isDebug = (androidContext().applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
            if (isDebug) MockInspectorOnboardingRepository() else FirestoreInspectorOnboardingRepository()
        }
        single<PaymentRepository> {
            val isDebug = (androidContext().applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
            if (isDebug) MockPaymentRepository() else FirebasePaymentRepository()
        }
    }

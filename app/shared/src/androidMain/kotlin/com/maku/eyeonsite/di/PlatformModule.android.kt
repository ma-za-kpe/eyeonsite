package com.maku.eyeonsite.di

import com.maku.eyeonsite.analytics.AnalyticsService
import com.maku.eyeonsite.analytics.FirebaseAnalyticsService
import com.maku.eyeonsite.data.EvidenceRepository
import com.maku.eyeonsite.data.FirestoreClientTaskRepository
import com.maku.eyeonsite.data.FirestoreEvidenceRepository
import com.maku.eyeonsite.data.FirestoreTaskRepository
import com.maku.eyeonsite.data.PendingUploadRepository
import com.maku.eyeonsite.data.SqlDelightPendingUploadRepository
import com.maku.eyeonsite.data.TaskRepository
import com.maku.eyeonsite.db.createDatabase
import com.maku.eyeonsite.platform.AndroidGeocoderService
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
        single<TaskRepository> { FirestoreTaskRepository() }
        single<EvidenceRepository> { FirestoreEvidenceRepository() }
        single<EvidenceUploader> { FirebaseStorageEvidenceUploader() }
        single<EvidenceReviewer> { FirestoreEvidenceReviewer() }
        single { createDatabase(androidContext()) }
        single<ClientTaskRepository> { FirestoreClientTaskRepository(get()) }
        single<PendingUploadRepository> { SqlDelightPendingUploadRepository(get()) }
        single<PhotoQualityAnalyzer> { LocalPhotoQualityAnalyzer() }
    }

package com.maku.eyeonsite.di

import com.maku.eyeonsite.analytics.AnalyticsService
import com.maku.eyeonsite.analytics.FirebaseAnalyticsService
import com.maku.eyeonsite.data.EvidenceRepository
import com.maku.eyeonsite.data.FirestoreEvidenceRepository
import com.maku.eyeonsite.data.FirestoreTaskRepository
import com.maku.eyeonsite.data.TaskRepository
import com.maku.eyeonsite.platform.AndroidGeocoderService
import com.maku.eyeonsite.platform.GeocoderService
import com.maku.eyeonsite.ui.evidence.EvidenceUploader
import com.maku.eyeonsite.ui.evidence.FirebaseStorageEvidenceUploader
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
    }

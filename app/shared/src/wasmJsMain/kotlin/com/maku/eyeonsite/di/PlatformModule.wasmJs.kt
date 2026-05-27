package com.maku.eyeonsite.di

import com.maku.eyeonsite.analytics.AnalyticsService
import com.maku.eyeonsite.analytics.NoOpAnalyticsService
import com.maku.eyeonsite.data.EvidenceRepository
import com.maku.eyeonsite.data.MockClientTaskRepository
import com.maku.eyeonsite.data.NoOpEvidenceRepository
import com.maku.eyeonsite.data.NoOpPendingUploadRepository
import com.maku.eyeonsite.data.NoOpTaskRepository
import com.maku.eyeonsite.data.PendingUploadRepository
import com.maku.eyeonsite.data.TaskRepository
import com.maku.eyeonsite.platform.GeocoderService
import com.maku.eyeonsite.platform.NoOpGeocoderService
import com.maku.eyeonsite.ui.dashboard.ClientTaskRepository
import com.maku.eyeonsite.ui.evidence.EvidenceUploader
import com.maku.eyeonsite.ui.evidence.NoOpEvidenceUploader
import com.maku.eyeonsite.ui.review.EvidenceReviewer
import com.maku.eyeonsite.ui.review.NoOpEvidenceReviewer
import org.koin.core.module.Module
import org.koin.dsl.module

actual val analyticsModule: Module =
    module {
        single<AnalyticsService> { NoOpAnalyticsService }
        single<GeocoderService> { NoOpGeocoderService }
        single<TaskRepository> { NoOpTaskRepository() }
        single<EvidenceRepository> { NoOpEvidenceRepository() }
        single<EvidenceUploader> { NoOpEvidenceUploader() }
        single<EvidenceReviewer> { NoOpEvidenceReviewer() }
        single<ClientTaskRepository> { MockClientTaskRepository() }
        single<PendingUploadRepository> { NoOpPendingUploadRepository() }
    }

package com.maku.eyeonsite.di

import com.maku.eyeonsite.analytics.AnalyticsService
import com.maku.eyeonsite.analytics.NoOpAnalyticsService
import com.maku.eyeonsite.data.EvidenceRepository
import com.maku.eyeonsite.data.NoOpEvidenceRepository
import com.maku.eyeonsite.data.NoOpTaskRepository
import com.maku.eyeonsite.data.TaskRepository
import com.maku.eyeonsite.platform.GeocoderService
import com.maku.eyeonsite.platform.NoOpGeocoderService
import com.maku.eyeonsite.ui.evidence.EvidenceUploader
import com.maku.eyeonsite.ui.evidence.NoOpEvidenceUploader
import org.koin.core.module.Module
import org.koin.dsl.module

actual val analyticsModule: Module =
    module {
        single<AnalyticsService> { NoOpAnalyticsService }
        single<GeocoderService> { NoOpGeocoderService }
        single<TaskRepository> { NoOpTaskRepository() }
        single<EvidenceRepository> { NoOpEvidenceRepository() }
        single<EvidenceUploader> { NoOpEvidenceUploader() }
    }

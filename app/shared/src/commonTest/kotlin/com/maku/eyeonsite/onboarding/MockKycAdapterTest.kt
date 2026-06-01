package com.maku.eyeonsite.onboarding

import com.maku.eyeonsite.domain.KycProvider
import com.maku.eyeonsite.domain.KycStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MockKycAdapterTest {
    private val repo = MockInspectorOnboardingRepository()

    @Test
    fun `uploadIdDocuments always succeeds without touching storage`() =
        runTest {
            val result = repo.uploadIdDocuments("uid-mock", byteArrayOf(1, 2), byteArrayOf(3, 4))
            assertTrue(result.isSuccess)
        }

    @Test
    fun `uploadIdDocuments returns mock paths containing the uid`() =
        runTest {
            val (idPath, selfiePath) = repo.uploadIdDocuments("uid-abc", byteArrayOf(), byteArrayOf()).getOrThrow()
            assertTrue(idPath.contains("uid-abc"))
            assertTrue(selfiePath.contains("uid-abc"))
        }

    @Test
    fun `submitProfile always succeeds without touching Firestore`() =
        runTest {
            val result = repo.submitProfile("uid-mock", "Jane Doe", "Accra", 20, 4, "", "")
            assertTrue(result.isSuccess)
        }

    @Test
    fun `observeKycStatus emits APPROVED with provider MOCK`() =
        runTest {
            val kyc = repo.observeKycStatus("uid-mock").first()
            assertEquals(KycProvider.MOCK, kyc.provider)
            assertEquals(KycStatus.APPROVED, kyc.status)
        }

    @Test
    fun `observeKycStatus APPROVED means debug builds skip Smile ID gate`() =
        runTest {
            val kyc = repo.observeKycStatus("any-uid").first()
            assertTrue(kyc.status == KycStatus.APPROVED, "Mock should auto-approve so debug builds reach the dashboard")
        }
}

package com.linkit.company.data.repository

import com.linkit.company.data.datasource.terms.TermsRemoteDataSource
import com.linkit.company.data.dto.terms.TermsResponse
import com.linkit.company.domain.model.terms.TermsDocumentType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.runBlocking

class TermsRepositoryImplTest {

    @Test
    fun unknownTermTypesAreDroppedFromAgreements() = runBlocking<Unit> {
        val repository = TermsRepositoryImpl(
            FakeTermsRemoteDataSource(
                listOf(
                    termsResponse("SERVICE", agreed = false),
                    termsResponse("MARKETING", agreed = false),
                ),
            ),
        )

        val agreements = repository.getTermsAgreements()

        assertEquals(listOf(TermsDocumentType.SERVICE), agreements.map { it.type })
        assertEquals(false, agreements.single().agreed)
    }

    @Test
    fun documentsUseConstantUrlsUntilServerListIsFetched() = runBlocking<Unit> {
        val repository = TermsRepositoryImpl(FakeTermsRemoteDataSource(emptyList()))

        val documents = repository.getTermsDocuments()

        assertEquals(TermsDocumentType.entries, documents.map { it.type })
        assertEquals("https://linktrip.cloud/terms/service", documents[0].url)
    }

    @Test
    fun fetchedDetailUrlsOverrideConstantUrlsForServerTerms() = runBlocking<Unit> {
        val repository = TermsRepositoryImpl(
            FakeTermsRemoteDataSource(
                listOf(
                    termsResponse("SERVICE", agreed = true, detailUrl = "https://pingo.notion.site/terms"),
                    termsResponse("PRIVACY", agreed = true, detailUrl = "https://pingo.notion.site/privacy"),
                ),
            ),
        )

        repository.getTermsAgreements()
        val documents = repository.getTermsDocuments().associateBy { it.type }

        assertEquals("https://pingo.notion.site/terms", documents.getValue(TermsDocumentType.SERVICE).url)
        assertEquals("https://pingo.notion.site/privacy", documents.getValue(TermsDocumentType.PRIVACY).url)
        assertEquals("https://linktrip.cloud/terms/oss", documents.getValue(TermsDocumentType.OPEN_SOURCE).url)
        assertEquals("https://linktrip.cloud/terms/location", documents.getValue(TermsDocumentType.LOCATION).url)
    }

    @Test
    fun agreeTermsSendsEnumNames() = runBlocking<Unit> {
        val remote = FakeTermsRemoteDataSource(emptyList())
        val repository = TermsRepositoryImpl(remote)

        repository.agreeTerms(listOf(TermsDocumentType.SERVICE, TermsDocumentType.PRIVACY))

        assertEquals(listOf(listOf("SERVICE", "PRIVACY")), remote.agreedTypes)
    }

    private fun termsResponse(
        type: String,
        agreed: Boolean,
        detailUrl: String = "https://example.com/$type",
    ) = TermsResponse(
        type = type,
        title = "$type 동의",
        required = true,
        version = 1,
        detailUrl = detailUrl,
        agreed = agreed,
    )

    private class FakeTermsRemoteDataSource(
        private val terms: List<TermsResponse>,
    ) : TermsRemoteDataSource {
        val agreedTypes = mutableListOf<List<String>>()

        override suspend fun getTerms(): List<TermsResponse> = terms

        override suspend fun agree(types: List<String>) {
            agreedTypes += types
        }
    }
}

package com.linkit.company.domain.fake

import com.linkit.company.domain.model.terms.TermsAgreement
import com.linkit.company.domain.model.terms.TermsDocument
import com.linkit.company.domain.model.terms.TermsDocumentType
import com.linkit.company.domain.repository.TermsRepository

/**
 * 응답을 큐로 주입할 수 있는 약관 저장소.
 * [getResults]·[agreeResults]의 원소가 [Throwable]이면 해당 호출에서 던지고, 그 외는 정상 응답한다.
 */
internal class ScriptedTermsRepository(
    private val agreements: List<TermsAgreement> = allAgreedTerms(),
    getResults: List<Throwable?> = emptyList(),
    agreeResults: List<Throwable?> = emptyList(),
) : TermsRepository {
    val events = mutableListOf<String>()
    val agreedTypes = mutableListOf<List<TermsDocumentType>>()
    private val getQueue = ArrayDeque(getResults)
    private val agreeQueue = ArrayDeque(agreeResults)

    override suspend fun getTermsDocuments(): List<TermsDocument> =
        agreements.map { TermsDocument(it.type, it.title, it.detailUrl) }

    override suspend fun getTermsAgreements(): List<TermsAgreement> {
        events += "getTermsAgreements"
        getQueue.removeFirstOrNull()?.let { throw it }
        return agreements
    }

    override suspend fun agreeTerms(types: List<TermsDocumentType>) {
        events += "agreeTerms(${types.joinToString(",")})"
        agreedTypes += types
        agreeQueue.removeFirstOrNull()?.let { throw it }
    }
}

internal fun termsAgreement(
    type: TermsDocumentType,
    agreed: Boolean,
    required: Boolean = true,
) = TermsAgreement(
    type = type,
    title = "${type.name} 약관",
    required = required,
    version = 1,
    detailUrl = "https://example.com/${type.name.lowercase()}",
    agreed = agreed,
)

internal fun allAgreedTerms() = listOf(
    termsAgreement(TermsDocumentType.SERVICE, agreed = true),
    termsAgreement(TermsDocumentType.PRIVACY, agreed = true),
)

internal fun pendingTerms() = listOf(
    termsAgreement(TermsDocumentType.SERVICE, agreed = true),
    termsAgreement(TermsDocumentType.PRIVACY, agreed = false),
)

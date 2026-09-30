package com.linkit.company.data.repository

import com.linkit.company.data.DataScope
import com.linkit.company.data.datasource.terms.TermsRemoteDataSource
import com.linkit.company.data.mapper.toDomainOrNull
import com.linkit.company.domain.model.terms.TermsAgreement
import com.linkit.company.domain.model.terms.TermsDocument
import com.linkit.company.domain.model.terms.TermsDocumentType
import com.linkit.company.domain.repository.TermsRepository
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * 약관 동의 상태는 서버(`/terms`)가 단일 출처이고, 열람용 문서 4종은 상수로 둔다.
 *
 * 서버는 서비스·개인정보 2종만 다루므로 오픈소스·위치기반 약관은 상수 주소를 유지한다.
 * [getTermsAgreements]로 받은 `detailUrl`은 메모리에 보관해 [getTermsDocuments]의 상수 주소를 덮어쓴다
 * (온보딩 `상세보기`·마이페이지 약관 화면이 서버가 정한 전문 주소를 열도록). 이 캐시 때문에 `@SingleIn`이 필요하다.
 */
@Inject
@ContributesBinding(DataScope::class)
@SingleIn(DataScope::class)
class TermsRepositoryImpl(
    private val termsRemoteDataSource: TermsRemoteDataSource,
) : TermsRepository {

    private val detailUrls = MutableStateFlow<Map<TermsDocumentType, String>>(emptyMap())

    override suspend fun getTermsDocuments(): List<TermsDocument> {
        val overrides = detailUrls.value
        return TermsDocumentType.entries.map { type ->
            val document = when (type) {
                TermsDocumentType.SERVICE -> TermsDocument(type, "서비스 이용약관", "$BaseUrl/service")
                TermsDocumentType.PRIVACY -> TermsDocument(type, "개인정보 처리방침", "$BaseUrl/privacy")
                TermsDocumentType.OPEN_SOURCE -> TermsDocument(type, "오픈소스 라이센스 고지", "$BaseUrl/oss")
                TermsDocumentType.LOCATION -> TermsDocument(type, "위치기반 서비스 이용약관", "$BaseUrl/location")
            }
            overrides[type]?.let { document.copy(url = it) } ?: document
        }
    }

    override suspend fun getTermsAgreements(): List<TermsAgreement> {
        val agreements = termsRemoteDataSource.getTerms().mapNotNull { it.toDomainOrNull() }
        detailUrls.update { cached -> cached + agreements.associate { it.type to it.detailUrl } }
        return agreements
    }

    override suspend fun agreeTerms(types: List<TermsDocumentType>) {
        termsRemoteDataSource.agree(types.map { it.name })
    }

    private companion object {
        /** 서버 목록에 없는 약관의 운영 확정 전 자리 표시 주소. */
        const val BaseUrl = "https://linktrip.cloud/terms"
    }
}

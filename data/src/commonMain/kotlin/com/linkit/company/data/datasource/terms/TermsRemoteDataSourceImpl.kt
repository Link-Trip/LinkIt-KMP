package com.linkit.company.data.datasource.terms

import com.linkit.company.data.DataScope
import com.linkit.company.data.api.TermsApi
import com.linkit.company.data.dto.terms.AgreeTermsRequest
import com.linkit.company.data.dto.terms.TermsResponse
import de.jensklingenberg.ktorfit.Ktorfit
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@Inject
@ContributesBinding(DataScope::class)
class TermsRemoteDataSourceImpl(
    ktorfit: Ktorfit,
) : TermsRemoteDataSource {

    private val api = ktorfit.create<TermsApi>()

    override suspend fun getTerms(): List<TermsResponse> {
        return api.getTerms().data?.terms.orEmpty()
    }

    override suspend fun agree(types: List<String>) {
        api.agree(AgreeTermsRequest(types = types))
    }
}

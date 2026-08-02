package com.linkit.company.feature.map.main

import androidx.compose.runtime.Composable

/** Resolves the country and first-level region at the current map center. */
@Composable
internal expect fun MapCenterLocationEffect(
    coordinate: MapCoordinateUiModel,
    onLocationResolved: (coordinate: MapCoordinateUiModel, label: String) -> Unit,
)

internal fun formatMapCenterLocationLabel(
    country: String?,
    administrativeArea: String?,
    locality: String?,
): String? {
    val normalizedCountry = country
        ?.trim()
        ?.takeIf(String::isNotEmpty)
        ?.toKoreanCountryName()
        ?: return null
    val region = sequenceOf(administrativeArea, locality)
        .mapNotNull { it?.trim()?.takeIf(String::isNotEmpty) }
        .firstOrNull { it.toKoreanCountryName() != normalizedCountry }
        ?: return null

    return "$normalizedCountry, ${region.normalizeForCountry(normalizedCountry)}"
}

private fun String.toKoreanCountryName(): String = when (aliasKey()) {
    "japan", "日本", "日本国", "일본" -> "일본"
    "republic of korea", "south korea", "korea", "대한민국", "한국" -> "대한민국"
    else -> this
}

private fun String.normalizeForCountry(country: String): String {
    val localizedRegion = when (country) {
        "일본" -> JapaneseRegionAliases[regionAliasKey()]
        "대한민국" -> KoreanRegionAliases[regionAliasKey()]
        else -> null
    }
    if (localizedRegion != null) return localizedRegion

    return if (country == "일본" && this != "홋카이도" && (endsWith("부") || endsWith("현"))) {
        dropLast(1)
    } else {
        this
    }
}

private fun String.aliasKey(): String = lowercase().trim()

private fun String.regionAliasKey(): String = aliasKey()
    .removeSuffix(" prefecture")
    .removeSuffix(" metropolis")
    .removeSuffix("-ken")
    .removeSuffix("-fu")
    .removeSuffix("-to")
    .removeSuffix("-do")

private val JapaneseRegionAliases = mapOf(
    "hokkaido" to "홋카이도",
    "北海道" to "홋카이도",
    "aomori" to "아오모리",
    "iwate" to "이와테",
    "miyagi" to "미야기",
    "akita" to "아키타",
    "yamagata" to "야마가타",
    "fukushima" to "후쿠시마",
    "ibaraki" to "이바라키",
    "tochigi" to "도치기",
    "gunma" to "군마",
    "saitama" to "사이타마",
    "埼玉県" to "사이타마",
    "chiba" to "지바",
    "千葉県" to "지바",
    "tokyo" to "도쿄",
    "東京都" to "도쿄",
    "도쿄도" to "도쿄",
    "kanagawa" to "가나가와",
    "神奈川県" to "가나가와",
    "niigata" to "니가타",
    "toyama" to "도야마",
    "ishikawa" to "이시카와",
    "fukui" to "후쿠이",
    "yamanashi" to "야마나시",
    "nagano" to "나가노",
    "gifu" to "기후",
    "shizuoka" to "시즈오카",
    "aichi" to "아이치",
    "mie" to "미에",
    "shiga" to "시가",
    "osaka" to "오사카",
    "大阪府" to "오사카",
    "오사카부" to "오사카",
    "kyoto" to "교토",
    "京都府" to "교토",
    "교토부" to "교토",
    "hyogo" to "효고",
    "nara" to "나라",
    "wakayama" to "와카야마",
    "tottori" to "돗토리",
    "shimane" to "시마네",
    "okayama" to "오카야마",
    "hiroshima" to "히로시마",
    "yamaguchi" to "야마구치",
    "tokushima" to "도쿠시마",
    "kagawa" to "가가와",
    "ehime" to "에히메",
    "kochi" to "고치",
    "fukuoka" to "후쿠오카",
    "福岡県" to "후쿠오카",
    "saga" to "사가",
    "nagasaki" to "나가사키",
    "kumamoto" to "구마모토",
    "oita" to "오이타",
    "miyazaki" to "미야자키",
    "kagoshima" to "가고시마",
    "okinawa" to "오키나와",
    "沖縄県" to "오키나와",
)

private val KoreanRegionAliases = mapOf(
    "seoul" to "서울특별시",
    "busan" to "부산광역시",
    "daegu" to "대구광역시",
    "incheon" to "인천광역시",
    "gwangju" to "광주광역시",
    "daejeon" to "대전광역시",
    "ulsan" to "울산광역시",
    "sejong" to "세종특별자치시",
    "gyeonggi" to "경기도",
    "gangwon" to "강원특별자치도",
    "chungcheongbuk" to "충청북도",
    "chungcheongnam" to "충청남도",
    "jeollabuk" to "전북특별자치도",
    "jeollanam" to "전라남도",
    "gyeongsangbuk" to "경상북도",
    "gyeongsangnam" to "경상남도",
    "jeju" to "제주특별자치도",
)

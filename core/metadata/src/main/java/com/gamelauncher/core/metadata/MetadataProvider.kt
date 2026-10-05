package com.gamelauncher.core.metadata

interface MetadataProvider {
    val id: String
    val displayName: String
    suspend fun search(query: MetadataQuery): List<MetadataCandidate>
    suspend fun fetch(reference: ProviderReference): MetadataResult?
}

data class MetadataQuery(
    val text: String? = null,
    val packageName: String? = null,
    val url: String? = null
)

data class ProviderReference(
    val providerId: String,
    val externalId: String
)

data class MetadataCandidate(
    val providerId: String,
    val externalId: String,
    val title: String? = null,
    val confidence: Float = 0f
)

data class MetadataResult(
    val providerId: String,
    val externalId: String,
    val title: String? = null,
    val description: String? = null,
    val developer: String? = null
)

package com.gamelauncher.core.database.repository

import com.gamelauncher.core.database.dao.TagDao
import com.gamelauncher.core.database.mapper.TagMapper
import com.gamelauncher.core.domain.model.Tag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface TagRepository {
    fun getAllTags(): Flow<List<Tag>>
    suspend fun getTagById(id: String): Tag?
    suspend fun insertTag(tag: Tag)
    suspend fun updateTag(tag: Tag)
    suspend fun deleteTag(tag: Tag)
}

class TagRepositoryImpl(
    private val tagDao: TagDao
) : TagRepository {
    override fun getAllTags(): Flow<List<Tag>> {
        return tagDao.getAllTags().map { entities -> entities.map(TagMapper::toDomain) }
    }

    override suspend fun getTagById(id: String): Tag? {
        return tagDao.getTagById(id)?.let(TagMapper::toDomain)
    }

    override suspend fun insertTag(tag: Tag) {
        tagDao.insertTag(TagMapper.toEntity(tag))
    }

    override suspend fun updateTag(tag: Tag) {
        tagDao.updateTag(TagMapper.toEntity(tag))
    }

    override suspend fun deleteTag(tag: Tag) {
        tagDao.deleteTag(TagMapper.toEntity(tag))
    }
}

package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StudioDao {

    @Query("SELECT * FROM saved_artworks ORDER BY lastModifiedMs DESC")
    fun observeAllArtworks(): Flow<List<SavedArtworkEntity>>

    @Query("SELECT * FROM saved_artworks WHERE templateId = :templateId LIMIT 1")
    suspend fun getArtworkById(templateId: String): SavedArtworkEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertArtwork(artwork: SavedArtworkEntity)

    @Query("DELETE FROM saved_artworks WHERE templateId = :templateId")
    suspend fun deleteArtwork(templateId: String)

    @Query("SELECT * FROM custom_swatches ORDER BY createdAtMs DESC")
    fun observeCustomSwatches(): Flow<List<CustomSwatchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomSwatch(swatch: CustomSwatchEntity)

    @Query("DELETE FROM custom_swatches WHERE id = :swatchId")
    suspend fun deleteCustomSwatch(swatchId: Int)
}

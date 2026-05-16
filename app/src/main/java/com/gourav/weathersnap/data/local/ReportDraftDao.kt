package com.gourav.weathersnap.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gourav.weathersnap.data.local.entity.ReportDraftEntity
import kotlinx.coroutines.flow.Flow

// this dao handles the temporary report data while the user is editing it
@Dao
interface ReportDraftDao {
    // we use a flow to get real time updates whenever the draft changes in the database
    @Query("SELECT * FROM report_drafts WHERE id = :id")
    fun observeDraft(id: Int = ReportDraftEntity.ACTIVE_DRAFT_ID): Flow<ReportDraftEntity?>

    // helper to get the current draft data once without a flow
    @Query("SELECT * FROM report_drafts WHERE id = :id")
    suspend fun getDraft(id: Int = ReportDraftEntity.ACTIVE_DRAFT_ID): ReportDraftEntity?

    // insert a new draft or replace the existing one if it has the same id
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDraft(draft: ReportDraftEntity)

    // specifically update only the notes field of our active draft
    @Query("UPDATE report_drafts SET notes = :notes, updatedAtMillis = :updatedAtMillis WHERE id = :id")
    suspend fun updateNotes(
        notes: String,
        updatedAtMillis: Long = System.currentTimeMillis(),
        id: Int = ReportDraftEntity.ACTIVE_DRAFT_ID,
    )

    // update the photo information when the user takes a new picture
    @Query(
        """
        UPDATE report_drafts
        SET originalImagePath = :originalImagePath,
            compressedImagePath = :compressedImagePath,
            originalSizeBytes = :originalSizeBytes,
            compressedSizeBytes = :compressedSizeBytes,
            updatedAtMillis = :updatedAtMillis
        WHERE id = :id
        """,
    )
    suspend fun updatePhoto(
        originalImagePath: String,
        compressedImagePath: String,
        originalSizeBytes: Long,
        compressedSizeBytes: Long,
        updatedAtMillis: Long = System.currentTimeMillis(),
        id: Int = ReportDraftEntity.ACTIVE_DRAFT_ID,
    )

    // clear the draft after it has been saved as a final report
    @Query("DELETE FROM report_drafts WHERE id = :id")
    suspend fun deleteDraft(id: Int = ReportDraftEntity.ACTIVE_DRAFT_ID)
}

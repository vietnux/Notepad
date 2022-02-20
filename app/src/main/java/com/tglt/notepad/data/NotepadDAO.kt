/* Copyright 2021 Braden Farmer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tglt.notepad.data

import androidx.room.*
import com.tglt.notepad.models.CrossRef
import com.tglt.notepad.models.Note
import com.tglt.notepad.models.NoteContents
import com.tglt.notepad.models.NoteMetadata
import kotlinx.coroutines.flow.Flow

@Dao
interface NotepadDAO {

    // Create or Update

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNoteContents(NoteContents: NoteContents): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNoteMetadata(NoteMetadata: NoteMetadata): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRef(crossRef: CrossRef): Long

    // Read

    @Query("SELECT * FROM NoteMetadata ORDER BY title")
    fun getNoteMetadataSortedByTitle(): Flow<List<NoteMetadata>>

    @Query("SELECT * FROM NoteMetadata ORDER BY date")
    fun getNoteMetadataSortedByDate(): Flow<List<NoteMetadata>>

    @Query("SELECT * FROM CrossRef WHERE metadataId = :id")
    suspend fun getCrossRef(id: Long): CrossRef?

    @Transaction
    @Query("SELECT * FROM NoteMetadata WHERE metadataId = :id")
    suspend fun getNote(id: Long): Note

    // Delete

    @Query("DELETE FROM NoteContents WHERE contentsId = :id")
    suspend fun deleteNoteContents(id: Long)

    @Query("DELETE FROM NoteMetadata WHERE metadataId = :id")
    suspend fun deleteNoteMetadata(id: Long)

    @Query("DELETE FROM CrossRef WHERE metadataId = :id")
    suspend fun deleteCrossRef(id: Long)
}

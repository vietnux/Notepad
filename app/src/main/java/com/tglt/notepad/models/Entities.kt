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

package com.tglt.notepad.models

import androidx.room.*
import java.util.*

@Entity
data class NoteContents(
    @PrimaryKey(autoGenerate = true)
    val contentsId: Long = 0,
    val text: String = "",
    val isDraft: Boolean = false
)

@Entity
data class NoteMetadata(
    @PrimaryKey(autoGenerate = true)
    val metadataId: Long = 0,
    val title: String = "",
    val date: Date = Date()
)

@Entity(
    primaryKeys = ["metadataId", "contentsId"],
    indices = [Index("contentsId")]
)
data class CrossRef(
    val metadataId: Long = 0,
    val contentsId: Long = 0
)

data class Note(
    @Embedded val metadata: NoteMetadata = NoteMetadata(),
    @Relation(
        parentColumn = "metadataId",
        entityColumn = "contentsId",
        associateBy = Junction(CrossRef::class)
    )
    val contents: NoteContents = NoteContents()
)
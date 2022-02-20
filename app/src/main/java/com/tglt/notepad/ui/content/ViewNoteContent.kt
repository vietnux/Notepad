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

package com.tglt.notepad.ui.content

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tglt.notepad.android.NotepadViewModel
import com.tglt.notepad.models.Note
import com.tglt.notepad.ui.previews.ViewNotePreview
import kotlinx.coroutines.launch

@Composable
fun viewState(
    id: Long,
    vm: NotepadViewModel?
) = produceState(Note()) {
    launch {
        vm?.getNote(id)?.let { value = it }
    }
}

@Composable
fun ViewNoteContent(note: Note) {
    Box(
        modifier = Modifier.verticalScroll(
            state = ScrollState(initial = 0)
        )
    ) {
        SelectionContainer {
            BasicText(
                text = note.contents.text,
                style = TextStyle(
                    fontSize = 16.sp
                ),
                modifier = Modifier
                    .padding(
                        horizontal = 16.dp,
                        vertical = 12.dp
                    )
                    .fillMaxWidth()
                    .fillMaxHeight()
            )
        }
    }
}

@Preview
@Composable
fun ViewNoteContentPreview() = ViewNotePreview()
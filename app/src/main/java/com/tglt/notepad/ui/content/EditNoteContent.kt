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

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tglt.notepad.R
import com.tglt.notepad.android.NotepadViewModel
import com.tglt.notepad.models.Note
import com.tglt.notepad.models.NoteMetadata
import com.tglt.notepad.ui.previews.EditNotePreview
import com.tglt.notepad.utils.UnitDisposableEffect
import kotlinx.coroutines.launch

@Composable
fun editState(
    id: Long?,
    vm: NotepadViewModel?
) = produceState(
    Note(
        metadata = NoteMetadata(
            title = stringResource(id = R.string.action_new)
        )
    )
) {
    id?.let {
        launch {
            vm?.getNote(it)?.let { value = it }
        }
    }
}

@Composable
fun textState(
    text: String
) = remember {
    mutableStateOf(TextFieldValue())
}.apply {
    value = TextFieldValue(
        text = text,
        selection = TextRange(text.length)
    )
}

@Composable
fun EditNoteContent(
    textState: MutableState<TextFieldValue>?
) {
    val focusRequester = remember { FocusRequester() }
    BasicTextField(
        value = textState?.value ?: TextFieldValue(),
        onValueChange = { textState?.value = it },
        textStyle = TextStyle(
            fontSize = 16.sp
        ),
        modifier = Modifier
            .padding(
                horizontal = 16.dp,
                vertical = 12.dp
            )
            .fillMaxWidth()
            .fillMaxHeight()
            .focusRequester(focusRequester)
    )

    if(textState?.value?.text.isNullOrEmpty()) {
        BasicText(
            text = stringResource(id = R.string.edit_text),
            style = TextStyle(
                fontSize = 16.sp,
                color = Color.LightGray
            ),
            modifier = Modifier
                .padding(
                    horizontal = 16.dp,
                    vertical = 12.dp
                )
        )
    }

    UnitDisposableEffect {
        focusRequester.requestFocus()
    }
}

@Preview
@Composable
fun EditNoteContentPreview() = EditNotePreview()
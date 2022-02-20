/* Copyright 2014 Braden Farmer
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

package com.tglt.notepad.old.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.tglt.notepad.old.activity.MainActivity;
import com.tglt.notepad.R;
import com.tglt.notepad.old.managers.ThemeManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class NoteEditFragment extends Fragment {

    private EditText noteContents;
    String filename = String.valueOf(System.currentTimeMillis());
    String contentsOnLoad = "";
    int length = 0;
    long draftName;
    boolean isSavedNote = false;
    String contents;
    boolean directEdit = false;

    // Receiver used to close fragment when a note is deleted
    public class DeleteNotesReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String[] filesToDelete = intent.getStringArrayExtra("files");

            for(Object file : filesToDelete) {
                if(filename.equals(file)) {
                    // Hide soft keyboard
                    EditText editText = getActivity().findViewById(R.id.editText1);
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

                    // Add NoteListFragment or WelcomeFragment
                    Fragment fragment;
                    if(getActivity().findViewById(R.id.layoutMain).getTag().equals("main-layout-normal"))
                        fragment = new NoteListFragment();
                    else
                        fragment = new WelcomeFragment();

                    getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.noteViewEdit, fragment, "NoteListFragment")
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit();
                }
            }
        }
    }

    IntentFilter filter = new IntentFilter("com.tglt.notepad.old.DELETE_NOTES");
    DeleteNotesReceiver receiver = new DeleteNotesReceiver();

    /* The activity that creates an instance of this fragment must
     * implement this interface in order to receive event call backs. */
    public interface Listener {
        void showBackButtonDialog(String filename);
        void showDeleteDialog();
        void showSaveButtonDialog();
        boolean isShareIntent();
        String loadNote(String filename) throws IOException;
        String loadNoteTitle(String filename) throws IOException;
        void exportNote(String filename);
        void printNote(String contentToPrint);
    }

    // Use this instance of the interface to deliver action events
    Listener listener;

    // Override the Fragment.onAttach() method to instantiate the Listener
    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the Listener so we can send events to the host
            listener = (Listener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                                         + " must implement Listener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_note_edit, container, false);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set values
        setRetainInstance(true);
        setHasOptionsMenu(true);

        // Show the Up button in the action bar.
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Animate elevation change
        if(getActivity() instanceof MainActivity
                && getActivity().findViewById(R.id.layoutMain).getTag().equals("main-layout-large")
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            LinearLayout noteViewEdit = getActivity().findViewById(R.id.noteViewEdit);
            LinearLayout noteList = getActivity().findViewById(R.id.noteList);

            noteList.animate().z(0f);
            if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                noteViewEdit.animate().z(getResources().getDimensionPixelSize(R.dimen.note_view_edit_elevation_land));
            else
                noteViewEdit.animate().z(getResources().getDimensionPixelSize(R.dimen.note_view_edit_elevation));
        }

        // Set up content view
        noteContents = getActivity().findViewById(R.id.editText1);

        // Apply theme
        ThemeManager.applyTextSettings(getActivity(), noteContents);
        
        SharedPreferences pref = getActivity().getSharedPreferences(getActivity().getPackageName() + "_preferences", Context.MODE_PRIVATE);

        // Get filename
        try {
            if(!getArguments().getString("filename").equals("new")) {
                filename = getArguments().getString("filename");
                if(!filename.equals("draft"))
                    isSavedNote = true;
            }
        } catch (NullPointerException e) {
            filename = "new";
        }

        // Load note from existing file
        if(isSavedNote) {
            try {
                contentsOnLoad = listener.loadNote(filename);
            } catch (IOException e) {
                showToast(R.string.error_loading_note);
                finish(null);
            }

            // Set TextView contents
            length = contentsOnLoad.length();
            noteContents.setText(contentsOnLoad);

            if(!pref.getBoolean("direct_edit", false))
                noteContents.setSelection(length, length);
        } else if(filename.equals("draft")) {
            SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
            String draftContents = sharedPref.getString("draft-contents", null);
            length = draftContents.length();
            noteContents.setText(draftContents);

            if(!pref.getBoolean("direct_edit", false))
                noteContents.setSelection(length, length);
        }

        // Show soft keyboard
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(noteContents, InputMethodManager.SHOW_IMPLICIT);

    }

    @Override
    public void onPause() {
        super.onPause();

        // Disable saving drafts if user launched Notepad through a share intent
        if(!listener.isShareIntent() && !isRemoving()) {
            // Set current note contents to a String
            noteContents = getActivity().findViewById(R.id.editText1);
            contents = noteContents.getText().toString();

            if(!contents.equals("")) {
                SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);

                // Save filename to draft-name preference
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putLong("draft-name", Long.parseLong(filename));
                editor.putBoolean("is-saved-note", isSavedNote);

                // Write draft to SharedPreferences
                editor.putString("draft-contents", contents);
                editor.apply();

                // Show toast notification
                showToast(R.string.draft_saved);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        // Disable restoring drafts if user launched Notepad through a share intent
        if(!listener.isShareIntent()) {
            if(filename.equals("draft")) {

                // Restore draft preferences
                draftName = sharedPref.getLong("draft-name", 0);
                isSavedNote = sharedPref.getBoolean("is-saved-note", false);

                // Restore filename of draft
                filename = Long.toString(draftName);

                // Reload old file into memory, so that correct contentsOnLoad is set
                if(isSavedNote) {
                    try {
                        contentsOnLoad = listener.loadNote(filename);
                    } catch (IOException e) {
                        showToast(R.string.error_loading_note);
                        finish(null);
                    }
                } else
                    contentsOnLoad = "";

                // Notify the user that a draft has been restored
                showToast(R.string.draft_restored);
            }

            // Clear draft preferences
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.remove("draft-name");
            editor.remove("is-saved-note");
            editor.remove("draft-contents");
            editor.apply();
        }

        // Change window title
        String title;

        if(isSavedNote)
            try {
                title = listener.loadNoteTitle(filename);
            } catch (IOException e) {
                title = getResources().getString(R.string.edit_note);
            }
        else
            title = getResources().getString(R.string.action_new);

        getActivity().setTitle(title);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Bitmap bitmap = ((BitmapDrawable) ContextCompat.getDrawable(getActivity(), R.drawable.ic_recents_logo)).getBitmap();

            ActivityManager.TaskDescription taskDescription = new ActivityManager.TaskDescription(title, bitmap, ContextCompat.getColor(getActivity(), R.color.primary));
            getActivity().setTaskDescription(taskDescription);
        }

        SharedPreferences pref = getActivity().getSharedPreferences(getActivity().getPackageName() + "_preferences", Context.MODE_PRIVATE);
        directEdit = pref.getBoolean("direct_edit", false);
    }

    // Register and unregister DeleteNotesReceiver
    @Override
    public void onStart() {
        super.onStart();

        if(!listener.isShareIntent())
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, filter);
    }

    @Override
    public void onStop() {
        super.onStop();

        if(!listener.isShareIntent())
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.note_edit, menu);

        if(listener.isShareIntent() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            menu.removeItem(R.id.action_export);
            menu.removeItem(R.id.action_print);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Hide soft keyboard
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getActivity().findViewById(R.id.editText1).getWindowToken(), 0);

        switch (item.getItemId()) {
            case android.R.id.home:
                // Override default Android "up" behavior to instead mimic the back button
                getActivity().onBackPressed();
                return true;

                // Save button
            case R.id.action_save:
                // Get current note contents from EditText
                noteContents = getActivity().findViewById(R.id.editText1);
                contents = noteContents.getText().toString();

                // If EditText is empty, show toast informing user to enter some text
                if(contents.equals(""))
                    showToast(R.string.empty_note);
                else if(directEdit)
                    getActivity().onBackPressed();
                else {
                    // If no changes were made since last save, switch back to NoteViewFragment
                    if(contentsOnLoad.equals(noteContents.getText().toString())) {
                        Bundle bundle = new Bundle();
                        bundle.putString("filename", filename);

                        Fragment fragment = new NoteViewFragment();
                        fragment.setArguments(bundle);

                        getFragmentManager()
                                .beginTransaction()
                                .replace(R.id.noteViewEdit, fragment, "NoteViewFragment")
                                .commit();
                    } else {
                        SharedPreferences pref = getActivity().getSharedPreferences(getActivity().getPackageName() + "_preferences", Context.MODE_PRIVATE);
                        if(pref.getBoolean("show_dialogs", true)) {
                            // Show save button dialog
                            listener.showSaveButtonDialog();
                        } else {
                            try {
                                Intent intent = new Intent();
                                intent.putExtra(Intent.EXTRA_TEXT, noteContents.getText().toString());
                                this.getActivity().setResult(Activity.RESULT_OK, intent);
                                saveNote();

                                if(listener.isShareIntent())
                                    getActivity().finish();
                                else {
                                    Bundle bundle = new Bundle();
                                    bundle.putString("filename", filename);

                                    Fragment fragment = new NoteViewFragment();
                                    fragment.setArguments(bundle);

                                    getFragmentManager()
                                            .beginTransaction()
                                            .replace(R.id.noteViewEdit, fragment, "NoteViewFragment")
                                            .commit();
                                }
                            } catch (IOException e) {
                                // Show error message as toast if file fails to save
                                showToast(R.string.failed_to_save);
                            }
                        }
                    }
                }
                return true;

                // Delete button
            case R.id.action_delete:
                listener.showDeleteDialog();
                return true;

                // Share menu item
            case R.id.action_share:
                // Set current note contents to a String
                contents = noteContents.getText().toString();

                // If EditText is empty, show toast informing user to enter some text
                if(contents.equals(""))
                    showToast(R.string.empty_note);
                else {
                    // Send a share intent
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_TEXT, contents);
                    intent.setType("text/plain");

                    // Verify that the intent will resolve to an activity, and send
                    if(intent.resolveActivity(getActivity().getPackageManager()) != null)
                        startActivity(Intent.createChooser(intent, getResources().getText(R.string.send_to)));
                }

                return true;

            // Export menu item
            case R.id.action_export:
                // Set current note contents to a String
                contents = noteContents.getText().toString();

                // If EditText is empty, show toast informing user to enter some text
                if(contents.equals(""))
                    showToast(R.string.empty_note);
                else {
                    String currentFilename = filename;
                    filename = "exported_note";

                    try {
                        saveNote();
                    } catch (IOException e) { /* Gracefully fail */ }

                    filename = currentFilename;

                    listener.exportNote("exported_note");
                }

                return true;

            // Print menu item
            case R.id.action_print:
                // Set current note contents to a String
                contents = noteContents.getText().toString();

                // If EditText is empty, show toast informing user to enter some text
                if(contents.equals(""))
                    showToast(R.string.empty_note);
                else
                    listener.printNote(contents);

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteNote(String filename) {
        // Build the pathname to delete file, then perform delete operation
        File fileToDelete = new File(getActivity().getFilesDir() + File.separator + filename);
        fileToDelete.delete();
    }

    // Saves notes to /data/data/com.tglt.notepad.old/files
    private void saveNote() throws IOException {
        // Set current note contents to a String
        noteContents = getActivity().findViewById(R.id.editText1);
        contents = noteContents.getText().toString();

        // Write the String to a new file with filename of current milliseconds of Unix time
        if(contents.equals("") && filename.equals("draft"))
            finish(null);
        else {

            // Set a new filename if this is not a draft
            String newFilename;
            if(filename.equals("draft") || filename.equals("exported_note"))
                newFilename = filename;
            else
                newFilename = String.valueOf(System.currentTimeMillis());

            // Write note to disk
            FileOutputStream output = getActivity().openFileOutput(newFilename, Context.MODE_PRIVATE);
            output.write(contents.getBytes());
            output.close();

            // Delete old file
            if(!(filename.equals("draft") || filename.equals("exported_note")))
                deleteNote(filename);

            // Show toast notification
            if(filename.equals("draft"))
                showToast(R.string.draft_saved);
            else if(!filename.equals("exported_note"))
                showToast(R.string.note_saved);

            // Old file is no more
            if(!(filename.equals("draft") || filename.equals("exported_note"))) {
                filename = newFilename;
                contentsOnLoad = contents;
                length = contentsOnLoad.length();
            }

            // Send broadcast to MainActivity to refresh list of notes
            Intent listNotesIntent = new Intent();
            listNotesIntent.setAction("com.tglt.notepad.old.LIST_NOTES");
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(listNotesIntent);
        }
    }

    // Method used to generate toast notifications
    private void showToast(int message) {
        Toast toast = Toast.makeText(getActivity(), getResources().getString(message), Toast.LENGTH_SHORT);
        toast.show();
    }

    public void onBackDialogNegativeClick(String filename) {
        // User touched the dialog's negative button
        showToast(R.string.changes_discarded);
        finish(filename);
    }

    public void onBackDialogPositiveClick(String filename) {
        // User touched the dialog's positive button
        try {
            saveNote();
            finish(filename);
        } catch (IOException e) {
            // Show error message as toast if file fails to save
            showToast(R.string.failed_to_save);
        }
    }

    public void onDeleteDialogPositiveClick() {
        // User touched the dialog's positive button
        deleteNote(filename);
        showToast(R.string.note_deleted);

        if(getActivity().getComponentName().getClassName().equals("com.tglt.notepad.old.activity.MainActivity")
                && getActivity().findViewById(R.id.layoutMain).getTag().equals("main-layout-large")) {
            // Send broadcast to NoteListFragment to refresh list of notes
            Intent listNotesIntent = new Intent();
            listNotesIntent.setAction("com.tglt.notepad.old.LIST_NOTES");
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(listNotesIntent);
        }

        finish(null);
    }

    public void onSaveDialogNegativeClick() {
        // User touched the dialog's negative button
        if(isSavedNote) {
            showToast(R.string.changes_discarded);

            Bundle bundle = new Bundle();
            bundle.putString("filename", filename);

            Fragment fragment = new NoteViewFragment();
            fragment.setArguments(bundle);

            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.noteViewEdit, fragment, "NoteViewFragment")
                    .commit();
        } else {
            showToast(R.string.changes_discarded);
            finish(null);
        }
    }

    public void onSaveDialogPositiveClick() {
        // User touched the dialog's positive button
        try {
            saveNote();

            if(listener.isShareIntent())
                finish(null);
            else {
                Bundle bundle = new Bundle();
                bundle.putString("filename", filename);

                Fragment fragment = new NoteViewFragment();
                fragment.setArguments(bundle);

                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.noteViewEdit, fragment, "NoteViewFragment")
                        .commit();
            }
        } catch (IOException e) {
            // Show error message as toast if file fails to save
            showToast(R.string.failed_to_save);
        }
    }

    public void onBackPressed(String filename) {
        // Pop back stack if no changes were made since last save
        if(contentsOnLoad.equals(noteContents.getText().toString())) {
            finish(filename);
        } else {
            // Finish if EditText is empty
            if(noteContents.getText().toString().isEmpty())
                finish(filename);
            else {
                SharedPreferences pref = getActivity().getSharedPreferences(getActivity().getPackageName() + "_preferences", Context.MODE_PRIVATE);
                if(pref.getBoolean("show_dialogs", true)) {
                    // Show back button dialog
                    listener.showBackButtonDialog(filename);
                } else {
                    try {
                        saveNote();
                        finish(filename);
                    } catch (IOException e) {
                        // Show error message as toast if file fails to save
                        showToast(R.string.failed_to_save);
                    }
                }
            }
        }
    }

    public void dispatchKeyShortcutEvent(int keyCode) {
        switch(keyCode) {

                // CTRL+S: Save
            case KeyEvent.KEYCODE_S:
                // Set current note contents to a String
                contents = noteContents.getText().toString();

                // If EditText is empty, show toast informing user to enter some text
                if(contents.equals(""))
                    showToast(R.string.empty_note);
                else {
                    try {
                        // Keyboard shortcut just saves the note; no dialog shown
                        saveNote();
                        isSavedNote = true;

                        // Change window title
                        String title;
                        try {
                            title = listener.loadNoteTitle(filename);
                        } catch (IOException e) {
                            title = getResources().getString(R.string.edit_note);
                        }

                        getActivity().setTitle(title);

                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            Bitmap bitmap = ((BitmapDrawable) ContextCompat.getDrawable(getActivity(), R.drawable.ic_recents_logo)).getBitmap();

                            ActivityManager.TaskDescription taskDescription = new ActivityManager.TaskDescription(title, bitmap, ContextCompat.getColor(getActivity(), R.color.primary));
                            getActivity().setTaskDescription(taskDescription);
                        }
                    } catch (IOException e) {
                        // Show error message as toast if file fails to save
                        showToast(R.string.failed_to_save);
                    }
                }
                break;

                // CTRL+D: Delete
            case KeyEvent.KEYCODE_D:
                listener.showDeleteDialog();
                break;

                // CTRL+H: Share
            case KeyEvent.KEYCODE_H:
                // Set current note contents to a String
                contents = noteContents.getText().toString();

                // If EditText is empty, show toast informing user to enter some text
                if(contents.equals(""))
                    showToast(R.string.empty_note);
                else {
                    // Send a share intent
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_TEXT, contents);
                    shareIntent.setType("text/plain");

                    // Verify that the intent will resolve to an activity, and send
                    if(shareIntent.resolveActivity(getActivity().getPackageManager()) != null)
                        startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.send_to)));
                }
                break;
        }
    }

    private void finish(String filename) {
        if(listener.isShareIntent())
            getActivity().finish();
        else if(filename == null) {
            // Add NoteListFragment or WelcomeFragment
            Fragment fragment;
            if(getActivity().findViewById(R.id.layoutMain).getTag().equals("main-layout-normal"))
                fragment = new NoteListFragment();
            else
                fragment = new WelcomeFragment();

            getFragmentManager()
                .beginTransaction()
                .replace(R.id.noteViewEdit, fragment, "NoteListFragment")
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
        } else {
            Bundle bundle = new Bundle();
            bundle.putString("filename", filename);

            Fragment fragment;
            String tag;

            if(directEdit) {
                fragment = new NoteEditFragment();
                tag = "NoteEditFragment";
            } else {
                fragment = new NoteViewFragment();
                tag = "NoteViewFragment";
            }

            fragment.setArguments(bundle);

            // Add NoteViewFragment or NoteEditFragment
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.noteViewEdit, fragment, tag)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                    .commit();
        }
    }

    public void switchNotes(String filename) {
        // Hide soft keyboard
        EditText editText = getActivity().findViewById(R.id.editText1);
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

        // Act as if the back button was pressed
        onBackPressed(filename);
    }

    public String getFilename() {
        return filename;
    }
}

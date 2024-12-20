package com.night.notes;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("ConstantConditions")
public class NotesActivity extends AppCompatActivity {

    private RecyclerView gvItems;
    private MaterialToolbar toolbar;
    private final ArrayList<TodoItem> tasksArr = new ArrayList<>();
    private final ArrayList<TodoItem> currentNotes = new ArrayList<>();
    public static ArrayList<MaterialCardView> cardArr = new ArrayList<>();
    private FirebaseFirestore db;
    private CustomGridAdapter gridAdapter;
    private final int ADD_OR_DISCARD = 1;
    private final int EDIT_OR_DISCARD = 2;
    public static View.OnClickListener noteOnClickListener;
    public static View.OnLongClickListener noteOnLongClickListener;
    private ActionMode mActionMode, currMode;
    private SearchView searchView;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        int NO_OF_COLUMNS = 2;

        ViewStub stubGrid = findViewById(R.id.stub_grid);
        stubGrid.inflate();
        noteOnClickListener = v -> {
            if(mActionMode == null) {
                Intent intent = new Intent(NotesActivity.this, AddActivity.class);
                String title = ((TextView) v.findViewById(R.id.item_title)).getText().toString();
                String description = ((TextView) v.findViewById(R.id.item_description)).getText().toString();
                String docid = ((TextView) v.findViewById(R.id.item_docid)).getText().toString();
                AppCompatImageView pin = (AppCompatImageView) v.findViewById(R.id.item_pin);
                intent.putExtra("title", title);
                intent.putExtra("description", description);
                intent.putExtra("docid", docid);
                intent.putExtra("isPinned", (boolean) pin.getTag());
                startActivityForResult(intent, EDIT_OR_DISCARD);
            }
            else {
                MaterialCardView cv = (MaterialCardView) v;
                cv.setChecked(!cv.isChecked());
                updateSelectedNotesCount();
                currMode.invalidate();
            }
        };
        noteOnLongClickListener = v -> {
            MaterialCardView cv = (MaterialCardView) v;
            cv.setChecked(!cv.isChecked());
            if(mActionMode == null) {
                mActionMode = toolbar.startActionMode(mActionModeCallback);
            }
            return true;
        };

        gvItems = findViewById(R.id.items_gridview);
        db = FirebaseFirestore.getInstance();
        CollectionReference cRef = db.collection(FirebaseAuth.getInstance().getUid());
        gvItems.setLayoutManager(new GridLayoutManager(NotesActivity.this, NO_OF_COLUMNS));
        gvItems.addItemDecoration(new SpaceItemDecoration(48, 24));
        gridAdapter = new CustomGridAdapter(tasksArr);

        cRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                return;
            }
            if (value != null && !value.isEmpty()) {
                ArrayList<DocumentSnapshot> todoList = (ArrayList<DocumentSnapshot>) value.getDocuments();
                cardArr.clear();
                tasksArr.clear();
                for (DocumentSnapshot d : todoList) {
                    TodoItem todoItem = new TodoItem(d.getString("title"), d.getString("description"), d.getId(), d.getBoolean("isPinned"));
                    tasksArr.add(todoItem);
                }
                int index = 0;
                for (int i = 0; i < tasksArr.size(); i++) {
                    TodoItem t = tasksArr.get(i);
                    if (!t.isPinned()) continue;
                    tasksArr.remove(t);
                    tasksArr.add(index, t);
                }
                if (tasksArr != null && tasksArr.size() > 0) {
                    currentNotes.clear();
                    currentNotes.addAll(tasksArr);
                    if (toolbar == null || !toolbar.hasExpandedActionView()) {
                        gvItems.setAdapter(gridAdapter);
                        return;
                    }
                    tasksArr.clear();
                    for (TodoItem t : currentNotes) {
                        String searchTerm = searchView.getQuery().toString().toLowerCase().trim();
                        if (t.getTitle().toLowerCase().contains(searchTerm)
                                || t.getDescription().toLowerCase().contains(searchTerm)) {
                            tasksArr.add(t);
                        }
                    }
                    gvItems.setAdapter(gridAdapter);
                }
            }
        });

        toolbar = findViewById(R.id.top_toolbar);
        setSupportActionBar(toolbar);

        ExtendedFloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> startActivityForResult(new Intent(NotesActivity.this, AddActivity.class), ADD_OR_DISCARD));

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == 3 && resultCode == RESULT_OK) {
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private final ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.contextual_app_bar, menu);
            currMode = mode;
            updateSelectedNotesCount();
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            boolean hasUnpinnedNote = false;
            int count = 0;
            for(MaterialCardView cv : cardArr) {
                if(cv.isChecked()) count++;
                cv.setLongClickable(false);
                AppCompatImageView pin = cv.findViewById(R.id.item_pin);
                if(cv.isChecked() && !(boolean) pin.getTag()) hasUnpinnedNote = true;
            }
            MenuItem item;
            item = menu.findItem(R.id.menu_pin);
            if(count == 0) {
                item.setVisible(false);
                return true;
            }
            item.setVisible(true);
            if(hasUnpinnedNote) {
                item.setIcon(R.drawable.ic_baseline_push_pin_24);
                item.setContentDescription("pin");
            }
            else {
                item.setIcon(R.drawable.ic_unpin);
                item.setContentDescription("unpin");
            }
            return true;
        }

        @SuppressLint("NonConstantResourceId")
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

            switch (item.getItemId()) {
                case R.id.menu_pin:
                    pinOrUnpinSelectedNotes(item);
                    mode.finish();
                    return true;
                case R.id.menu_delete:
                    deleteSelectedNotes();
                    return true;
                case R.id.menu_select_all:
                    for(MaterialCardView cv : cardArr)
                        cv.setChecked(true);
                    int n = cardArr.size();
                    currMode.setTitle(n + "/" + n + " selected");
                    return true;
                case R.id.menu_unselect_all:
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            currMode = null;
            for(MaterialCardView cv : cardArr) {
                if(cv.isChecked()) {
                    cv.setChecked(false);
                }
                cv.setLongClickable(true);
            }
        }
    };

    private void updateSelectedNotesCount() {
        int count = 0;
        for(MaterialCardView cv : cardArr) {
            if(cv.isChecked()) count++;
        }
        currMode.setTitle(count + "/" + cardArr.size() + " selected");
    }

    private void deleteSelectedNotes() {
        new MaterialAlertDialogBuilder(NotesActivity.this)
                .setTitle("Delete")
                .setMessage("Are you sure you want to delete?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    int count = 0;
                    for(MaterialCardView cv : cardArr) {
                        if(!cv.isChecked()) continue;
                        count++;
                        MaterialTextView docidView = cv.findViewById(R.id.item_docid);
                        String docid = docidView.getText().toString();
                        db.collection(FirebaseAuth.getInstance().getUid()).document(docid).delete();
                    }
                    Toast.makeText(NotesActivity.this, count + " notes deleted", Toast.LENGTH_SHORT).show();
                    currMode.finish();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                })
                .show();
    }

    private void pinOrUnpinSelectedNotes(MenuItem item) {
        if(item.getContentDescription().toString().equals("pin")) {
            for(MaterialCardView cv : cardArr) {
                if(!cv.isChecked()) continue;
                MaterialTextView docidView = cv.findViewById(R.id.item_docid);
                String docid = docidView.getText().toString();
                Map<String, Object> data  = new HashMap<>();
                data.put("isPinned", true);
                db.collection(FirebaseAuth.getInstance().getUid()).document(docid).update(data);
            }
            Toast.makeText(getApplicationContext(), "Pinned", Toast.LENGTH_SHORT).show();
        }
        else if(item.getContentDescription().toString().equals("unpin")) {
            for(MaterialCardView cv : cardArr) {
                if(!cv.isChecked()) continue;
                MaterialTextView docidView = cv.findViewById(R.id.item_docid);
                String docid = docidView.getText().toString();
                Map<String, Object> data = new HashMap<>();
                data.put("isPinned", false);
                db.collection(FirebaseAuth.getInstance().getUid()).document(docid).update(data);
            }
            Toast.makeText(getApplicationContext(), "Unpinned", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_app_bar, menu);

        MenuItem.OnActionExpandListener onActionExpandListener = new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                tasksArr.clear();
                tasksArr.addAll(currentNotes);
                gvItems.setAdapter(gridAdapter);
                return true;
            }
        };
        menu.findItem(R.id.search).setOnActionExpandListener(onActionExpandListener);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setIconifiedByDefault(false);
        searchView.setFocusable(true);
        searchView.setIconified(false);
        searchView.requestFocusFromTouch();
        searchView.setQueryHint("Search");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String searchPattern = newText.toLowerCase().trim();
                tasksArr.clear();
                for(TodoItem t : currentNotes) {
                    if(t.getTitle().toLowerCase().contains(searchPattern) || t.getDescription().toLowerCase().contains(searchPattern)) {
                        tasksArr.add(t);
                    }
                }
                gvItems.setAdapter(gridAdapter);
                return false;
            }
        });
        return super.onPrepareOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                startActivityForResult(new Intent(NotesActivity.this, SettingsActivity.class), 3);
                return true;
            case R.id.search:
                item.expandActionView();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
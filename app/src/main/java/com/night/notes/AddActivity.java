package com.night.notes;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("ConstantConditions")
public class AddActivity extends AppCompatActivity {
    private MaterialToolbar toolbar;
    private TextInputEditText inputTitle, inputDescription;
    private FirebaseFirestore db;
    private String docid;
    private Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        toolbar = findViewById(R.id.add_top_toolbar);
        toolbar.setTitle(null);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        bundle = getIntent().getExtras();
        db = FirebaseFirestore.getInstance();
        inputTitle = findViewById(R.id.input_title);
        inputDescription = findViewById(R.id.input_description);
        FloatingActionButton deleteFab = null;
        if(bundle != null) {
            docid = bundle.getString("docid");
            inputTitle.setText(bundle.getString("title"));
            inputDescription.setText(bundle.getString("description"));
            deleteFab = findViewById(R.id.delete_fab);
            deleteFab.setVisibility(View.VISIBLE);
        }
        inputTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                MenuItem save = toolbar.getMenu().findItem(R.id.add_menu_save);
                save.setVisible(count != 0 || !inputDescription.getText().toString().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        inputDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                MenuItem save = toolbar.getMenu().findItem(R.id.add_menu_save);
                save.setVisible(count != 0 || !inputTitle.getText().toString().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        if(deleteFab == null) return;
        deleteFab.setOnClickListener(view -> new MaterialAlertDialogBuilder(AddActivity.this)
                .setTitle("Delete")
                .setMessage("Are you sure you want to delete this note?")
                .setPositiveButton("Delete", (dialogInterface, i) -> {
                    db.collection(FirebaseAuth.getInstance().getUid()).document(docid).delete();
                    Toast.makeText(getApplicationContext(), "Deleted", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .setNegativeButton("Cancel", (dialogInterface, i) -> {

                })
                .show());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_add_app_bar, menu);

        MenuItem pin = menu.findItem(R.id.add_menu_pin);
        if(bundle != null) {
            if(bundle.getBoolean("isPinned")) {
                pin.setIcon(R.drawable.ic_unpin);
                pin.setTitle("Unpin");
                pin.setContentDescription("true");
            }
            else {
                pin.setIcon(R.drawable.ic_baseline_push_pin_24);
                pin.setTitle("Pin");
                pin.setContentDescription("false");
            }
        }
        else {
            pin.setIcon(R.drawable.ic_baseline_push_pin_24);
            pin.setContentDescription("false");
        }
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.add_menu_pin:
                if(item.getContentDescription().toString().equals("true")) {
                    item.setIcon(R.drawable.ic_baseline_push_pin_24);
                    item.setContentDescription("false");
                }
                else {
                    item.setIcon(R.drawable.ic_unpin);
                    item.setContentDescription("true");
                }
                toolbar.getMenu().findItem(R.id.add_menu_save).setVisible(true);
                return true;
            case R.id.add_menu_save:
                String title = inputTitle.getText().toString();
                String description = inputDescription.getText().toString();
                boolean isPinned = toolbar.getMenu().findItem(R.id.add_menu_pin).getContentDescription().toString().equals("true");
                Map<String, Object> data = new HashMap<>();
                data.put("title", title);
                data.put("description", description);
                data.put("isPinned", isPinned);
                if(bundle != null) {
                    db.collection(FirebaseAuth.getInstance().getUid()).document(docid).update(data);
                }
                else {
                    db.collection(FirebaseAuth.getInstance().getUid()).add(data);
                }
                setResult(RESULT_OK);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
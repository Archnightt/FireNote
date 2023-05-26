package com.night.notes;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

public class CustomGridAdapter extends RecyclerView.Adapter<CustomGridAdapter.ViewHolder> {
    private final List<TodoItem> itemList;

    public CustomGridAdapter(List<TodoItem> itemList) {
        this.itemList = itemList;
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item, null));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TodoItem todoItem = itemList.get(position);

        holder.title.setText(todoItem.getTitle());
        holder.description.setText(todoItem.getDescription());
        holder.docid.setText(todoItem.getDocid());
        if(todoItem.isPinned())
            holder.pinned.setImageResource(R.drawable.ic_baseline_push_pin_24);
        else
            holder.pinned.setImageDrawable(null);
        holder.pinned.setTag(todoItem.isPinned());
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        MaterialCardView root;
        MaterialTextView title, description, docid;
        AppCompatImageView pinned;

        public ViewHolder(View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.item_title);
            description = itemView.findViewById(R.id.item_description);
            docid = itemView.findViewById(R.id.item_docid);
            pinned = itemView.findViewById(R.id.item_pin);
            root = itemView.findViewById(R.id.item_card_view);
            NotesActivity.cardArr.add(root);
            root.setOnClickListener(NotesActivity.noteOnClickListener);
            root.setOnLongClickListener(NotesActivity.noteOnLongClickListener);
        }
    }
}

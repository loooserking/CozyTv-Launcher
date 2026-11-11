package com.stvlauncher;

import android.content.ComponentName;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

final class AppTileAdapter extends RecyclerView.Adapter<AppTileAdapter.TileHolder> {

    interface ItemAction {
        void run(AppItem item);
    }

    static final class AppItem {
        final String packageName;
        final String label;
        final ComponentName component;
        final Drawable icon;

        AppItem(String packageName, String label, ComponentName component, Drawable icon) {
            this.packageName = packageName;
            this.label = label;
            this.component = component;
            this.icon = icon;
        }
    }

    private final LayoutInflater inflater;
    private final ItemAction onClick;
    private final ItemAction onLongClick;
    private final ItemAction onFocus;
    private final Runnable onUp;
    private final List<AppItem> items = new ArrayList<>();

    AppTileAdapter(Context context, ItemAction onClick, ItemAction onLongClick, ItemAction onFocus, Runnable onUp) {
        inflater = LayoutInflater.from(context);
        this.onClick = onClick;
        this.onLongClick = onLongClick;
        this.onFocus = onFocus;
        this.onUp = onUp;
    }

    void setItems(List<AppItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    AppItem getItem(int position) {
        return items.get(position);
    }

    @NonNull
    @Override
    public TileHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TileHolder(inflater.inflate(R.layout.item_launcher_app, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull TileHolder holder, int position) {
        AppItem item = items.get(position);
        holder.label.setText(item.label);
        holder.icon.setImageDrawable(item.icon);
        holder.itemView.setContentDescription(item.label);
        holder.itemView.setOnClickListener(view -> onClick.run(item));
        holder.itemView.setOnLongClickListener(view -> {
            onLongClick.run(item);
            return true;
        });
        holder.itemView.setOnFocusChangeListener((view, focused) -> {
            view.animate().scaleX(focused ? 1.05f : 1f).scaleY(focused ? 1.05f : 1f)
                    .translationZ(focused ? 8f : 0f).setDuration(150).start();
            holder.label.setTextColor(view.getContext().getColor(
                    focused ? R.color.launcher_text_primary : R.color.launcher_text_secondary));
            if (focused) onFocus.run(item);
        });
        holder.itemView.setOnKeyListener((view, keyCode, event) -> {
            if (event.getAction() != KeyEvent.ACTION_DOWN) return false;
            if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                onUp.run();
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) return true;
            int current = holder.getBindingAdapterPosition();
            return (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && current == 0)
                    || (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && current == items.size() - 1);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static final class TileHolder extends RecyclerView.ViewHolder {
        final ImageView icon;
        final TextView label;

        TileHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.appIcon);
            label = itemView.findViewById(R.id.appLabel);
        }
    }
}
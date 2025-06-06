package com.example.bugtaw;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RingtoneAdapter extends RecyclerView.Adapter<RingtoneAdapter.ViewHolder> {
    public interface OnRingtoneSelectListener {
        void onSelect(String name);
    }
    public interface OnRingtonePreviewListener {
        void onPreview(String name);
    }

    private List<String> ringtones;
    private OnRingtoneSelectListener selectListener;
    private OnRingtonePreviewListener previewListener;
    private int selectedPosition = -1;

    // Helper to format ringtone names
    private String formatRingtoneName(String raw) {
        // Remove numbers and underscores, capitalize each word
        String noNum = raw.replaceAll("\\d", "");
        String[] parts = noNum.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) sb.append(part.substring(1));
                sb.append(" ");
            }
        }
        return sb.toString().trim();
    }

    public RingtoneAdapter(List<String> ringtones, OnRingtoneSelectListener selectListener, OnRingtonePreviewListener previewListener) {
        this.ringtones = ringtones;
        this.selectListener = selectListener;
        this.previewListener = previewListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ringtone, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String name = ringtones.get(position);
        holder.ringtoneName.setText(formatRingtoneName(name));
        holder.radioSelect.setChecked(position == selectedPosition);

        View.OnClickListener selectAndPreview = v -> {
            int prevSelected = selectedPosition;
            int newSelected = holder.getAdapterPosition();
            if (newSelected == selectedPosition) {
                // Already selected, but play preview anyway
                previewListener.onPreview(name);
                return;
            }
            selectedPosition = newSelected;
            notifyItemChanged(prevSelected);
            notifyItemChanged(selectedPosition);
            previewListener.onPreview(name);
            selectListener.onSelect(name);
        };

        holder.itemView.setOnClickListener(selectAndPreview);
        holder.radioSelect.setOnClickListener(selectAndPreview);
    }

    @Override
    public int getItemCount() {
        return ringtones.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView ringtoneName;
        RadioButton radioSelect;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ringtoneName = itemView.findViewById(R.id.ringtone_name);
            radioSelect = itemView.findViewById(R.id.radio_select);
        }
    }
}

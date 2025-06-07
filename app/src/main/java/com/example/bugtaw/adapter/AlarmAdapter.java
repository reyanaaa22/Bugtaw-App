package com.example.bugtaw.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bugtaw.R;
import com.example.bugtaw.data.Alarm;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.List;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {
    private List<Alarm> alarms;
    private final OnAlarmActionListener listener;

    public interface OnAlarmActionListener {
        void onAlarmToggled(Alarm alarm, boolean isEnabled);
        void onAlarmClicked(Alarm alarm);
    }

    public AlarmAdapter(OnAlarmActionListener listener) {
        this.alarms = new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public AlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_alarm, parent, false);
        return new AlarmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlarmViewHolder holder, int position) {
        Alarm alarm = alarms.get(position);
        holder.bind(alarm);
    }

    @Override
    public int getItemCount() {
        return alarms.size();
    }

    public void setAlarms(List<Alarm> alarms) {
        this.alarms = alarms;
        notifyDataSetChanged();
    }

    public Alarm getAlarmAt(int position) {
        return alarms.get(position);
    }

    class AlarmViewHolder extends RecyclerView.ViewHolder {
        private final TextView timeText;
        private final TextView daysText;
        private final TextView puzzleTypeText;
        private final SwitchMaterial alarmSwitch;

        public AlarmViewHolder(@NonNull View itemView) {
            super(itemView);
            timeText = itemView.findViewById(R.id.timeText);
            daysText = itemView.findViewById(R.id.daysText);
            puzzleTypeText = itemView.findViewById(R.id.puzzleTypeText);
            alarmSwitch = itemView.findViewById(R.id.alarmSwitch);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onAlarmClicked(alarms.get(position));
                }
            });
        }

        public void bind(Alarm alarm) {
            timeText.setText(alarm.getTimeString());
            daysText.setText(formatDays(alarm.getDays()));
            puzzleTypeText.setText(alarm.getPuzzleType());
            
            alarmSwitch.setChecked(alarm.isEnabled());
            alarmSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                listener.onAlarmToggled(alarm, isChecked));
        }

        private String formatDays(String days) {
            // Convert "1,2,3" format to "Mon, Tue, Wed"
            StringBuilder formatted = new StringBuilder();
            String[] daysArray = days.split(",");
            for (String day : daysArray) {
                if (formatted.length() > 0) {
                    formatted.append(", ");
                }
                switch (day) {
                    case "1": formatted.append("Mon"); break;
                    case "2": formatted.append("Tue"); break;
                    case "3": formatted.append("Wed"); break;
                    case "4": formatted.append("Thu"); break;
                    case "5": formatted.append("Fri"); break;
                    case "6": formatted.append("Sat"); break;
                    case "7": formatted.append("Sun"); break;
                }
            }
            return formatted.toString();
        }
    }
} 
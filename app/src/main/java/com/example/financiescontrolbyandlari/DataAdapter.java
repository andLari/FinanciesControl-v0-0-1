package com.example.financiescontrolbyandlari;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DataAdapter extends RecyclerView.Adapter<DataAdapter.ViewHolder> {

    private List<Transaction> data;

    public DataAdapter(List<Transaction> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_data, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction transaction = data.get(position);
        holder.bind(transaction);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView dateTextView;
        private TextView typeTextView;
        private TextView amountTextView;
        private TextView nameTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            typeTextView = itemView.findViewById(R.id.typeTextView);
            amountTextView = itemView.findViewById(R.id.amountTextView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
        }

        public void bind(Transaction transaction) {
            dateTextView.setText(transaction.getDate());
            typeTextView.setText(transaction.getType());
            amountTextView.setText(String.valueOf(transaction.getAmount()));
            nameTextView.setText(transaction.getName());
        }
    }
}

package com.example.financiescontrolbyandlari;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class DataAdapter extends BaseAdapter {

    private List<Transaction> dataList;
    private LayoutInflater inflater;

    public DataAdapter(List<Transaction> dataList, Context context) {
        this.dataList = dataList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public Object getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_data, null);
            holder = new ViewHolder();

            holder.dateTextView = convertView.findViewById(R.id.dateTextView);
            holder.typeTextView = convertView.findViewById(R.id.typeTextView);
            holder.amountTextView = convertView.findViewById(R.id.amountTextView);
            holder.nameTextView = convertView.findViewById(R.id.nameTextView);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Transaction transaction = dataList.get(position);

        holder.dateTextView.setText(transaction.getDate());
        holder.typeTextView.setText(getTypeInRussian(transaction.getType()));
        holder.amountTextView.setText(String.valueOf(transaction.getAmount()));
        holder.nameTextView.setText(transaction.getName());

        return convertView;
    }

    private String getTypeInRussian(String type) {
        if ("expense".equals(type)) {
            return "Расход";
        } else if ("income".equals(type)) {
            return "Доход";
        }
        return type;
    }



    private static class ViewHolder {
        TextView dateTextView;
        TextView typeTextView;
        TextView amountTextView;
        TextView nameTextView;
    }
}
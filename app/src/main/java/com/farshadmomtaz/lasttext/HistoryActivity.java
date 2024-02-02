package com.farshadmomtaz.lasttext;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {
    private ArrayList<ListItem> HistoryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // populate history list
        DBHelper LastTextdb = new DBHelper(this);
        HistoryList = LastTextdb.getAllHistory();

        ListView HistoryListView = (ListView) findViewById(R.id.history_list);
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_2, android.R.id.text1, HistoryList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);
                text2.setEllipsize(TextUtils.TruncateAt.END);
                text2.setMaxLines(2);

                text1.setText(HistoryList.get(position).ItemTitle);
                text2.setText(HistoryList.get(position).ItemDescription);
                return view;
            }
        };
        HistoryListView.setAdapter(arrayAdapter);

        // Change UI based on history list
        TextView noHistoryText = (TextView) findViewById(R.id.noHistoryText);
        Button DeleteAllHistory = (Button) findViewById(R.id.DeleteAllHistory);
        ListView history_list = (ListView) findViewById(R.id.history_list);
        if (HistoryList.size() == 0) {
            noHistoryText.setVisibility(View.VISIBLE);
            DeleteAllHistory.setVisibility(View.GONE);
            history_list.setVisibility(View.GONE);
        }
        else {
            noHistoryText.setVisibility(View.GONE);
            DeleteAllHistory.setVisibility(View.VISIBLE);
            history_list.setVisibility(View.VISIBLE);
        }
    }

    public void DeleteHistory(View view) {
        DBHelper LastTextdb = new DBHelper(this);
        LastTextdb.deleteHistory();
        Toast.makeText(HistoryActivity.this, getString(R.string.activity_history_code_history_cleared), Toast.LENGTH_SHORT).show();

        // Change UI based on history list
        TextView noHistoryText = (TextView) findViewById(R.id.noHistoryText);
        Button DeleteAllHistory = (Button) findViewById(R.id.DeleteAllHistory);
        ListView history_list = (ListView) findViewById(R.id.history_list);
        noHistoryText.setVisibility(View.VISIBLE);
        DeleteAllHistory.setVisibility(View.GONE);
        history_list.setVisibility(View.GONE);

    }
}

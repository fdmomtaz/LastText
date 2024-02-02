package com.farshadmomtaz.lasttext;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ArrayList<ListItem> ContactList;
    DBHelper LastTextdb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // check if auto message is disabled
        SharedPreferences sharedPref = getSharedPreferences("LAST_TEXT_SETTING", Context.MODE_PRIVATE);
        boolean disableAutoMessage = sharedPref.getBoolean("DISABLE_AUTO_MESSAGE", false);

        // Start/Stop service
        Intent serviceIntent = new Intent(this, BatteryService.class);
        TextView disableAutoMessageText = (TextView) findViewById(R.id.disableAutoMessage);
        if (disableAutoMessage) {
            this.stopService(serviceIntent);
            disableAutoMessageText.setVisibility(View.VISIBLE);
        }
        else {
            this.startService(serviceIntent);
            disableAutoMessageText.setVisibility(View.GONE);
        }

        // Fab
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("com.farshadmomtaz.lasttext.DetailActivity");
                intent.putExtra("ContactId", "0");
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume(){
        super.onResume();

        // Database
        LastTextdb = new DBHelper(this);

        // Contact List
        ListView ContactListView = (ListView) findViewById(R.id.contact_list);
        TextView noContactsText = (TextView) findViewById(R.id.noContactsText);
        ContactList = LastTextdb.getAllContacts();

        if (ContactList.size() != 0) {
            noContactsText.setVisibility(View.GONE);
            ContactListView.setVisibility(View.VISIBLE);
        }
        else {
            noContactsText.setVisibility(View.VISIBLE);
            ContactListView.setVisibility(View.GONE);
        }

        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_2, android.R.id.text1, ContactList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);
                text2.setEllipsize(TextUtils.TruncateAt.END);
                text2.setMaxLines(2);

                text1.setText(ContactList.get(position).ItemTitle);
                text2.setText(ContactList.get(position).ItemDescription);
                return view;
            }
        };
        ContactListView.setAdapter(arrayAdapter);

        ContactListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent("com.farshadmomtaz.lasttext.DetailActivity");
                intent.putExtra("ContactId", Integer.toString(ContactList.get(position).ItemId));
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // History
        if (id == R.id.action_history) {
            Intent intent = new Intent("com.farshadmomtaz.lasttext.HistoryActivity");
            startActivity(intent);
            return true;
        }
        
        // Rate This App
        if (id == R.id.action_rate_app) {
            Uri uri = Uri.parse("market://details?id=" + this.getPackageName());
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            try {
                startActivity(goToMarket);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://play.google.com/store/apps/details?id=" + this.getPackageName())));
            }
            return true;
        }

        // Disable Auto Message
        if (id == R.id.action_disable_message) {
            // get current disable auto message status
            SharedPreferences sharedPref = getSharedPreferences("LAST_TEXT_SETTING", Context.MODE_PRIVATE);
            boolean disableAutoMessage = sharedPref.getBoolean("DISABLE_AUTO_MESSAGE", false);

            // edit the auto message status
            SharedPreferences.Editor prefEditor = sharedPref.edit();
            disableAutoMessage = !disableAutoMessage;
            prefEditor.putBoolean("DISABLE_AUTO_MESSAGE", disableAutoMessage);
            prefEditor.commit();

            Intent serviceIntent = new Intent(this, BatteryService.class);
            TextView disableAutoMessageText = (TextView) findViewById(R.id.disableAutoMessage);
            if (disableAutoMessage) {
                this.stopService(serviceIntent);
                disableAutoMessageText.setVisibility(View.VISIBLE);
            }
            else {
                this.startService(serviceIntent);
                disableAutoMessageText.setVisibility(View.GONE);
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

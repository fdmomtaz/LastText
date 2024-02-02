package com.farshadmomtaz.lasttext;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class DBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "LastText.db";
    public static final String CONTACTS_TABLE_NAME = "contacts";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME , null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Contact
        db.execSQL(
                "create table Contact (ContactId integer primary key autoincrement, Name text, PhoneNumber text, BatteryPercentage integer, Message blob, MessageSent boolean)"
        );

        // History
        db.execSQL(
                "create table History (MessageId integer primary key autoincrement, ContactId integer, MessageTime datetime, FOREIGN KEY(ContactId) REFERENCES Contact(ContactId))"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS Contact");
        db.execSQL("DROP TABLE IF EXISTS History");
        onCreate(db);
    }

    public Cursor getContact(int ContactId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from Contact where ContactId = " + ContactId + "", null );
        return res;
    }

    public void insertContact(String Name, String PhoneNumber, int BatteryPercentage, String Message) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("Name", Name);
        contentValues.put("PhoneNumber", PhoneNumber);
        contentValues.put("BatteryPercentage", BatteryPercentage);
        contentValues.put("Message", Message);
        contentValues.put("MessageSent", false);
        db.insert("Contact", null, contentValues);
    }

    public void updateContact(int ContactId, String Name, String PhoneNumber, int BatteryPercentage, String Message) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("Name", Name);
        contentValues.put("PhoneNumber", PhoneNumber);
        contentValues.put("BatteryPercentage", BatteryPercentage);
        contentValues.put("Message", Message);
        db.update("Contact", contentValues, "ContactId = ? ", new String[] { Integer.toString(ContactId) } );
    }

    public void deleteContact (int ContactId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("Contact", "ContactId = ? ", new String[] { Integer.toString(ContactId) });
        db.delete("History", "ContactId = ? ", new String[] { Integer.toString(ContactId) });
    }

    public void resetMessageSentAllContact () {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("MessageSent", false);
        db.update("Contact", contentValues, null, null);
    }

    public boolean getMessageSentStatus() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery("select * from Contact where MessageSent = 1", null);

        return res.getCount() > 0 ? true : false;
    }

    public ArrayList<ListItem> getAllContacts() {
        ArrayList<ListItem> array_list = new ArrayList<ListItem>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery("select * from Contact", null);
        res.moveToFirst();

        while(res.isAfterLast() == false){
            ListItem item = new ListItem();
            item.ItemId = res.getInt(res.getColumnIndex("ContactId"));
            item.ItemTitle = res.getString(res.getColumnIndex("Name"));
            item.ItemDescription = res.getString(res.getColumnIndex("Message"));

            array_list.add(item);

            res.moveToNext();
        }
        return array_list;
    }

    public ArrayList<ListItem> getContactsByPercentage(int batteryPercentage) {
        ArrayList<ListItem> array_list = new ArrayList<ListItem>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery("select * from Contact where BatteryPercentage >= " + batteryPercentage + " and MessageSent = 0", null);
        res.moveToFirst();

        while(res.isAfterLast() == false){
            ListItem item = new ListItem();
            item.ItemId = res.getInt(res.getColumnIndex("ContactId"));
            item.ItemTitle = res.getString(res.getColumnIndex("PhoneNumber"));
            item.ItemDescription = res.getString(res.getColumnIndex("Message"));
            item.ItemDetail = res.getString(res.getColumnIndex("Name"));

            array_list.add(item);

            res.moveToNext();
        }
        return array_list;
    }

    public ArrayList<ListItem> getAllHistory() {
        ArrayList<ListItem> array_list = new ArrayList<ListItem>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery("select * from History LEFT JOIN Contact ON History.ContactId = Contact.ContactId order by MessageTime DESC limit 25", null);
        res.moveToFirst();

        while(res.isAfterLast() == false){
            ListItem item = new ListItem();
            item.ItemId = res.getInt(res.getColumnIndex("MessageId"));
            item.ItemTitle = res.getString(res.getColumnIndex("Name"));
            String MessageTime = res.getString(res.getColumnIndex("MessageTime"));
            item.ItemDescription =  MessageTime.substring(5,7) + "/" +
                                    MessageTime.substring(8,10) + "/" +
                                    MessageTime.substring(0,4) + " - " +
                                    MessageTime.substring(11,19);

            array_list.add(item);

            res.moveToNext();
        }
        return array_list;
    }

    public void insertHistory(int ContactId) {
        String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(Calendar.getInstance().getTime());

        // Update contact table
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contactValues = new ContentValues();
        contactValues.put("MessageSent", true);
        db.update("Contact", contactValues, "ContactId = ? ", new String[] { Integer.toString(ContactId) } );

        // Add to history table
        ContentValues historyValues = new ContentValues();
        historyValues.put("ContactId", ContactId);
        historyValues.put("MessageTime", currentDate);
        db.insert("History", null, historyValues);
    }

    public void deleteHistory () {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("History", null, null);
    }
}

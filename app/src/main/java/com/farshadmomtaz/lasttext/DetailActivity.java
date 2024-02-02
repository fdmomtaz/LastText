package com.farshadmomtaz.lasttext;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import static java.lang.Integer.*;

public class DetailActivity extends AppCompatActivity {
    int ContactId = 0;
    DBHelper LastTextdb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        try {
            String ContactIdText = getIntent().getStringExtra("ContactId");
            ContactId = Integer.parseInt(ContactIdText);
        }
        catch (Exception ex) {
            ContactId = 0;
        }

        if (ContactId != 0) {
            // update UI for update contact
            Button CreateUpdateButton = (Button) findViewById(R.id.CreateUpdateButton);
            Button RemoveButton = (Button) findViewById(R.id.RemoveButton);
            Button ContactButton = (Button) findViewById(R.id.ContactButton);
            CreateUpdateButton.setText(getString(R.string.activity_detail_button_update));
            RemoveButton.setVisibility(View.VISIBLE);
            ContactButton.setVisibility(View.GONE);

            // get data from database
            LastTextdb = new DBHelper(this);
            EditText name = (EditText)findViewById(R.id.editTextName);
            EditText number = (EditText)findViewById(R.id.editTextNumber);
            EditText message = (EditText)findViewById(R.id.editTextMessage);
            RadioButton batteryPercentage = null;

            Cursor contactInfo = LastTextdb.getContact(ContactId);
            contactInfo.moveToPosition(0);
            name.setText(contactInfo.getString(contactInfo.getColumnIndex("Name")));
            number.setText(contactInfo.getString(contactInfo.getColumnIndex("PhoneNumber")));
            message.setText(contactInfo.getString(contactInfo.getColumnIndex("Message")));

            switch (contactInfo.getInt(contactInfo.getColumnIndex("BatteryPercentage"))) {
                case 5:
                    batteryPercentage = (RadioButton) findViewById(R.id.radio5);
                    break;
                case 10:
                    batteryPercentage = (RadioButton) findViewById(R.id.radio10);
                    break;
                case 15:
                    batteryPercentage = (RadioButton) findViewById(R.id.radio15);
                    break;
                case 20:
                    batteryPercentage = (RadioButton) findViewById(R.id.radio20);
                    break;
                case 25:
                    batteryPercentage = (RadioButton) findViewById(R.id.radio25);
                    break;
            }
            batteryPercentage.setChecked(true);
        }
    }

    public void onCancelClick(View v) {
        finish();
    }

    public void onCreateUpdateClick(View v) {
        EditText name = (EditText)findViewById(R.id.editTextName);
        EditText number = (EditText)findViewById(R.id.editTextNumber);
        EditText message = (EditText)findViewById(R.id.editTextMessage);
        RadioGroup batteryPercentage = (RadioGroup)findViewById(R.id.radioBatteryPercentage);
        RadioButton selectedBatteryPercentage = (RadioButton)findViewById(batteryPercentage.getCheckedRadioButtonId());

        if (name.getText().toString().isEmpty() || number.getText().toString().isEmpty()
                || message.getText().toString().isEmpty() || selectedBatteryPercentage == null) {
            Toast.makeText(DetailActivity.this, getString(R.string.activity_detail_code_create_fields_needed), Toast.LENGTH_SHORT).show();
            return;
        }

        // add or update Db
        int batteryPer = parseInt(selectedBatteryPercentage.getText().toString().replace("%", ""));
        LastTextdb = new DBHelper(this);
        if(ContactId == 0) {
            LastTextdb.insertContact(name.getText().toString(), number.getText().toString(), batteryPer, message.getText().toString());
            Toast.makeText(DetailActivity.this, getString(R.string.activity_detail_code_create_success), Toast.LENGTH_SHORT).show();
        }
        else {
            LastTextdb.updateContact(ContactId, name.getText().toString(), number.getText().toString(), batteryPer, message.getText().toString());
            Toast.makeText(DetailActivity.this, getString(R.string.activity_detail_code_update_success), Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    public void onRemoveClick(View v) {
        LastTextdb = new DBHelper(this);

        if(ContactId != 0) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

            alertDialogBuilder.setTitle(R.string.activity_detail_code_delete_title);
            alertDialogBuilder
                .setMessage(getString(R.string.activity_detail_code_delete_question))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.activity_detail_code_delete_yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        LastTextdb.deleteContact(ContactId);
                        Toast.makeText(DetailActivity.this, getString(R.string.activity_detail_code_delete_success), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .setNegativeButton(getString(R.string.activity_detail_code_delete_no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                    }
                });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }

    public void onGetContactClick(View v) {
        startActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {

            Uri uriContact = data.getData();

            String ContactName = retrieveContactName(uriContact);
            String ContactNumber = retrieveContactNumber(uriContact);

            EditText name = (EditText)findViewById(R.id.editTextName);
            EditText number = (EditText)findViewById(R.id.editTextNumber);

            name.setText(ContactName);
            number.setText(ContactNumber);
        }
    }

    private String retrieveContactNumber(Uri uriContact) {
        String contactNumber = null;

        try {
            // getting contacts ID
            Cursor cursorID = getContentResolver().query(uriContact,
                    new String[]{ContactsContract.Contacts._ID},
                    null, null, null);

            String contactID = "";
            if (cursorID.moveToFirst()) {
                contactID = cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts._ID));
            }

            cursorID.close();

            // Using the contact ID now we will get contact phone number
            Cursor cursorPhone = getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND (" +
                            ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                            ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE + " OR " +
                            ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                            ContactsContract.CommonDataKinds.Phone.TYPE_MAIN + ")",
                    new String[]{contactID},
                    null
            );

            if (cursorPhone != null && cursorPhone.getCount() > 0 && cursorPhone.moveToFirst()) {
                contactNumber = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            }
            else {
                // get other number
                Cursor cursorAllPhone = getContentResolver().query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? ",
                        new String[]{contactID},
                        null
                );

                if (cursorAllPhone != null && cursorAllPhone.getCount() > 0 && cursorAllPhone.moveToFirst()) {
                    contactNumber = cursorPhone.getString(cursorAllPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                }
            }

            cursorPhone.close();

            return contactNumber;
        }
        catch (Exception ex) {
            return contactNumber;
        }
    }

    private String retrieveContactName(Uri uriContact) {
        String contactName = null;

        try {
            // querying contact data store
            Cursor cursor = getContentResolver().query(uriContact, null, null, null, null);

            if (cursor.moveToFirst()) {
                contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            }

            cursor.close();

            return contactName;
        }
        catch (Exception ex) {
            return  contactName;
        }
    }
}

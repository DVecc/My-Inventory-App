package com.snhu.vincentdelvecchio_final_project;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;

import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;


public class AddEditInventoryActivity extends AppCompatActivity {
    // View variables
    private TextView titleText;
    private EditText itemName, itemQTY;
    private Button saveButton, cancelButton;
    private ImageButton increaseButton, decreaseButton;
    // Inventory Item Database
    private InventoryDatabase mItemDb;
    // Variables for Intent Extras
    private String username, mode;
    private Integer itemId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize variables
        setContentView(R.layout.add_edit_inventory);
        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        mode = intent.getStringExtra("mode");
        mItemDb = InventoryDatabase.getInstance(this);
        titleText = findViewById(R.id.titleTextView);
        itemName = findViewById(R.id.itemNameEditText);
        itemQTY = findViewById(R.id.qtyEditText);
        saveButton = findViewById(R.id.SaveButton);
        cancelButton = findViewById(R.id.cancelButton);
        increaseButton = findViewById(R.id.itemQtyIncreaseButton);
        decreaseButton = findViewById(R.id.itemQtyDecreaseButton);

        // Change Title text to reflect which button launched the activity
        titleText.setText(mode + " Item");

        // If the Activity was launched via the edit button, populate the fields with the existing
        // Items information
        if(mode.equals("Edit")) {
            // Get itemId passed to the intent and retrieve the Item from the database
            itemId = intent.getIntExtra("itemID",-1);
            InventoryItem item = mItemDb.getItem(itemId);

            // Populate text views to reflect items information
            itemName.setText(item.getmItemName());
            itemQTY.setText(String.valueOf(item.getmItemQTY()));
        }
    }

    // OnCLick handler for Increase button
    public void increaseButtonOnClick(View view) {
        // If button was pressed without an existing integer, set it to 0
        if(itemQTY.getText().toString().matches("")) {
            itemQTY.setText("0");
        }
        // Otherwise Increase by 1
        else {
            itemQTY.setText(String.valueOf(Integer.parseInt(itemQTY.getText().toString())+1));
        }
    }

    // OnClick handler for Decrease Button
    public void decreaseButtonOnClick(View view) {
        // If button was pressed without an existing integer or the integer would become negative, set it to 0
        if(itemQTY.getText().toString().matches("") || Integer.parseInt(itemQTY.getText().toString()) -1 < 0) {
            itemQTY.setText("0");
        }
         // Otherwise decrease by 1
        else {
            itemQTY.setText(String.valueOf(Integer.parseInt(itemQTY.getText().toString())-1));
        }
    }

    // OnClick handler for Save Button
    public void saveButtonOnClick(View view) {
        // If Item Name is Empty send toast and return
        if(itemName.getText().toString().matches("")) {
            Toast.makeText(this, "Must enter an item name", Toast.LENGTH_SHORT).show();
            return;
        }

        // If Item QTY is Empty send toast and return
        if(itemQTY.getText().toString().matches("")) {
            Toast.makeText(this, "Must enter an item QTY", Toast.LENGTH_SHORT).show();
            return;
        }

        // If item QTY is less than or equal to 0 and activity was launched in Add item mode send toast and return
        if(Integer.parseInt(itemQTY.getText().toString()) <= 0 && mode.equals("Add")) {
            Toast.makeText(this, "ItemQTY must be greater than 0", Toast.LENGTH_SHORT).show();
            return;
        }

        // If item QTY is less than 0 and activity was launched in Edit item mode send toast and return
        if(Integer.parseInt(itemQTY.getText().toString()) < 0 && mode.equals("Edit")) {
            Toast.makeText(this, "ItemQTY must be greater than or equal to 0", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create new Inventory item with fields from editText views
        InventoryItem item = new InventoryItem();
        item.setmItemName(itemName.getText().toString());
        item.setmItemQTY(Integer.parseInt(itemQTY.getText().toString()));

        // If activity is in Add mode, add the item to the database
        if(mode.equals("Add")) {
            mItemDb.addItem(username,item);
        }

        // If activity is in edit mode, set the items ID and update item in the database
        else{
            item.setmItemId(itemId);
            mItemDb.updateItem(item);
            // If permission was enabled, Send low inventory SMS if editing item was saved with 0 QTY remaining
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PermissionChecker.PERMISSION_GRANTED && item.getmItemQTY()==0) {
                sendSMS(item);
            }
        }
        finish();
    }

    // Sends an SMS notification stating that the item passed in has 0 QTY remaining
    private void sendSMS(InventoryItem item) {
        // Open Preferences file to fetch phone number to send SMS to
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        String phoneNumber = sharedPref.getString("phoneNumber","555-5555"); // Send to fake number if preference does not exist
        String message = String.format(getResources().getString(R.string.inventoryNotificaiton),item.getmItemName()); // Format Message to include item name
        try{
            // Use smsManager to send SMS to the phone number above with the notification message.
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message,null,null);
            Toast.makeText(this, "Low Inventory Notification SMS sent",Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.failedSMS,Toast.LENGTH_SHORT).show();
        }


    }
    // OnClick handler for cancel button, coloses activity without saving, pressing the up button and back button result in the same behavior
    public void cancelButtonOnClick(View view) {
        finish();
    }

    // Override options select for up button so that the activity is removed from the stack when pressed
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        Log.d("item ID : ", "onOptionsItemSelected Item ID" + id);
        if (id == android.R.id.home) {
            finish();
            return true;

        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}

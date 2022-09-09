package com.snhu.vincentdelvecchio_final_project;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class InventoryActivity extends AppCompatActivity  {
    // Item Database
    private  InventoryDatabase mItemDb;
    // Adapter for Recycler View
    private ItemAdapter mItemAdapter;
    // Username sent from logIn activity for database queries
    private String username;
    // Request code for handling permission results
    private final int REQUEST_SMS_CODE = 0;
    // Instance of Menu to dynamically change menu items
    private  Menu activityBarMenu;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);
        // Get username from intent extra
        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        // Get database singleton
        mItemDb = InventoryDatabase.getInstance(getApplicationContext());
        // Initialize Recycler view
        RecyclerView mRecyclerView = findViewById(R.id.inventoryRecyclerView);
        // Create and set layout manager
        RecyclerView.LayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.VERTICAL,false);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        // create and set adapter for recycler view
        mItemAdapter = new ItemAdapter(mItemDb.getItems(username));
        mRecyclerView.setAdapter(mItemAdapter);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    // Update adapter on resume which occurs when returning from the add/edit activity so that the newly added items are displayed
    protected void onResume() {
        super.onResume();
        mItemAdapter.mItemList.clear();
        mItemAdapter.mItemList = mItemDb.getItems(username);
        mItemAdapter.notifyDataSetChanged();
    }

    @Override
    // Inflate Menu and hide the SMS button if permissions have been denied tracked via preferences
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_menu, menu);
        activityBarMenu = menu;
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        menu.findItem(R.id.smsSettings).setVisible(sharedPref.getBoolean("showSMS",true));
        return true;
    }

    @Override
    // On Click handler for menu items
    public boolean onOptionsItemSelected(MenuItem item) {
        // Ask for sms permissions by calling the SMSPermissions method
        if (item.getItemId() == R.id.smsSettings) {
            SMSPermissions();
            return true;
        }

        //Log out of the app and destroy the activity stack so that users may not back into the inventory screen after logging out.
        if (item.getItemId() == R.id.logout) {
            Intent intent = new Intent(this, LogInActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            return true;
        }

        // Create an alert Dialog that allows the user to input the phone number for sms notifications to be sent to
        if(item.getItemId() == R.id.settings) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.phoneNumber);
            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_PHONE);
            input.setText(sharedPref.getString("phoneNumber", ""));
            builder.setView(input);

            builder.setPositiveButton("Save", (dialogInterface, i) -> sharedPref.edit().putString("phoneNumber", input.getText().toString()).apply());
            builder.show();
            return true;
            }

            else{
                return super.onOptionsItemSelected(item);
        }
    }

    // On click listener for the FAB that launches the add/edit activity passing the mode it should reflect and the current users username in the intent extras
    public void fabOnClickListener(View view) {
        Intent intent = new Intent(this, AddEditInventoryActivity.class);
        intent.putExtra("mode", "Add");
        intent.putExtra("username", username);
        startActivity(intent);

    }

    // Show permission rationale and request permissions from the user for sending sms notifications.
    public void SMSPermissions() {
        String smsPermission = Manifest.permission.SEND_SMS;
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, smsPermission )) {
                showPermissionRationaleDialog(this, (dialogInterface, i) -> ActivityCompat.requestPermissions(InventoryActivity.this, new String[]{smsPermission}, REQUEST_SMS_CODE));
        } else {
            ActivityCompat.requestPermissions(this, new String[]{smsPermission}, REQUEST_SMS_CODE);
        }

    }


    // Build the permission rationale dialog with an onClick listener to request permissions after user acknowledges the rationale dialog
    private static void showPermissionRationaleDialog(Activity activity,
                                                      DialogInterface.OnClickListener onClickListener) {
        // Show dialog explaining why permission is needed
        new AlertDialog.Builder(activity)
                .setTitle(R.string.permission_needed)
                .setMessage(R.string.rationalMessage)
                .setPositiveButton(R.string.ok, onClickListener)
                .create()
                .show();
    }

    @Override
    // Handle results from permissions request
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0) {
                // Set preference letting the app know the user has seen the permission dialogue so that we can hide the SMS permissions button from the menu afterwards
                SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                sharedPref.edit().putBoolean("showSMS", false).apply();
                // Hide the SMS icon from the bar
                activityBarMenu.findItem(R.id.smsSettings).setVisible(false);

                // If permissions were granted, trigger the settings button in the menu bar so that users are prompted to enter a phone number to send SMS messages to
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    activityBarMenu.performIdentifierAction(R.id.settings,0);
                }

            }
    }

    // Sends an SMS to the phone number saved in preferences alerting the user that the inventory items qty is 0
    private void sendSMS(InventoryItem item) {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        String phoneNumber = sharedPref.getString("phoneNumber","555-5555");
        String message = String.format(getResources().getString(R.string.inventoryNotificaiton),item.getmItemName());
        try{
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message,null,null);
            Toast.makeText(this, "Low Inventory Notification SMS sent",Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.failedSMS,Toast.LENGTH_SHORT).show();
        }


    }

    // itemHolder for recyclerView
    private static class ItemHolder extends RecyclerView.ViewHolder {

        // Variables for the item rows views and item that the row represents
        private InventoryItem mItem;
        private  final ImageButton increaseButton, decreaseButton, editButton, deleteButton;
        private final TextView itemNameTextView, qtyTextView;

        public ItemHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.recycler_item, parent, false));
            // initialize variables for itemHolder
            increaseButton = itemView.findViewById(R.id.increaseButton);
            decreaseButton = itemView.findViewById(R.id.decreaseButton);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            itemNameTextView = itemView.findViewById(R.id.recyclerItemNameTextView);
            qtyTextView = itemView.findViewById(R.id.quantityTextView);
        }
        // on bind initialize the item and set the text and qty views to that items information
        public void bind(InventoryItem item) {
            mItem = item;
            itemNameTextView.setText(item.getmItemName());
            qtyTextView.setText(String.valueOf(item.getmItemQTY()));
        }

    }

    // Item Adapter for the recyclerView
    private class ItemAdapter extends RecyclerView.Adapter<ItemHolder> {

        // List Variable to hold the list of every item in the users inventory returned from the database
        private List<InventoryItem> mItemList;

        // Constructor that initializes the items list
        public ItemAdapter(List<InventoryItem> items) {
            mItemList = items;
        }

        @NonNull
        @Override
        // create view holder that represents the item in the recycler view
        public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getApplicationContext());
            return new ItemHolder(layoutInflater, parent);
        }

        @Override
        // when Item is bound to the recycler, set that view holders buttons on click handlers
        public void onBindViewHolder(ItemHolder holder, int position) {
            holder.bind(mItemList.get(position)); // Bind the item holder with the item located in the item list at the position

            // set the onclick listener for the increase button in the itemHolder View
            holder.increaseButton.setOnClickListener(view -> {
                // Increase the QTY by 1
                holder.mItem.setmItemQTY(holder.mItem.getmItemQTY() + 1);
                // Update item in database
                mItemDb.updateItem(holder.mItem);
                // Notify adapter that the item has changed so that it may update the holder with the newly updated information
                mItemAdapter.notifyItemChanged(holder.getAdapterPosition());
                    });

            // set the onclick listener for the decrease button in the itemHolder View
            holder.decreaseButton.setOnClickListener(view -> {
                // Only decrease the item if it will not be brought below 0 qty left
                if(holder.mItem.getmItemQTY() - 1 >= 0) {
                    // decrease the qty by 1
                    holder.mItem.setmItemQTY(holder.mItem.getmItemQTY() - 1);
                    // update item in database
                    mItemDb.updateItem(holder.mItem);
                    // Notify adapter that the item has changed so that it may update the holder with the newly updated information
                    mItemAdapter.notifyItemChanged(holder.getAdapterPosition());

                    // If permissions were granted, and the items qty is now 0, send SMS alerting user the qty is 0
                    if(ContextCompat.checkSelfPermission(view.getContext(),Manifest.permission.SEND_SMS)==PackageManager.PERMISSION_GRANTED && holder.mItem.getmItemQTY() == 0){
                        sendSMS(holder.mItem);
                    }
                }

            });

            // set onclick listener for the delete button
            holder.deleteButton.setOnClickListener(view -> {
                // delete the item from the database
                mItemDb.deleteItem(holder.mItem.getmItemId());
                // remove the item from the adapters list of items
                mItemList.remove(holder.getAdapterPosition());
                // notify the adapter that the item has been removed so that it can update the recyclerView
                mItemAdapter.notifyItemRemoved(holder.getAdapterPosition());
            });

            // set onclick listener for the edit button
            holder.editButton.setOnClickListener(view -> {
                // create new intent with extras telling the add/edit activity to open in edit mode, and the username and itemid for the item represented by the itemHolder
                Intent intent = new Intent(view.getContext(), AddEditInventoryActivity.class);
                intent.putExtra("mode", "Edit");
                intent.putExtra("username", username);
                intent.putExtra("itemID", holder.mItem.getmItemId());
                startActivity(intent);
            });
        }

        @Override
        // Return the number of items in the items list
        public int getItemCount() {
            return mItemList.size();
        }

    }
}



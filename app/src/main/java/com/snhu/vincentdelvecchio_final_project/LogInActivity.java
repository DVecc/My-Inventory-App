package com.snhu.vincentdelvecchio_final_project;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import android.view.View;

import android.widget.EditText;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

public class LogInActivity extends AppCompatActivity {

    // View Variables
    private EditText username, password;
    // User Database
    UserDatabase DB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // initialize views and retrieve singleton database instance
        username = findViewById(R.id.usernameEditText);
        password = findViewById(R.id.passwordEditText);
        DB = UserDatabase.getInstance(getApplicationContext());
    }

    // Register Button OnClick handler
    public void registerOnClick(View view) {
        // Retrieve data from views
        String userNameValue = username.getText().toString();
        String passwordValue = password.getText().toString();

        //Check if Username is Empty
        if(userNameValue.matches("")) {
            Toast.makeText(this, "Must enter a username", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if password is Empty
        if(passwordValue.matches("")) {
            Toast.makeText(this, "Must enter a password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if Username is already Taken
        if(DB.getUser(userNameValue).moveToFirst()){
            Toast.makeText(this, "Username already exist", Toast.LENGTH_SHORT).show();
            return;
        }

        // variable to hold result of database result
        boolean registerResult = DB.addUser(userNameValue,passwordValue);

        // Show toast of successful or Unsuccessful account creation.
        if(registerResult){
            Toast.makeText(this, "Account Created", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this, "Could not Create Account", Toast.LENGTH_SHORT).show();
        }
    }

    // OnClick handler for Login button
    public void loginOnClick(View view) {
        // Retrieve variables from views
        String userNameValue = username.getText().toString();
        String passwordValue = password.getText().toString();

        // Retrieve User info from database for given username
        Cursor userInfo = DB.getUser(userNameValue);

        // If username exist in database
        if(userInfo.moveToFirst()){
            // Get User password from cursor and check against provided password
            String userPassword = userInfo.getString(1);
            // If password is correct start main activity screen with username as intent extra for database queries.
            if (userPassword.equals(passwordValue)){
                Intent intent = new Intent(this, InventoryActivity.class);
                intent.putExtra("username", userNameValue);
                startActivity(intent);
                // specifically not finalizing activity so that we may come back to it with our logout button deleting the stack above it preventing users from hitting the back button back into the app on log out
            }
            // Password was invalid
            else{
                Toast.makeText(this, "Invalid password", Toast.LENGTH_SHORT).show();
            }
        }
        // Username wasn't in database
        else {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
        }

    }
}


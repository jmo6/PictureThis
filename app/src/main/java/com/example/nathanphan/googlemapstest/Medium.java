package com.example.nathanphan.googlemapstest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Medium extends AppCompatActivity {

    AppInfo appLocation;
    boolean nameEntered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medium);



        appLocation = AppInfo.getInstance(this);
        appLocation.setLocationName("");
    }

    public void uploadPicture(View view) {
        if(nameEntered == false){
            Toast.makeText(getApplicationContext(), "Enter Location!", Toast.LENGTH_LONG).show();
        }
        else {
            Intent intent = new Intent(this, MainPictureActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    public void updateName(View view) {
        EditText edv1 = (EditText) findViewById(R.id.editTextNewName);
        String newName = edv1.getText().toString();
        if(newName.matches("")) {
            Toast.makeText(getApplicationContext(), "Enter Location!", Toast.LENGTH_LONG).show();
            nameEntered = false;
        }
        else {
            appLocation.setLocationName(newName);
            Toast.makeText(getApplicationContext(), "Name Changed", Toast.LENGTH_LONG).show();
            nameEntered = true;
        }
    }
}

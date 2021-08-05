package com.example.attendanceuserapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class Login extends AppCompatActivity {
    Button btn_login;
    EditText txt_uid, txt_pwd;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btn_login = findViewById(R.id.btn_login);
        txt_uid = findViewById(R.id.txt_uid);
        txt_pwd = findViewById(R.id.txt_pwd);

        collectionReference = db.collection("student");
        sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);

        String uid = sharedPreferences.getString("uid","");
        txt_uid.setText(uid);

        if(txt_uid.equals(null)) {
            txt_uid.requestFocus();
            if (txt_uid.requestFocus()) {
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }
        }
        else{
            txt_pwd.requestFocus();
            if (txt_pwd.requestFocus()) {
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }
        }
    }

    public void checkCredentials(View view) {
        if (txt_uid.getText().toString().isEmpty() && txt_pwd.getText().toString().isEmpty())
            Toast.makeText(this, "Enter UID and Password.", Toast.LENGTH_SHORT).show();
        else if (txt_uid.getText().toString().isEmpty())
            Toast.makeText(this, "Enter UID.", Toast.LENGTH_SHORT).show();
        else if (txt_pwd.getText().toString().isEmpty())
            Toast.makeText(this, "Enter Password.", Toast.LENGTH_SHORT).show();
        else {
            DocumentReference documentReference = collectionReference.document(txt_uid.getText().toString());
            documentReference.get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.getString("password").equals(txt_pwd.getText().toString())) {

                            final UserDetails userDetails = (UserDetails) getApplicationContext();
                            Toast.makeText(userDetails, "Welcome " +
                                    documentSnapshot.getString("Name"), Toast.LENGTH_SHORT).show();
                            userDetails.setUid(txt_uid.getText().toString());

                            SharedPreferences.Editor editor = sharedPreferences.edit();

                            editor.putString("uid", txt_uid.getText().toString());//txt_uid.getText().toString());
                            editor.apply();

                            Intent mainActivity = new Intent(Login.this, MainActivity.class);
                            startActivity(mainActivity);
                        } else
                            Toast.makeText(Login.this, "Incorrect Credentials. Try again!", Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
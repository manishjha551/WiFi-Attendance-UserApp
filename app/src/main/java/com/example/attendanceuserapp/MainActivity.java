package com.example.attendanceuserapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    Button btn_enable, btn_submit;
    TextView txt_ssid, txt_wait;
    EditText edt_otp;

    private static final int LOCATION = 1;
    private static String check_ssid = "";
    private static String check_bssid = "";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference;
    String uid;
    SharedPreferences sharedPreferences;

    LocalDate date = LocalDate.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Toast.makeText(this, "onCreate", Toast.LENGTH_SHORT).show();

        sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);

        UserDetails userDetails = (UserDetails) getApplicationContext();
        uid = userDetails.getUid();

        btn_submit = findViewById(R.id.btn_submit);
        edt_otp = findViewById(R.id.edt_code);
        txt_ssid = findViewById(R.id.txt_ssid);
        txt_wait = findViewById(R.id.txt_waitForAttendance);

        collectionReference = db.collection("attendance");
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onStart() {
        super.onStart();
        DocumentReference documentReference = collectionReference.document(date.format(formatter));
        documentReference.addSnapshotListener(this, (documentSnapshot, e) -> {
            if (e != null) {
                Toast.makeText(MainActivity.this, "Error while loading!", Toast.LENGTH_SHORT).show();
                return;
            }
//            assert documentSnapshot != null;
//            if (documentSnapshot.exists() && Objects.equals(documentSnapshot.getString("allowAttendance"), "1")) {
//                txt_wait.setVisibility(View.INVISIBLE);
//                edt_otp.setVisibility(View.VISIBLE);
//                btn_submit.setVisibility(View.VISIBLE);
//            }
//            else if (documentSnapshot.exists() && Objects.equals(documentSnapshot.getString("allowAttendance"), "0")) {
//                edt_otp.setVisibility(View.INVISIBLE);
//                btn_submit.setVisibility(View.INVISIBLE);
//                txt_wait.setText("Time is up. Contact the Faculty if you couldn't mark your attendance.");
//                txt_wait.setVisibility(View.VISIBLE);
//                Handler handler = new Handler();
//                handler.postDelayed(() ->
//                        txt_wait.setText("Wait for Faculty to generate an attendance code."),
//                        5000);
//            }
        });
//        txt_wait.setText("Wait for Faculty to generate an attendance code.");
//        edt_otp.setVisibility(View.VISIBLE);
//        btn_submit.setVisibility(View.VISIBLE);

        if (uid == null) {
            Intent login = new Intent(MainActivity.this, Login.class);
            startActivity(login);
        } else {
            DocumentReference routerDocumentReference = collectionReference.document("routerDetails");

            routerDocumentReference.get()
                    .addOnSuccessListener(documentSnapshot -> {
                        check_ssid = Objects.requireNonNull(documentSnapshot.get("ssid")).toString();
                        check_bssid = Objects.requireNonNull(documentSnapshot.get("bssid")).toString();
                        tryToReadSSID();
//                        txt_ssid.setText("Device is connected to University Wi-Fi.");
                    });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, int[] grantResults) {
//        Toast.makeText(this, "onRequestPermission", Toast.LENGTH_SHORT).show();
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED && requestCode == LOCATION) {
            tryToReadSSID();
        }
    }

    @SuppressLint("SetTextI18n")
    private void tryToReadSSID() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION);
        } else {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {

//                String str_ssid = "SSID = " + wifiInfo.getSSID();
//                String ip = "IP Address = " + wifiInfo.getIpAddress();
//                String bssid = "BSSID = " + wifiInfo.getBSSID();
//                String rssi = "RSSI = " + wifiInfo.getRssi();
//                String linkspeed = "Linkspeed = " + wifiInfo.getLinkSpeed();
//                String nwid = "Network ID = " + wifiInfo.getNetworkId();

                if (wifiInfo.getSSID().toLowerCase().equals(check_ssid.toLowerCase()) && wifiInfo.getBSSID().toLowerCase().equals(check_bssid.toLowerCase())) {
                    txt_ssid.setText("Device is connected to University Wi-Fi.");
//                    txt_ssid.setText("You are eligible for attendance.\n" + check_ssid + " | " + wifiInfo.getSSID() + "\n" + check_bssid + " | " + wifiInfo.getBSSID());
                    txt_wait.setVisibility(View.INVISIBLE);
                    edt_otp.setVisibility(View.VISIBLE);
                    btn_submit.setVisibility(View.VISIBLE);
                    edt_otp.requestFocus();
//                    if (edt_otp.requestFocus()) {
//                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
//                    }
                } else {
                    txt_wait.setText("You are not eligible for attendance. Connect to University Wi-FI.");// \n" + check_ssid + " | " + wifiInfo.getSSID() + "\n" + check_bssid + " | " + wifiInfo.getBSSID());
                }
            }
        }
    }

    public void checkCode(View view) {
        if (edt_otp.getText().length() < 4)
            Toast.makeText(this, "Enter a 4 digit code.", Toast.LENGTH_SHORT).show();
        else {
            LocalDate date = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DocumentReference documentReference = collectionReference.document(date.format(formatter));

            documentReference.get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (Objects.equals(documentSnapshot.getString("allowAttendance"), "1")) {
                            if (edt_otp.getText().toString().equals(documentSnapshot.getString("code"))) {
                                Toast.makeText(MainActivity.this, "Code matches!", Toast.LENGTH_SHORT).show();

                                HashMap<String, Object> present = new HashMap<>();
                                present.put("Attend", "1");

                                DocumentReference docRef = db.document("attendance/" +
                                        date.format(formatter) + "/subjects/" + documentSnapshot.getString("lecture")
                                        + "/students/" + uid);

                                docRef.set(present).addOnSuccessListener(aVoid ->
                                        Toast.makeText(MainActivity.this, "Attendance has been marked.", Toast.LENGTH_SHORT).show()
                                ).addOnFailureListener(e ->
                                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show()
                                );

                                documentReference.update(present);
                            } else
                                Toast.makeText(MainActivity.this, "Incorrect Code! Try Again!", Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(MainActivity.this, "You cannot mark attendance. Contact your teacher.", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                    });
        }
    }

    public void logout(View view) {
        UserDetails userDetails = (UserDetails) getApplicationContext();
        userDetails.setUid(null);
        Toast.makeText(userDetails, "Logged Out!", Toast.LENGTH_SHORT).show();
        Intent login = new Intent(MainActivity.this, Login.class);
        finish();
        startActivity(login);
    }

}
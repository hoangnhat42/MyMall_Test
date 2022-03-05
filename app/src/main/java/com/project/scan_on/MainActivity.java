package com.project.scan_on;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.androidstudy.networkmanager.Monitor;
import com.androidstudy.networkmanager.Tovuti;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.project.scan_on.Helper.BaseActivity;

import java.util.Locale;

public class MainActivity extends BaseActivity {

    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
//        SystemClock.sleep(3000);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser== null){
            Intent registerIntent =new Intent(MainActivity.this,PhoneLoginActivity.class);
            startActivity(registerIntent);
            finish();

        }else {

            Tovuti.from(MainActivity.this).monitor(new Monitor.ConnectivityListener(){
                @Override
                public void onConnectivityChanged(int connectionType, boolean isConnected, boolean isFast){
                    // TODO: Handle the connection...
                    if (isConnected){

                        FirebaseFirestore.getInstance().collection("USERS").document(currentUser.getUid()).update("Last seen", FieldValue.serverTimestamp())
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){

                                                   Intent main2Intent =new Intent(MainActivity.this,Main2Activity.class);
                                                   startActivity(main2Intent);
                                                   finish();



                                        }else {
                                            String error = task.getException().getMessage();
                                            Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }else {
                        Toast.makeText(MainActivity.this, "No internet Connection.", Toast.LENGTH_SHORT).show();
                        DBqueries.clearData();
                        Intent registerIntent =new Intent(MainActivity.this,PhoneLoginActivity.class);
                        startActivity(registerIntent);
                        finish();
                    }

                }
            });

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Tovuti.from(this).stop();
    }
}

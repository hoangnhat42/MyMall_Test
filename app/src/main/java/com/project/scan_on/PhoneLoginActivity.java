package com.project.scan_on;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.project.scan_on.Helper.BaseActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dmax.dialog.SpotsDialog;


public class PhoneLoginActivity extends BaseActivity {

    AlertDialog alertDialog = null;
    ImageView skipview;
    MaterialButton singInButton;
    private List<AuthUI.IdpConfig> providers;
    private static final int REQUEST_CODE = 7171;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    FirebaseFirestore database;
    FirebaseUser userr;
    public static boolean disableCloseBtn = false;
    int PERMISSION_ID = 44;
    FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        skipview = findViewById(R.id.skipbtnsignin);
        singInButton = findViewById(R.id.signinmaterialbtn);
        database = FirebaseFirestore.getInstance();
        List<String> blacklistcountry = new ArrayList<>();
        blacklistcountry.add("+91");
        blacklistcountry.add("us");

        userr = firebaseAuth.getInstance().getCurrentUser();

        providers = Arrays.asList(new AuthUI.IdpConfig.PhoneBuilder().setDefaultCountryIso("in").setWhitelistedCountries(blacklistcountry).build());


        skipview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                main2Intent();
            }
        });


        singInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers).setTheme(R.style.phoneVerificationTheme)
                        .build(), REQUEST_CODE);

            }
        });


    }



    private void main2Intent(){

        if (disableCloseBtn){
            disableCloseBtn = false;
        }else {
            Intent main2Intent = new Intent(PhoneLoginActivity.this, Main2Activity.class);
            startActivity(main2Intent);
        }
        finish();
        overridePendingTransition(R.anim.slide_from_right,R.anim.slideout_from_left);
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (authStateListener !=null){
            //   firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //firebaseAuth.addAuthStateListener(authStateListener);
        if (userr != null){
            main2Intent();
        }

    }


    private void addusertofirebase(FirebaseUser user) {
        final AlertDialog waitingDialog = new SpotsDialog(this);
        waitingDialog.show();
        waitingDialog.setMessage("Please wait...");
        waitingDialog.setCancelable(false);
        //check firebase user

        ////////////////////////  firestore code  //////////////////

        DocumentReference docRef = FirebaseFirestore.getInstance().collection("USERS").document(user.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // doc exist
                        waitingDialog.hide();
                        main2Intent();
                        Toast.makeText(PhoneLoginActivity.this, "Welcome Back! Signed In successfully", Toast.LENGTH_SHORT).show();

                    } else {

                        FirebaseInstanceId.getInstance().getInstanceId()
                                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                        if (!task.isSuccessful()) {
                                            Toast.makeText(PhoneLoginActivity.this, "Something Went Wrong please try later", Toast.LENGTH_SHORT).show();
                                        }

                                        // Get new Instance ID token
                                        String token = task.getResult().getToken()!=null?task.getResult().getToken():"";


                                        // no such document /////////////////////
                                        Map<String, Object> userdata = new HashMap<>();
                                        userdata.put("fullname", "");
                                        userdata.put("email", user.getPhoneNumber());
                                        userdata.put("profile", "");
                                        userdata.put("phoneupdate",new Date());
                                        userdata.put("registered_date", FieldValue.serverTimestamp());
                                        userdata.put("money",0);
                                        userdata.put("no_of_orders",0);
                                        userdata.put("thisweekmoney",0);
                                        userdata.put("thimonthmoney",0);
                                        userdata.put("ismobileactivated",false);
                                        userdata.put("deviceTocken",token.toString());
                                        userdata.put("activatedmobileimei","");


            FirebaseFirestore.getInstance().collection("USERS").document(user.getUid())
                                                .set(userdata)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {

                                                            CollectionReference userDataReference = FirebaseFirestore.getInstance().collection("USERS").document(user.getUid()).collection("USER_DATA");

                                                            /////////MAPS START
                                                            Map<String, Object> wishlistMap = new HashMap<>();
                                                            wishlistMap.put("list_size", (long) 0);

                                                            Map<String, Object> ratingsMap = new HashMap<>();
                                                            ratingsMap.put("list_size", (long) 0);

                                                            Map<String, Object> cartMap = new HashMap<>();
                                                            cartMap.put("list_size", (long) 0);

                                                            Map<String, Object> myAddressMap = new HashMap<>();
                                                            myAddressMap.put("list_size", (long) 0);

                                                            Map<String, Object> notificationsMap = new HashMap<>();
                                                            notificationsMap.put("list_size", (long) 0);

                                                            ///////MAPS END

                                                            final List<String> documentNames = new ArrayList<>();
                                                            documentNames.add("MY_WISHLIST");
                                                            documentNames.add("MY_RATINGS");
                                                            documentNames.add("MY_CART");
                                                            documentNames.add("MY_ADDRESSES");
                                                            documentNames.add("MY_NOTIFICATIONS");


                                                            List<Map<String, Object>> documentFields = new ArrayList<>();
                                                            documentFields.add(wishlistMap);
                                                            documentFields.add(ratingsMap);
                                                            documentFields.add(cartMap);
                                                            documentFields.add(myAddressMap);
                                                            documentFields.add(notificationsMap);


                                                            for (int x = 0; x < documentNames.size(); x++) {

                                                                final int finalX = x;
                                                                userDataReference.document(documentNames.get(x))
                                                                        .set(documentFields.get(x)).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()) {
                                                                            if (finalX == documentNames.size() - 1) {
                                                                                waitingDialog.dismiss();
// here

                                                                                getLastLocation();
                                                                                Toast.makeText(PhoneLoginActivity.this, "Signed In successfully", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        } else {
                                                                            waitingDialog.hide();
                                                                            String error = task.getException().getMessage();
                                                                            Toast.makeText(PhoneLoginActivity.this, error, Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    }
                                                                });

                                                            }
                                                        } else {
                                                            String error = task.getException().getMessage();
                                                            Toast.makeText(PhoneLoginActivity.this, error, Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                    }
                });
                        ////////////////////////////////////////
                    }
                } else {
                    /// faild with execption   Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });


    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.getLastLocation().addOnCompleteListener(
                        new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                Location location = task.getResult();
                                if (location == null) {
                                    requestNewLocationData();
                                } else {
                                 PrefManager prefManager = new PrefManager(PhoneLoginActivity.this);
                                    prefManager.setGeoPoint(new GeoPoint(location.getLatitude(),location.getLongitude()));


                                    main2Intent();

                                    //    latTextView.setText(location.getLatitude()+"");
                                 //   lonTextView.setText(location.getLongitude()+"");

                                }
                            }
                        }
                );
            } else {

                if (!isLocationEnabled()) {

                    alertDialog = new AlertDialog.Builder(PhoneLoginActivity.this, R.style.alerttheme).setTitle("GPS Request").setMessage("GPS is required for this application, please enable GPS")
                            .setPositiveButton("yes", ((dialogInterface, i) -> {
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(intent);
                            })).setCancelable(false).show();

                }
            }
        } else {
            requestPermissionss();
        }


    }


    @SuppressLint("MissingPermission")
    private void requestNewLocationData(){

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(0);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
        );

    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();

            PrefManager prefManager = new PrefManager(PhoneLoginActivity.this);
            prefManager.setGeoPoint(new GeoPoint(mLastLocation.getLatitude(),mLastLocation.getLongitude()));

            main2Intent();

        }
    };

    private void requestPermissionss() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_ID
        );
    }
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }else {
                getLastLocation();
            }
        }
    }
    private boolean checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }
    @Override
    public void onResume(){
        super.onResume();
        if (userr!=null) {
            if (checkPermissions()) {
                getLastLocation();
            }
            if (isLocationEnabled()) {
                if (checkPermissions()) {
                    if (alertDialog != null) {
                        if (alertDialog.isShowing()) {
                            alertDialog.dismiss();
                        }
                    }
                }

            }
        }

    }



    ///////////////////////   firestore code //////////////////


    // ok
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE)
        {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK){
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                addusertofirebase(user);


            }else {
                Toast.makeText(this, "Failed To Sign In", Toast.LENGTH_SHORT).show();
            }
        }
    }



}
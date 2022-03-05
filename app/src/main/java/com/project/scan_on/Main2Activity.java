package com.project.scan_on;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.GeoPoint;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.project.scan_on.Helper.BaseActivity;
import com.project.scan_on.policy.PrivacyPolicyFrag;
import com.project.scan_on.policy.TermsAndConditionFragment;
import com.project.scan_on.policy.WarrantyFragment;
import com.project.scan_on.ui.home.HomeFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import belka.us.androidtoggleswitch.widgets.ToggleSwitch;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.project.scan_on.Helper.LocaleManager.LANGUAGE_ENGLISH;
import static com.project.scan_on.Helper.LocaleManager.LANGUAGE_HINDI;
import static com.project.scan_on.RegisterActivity.setSignUpFragment;

public class Main2Activity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener, PermissionListener {
    private static final int HOME_FRAGMENT = 0;
    private static final int CART_FRAGMENT = 1;
    private static final int ORDERS_FRAGMENT = 2;
    private static final int WISHLIST_FRAGMENT = 3;
    private static final int PLAY_SERVICES_ERROR_CODE = 12121;
    private static final int REWARDS_FRAGMENT = 4;
    private static final int ACCOUNT_FRAGMENT = 5;
    Dialog alertDialog = null;
    public static boolean showCart = false;
    public static Activity main2Activity;
    public static boolean resetMain2Activity = false;
    private ToggleSwitch language_switch;
    int PERMISSION_ID = 44;
    private FrameLayout frameLayout;
    private ImageView actionBarLogo;
    private int currentFragment = -1;
    public static NavigationView navigationView;
    private Window window;
    private Toolbar toolbar;
    private Dialog signInDialog;
    private FirebaseUser currentuser;
    private TextView badgeCount;
    private int scrollFlags;
    private AppBarLayout.LayoutParams params;
    private CircleImageView profileView;
    private TextView fullname, email;
    private ImageView addProfileIcon;
    public static DrawerLayout drawer;
    private boolean requestforLocationpermission = false;
    FusedLocationProviderClient mFusedLocationClient;
    Snackbar snackbar;
    Dialog disableLocationdialog;
    private PrefManager prefManager;

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        actionBarLogo = findViewById(R.id.actionbar_logo);
        setSupportActionBar(toolbar);
        window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);


        //thread.start();


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        params = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
        scrollFlags = params.getScrollFlags();
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        prefManager = new PrefManager(this);
        alertDialog = new Dialog(Main2Activity.this);
        alertDialog.setTitle("GPS Required");
        alertDialog.setContentView(R.layout.alertdialogforlocation);
        Button btn = alertDialog.findViewById(R.id.okbtn);
        alertDialog.setCancelable(false);

        // disable location dialog


        disableLocationdialog = new Dialog(Main2Activity.this);
        disableLocationdialog.setTitle("Location permission Denied");
        disableLocationdialog.setContentView(R.layout.alertdialogfordisablelocation);
        Button appsettingbtn = disableLocationdialog.findViewById(R.id.scanonsetting);
        disableLocationdialog.setCancelable(false);

        snackbar = Snackbar.make(drawer, getResources().getString(R.string.message_no_locaion_permission_snackbar), Snackbar.LENGTH_LONG);
        snackbar.setDuration(BaseTransientBottomBar.LENGTH_INDEFINITE);







        appsettingbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
                requestforLocationpermission = true;
                disableLocationdialog.dismiss();
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        });


        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);

            }
        });

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            getLastLocation();
        }


        profileView = navigationView.getHeaderView(0).findViewById(R.id.main2_profile_image);
        fullname = navigationView.getHeaderView(0).findViewById(R.id.main2_fullname);
        email = navigationView.getHeaderView(0).findViewById(R.id.main2_email);
        addProfileIcon = navigationView.getHeaderView(0).findViewById(R.id.add_profile_icon);
        language_switch = navigationView.getHeaderView(0).findViewById(R.id.languageswitch);


        if (Locale.getDefault().getLanguage().equals(LANGUAGE_HINDI)) {
            language_switch.setCheckedTogglePosition(0);
        } else {
            language_switch.setCheckedTogglePosition(1);
        }


        Timer timer = new Timer();
        language_switch.setOnToggleSwitchChangeListener(new ToggleSwitch.OnToggleSwitchChangeListener() {
            @Override
            public void onToggleSwitchChangeListener(int position, boolean isChecked) {
                if (position == 1) {

                    timer.schedule(
                            new TimerTask() {
                                @Override
                                public void run() {
                                    // your code here
                                    setNewLocale(LANGUAGE_ENGLISH, false);

                                    timer.cancel();
                                }
                            },
                            500
                    );

                } else {


                    timer.schedule(
                            new TimerTask() {
                                @Override
                                public void run() {
                                    // your code here

                                    setNewLocale(LANGUAGE_HINDI, false);
                                    timer.cancel();
                                }
                            },
                            500
                    );


                }
            }
        });


        signInDialog = new Dialog(Main2Activity.this);
        signInDialog.setContentView(R.layout.sign_in_dialog);
        signInDialog.setCancelable(true);
        signInDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        Button dialogSignInBtn = signInDialog.findViewById(R.id.sign_in_btn);


        if (showCart) {
            //  main2Activity = this;
            drawer.setDrawerLockMode(1);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            frameLayout = findViewById(R.id.main2_framelayout);
            gotoFragment("My Cart", new MyCartFragment(), -2);
        } else {

            navigationView.setNavigationItemSelectedListener(this);
            navigationView.getMenu().getItem(0).setChecked(true);

            frameLayout = findViewById(R.id.main2_framelayout);

            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();
            setFragment(new HomeFragment(), HOME_FRAGMENT);

        }


        final Intent registerIntent = new Intent(Main2Activity.this, PhoneLoginActivity.class);

        dialogSignInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignInFragment.disableCloseBtn = true;
                SignUpFragment.disableCloseBtn = true;
                signInDialog.dismiss();
                setSignUpFragment = false;
                startActivity(registerIntent);
            }
        });


    }


    @Override
    protected void onStart() {
        super.onStart();
        currentuser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentuser == null) {
            navigationView.getMenu().getItem(navigationView.getMenu().size() - 1).setEnabled(false);
        } else {
            if (DBqueries.email == null) {
                FirebaseFirestore.getInstance().collection("USERS").document(currentuser.getUid())
                        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DBqueries.fullname = task.getResult().getString("fullname");
                            DBqueries.email = task.getResult().getString("email");
                            DBqueries.profile = task.getResult().getString("profile");

                            PrefManager prefManager = new PrefManager(Main2Activity.this);
                            if (task.getResult().getDate("Last seen") != null) {
                                prefManager.setDate(task.getResult().getDate("Last seen").getTime());

                            }


                            fullname.setText(DBqueries.fullname);
                            email.setText(DBqueries.email);
                            if (DBqueries.profile.equals("")) {
                                addProfileIcon.setVisibility(View.VISIBLE);
                            } else {
                                addProfileIcon.setVisibility(View.INVISIBLE);
                                Glide.with(Main2Activity.this).load(DBqueries.profile).apply(new RequestOptions().placeholder(R.mipmap.placeholder_big)).into(profileView);
                            }
                        } else {
                            String error = task.getException().getMessage();
                            Toast.makeText(Main2Activity.this, error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                fullname.setText(DBqueries.fullname);
                email.setText(DBqueries.email);
                if (DBqueries.profile.equals("")) {
                    profileView.setImageResource(R.mipmap.placeholder_big);
                    addProfileIcon.setVisibility(View.VISIBLE);
                } else {
                    addProfileIcon.setVisibility(View.INVISIBLE);
                    Glide.with(Main2Activity.this).load(DBqueries.profile).apply(new RequestOptions().placeholder(R.mipmap.placeholder_big)).into(profileView);
                }
            }
            navigationView.getMenu().getItem(navigationView.getMenu().size() - 1).setEnabled(true);

        }
        if (resetMain2Activity) {
            resetMain2Activity = false;
            actionBarLogo.setVisibility(View.VISIBLE);
            setFragment(new HomeFragment(), HOME_FRAGMENT);
            navigationView.getMenu().getItem(0).setChecked(true);
        }
        invalidateOptionsMenu();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (currentuser != null) {
            DBqueries.checkNotifications(true, null);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (currentFragment == HOME_FRAGMENT) {
                currentFragment = -1;
                super.onBackPressed();
            } else {
                if (showCart) {
                    main2Activity = null;
                    showCart = false;
                    finish();
                } else {
                    actionBarLogo.setVisibility(View.VISIBLE);
                    invalidateOptionsMenu();
                    setFragment(new HomeFragment(), HOME_FRAGMENT);
                    navigationView.getMenu().getItem(0).setChecked(true);
                }
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (currentFragment == HOME_FRAGMENT) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getMenuInflater().inflate(R.menu.main2, menu);

            MenuItem cartItem = menu.findItem(R.id.main2_cart_icon);
            cartItem.setActionView(R.layout.badge_layout);
            ImageView badgeIcon = cartItem.getActionView().findViewById(R.id.badge_icon);
            badgeIcon.setImageResource(R.mipmap.cart_white);
            badgeCount = cartItem.getActionView().findViewById(R.id.badge_count);
            if (currentuser != null) {
                if (DBqueries.cartList.size() == 0) {
                    DBqueries.loadCartList(Main2Activity.this, new Dialog(Main2Activity.this), false, badgeCount, new TextView(Main2Activity.this));
                } else {
                    badgeCount.setVisibility(View.VISIBLE);
                    if (DBqueries.cartList.size() < 99) {
                        badgeCount.setText(String.valueOf(DBqueries.cartList.size()));
                    } else {
                        badgeCount.setText("99");
                    }
                }
            }

            cartItem.getActionView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (currentuser == null) {
                        signInDialog.show();
                    } else {
                        gotoFragment("My Cart", new MyCartFragment(), CART_FRAGMENT);
                    }
                }
            });

            MenuItem notifyItem = menu.findItem(R.id.main2_notification_icon);
            notifyItem.setActionView(R.layout.badge_layout);
            ImageView notifyIcon = notifyItem.getActionView().findViewById(R.id.badge_icon);
            notifyIcon.setImageResource(R.mipmap.bell);
            TextView notifyCount = notifyItem.getActionView().findViewById(R.id.badge_count);
            if (currentuser != null) {
                DBqueries.checkNotifications(false, notifyCount);
            }

            notifyItem.getActionView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent notificationIntent = new Intent(Main2Activity.this, NotificationActivity.class);
                    startActivity(notificationIntent);
                }
            });

        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.main2_search_icon) {
            if (currentuser == null) {
                signInDialog.show();
            } else {
                Intent searchIntent = new Intent(this, SearchActivity.class);
                startActivity(searchIntent);
            }
            return true;
        } else if (id == R.id.main2_notification_icon) {
            if (currentuser == null) {
                signInDialog.show();
            } else {
                Intent notificationIntent = new Intent(this, NotificationActivity.class);
                startActivity(notificationIntent);
            }
            return true;
        } else if (id == R.id.main2_cart_icon) {
            if (currentuser == null) {
                signInDialog.show();
            } else {
                gotoFragment("My Cart", new MyCartFragment(), CART_FRAGMENT);
            }
            return true;
        } else if (id == android.R.id.home) {
            if (showCart) {
                main2Activity = null;
                showCart = false;
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void gotoFragment(String title, Fragment fragment, int fragmentNo) {
        actionBarLogo.setVisibility(View.GONE);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(title);
        invalidateOptionsMenu();
        setFragment(fragment, fragmentNo);
        if (fragmentNo == CART_FRAGMENT || showCart) {
            navigationView.getMenu().getItem(7).setChecked(true);
            params.setScrollFlags(0);
        } else {
            params.setScrollFlags(scrollFlags);
        }
    }

    MenuItem menuItem;

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        menuItem = item;

            drawer.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
                @Override
                public void onDrawerClosed(View drawerView) {
                    super.onDrawerClosed(drawerView);
                    int id = menuItem.getItemId();

                        if (id == R.id.nav_boomshopy) {
                            actionBarLogo.setVisibility(View.VISIBLE);
                            invalidateOptionsMenu();
                            setFragment(new HomeFragment(), HOME_FRAGMENT);
                              return;
                        }
                    else if (id == R.id.privacy) {
                        gotoFragment("Privacy Policy", new PrivacyPolicyFrag(), 18);
                        return;

                    } else if (id == R.id.terms) {
                        gotoFragment("Terms & Conditions", new TermsAndConditionFragment(), 20);
                        return;

                    } else if (id == R.id.warranty) {
                        gotoFragment("Warranty Policy", new WarrantyFragment(), 25);
                        return;
                    }


                        if (currentuser != null) {

                        if (id == R.id.nav_recharge) {
                            Intent rechargeIntent = new Intent(Main2Activity.this, RechargeActivity.class);
                            startActivity(rechargeIntent);
                            return;
                        } else if (id == R.id.nav_my_orders) {
                            gotoFragment("My orders", new MyOrdersFragment(), ORDERS_FRAGMENT);
                            return;
                        } else if (id == R.id.nav_my_rewards) {
                            gotoFragment("My Rewards", new MyRewardsFragment(), REWARDS_FRAGMENT);
                            return;
                        } else if (id == R.id.nav_my_cart) {
                            gotoFragment("My Cart", new MyCartFragment(), CART_FRAGMENT);
                            return;
                        } else if (id == R.id.nav_my_wishlist) {
                            gotoFragment("My Wishlist", new MyWishlistFragment(), WISHLIST_FRAGMENT);
                            return;
                        } else if (id == R.id.nav_my_account) {
                            gotoFragment("My Account", new MyAccountFragment(), ACCOUNT_FRAGMENT);
                            return;
                        } else if (id == R.id.audioorder) {
                            Intent registerIntent = new Intent(Main2Activity.this, RecordActivity.class);
                            startActivity(registerIntent);
                            return;

                        }
                           else if (id == R.id.nav_sign_out) {
                                FirebaseAuth.getInstance().signOut();
                                DBqueries.clearData();
                                Intent registerIntent = new Intent(Main2Activity.this, PhoneLoginActivity.class);
                                startActivity(registerIntent);
                                finish();
                            }


                        }else{
                          signInDialog.show();
                          return;
                    }





                    drawer.removeDrawerListener(this);
                }
            });
            return true;

    }

    private void setFragment(Fragment fragment, int fragmentNo) {
        if (fragmentNo != currentFragment) {
            if (fragmentNo == REWARDS_FRAGMENT) {
                window.setStatusBarColor(Color.parseColor("#5B04B1"));
                toolbar.setBackgroundColor(Color.parseColor("#5B04B1"));
            } else {
                window.setStatusBarColor(getResources().getColor(R.color.colorPrimary));
                toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            }
            currentFragment = fragmentNo;
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fede_out);
            fragmentTransaction.replace(frameLayout.getId(), fragment);
            fragmentTransaction.commit();
        }

    }

    private boolean setNewLocale(String language, boolean restartProcess) {
        App.localeManager.setNewLocale(this, language);
        Intent i = new Intent(this, Main2Activity.class);
        startActivity(i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));

        if (restartProcess) {
            System.exit(0);
        } else {
            // Toast.makeText(this, "Activity restarted", Toast.LENGTH_SHORT).show();
        }
        return true;
    }




    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        if (isServiceOk()) {
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
                                        //    Toast.makeText(Main2Activity.this, "Latitude: "+location.getLatitude() +"Longitede: "+ location.getLongitude(), Toast.LENGTH_SHORT).show();

                                        prefManager.setGeoPoint(new GeoPoint(location.getLatitude(), location.getLongitude()));
                                        //    main2Intent();
                                        //    latTextView.setText(location.getLatitude()+"");
                                        //   lonTextView.setText(location.getLongitude()+"");

                                    }
                                }
                            }
                    );
                } else {
                    if (!isLocationEnabled()) {
                        alertDialog.show();
                    }
                }
            } else {
                requestPermissionss();
            }
        }else {
            Toast.makeText(Main2Activity.this, "Google Services is not available Maps and location will not be work.", Toast.LENGTH_LONG).show();
        }

    }


    private boolean isServiceOk() {
        GoogleApiAvailability googleApi = GoogleApiAvailability.getInstance();
        int result = googleApi.isGooglePlayServicesAvailable(Main2Activity.this);
        // result 3 types ka ho sakta he 1. google play services available , 2 . user can update google play or on google play services . 3. user not have google play and cannot do anything
        if (result == ConnectionResult.SUCCESS) {
            return true;
        } else if (googleApi.isUserResolvableError(result)) {   // agr resolve kr ske error ko to true nhi to false
            Dialog dialog = googleApi.getErrorDialog(this, result, PLAY_SERVICES_ERROR_CODE, task ->{

            });
            dialog.show();
        } else {
            Toast.makeText(this, "google play services are required for this application", Toast.LENGTH_SHORT).show();
        }
        return false;
    }


    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {

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
         //   Toast.makeText(Main2Activity.this, "Latitude: "+mLastLocation.getLatitude() +"Longitede: "+ mLastLocation.getLongitude(), Toast.LENGTH_SHORT).show();
            prefManager.setGeoPoint(new GeoPoint(mLastLocation.getLatitude(),mLastLocation.getLongitude()));
            // main2Intent();

        }
    };


    private void requestPermissionss() {
        Dexter.withActivity(Main2Activity.this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(this)
                .check();


    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
    }



    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                return true;
            }else{
                return false;
            }

        }else {
            return true;
        }

    }

    @Override
    public void onResume() {
        super.onResume();



        if (currentuser!=null){
            if (checkPermissions()) {
                if (isLocationEnabled()) {
                    if (alertDialog != null) {
                        alertDialog.dismiss();


                    }


                }
            }

            if (checkPermissions()) {
                disableLocationdialog.dismiss();
                snackbar.dismiss();
                getLastLocation();

            } else {
                //     getLastLocation();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    if (requestforLocationpermission){
                        requestforLocationpermission =false;
                        getLastLocation();
                    }
                }
            }
        }
    }

    @Override
    public void onPermissionGranted(PermissionGrantedResponse response) {
        getLastLocation();
    }
    @Override
    public void onPermissionDenied(PermissionDeniedResponse response) {

        if (response.isPermanentlyDenied()) {
            // navigate user to app settings
            disableLocationdialog.show();
            snackbar.show();
        }else {
            androidx.appcompat.app.AlertDialog.Builder alertDialogBuilder = new androidx.appcompat.app.AlertDialog.Builder(this, R.style.alerttheme);
            alertDialogBuilder.setMessage("Location Permission is required for this application to work propperly.");
            alertDialogBuilder.setIcon(R.mipmap.ic_launcher_round);
            alertDialogBuilder.setCancelable(false);
            alertDialogBuilder.setPositiveButton(getResources().getString(R.string.allow),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            Dexter.withActivity(Main2Activity.this)
                                    .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                                    .withListener(Main2Activity.this)
                                    .check();
                        }
                    });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

        }


    }

    @Override
    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
        token.continuePermissionRequest();
    }
}

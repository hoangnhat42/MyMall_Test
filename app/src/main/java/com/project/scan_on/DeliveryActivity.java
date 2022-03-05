package com.project.scan_on;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.github.javiersantos.bottomdialogs.BottomDialog;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.google.type.LatLng;
import com.project.scan_on.Helper.BaseActivity;
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;
import com.project.scan_on.Helper.BaseActivity;
import com.project.scan_on.Helper.MyUploadService;
import com.shreyaspatil.EasyUpiPayment.EasyUpiPayment;
import com.shreyaspatil.EasyUpiPayment.listener.PaymentStatusListener;
import com.shreyaspatil.EasyUpiPayment.model.PaymentApp;
import com.shreyaspatil.EasyUpiPayment.model.TransactionDetails;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import me.jagar.chatvoiceplayerlibrary.VoicePlayerView;

import static com.project.scan_on.Helper.Utility.paymentmessog;
import static com.project.scan_on.Helper.Utility.yourBusinessname;
import static com.project.scan_on.Helper.Utility.yourupid;
public class DeliveryActivity extends BaseActivity implements View.OnClickListener,PaymentStatusListener,CartAdapter.TotalAmountinterface {

    private static final String TAG = "mytag";
    public static List<CartItemModel> cartItemModelList;
    private RecyclerView deliveryRecyclerView;
    public static CartAdapter cartAdapter;
    private Button changeOrAddNewAddressBtn;
    public static final int SELECT_ADDRESS = 0;
    private TextView totalAmount;
    private TextView fullname;
    private String name, mobileNo;
    private String adminPhone = "9555136451";
    private TextView fullAddress;
    private TextView pincode;
    private Button continueBtn;
    public static Dialog loadingDialog;
    private Dialog paymentMethodDialog;
    private ImageButton paytm, cod;
    private String paymentMethod = "PAYTM";
    private ConstraintLayout orderConfirmationLayout;
    private ImageButton continueShoppingBtn;
    private TextView orderId;
    private boolean continuecart;

    private boolean successResponse = false;
    public static boolean fromCart;
    private String order_id;
    public static boolean codOrderConfirmed = false;
    private MaterialCardTranslation googlepay,phonepe,codcard,paytmcard;
    private MaterialButton placeorder;
    private FirebaseFirestore firebaseFirestore;
    public static boolean getQtyIDs = true;
    private LinearLayout allmethodscards;
    private BottomDialog dialog;
    ImageView prescription;
    Button  skiporContinuebtn;
    private BroadcastReceiver mBroadcastReceiver;
    private Uri mFileUri;
    private Uri mDownloadUrl = null;
    TextView downloadurl;
    PrefManager prefManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        downloadurl = findViewById(R.id.downloadurl);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Delivery");
        prefManager = new PrefManager(this);

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "onReceive:" + intent);
                loadingDialog.dismiss();

                switch (intent.getAction()) {
                    case MyUploadService.UPLOAD_COMPLETED:
                    {
                        mFileUri = intent.getParcelableExtra(MyUploadService.EXTRA_FILE_URI);
                        Toast.makeText(context, "Images Uplaoded", Toast.LENGTH_SHORT).show();

                    }
                    case MyUploadService.UPLOAD_ERROR:

                        Toast.makeText(context, "Something went wrong! try later", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };


        deliveryRecyclerView = findViewById(R.id.delivery_recyclerview);
        changeOrAddNewAddressBtn = findViewById(R.id.change_or_add_address_btn);
        totalAmount = findViewById(R.id.total_cart_amount);
        fullname = findViewById(R.id.address_fullname);
        fullAddress = findViewById(R.id.address);
        pincode = findViewById(R.id.pincode);
        continueBtn = findViewById(R.id.cart_continue_btn);
        orderConfirmationLayout = findViewById(R.id.order_confirmation_layout);
        continueShoppingBtn = findViewById(R.id.continue_shopping_btn);
        orderId = findViewById(R.id.order_id);

        //////////////loading Dialog Start
        loadingDialog = new Dialog(DeliveryActivity.this);
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loadingDialog.setContentView(R.layout.loading_progress_dialog);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.slider_background));
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        ////////////////loading Dialog End






        //////////////payment Dialog Start
        paymentMethodDialog = new Dialog(DeliveryActivity.this);
        paymentMethodDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        paymentMethodDialog.setContentView(R.layout.payment_method_dialog);
        paymentMethodDialog.setCancelable(true);
        paymentMethodDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.slider_background));
        paymentMethodDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);


        googlepay =  paymentMethodDialog.findViewById(R.id.pickanddrop);
        phonepe =  paymentMethodDialog.findViewById(R.id.branchvisit);
        codcard =  paymentMethodDialog.findViewById(R.id.cod);
        paytmcard =  paymentMethodDialog.findViewById(R.id.courier);
        placeorder = paymentMethodDialog.findViewById(R.id.placeorder);
        allmethodscards = paymentMethodDialog.findViewById(R.id.allmethodscards);

        placeorder.setEnabled(false);
        placeorder.setAlpha(0.6f);

        ////////////////payment Dialog End
        firebaseFirestore = FirebaseFirestore.getInstance();
        getQtyIDs = true;

        googlepay.setOnClickListener(this::onClick);
        phonepe.setOnClickListener(this::onClick);
        paytmcard.setOnClickListener(this::onClick);
        codcard.setOnClickListener(this::onClick);
        placeorder.setOnClickListener(this::onClick);

        order_id = UUID.randomUUID().toString().substring(0, 16);


        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        deliveryRecyclerView.setLayoutManager(layoutManager);

        cartAdapter = new CartAdapter(cartItemModelList, totalAmount, false,this::totalcartamount);
        deliveryRecyclerView.setAdapter(cartAdapter);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.bottomdialog_for_imagepick, null);

        FloatingActionButton gallarybtn =  customView.findViewById(R.id.gallery);
        FloatingActionButton capturebtn =  customView.findViewById(R.id.camera);
        skiporContinuebtn = customView.findViewById(R.id.skip);
        prescription = customView.findViewById(R.id.prescription);
        gallarybtn.setOnClickListener(this::onClick);
        capturebtn.setOnClickListener(this::onClick);
        skiporContinuebtn.setOnClickListener(this::onClick);

        cartAdapter.notifyDataSetChanged();

        changeOrAddNewAddressBtn.setVisibility(View.VISIBLE);
        changeOrAddNewAddressBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getQtyIDs = false;
                Intent myAddressesIntent = new Intent(DeliveryActivity.this, MyAddressesActivity.class);
                myAddressesIntent.putExtra("MODE", SELECT_ADDRESS);
                startActivity(myAddressesIntent);
            }
        });

        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!continuecart){
                    ProductDetailsActivity.open(DeliveryActivity.this,"Minimum Order should be atleast 250 Rupees.");
                    return;
                }

                Boolean allProductsAvailable = true;
                Boolean isitMedicalProduct = false;
              for (CartItemModel cartItemModel : cartItemModelList){
                  if (cartItemModel.isQtyError()){
                      allProductsAvailable = false;
                      break;
                  }
                  if (cartItemModel.getType() == CartItemModel.CART_ITEM) {
                      if (!cartItemModel.isCOD()) {
                          codcard.setVisibility(View.GONE);
                          break;
                      } else {
                          codcard.setVisibility(View.VISIBLE);
                      }
                  }

                  if (cartItemModel.isIsitMedicalProduct()){
                      isitMedicalProduct = true;
                  }

              }
              if (allProductsAvailable){

                       if (!isitMedicalProduct){
                           paymentMethodDialog.show();
                       }else {

                           dialog = new BottomDialog.Builder(DeliveryActivity.this)
                                   .setTitle("Upload Medicine prescription Image")
                                   .setCustomView(customView)
                                   .setCancelable(false)
                                   // You can also show the custom view with some padding in DP (left, top, right, bottom)
                                   //.setCustomView(customView, 20, 20, 20, 0)
                                   .show();

                       }



  //                paymentMethodDialog.show();
              }
            }
        });


    }


    @Override
    protected void onStop() {
        super.onStop();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);

    }

    @Override
    protected void onStart() {
        super.onStart();

        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.registerReceiver(mBroadcastReceiver, MyUploadService.getIntentFilter());
/////////////////accessing quantity start


        if (getQtyIDs) {
            loadingDialog.show();
            for (int x = 0; x < cartItemModelList.size() - 1; x++) {
                for (int y = 0; y < cartItemModelList.get(x).getProductQuantity(); y++) {
                    final String quantityDocumentName = UUID.randomUUID().toString().substring(0, 20);

                    final Map<String, Object> timestamp = new HashMap<>();
                    timestamp.put("time", FieldValue.serverTimestamp());
                    final int finalX = x;
                    final int finalY = y;
                    final int finalX1 = x;
                    final int finalX2 = x;
                    firebaseFirestore.collection("PRODUCTS").document(cartItemModelList.get(x).getProductID()).collection("QUANTITY").document(quantityDocumentName).set(timestamp)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()){
                                        firebaseFirestore.collection("PRODUCTS").document(cartItemModelList.get(finalX2).getProductID()).collection(cartItemModelList.get(finalX2).getProductColor()+"_"+cartItemModelList.get(finalX2).getProductSize()+"_qty").document(quantityDocumentName).set(timestamp)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if (task.isSuccessful()) {


                                                            cartItemModelList.get(finalX).getQtyIDs().add(quantityDocumentName);
                                                            if (finalY + 1 == cartItemModelList.get(finalX).getProductQuantity()) {
                                                                firebaseFirestore.collection("PRODUCTS").document(cartItemModelList.get(finalX).getProductID()).collection("QUANTITY").orderBy("time", Query.Direction.ASCENDING).limit(cartItemModelList.get(finalX).getStockQuantity()).get()
                                                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                                if (task.isSuccessful()) {
                                                                                    List<String> serverQuantity = new ArrayList<>();

                                                                                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                                                                        serverQuantity.add(queryDocumentSnapshot.getId());
                                                                                    }
                                                                                    long availableQty = 0;
                                                                                    boolean noLongerAvailable = true;
                                                                                    for (String qtyId : cartItemModelList.get(finalX).getQtyIDs()) {
                                                                                        cartItemModelList.get(finalX).setQtyError(false);
                                                                                        if (!serverQuantity.contains(qtyId)) {
                                                                                            if (noLongerAvailable) {
                                                                                                cartItemModelList.get(finalX).setInStock(false);
                                                                                            } else {
                                                                                                cartItemModelList.get(finalX).setQtyError(true);
                                                                                                cartItemModelList.get(finalX).setMaxQuantity(availableQty);
                                                                                                Toast.makeText(DeliveryActivity.this, "Sorry ! All products may not be available in required quantity..", Toast.LENGTH_LONG).show();
                                                                                            }
                                                                                        } else {
                                                                                            availableQty++;
                                                                                            noLongerAvailable = false;
                                                                                        }
                                                                                    }
                                                                                    cartAdapter.notifyDataSetChanged();
                                                                                } else {
                                                                                    String error = task.getException().getMessage();
                                                                                    Toast.makeText(DeliveryActivity.this, error, Toast.LENGTH_SHORT).show();
                                                                                }
                                                                                loadingDialog.dismiss();
                                                                            }
                                                                        });
                                                            }


                                                        }}
                                                });


                                    }else {
                                        loadingDialog.dismiss();
                                        String error = task.getException().getMessage();
                                        Toast.makeText(DeliveryActivity.this, error, Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });
                }

            }
        } else {
            getQtyIDs = true;
        }

        /////////////////accessing quantity end

        name = DBqueries.addressesModelList.get(DBqueries.selectedAddress).getName();
        mobileNo = DBqueries.addressesModelList.get(DBqueries.selectedAddress).getMobileNo();
        if (DBqueries.addressesModelList.get(DBqueries.selectedAddress).getAlternateMobileNo().equals("")) {
            fullname.setText(name + " - " + mobileNo);
        }else {
            fullname.setText(name + " - " + mobileNo + " or " + DBqueries.addressesModelList.get(DBqueries.selectedAddress).getAlternateMobileNo());
        }
        String flatNo = DBqueries.addressesModelList.get(DBqueries.selectedAddress).getFlatNo();
        String locality = DBqueries.addressesModelList.get(DBqueries.selectedAddress).getLocality();
        String landmark = DBqueries.addressesModelList.get(DBqueries.selectedAddress).getLandmark();
        String city = DBqueries.addressesModelList.get(DBqueries.selectedAddress).getCity();
        String state = DBqueries.addressesModelList.get(DBqueries.selectedAddress).getState();

        if (landmark.equals("")){
            fullAddress.setText(flatNo +" " + locality +" " + city +" " + state);
        }else {
            fullAddress.setText(flatNo +" " + locality +" " + landmark +" " + city +" " + state);
        }
        pincode.setText(DBqueries.addressesModelList.get(DBqueries.selectedAddress).getPincode());

        if (codOrderConfirmed) {
            showConfirmationLayout();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        loadingDialog.dismiss();

        if (getQtyIDs) {
            for (int x = 0; x < cartItemModelList.size() - 1; x++) {
                if (!successResponse) {
                    for (final String qtyID : cartItemModelList.get(x).getQtyIDs()) {
                        final int finalX = x;
                        final int finalX1 = x;
                        firebaseFirestore.collection("PRODUCTS").document(cartItemModelList.get(x).getProductID()).collection("QUANTITY").document(qtyID).delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        firebaseFirestore.collection("PRODUCTS").document(cartItemModelList.get(finalX1).getProductID()).collection(cartItemModelList.get(finalX1).getProductColor()+"_"+cartItemModelList.get(finalX1).getProductSize()+"_qty").document(qtyID).delete()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                        if (qtyID.equals(cartItemModelList.get(finalX).getQtyIDs().get(cartItemModelList.get(finalX).getQtyIDs().size() - 1))) {
                                            cartItemModelList.get(finalX).getQtyIDs().clear();
                                        }
                                                    }
                                                });
                                    }
                                });
                    }
                } else {
                    cartItemModelList.get(x).getQtyIDs().clear();
                }
            }
        }

    }

    @Override
    public void onBackPressed() {
        if (successResponse) {
            finish();
            return;
        }
        super.onBackPressed();
    }

    private void showConfirmationLayout() {
        successResponse = true;
        codOrderConfirmed = false;
        getQtyIDs = false;
        for (int x = 0; x < cartItemModelList.size() - 1; x++) {

            for (String qtyID : cartItemModelList.get(x).getQtyIDs()) {
                firebaseFirestore.collection("PRODUCTS").document(cartItemModelList.get(x).getProductID()).collection("QUANTITY").document(qtyID).update("user_ID", FirebaseAuth.getInstance().getUid());
                firebaseFirestore.collection("PRODUCTS").document(cartItemModelList.get(x).getProductID()).collection(cartItemModelList.get(x).getProductColor()+"_"+cartItemModelList.get(x).getProductSize()+"_qty").document(qtyID).update("user_ID", FirebaseAuth.getInstance().getUid());

            }
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            Bundle bundle = new Bundle();
            RemoteMessage remoteMessage = new RemoteMessage(bundle);

            MyFirebaseMessaging.sendNotificationAPI26(this, remoteMessage,false,"Scan On","Thank u so much for your Order!");
        }else {
            Bundle bundle = new Bundle();
            RemoteMessage remoteMessage = new RemoteMessage(bundle);
            MyFirebaseMessaging.sendNotification(this, remoteMessage,false,"Scan On","Thank you so much for your Order!");

        }
        if (Main2Activity.main2Activity != null) {
            Main2Activity.main2Activity.finish();
            Main2Activity.main2Activity = null;
            Main2Activity.showCart = false;
        } else {
            Main2Activity.resetMain2Activity = true;
        }
        if (ProductDetailsActivity.productDetailsActivity != null) {
            ProductDetailsActivity.productDetailsActivity.finish();
            ProductDetailsActivity.productDetailsActivity = null;
        }
        /////////////////////////////////sent Confirmation SMS
        String SMS_API = "https://www.fast2sms.com/dev/bulk";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, SMS_API, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                ///////////////nothing
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
         
                ///////////////nothing
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("authorization", "Cjdu5BvlAfPRikJnmU0rpG3K9LXcNEthsMZ8ebTo2DF6wYQaHggxOjY4w5NpZHtm2UcAnhDk3lCbs7Wo");
                return headers;
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> body = new HashMap<>();
                body.put("sender_id", "FSTSMS");
                body.put("language", "english");
                body.put("route", "qt");
                body.put("numbers", adminPhone);
                body.put("message", "29421");
                body.put("variables", "{#DD#}|{#FF#}|{#CC#}|{#BB#}");
                body.put("variables_values", name+"|"+FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()+"|"+order_id+"|Rs."+totalAmount.getText().toString().substring(3, totalAmount.getText().length() - 2)+".00");

                return body;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                6000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));
        RequestQueue requestQueue = Volley.newRequestQueue(DeliveryActivity.this);
        requestQueue.add(stringRequest);

        /////////////////////////////////sent Confirmation SMS End


        if (fromCart) {
            loadingDialog.show();
            Map<String, Object> updateCartList = new HashMap<>();
            long cartListSize = 0;
            final List<Integer> indexList = new ArrayList<>();

            Log.d(TAG, "DBQuerys.cartlist.size():"+DBqueries.cartList.size());
            for (int x = 0; x < DBqueries.cartList.size(); x++) {
                if (!DBqueries.cartItemModelList.get(x).isInStock()) {
                    updateCartList.put("product_ID_" + cartListSize, DBqueries.cartItemModelList.get(x).getProductID());
                    cartListSize++;
                } else {
                    indexList.add(x);
                }
            }

            updateCartList.put("list_size", cartListSize);



            FirebaseFirestore.getInstance().collection("USERS").document(FirebaseAuth.getInstance().getUid()).collection("USER_DATA").document("MY_CART")
                    .set(updateCartList).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Collections.sort(indexList);
                        for (int x = indexList.size() -1; x >=  0; x--) {
                            DBqueries.cartList.remove(indexList.get(x).intValue());
                            DBqueries.cartItemModelList.remove(indexList.get(x).intValue());
                        }
                        DBqueries.cartItemModelList.remove(DBqueries.cartItemModelList.size() - 1);
                    } else {
                        String error = task.getException().getMessage();
                        Toast.makeText(DeliveryActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                    loadingDialog.dismiss();
                }
            });
        }
        continueBtn.setEnabled(false);
        changeOrAddNewAddressBtn.setEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        orderId.setText("Order ID " + order_id);
        orderConfirmationLayout.setVisibility(View.VISIBLE);
        continueShoppingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    private void placeorderDetails(){

        String userID = FirebaseAuth.getInstance().getUid();
        loadingDialog.show();

        // cart item
        /// 1 product in cart  cartlistsize 2 for loop for two time size will be 2
        // loop will run one time  2-1 = 1 size = cartitemmodelsit.size-1
        // 3 product      cartlst 4   = 4-1 = 3 ==3
        int size = 0;
        for (CartItemModel cartItemModel : cartItemModelList) {

            if (cartItemModel.getType() == CartItemModel.CART_ITEM) {
                size++;

                Map<String,Object> orderDetails = new HashMap<>();
                orderDetails.put("ORDER ID",order_id);
                orderDetails.put("Prescriptionimage",downloadurl.getText().toString());
                orderDetails.put("Product_size",cartItemModel.getProductSize());
                orderDetails.put("Product_color",cartItemModel.getProductColor());
                orderDetails.put("Product Id",cartItemModel.getProductID());
                orderDetails.put("Product Image",cartItemModel.getProductImage());
                orderDetails.put("Product Title",cartItemModel.getProductTitle());
                orderDetails.put("User Id",userID);
                orderDetails.put("Product Quantity",cartItemModel.getProductQuantity());
                if (cartItemModel.getCuttedPrice() != null){
                    orderDetails.put("Cutted price",cartItemModel.getCuttedPrice());
                }else {
                    orderDetails.put("Cutted price","");
                }
                orderDetails.put("Product Price",cartItemModel.getProductPrice());
                if (cartItemModel.getSelectedCoupenId() != null) {
                    orderDetails.put("Coupen Id", cartItemModel.getSelectedCoupenId());
                }else {
                    orderDetails.put("Coupen Id", "");
                }
                if (cartItemModel.getDiscountedPrice() != null) {
                    orderDetails.put("Discounted Price", cartItemModel.getDiscountedPrice());
                }else {
                    orderDetails.put("Discounted Price", "");
                }
                orderDetails.put("Ordered date ",FieldValue.serverTimestamp());
                orderDetails.put("Packed date ",FieldValue.serverTimestamp());
                orderDetails.put("Shipped date ",FieldValue.serverTimestamp());
                orderDetails.put("Delivered date ",FieldValue.serverTimestamp());
                orderDetails.put("Cancelled date ",FieldValue.serverTimestamp());
                if (paymentMethod.equals("cod")){
                    orderDetails.put("Order Status","Ordered");
                }else {
                    orderDetails.put("Order Status","Cancelled");
                }
                orderDetails.put("Payment Method",paymentMethod);
                orderDetails.put("Address",fullAddress.getText());
                orderDetails.put("FullName",fullname.getText());
                orderDetails.put("Pincode",pincode.getText());
                orderDetails.put("Free Coupens",cartItemModel.getFreeCoupens());
               // orderDetails.put("Delivery Price",cartItemModelList.get(cartItemModelList.size() - 1).getDeliveryPrice());
                orderDetails.put("Delivery Price",cartItemModel.getDeliveryPrice());

                orderDetails.put("Cancellation requested",false);

                firebaseFirestore.collection("ORDERS").document(order_id).collection("OrderItems").document(cartItemModel.getProductID())
                .set(orderDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()){
                          String error = task.getException().getMessage();
                            Toast.makeText(DeliveryActivity.this, error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                if (size == cartItemModelList.size()-1) {
                    Map<String, Object> orderDetailss = new HashMap<>();
                    orderDetailss.put("Total Items", cartItemModel.getTotalItems());
                    orderDetailss.put("Total Items Price", cartItemModel.getTotalItemPrice());
                    orderDetailss.put("Delivery Price", cartItemModel.getDeliveryPrice());
                    orderDetailss.put("Total Amount", cartItemModel.getTotalAmount());
                    orderDetailss.put("Saved Amount", cartItemModel.getSavedAmount());
                    orderDetailss.put("Payment Status", "not paid");
                    orderDetailss.put("orderdate",FieldValue.serverTimestamp());
                    Gson gson = new Gson();
                    GeoPoint geoPoint= gson.fromJson(prefManager.getGeoPoint(),GeoPoint.class);
                    orderDetailss.put("UserLocation",geoPoint);
                    orderDetailss.put("Order Status", "Ordered");
                    firebaseFirestore.collection("ORDERS").document(order_id)
                            .set(orderDetailss).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                if (paymentMethod.equals("paytm")) {
                                    paytm();
                                } else if (paymentMethod.equals("cod")){
                                    cod();
                                }else if(paymentMethod.equals("phonepe")){
                                  phonepeMethod();
                                }else if (paymentMethod.equals("googlepay")){
                                    googlepayMethod();
                                }
                            } else {
                                String error = task.getException().getMessage();
                                Toast.makeText(DeliveryActivity.this, error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }    }
        }
    }

    private void phonepeMethod() {
        getQtyIDs = false;
        paymentMethodDialog.dismiss();

        EasyUpiPayment mEasyUpiPayment = new EasyUpiPayment.Builder()
                .with(this)
                .setPayeeVpa(yourupid)
                .setPayeeName(yourBusinessname)
                .setTransactionId(FirebaseAuth.getInstance().getCurrentUser().getUid().toString())
                .setTransactionRefId(order_id)
                .setDescription(paymentmessog)
                .setAmount(totalAmount.getText().toString().substring(3, totalAmount.getText().length() - 2)+".00")
                .build();
        mEasyUpiPayment.setPaymentStatusListener(DeliveryActivity.this);
        mEasyUpiPayment.setDefaultPaymentApp(PaymentApp.PHONE_PE);
        mEasyUpiPayment.startPayment();

        // phonepe coding
    }
    private void googlepayMethod() {
        getQtyIDs = false;
        paymentMethodDialog.dismiss();

        EasyUpiPayment mEasyUpiPayment = new EasyUpiPayment.Builder()
                .with(this)
                .setPayeeVpa(yourupid)
                .setPayeeName(yourBusinessname)
                .setTransactionId(FirebaseAuth.getInstance().getCurrentUser().getUid().toString())
                .setTransactionRefId(order_id)
                .setDescription(paymentmessog)
                .setAmount(totalAmount.getText().toString().substring(3, totalAmount.getText().length() - 2)+".00")
                .build();

        mEasyUpiPayment.setPaymentStatusListener(DeliveryActivity.this);
        mEasyUpiPayment.setDefaultPaymentApp(PaymentApp.GOOGLE_PAY);
        mEasyUpiPayment.startPayment();

    }

    private void paytm(){
            getQtyIDs = false;
            paymentMethodDialog.dismiss();
            loadingDialog.show();
            if (ContextCompat.checkSelfPermission(DeliveryActivity.this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(DeliveryActivity.this, new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS}, 101);
            }

            final String M_id = "FvAYXM76778036317633";
            final String customer_id = FirebaseAuth.getInstance().getUid();
            String url = "https://boomshopypaytm.000webhostapp.com/paytm/generateChecksum.php";
            final String callBackUrl = "https://pguat.paytm.com/paytmchecksum/paytmCallback.jsp";

            RequestQueue requestQueue = Volley.newRequestQueue(DeliveryActivity.this);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.has("CHECKSUMHASH")) {
                            String CHECKSUMHASH = jsonObject.getString("CHECKSUMHASH");

                            PaytmPGService paytmPGService = PaytmPGService.getStagingService();
                            HashMap<String, String> paramMap = new HashMap<String, String>();
                            paramMap.put("MID", M_id);
                            paramMap.put("ORDER_ID", order_id);
                            paramMap.put("CUST_ID", customer_id);
                            paramMap.put("CHANNEL_ID", "WAP");
                            paramMap.put("TXN_AMOUNT", totalAmount.getText().toString().substring(3, totalAmount.getText().length() - 2));
                            paramMap.put("WEBSITE", "WEBSTAGING");
                            paramMap.put("INDUSTRY_TYPE_ID", "Retail");
                            paramMap.put("CALLBACK_URL", callBackUrl);
                            paramMap.put("CHECKSUMHASH", CHECKSUMHASH);

                            PaytmOrder order = new PaytmOrder(paramMap);


                            paytmPGService.initialize(order, null);
                            paytmPGService.startPaymentTransaction(DeliveryActivity.this, true, true, new PaytmPaymentTransactionCallback() {
                                @Override
                                public void onTransactionResponse(Bundle inResponse) {
//                                        Toast.makeText(getApplicationContext(), "Payment Transaction response " + inResponse.toString(), Toast.LENGTH_LONG).show();

                                    if (inResponse.getString("STATUS").equals("TXN_SUCCESS")) {

                                            Map<String,Object> updateStatus = new HashMap<>();
                                            updateStatus.put("Payment Status","Paid through Paytm ");
                                            updateStatus.put("Order Status","Ordered");
                                            firebaseFirestore.collection("ORDERS").document(order_id).update(updateStatus)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()){
                                                                Map<String,Object> userOrder = new HashMap<>();
                                                                userOrder.put("order_id",order_id);
                                                                userOrder.put("time",FieldValue.serverTimestamp());
                                                                firebaseFirestore.collection("USERS").document(FirebaseAuth.getInstance().getUid()).collection("USER_ORDERS").document(order_id).set(userOrder)
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if (task.isSuccessful()){
                                                                                    showConfirmationLayout();
                                                                                }else {
                                                                                    Toast.makeText(DeliveryActivity.this, "Failed to update user's orderList", Toast.LENGTH_SHORT).show();
                                                                                }
                                                                            }
                                                                        });
                                                            }else {
                                                                Toast.makeText(DeliveryActivity.this, "Order Cancelled", Toast.LENGTH_LONG).show();
                                                            }
                                                        }
                                                    });

                                    }
                                }

                                @Override
                                public void networkNotAvailable() {
                                    Toast.makeText(getApplicationContext(), "Network connection error: Check your internet connectivity", Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void clientAuthenticationFailed(String inErrorMessage) {
                                    Toast.makeText(getApplicationContext(), "Authentication failed: Server error" + inErrorMessage.toString(), Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void someUIErrorOccurred(String inErrorMessage) {
                                    Toast.makeText(getApplicationContext(), "UI Error " + inErrorMessage, Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void onErrorLoadingWebPage(int iniErrorCode, String inErrorMessage, String inFailingUrl) {
                                    Toast.makeText(getApplicationContext(), "Unable to load webpage " + inErrorMessage.toString(), Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void onBackPressedCancelTransaction() {
                                    Toast.makeText(getApplicationContext(), "Transaction cancelled", Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void onTransactionCancel(String inErrorMessage, Bundle inResponse) {
                                    Toast.makeText(getApplicationContext(), "Transaction cancelled" + inResponse.toString(), Toast.LENGTH_LONG).show();
                                }
                            });

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    loadingDialog.dismiss();
                    Toast.makeText(DeliveryActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> paramMap = new HashMap<String, String>();
                    paramMap.put("MID", M_id);
                    paramMap.put("ORDER_ID", order_id);
                    paramMap.put("CUST_ID", customer_id);
                    paramMap.put("CHANNEL_ID", "WAP");
                    paramMap.put("TXN_AMOUNT", totalAmount.getText().toString().substring(3, totalAmount.getText().length() - 2));
                    paramMap.put("WEBSITE", "WEBSTAGING");
                    paramMap.put("INDUSTRY_TYPE_ID", "Retail");
                    paramMap.put("CALLBACK_URL", callBackUrl);
                    return paramMap;
                }
            };

            requestQueue.add(stringRequest);

    }

    private void cod(){
        getQtyIDs = false;
        paymentMethodDialog.dismiss();
        Map<String, Object> userOrder = new HashMap<>();
                            userOrder.put("order_id", order_id);
                            userOrder.put("time", FieldValue.serverTimestamp());
                            FirebaseFirestore.getInstance().collection("USERS").document(FirebaseAuth.getInstance().getUid()).collection("USER_ORDERS").document(order_id).set(userOrder)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                loadingDialog.dismiss();
                                    showConfirmationLayout();
                                            } else {
                                                Toast.makeText(DeliveryActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            //Image Uri will not be null for RESULT_OK
            Uri fileUri = data.getData();
            if (fileUri != null) {
                prescription.setImageURI(fileUri);
                skiporContinuebtn.setText("Continue");
                mFileUri = fileUri;

            }
        }else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, "Something Went wrong", Toast.LENGTH_SHORT).show();
            skiporContinuebtn.setText("SKIP");


        }
            else {
            skiporContinuebtn.setText("SKIP");

       //     Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.gallery:{
                getQtyIDs = false;
                ImagePicker.Companion.with(DeliveryActivity.this)
                        .compress(500)
                        .galleryOnly()//Final image size will be less than 1 MB(Optional)
                        .start();
                return;
            }
            case R.id.camera:{
                getQtyIDs = false;
                String path = getExternalFilesDir("/").getAbsolutePath();

                ImagePicker.Companion.with(DeliveryActivity.this)
                        .compress(500)
                        .saveDir(path + "/" + "perscription_"+System.currentTimeMillis())
                        .cameraOnly()//Final image size will be less than 1 MB(Optional)
                        .start();
                return;
            }
            case R.id.skip:{

                if (skiporContinuebtn.getText().equals("SKIP")){
                    dialog.dismiss();
                    paymentMethodDialog.show();
                    return;
                }else {
                    if (mFileUri!=null) {
                        uploadFromUri(mFileUri);
                    }
                }



            }

            case R.id.courier:{
                paytmcard.simulateClickElevation (null );
                paytmcard.setStrokeColor(getResources().getColor(R.color.colorPrimary));
                paytmcard.setStrokeWidth(2);

                codcard.setActivated(false);
                paytmcard.setActivated(true);
                googlepay.setActivated(false);
                phonepe.setActivated(false);

                phonepe.setStrokeWidth(1);
                phonepe.setStrokeColor(getResources().getColor(R.color.RecyclerViewBackground));

                googlepay.setStrokeWidth(1);
                googlepay.setStrokeColor(getResources().getColor(R.color.RecyclerViewBackground));

                codcard.setStrokeColor(getResources().getColor(R.color.RecyclerViewBackground));
                codcard.setStrokeWidth(1);
                //   courier.setCheckedIconTint(null);
                placeorder.setAlpha(1f);
                placeorder.setEnabled(true);
                placeorder.setText("Continue");
                Toast.makeText(DeliveryActivity.this, "courier", Toast.LENGTH_SHORT).show();
                return;


            }
            case R.id.cod:{
                codcard.simulateClickElevation (null );
                codcard.setActivated(true);
                phonepe.setActivated(false);
                googlepay.setActivated(false);
                paytmcard.setActivated(false);

                placeorder.setText("Place Order");
                phonepe.setStrokeWidth(1);
                phonepe.setStrokeColor(getResources().getColor(R.color.RecyclerViewBackground));

                googlepay.setStrokeWidth(1);
                googlepay.setStrokeColor(getResources().getColor(R.color.RecyclerViewBackground));

                codcard.setStrokeColor(getResources().getColor(R.color.colorPrimary));
                codcard.setStrokeWidth(2);

                paytmcard.setStrokeWidth(1);
                paytmcard.setStrokeColor(getResources().getColor(R.color.RecyclerViewBackground));

                placeorder.setAlpha(1f);
                placeorder.setEnabled(true);
                return;
            }
            case R.id.branchvisit:{
                                codcard.setActivated(false);
                paytmcard.setActivated(false);
                phonepe.setActivated(true);
                googlepay.setActivated(false);

                phonepe.setStrokeColor(getResources().getColor(R.color.colorPrimary));
                phonepe.simulateClickElevation (null );
                phonepe.setStrokeWidth(2);
                //    shopvisit.setCheckedIconTint(null);
                placeorder.setText("Place Order");

                codcard.setStrokeColor(getResources().getColor(R.color.RecyclerViewBackground));
                codcard.setStrokeWidth(1);

                googlepay.setStrokeWidth(1);
                googlepay.setStrokeColor(getResources().getColor(R.color.RecyclerViewBackground));

                paytmcard.setStrokeWidth(1);
                paytmcard.setStrokeColor(getResources().getColor(R.color.RecyclerViewBackground));
                placeorder.setAlpha(1f);
                placeorder.setEnabled(true);
                return;
            }
            case R.id.pickanddrop:{

                googlepay.simulateClickElevation (null );
                googlepay.setStrokeColor(getResources().getColor(R.color.colorPrimary));
                googlepay.setStrokeWidth(2);
                placeorder.setText("Continue");

                // pickanddrop.setCheckedIconTint(null);

                phonepe.setStrokeColor(getResources().getColor(R.color.RecyclerViewBackground));
                phonepe.setStrokeWidth(1);
                codcard.setStrokeColor(getResources().getColor(R.color.RecyclerViewBackground));
                codcard.setActivated(false);
                phonepe.setActivated(false);
                paytmcard.setActivated(false);
                googlepay.setActivated(true);

                codcard.setStrokeWidth(1);
                paytmcard.setStrokeWidth(1);
                paytmcard.setStrokeColor(getResources().getColor(R.color.RecyclerViewBackground));
                placeorder.setAlpha(1f);
                placeorder.setEnabled(true);
                return;

            }



            case R.id.placeorder:{

                if (placeorder.isEnabled()){
                    for (int x=0;x<allmethodscards.getChildCount(); x++){

                        MaterialCardTranslation materialCardTranslation = (MaterialCardTranslation) allmethodscards.getChildAt(x);
                        if (materialCardTranslation.isActivated()){     // there was ischecked   /////////////////////////////////////////////////////////////////////////////
                            //   Toast.makeText(DeliveryActivity.this, ""+materialCardTranslation.getTag(), Toast.LENGTH_SHORT).show();

                            paymentMethod = materialCardTranslation.getTag().toString();
                            placeorderDetails();

                            break;
                        }
                    }

                }else {
                    Toast.makeText(DeliveryActivity.this, "Please First Choose Payment Method!", Toast.LENGTH_SHORT).show();
                }

                return;
            }



            default:
                throw new IllegalStateException("Unexpected value: " + v.getId());
        }

    }

    private void uploadFromUri(Uri fileUri) {
        Log.d(TAG, "uploadFromUri:src:" + fileUri.toString());
       TextView textView= loadingDialog.findViewById(R.id.textView34);
        textView.setText("Please wait while image getting upload!");
        loadingDialog.show();
        // Save the File URI
        mFileUri = fileUri;
        // Clear the last download, if any
        mDownloadUrl = null;

        StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
        final StorageReference photoRef = mStorageRef.child("prescriptions")
                .child(fileUri.getLastPathSegment());
        photoRef.putFile(fileUri).
                addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    }
                })
                .continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        // Forward any exceptions
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        Log.d(TAG, "uploadFromUri: upload success");

                        // Request the public download URL
                        return photoRef.getDownloadUrl();
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(@NonNull Uri downloadUri) {

                        mDownloadUrl = downloadUri;
                        downloadurl.setText(downloadUri.toString());
                        loadingDialog.dismiss();
                        textView.setText("Loading..");
                        dialog.dismiss();
                        paymentMethodDialog.show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(DeliveryActivity.this, ""+exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


    }


    @Override
    public void onTransactionCompleted(TransactionDetails transactionDetails) {
        // Transaction Completed
        Log.d("TransactionDetails", transactionDetails.toString());
        totalAmount.setText(transactionDetails.toString());
    }

    @Override
    public void onTransactionSuccess() {
        // Payment Success

        loadingDialog.show();
        Map<String,Object> updateStatus = new HashMap<>();
        updateStatus.put("Payment Status","Paid through Paytm ");
        updateStatus.put("Order Status","Ordered");
        firebaseFirestore.collection("ORDERS").document(order_id).update(updateStatus)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Map<String,Object> userOrder = new HashMap<>();
                            userOrder.put("order_id",order_id);
                            userOrder.put("time",FieldValue.serverTimestamp());
                            firebaseFirestore.collection("USERS").document(FirebaseAuth.getInstance().getUid()).collection("USER_ORDERS").document(order_id).set(userOrder)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                loadingDialog.dismiss();
                                                showConfirmationLayout();
                                            }else {
                                                Toast.makeText(DeliveryActivity.this, "Failed to update user's orderList", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }else {
                            Toast.makeText(DeliveryActivity.this, "Order Cancelled", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    //    Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onTransactionSubmitted() {
        // Payment Pending
        loadingDialog.dismiss();
   //     paymentMethodDialog.show();
        Toast.makeText(this, "Pending | Submitted", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTransactionFailed() {
        // Payment Failed
        loadingDialog.dismiss();
        paymentMethodDialog.show();
        Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTransactionCancelled() {
        loadingDialog.dismiss();
        paymentMethodDialog.show();
        // Payment Cancelled by User
        Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAppNotFound() {
        paymentMethodDialog.show();
        Toast.makeText(this, "App Not Found", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void totalcartamount(int ammount) {
        if (ammount>250){
            continuecart = true;
        }else {
            continuecart=false;
        }

    }
}

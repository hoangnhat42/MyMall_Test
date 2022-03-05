package com.project.scan_on;

import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.project.scan_on.Helper.BaseActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderDetailsActivity extends BaseActivity {

    private int position;
    private int num = 0;
    private RecyclerView orderitemrecyclerview;

    private TextView title, price, quantity;
    private ImageView productImage, orderedIndicator, packedIndicator, shippedIndicator, deliveredIndicator;
    private ProgressBar O_P_progress, P_S_progress, S_D_progress;
    private TextView orderedTitle, packedTitle, shippedTitle, deliveredTitle;
    private TextView orderedDate, packedDate, shippedDate, deliveredDate;
    private TextView orderedBody, packedBody, shippedBody, deliveredBody;
    private LinearLayout rateNowContainer;
    private int rating;
    private TextView fullName,address,pincode;
    private TextView totalItems,totalItemsPrice,deliveryPrice,totalAmount,savedAmount;
    private Dialog loadingDialog,cancelDialog;
    private SimpleDateFormat simpleDateFormat;
    private String OrderId, totalitemprice, totalammount, deliverypricee, totalitem, savedammount, orderstatus,paymentmethod;
    public static MyOrderItemAdapter myOrderAdaperItems;
    private List<MyOrderItemModel> myOrderItemModelList = new ArrayList<>();
    private ConstraintLayout shippingdetails, secondconstrant;


    private Button cancelOrderBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Order Details");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        secondconstrant = findViewById(R.id.secondconstrant);
        orderitemrecyclerview = findViewById(R.id.orderitemrecyclerview);
        orderitemrecyclerview.setNestedScrollingEnabled(false);
        secondconstrant.setVisibility(View.GONE);

        //////////////loading Dialog Start
        loadingDialog = new Dialog(OrderDetailsActivity.this);
        loadingDialog.setContentView(R.layout.loading_progress_dialog);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.slider_background));
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ////////////////loading Dialog End

        /////////////orderCancel Dialog Start
        cancelDialog = new Dialog(OrderDetailsActivity.this);
        cancelDialog.setContentView(R.layout.order_cancel_dialog);
        cancelDialog.setCancelable(true);
        cancelDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.slider_background));
       // cancelDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ////////////////orderCancel Dialog End

  //      position = getIntent().getIntExtra("Position", -1);
//        final MyOrderItemModel model = DBqueries.myOrderItemModelList.get(position);
        OrderId = getIntent().getStringExtra("orderid");
        totalitemprice = getIntent().getStringExtra("totalitemprice");
        totalammount = getIntent().getStringExtra("totalammount");
        deliverypricee = getIntent().getStringExtra("deliveryprice");
        totalitem = getIntent().getStringExtra("totalitem");
        savedammount = getIntent().getStringExtra("savedammount");
        orderstatus = getIntent().getStringExtra("orderstatus");
        paymentmethod = getIntent().getStringExtra("paymentmethod");
     //   Toast.makeText(this, ""+savedammount +" ordersttus"+orderstatus, Toast.LENGTH_SHORT).show();





        title = findViewById(R.id.product_title);
        price = findViewById(R.id.product_price);
        quantity = findViewById(R.id.product_quantity);

        productImage = findViewById(R.id.product_image);
        cancelOrderBtn = findViewById(R.id.cancel_btn);

        orderedIndicator = findViewById(R.id.ordered_indicator);
        packedIndicator = findViewById(R.id.packed_indicator);
        shippedIndicator = findViewById(R.id.shipping_indicator);
        deliveredIndicator = findViewById(R.id.delivered_indicator);

        O_P_progress = findViewById(R.id.ordered_packed_progress);
        P_S_progress = findViewById(R.id.packed_shiping_progress);
        S_D_progress = findViewById(R.id.shipping_delivered_progress);

        orderedTitle = findViewById(R.id.ordered_title);
        packedTitle = findViewById(R.id.packed_title);
        shippedTitle = findViewById(R.id.shipping_title);
        deliveredTitle = findViewById(R.id.delivered_title);

        orderedDate = findViewById(R.id.ordered_date);
        packedDate = findViewById(R.id.packed_date);
        shippedDate = findViewById(R.id.shipping_date);
        deliveredDate = findViewById(R.id.delivered_date);
        orderedBody = findViewById(R.id.ordered_body);
        packedBody = findViewById(R.id.packed_body);
        shippedBody = findViewById(R.id.shipping_body);
        deliveredBody = findViewById(R.id.delivered_body);
        rateNowContainer = findViewById(R.id.rate_now_container);
        fullName = findViewById(R.id.address_fullname);
        address = findViewById(R.id.address);
        pincode = findViewById(R.id.pincode);
        totalItems = findViewById(R.id.total_items);
        totalItemsPrice = findViewById(R.id.total_items_price);
        deliveryPrice = findViewById(R.id.delivery_price);
        totalAmount = findViewById(R.id.total_price);
        savedAmount = findViewById(R.id.saved_amount);
        title.setText("OrderId:"+OrderId);
        price.setText("Total Ammount:" + totalammount + "/-");
        quantity.setText("Total Items :" + totalitem);



        myOrderItemModelList.clear();
        loadingDialog.show();

        FirebaseFirestore.getInstance().collection("ORDERS").document(OrderId).collection("OrderItems").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            int sizee = task.getResult().getDocuments().size();

                            for (DocumentSnapshot orderItems : task.getResult().getDocuments()) {
                                num++;
                                final MyOrderItemModel myOrderItemModel = new MyOrderItemModel(
                                        orderItems.getString("Product Id"),
                                        orderItems.getString("Order Status"),
                                        orderItems.getString("Address"),
                                        orderItems.getString("Coupen Id"),
                                        orderItems.getString("Cutted price"),
                                        orderItems.getDate("Ordered date "),
                                        orderItems.getDate("Packed date "),
                                        orderItems.getDate("Shipped date "),
                                        orderItems.getDate("Delivered date "),
                                        orderItems.getDate("Cancelled date "),
                                        orderItems.getString("Discounted Price"),
                                        orderItems.getLong("Free Coupens"),
                                        orderItems.getString("FullName"),
                                        orderItems.getString("ORDER ID"),
                                        orderItems.getString("Payment Method"),
                                        orderItems.getString("Pincode"),
                                        orderItems.getString("Product Price"),
                                        orderItems.getLong("Product Quantity"),
                                        orderItems.getString("User Id"),
                                        orderItems.getString("Product Image"),
                                        orderItems.getString("Product Title"),
                                        orderItems.getString("Delivery Price"),
                                        orderItems.getBoolean("Cancellation requested")
                                        , orderItems.getString("Product_size")
                                        , orderItems.getString("Product_color")
                                );
                                myOrderItemModelList.add(myOrderItemModel);

                                if (num == sizee){
                                    DBqueries.myOrderItemModelList.clear();
                                    DBqueries.myOrderItemModelList.addAll(myOrderItemModelList);
                                    num = 0;
                                    myOrderAdaperItems = new MyOrderItemAdapter(myOrderItemModelList,loadingDialog);
                                    LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
                                    layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                                    orderitemrecyclerview.setLayoutManager(layoutManager);
                                    orderitemrecyclerview.setAdapter(myOrderAdaperItems);
                                    myOrderAdaperItems.notifyDataSetChanged();
                                    DBqueries.loadRatingList(OrderDetailsActivity.this);
                                    secondconstrant.setVisibility(View.VISIBLE);
                                    loadingDialog.dismiss();
                                    final MyOrderItemModel model = myOrderItemModelList.get(0);

                                      // here going to set complete details





                                    //simpleDateFormat = new SimpleDateFormat("EEE-dd-MMM-YYYY-hh-mm-aa");
                                    switch (model.getOrderStatus()) {

                                        case "Ordered":
                                            orderedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
                                            orderedDate.setText(String.valueOf(model.getOrderedDate()));
                                            /////simpleDateFormat.format(model.getOrderedDate())

                                            O_P_progress.setVisibility(View.GONE);
                                            P_S_progress.setVisibility(View.GONE);
                                            S_D_progress.setVisibility(View.GONE);

                                            packedIndicator.setVisibility(View.GONE);
                                            packedBody.setVisibility(View.GONE);
                                            packedDate.setVisibility(View.GONE);
                                            packedTitle.setVisibility(View.GONE);

                                            shippedIndicator.setVisibility(View.GONE);
                                            shippedBody.setVisibility(View.GONE);
                                            shippedDate.setVisibility(View.GONE);
                                            shippedTitle.setVisibility(View.GONE);

                                            deliveredIndicator.setVisibility(View.GONE);
                                            deliveredBody.setVisibility(View.GONE);
                                            deliveredDate.setVisibility(View.GONE);
                                            deliveredTitle.setVisibility(View.GONE);
                                            break;
                                        case "Packed":
                                            orderedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
                                            orderedDate.setText(String.valueOf(model.getOrderedDate()));

                                            packedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
                                            packedDate.setText(String.valueOf(model.getPackedDate()));

                                            O_P_progress.setProgress(100);

                                            P_S_progress.setVisibility(View.GONE);
                                            S_D_progress.setVisibility(View.GONE);

                                            shippedIndicator.setVisibility(View.GONE);
                                            shippedBody.setVisibility(View.GONE);
                                            shippedDate.setVisibility(View.GONE);
                                            shippedTitle.setVisibility(View.GONE);

                                            deliveredIndicator.setVisibility(View.GONE);
                                            deliveredBody.setVisibility(View.GONE);
                                            deliveredDate.setVisibility(View.GONE);
                                            deliveredTitle.setVisibility(View.GONE);
                                            break;
                                        case "Shipped":
                                            orderedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
                                            orderedDate.setText(String.valueOf(model.getOrderedDate()));

                                            packedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
                                            packedDate.setText(String.valueOf(model.getPackedDate()));

                                            shippedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
                                            shippedDate.setText(String.valueOf(model.getShippedDate()));

                                            O_P_progress.setProgress(100);
                                            P_S_progress.setProgress(100);

                                            S_D_progress.setVisibility(View.GONE);

                                            deliveredIndicator.setVisibility(View.GONE);
                                            deliveredBody.setVisibility(View.GONE);
                                            deliveredDate.setVisibility(View.GONE);
                                            deliveredTitle.setVisibility(View.GONE);
                                            break;
                                        case "Out for delivery":
                                            orderedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
                                            orderedDate.setText(String.valueOf(model.getOrderedDate()));

                                            packedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
                                            packedDate.setText(String.valueOf(model.getPackedDate()));

                                            shippedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
                                            shippedDate.setText(String.valueOf(model.getShippedDate()));

                                            deliveredIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
                                            deliveredDate.setText(String.valueOf(model.getDeliveredDate()));

                                            O_P_progress.setProgress(100);
                                            P_S_progress.setProgress(100);
                                            S_D_progress.setProgress(100);

                                            deliveredTitle.setText("Our for Delivery");
                                            deliveredBody.setText("Your order is out for delivery");

                                            break;
                                        case "Delivered":
                                            orderedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
                                            orderedDate.setText(String.valueOf(model.getOrderedDate()));

                                            packedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
                                            packedDate.setText(String.valueOf(model.getPackedDate()));

                                            shippedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
                                            shippedDate.setText(String.valueOf(model.getShippedDate()));

                                            deliveredIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
                                            deliveredDate.setText(String.valueOf(model.getDeliveredDate()));

                                            O_P_progress.setProgress(100);
                                            P_S_progress.setProgress(100);
                                            S_D_progress.setProgress(100);

                                            break;
                                        case "Cancelled":

                                            if (model.getPackedDate().after(model.getOrderedDate())){

                                                if (model.getShippedDate().after(model.getPackedDate())){
                                                    orderedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
                                                    orderedDate.setText(String.valueOf(model.getOrderedDate()));

                                                    packedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
                                                    packedDate.setText(String.valueOf(model.getPackedDate()));

                                                    shippedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
                                                    shippedDate.setText(String.valueOf(model.getShippedDate()));

                                                    deliveredIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.unsuccessred)));
                                                    deliveredDate.setText(String.valueOf(model.getCancelledDate()));
                                                    deliveredTitle.setText("Cancelled");
                                                    deliveredBody.setText("Your order has been cancelled.");


                                                    O_P_progress.setProgress(100);
                                                    P_S_progress.setProgress(100);
                                                    S_D_progress.setProgress(100);

                                                }else {
                                                    orderedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
                                                    orderedDate.setText(String.valueOf(model.getOrderedDate()));

                                                    packedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
                                                    packedDate.setText(String.valueOf(model.getPackedDate()));

                                                    shippedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.unsuccessred)));
                                                    shippedDate.setText(String.valueOf(model.getCancelledDate()));
                                                    shippedTitle.setText("Cancelled");
                                                    shippedBody.setText("Your order has been cancelled.");


                                                    O_P_progress.setProgress(100);
                                                    P_S_progress.setProgress(100);

                                                    S_D_progress.setVisibility(View.GONE);

                                                    deliveredIndicator.setVisibility(View.GONE);
                                                    deliveredBody.setVisibility(View.GONE);
                                                    deliveredDate.setVisibility(View.GONE);
                                                    deliveredTitle.setVisibility(View.GONE);
                                                }

                                            }else {
                                                orderedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
                                                orderedDate.setText(String.valueOf(model.getOrderedDate()));

                                                packedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.unsuccessred)));
                                                packedDate.setText(String.valueOf(model.getCancelledDate()));
                                                packedTitle.setText("Cancelled");
                                                packedBody.setText("Your order has been cancelled.");

                                                O_P_progress.setProgress(100);

                                                P_S_progress.setVisibility(View.GONE);
                                                S_D_progress.setVisibility(View.GONE);

                                                shippedIndicator.setVisibility(View.GONE);
                                                shippedBody.setVisibility(View.GONE);
                                                shippedDate.setVisibility(View.GONE);
                                                shippedTitle.setVisibility(View.GONE);

                                                deliveredIndicator.setVisibility(View.GONE);
                                                deliveredBody.setVisibility(View.GONE);
                                                deliveredDate.setVisibility(View.GONE);
                                                deliveredTitle.setVisibility(View.GONE);
                                            }

                                            break;


                                    }


                                    if (model.isCancellationRequested()){
                                        cancelOrderBtn.setVisibility(View.VISIBLE);
                                        cancelOrderBtn.setEnabled(false);
                                        cancelOrderBtn.setText("Cancellation in process.");
                                        cancelOrderBtn.setTextColor(getResources().getColor(R.color.unsuccessred));
                                        cancelOrderBtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFFFFF")));
                                    }else {
                                        if (model.getOrderStatus().equals("Ordered") || model.getOrderStatus().equals("Packed")){
                                            cancelOrderBtn.setVisibility(View.VISIBLE);
                                            cancelOrderBtn.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    cancelDialog.findViewById(R.id.no_btn).setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            cancelDialog.dismiss();
                                                        }
                                                    });
                                                    cancelDialog.findViewById(R.id.yes_btn).setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            cancelDialog.dismiss();
                                                            loadingDialog.show();
                                                            Map<String,Object> map = new HashMap<>();
                                                            map.put("Order Id",model.getOrderID());
                                                            map.put("Product Id",model.getProductId());
                                                            map.put("Order Cancelled",false);
                                                            FirebaseFirestore.getInstance().collection("CANCELLED ORDERS").document().set(map)
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()){
                                                                                FirebaseFirestore.getInstance().collection("ORDERS").document(model.getOrderID()).collection("OrderItems").document(model.getProductId()).update("Cancellation requested",true)
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if (task.isSuccessful()){
                                                                                                    model.setCancellationRequested(true);
                                                                                                    cancelOrderBtn.setEnabled(false);
                                                                                                    cancelOrderBtn.setText("Cancellation in process.");
                                                                                                    cancelOrderBtn.setTextColor(getResources().getColor(R.color.unsuccessred));
                                                                                                    cancelOrderBtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFFFFF")));
                                                                                                }else {
                                                                                                    String error = task.getException().getMessage();
                                                                                                    Toast.makeText(OrderDetailsActivity.this, error, Toast.LENGTH_SHORT).show();
                                                                                                }
                                                                                                loadingDialog.dismiss();
                                                                                            }
                                                                                        });
                                                                            }else {
                                                                                loadingDialog.dismiss();
                                                                                String error = task.getException().getMessage();
                                                                                Toast.makeText(OrderDetailsActivity.this, error, Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        }
                                                                    });
                                                        }
                                                    });
                                                    cancelDialog.show();
                                                }
                                            });
                                        }
                                    }

                                    fullName.setText(model.getFullName());
                                    address.setText(model.getAddress());
                                    pincode.setText(model.getPincode());


                                    ///  complete items details


                                }
                            }



                        } else {
                            String error = task.getException().getMessage();
                            Toast.makeText(OrderDetailsActivity.this, error, Toast.LENGTH_SHORT).show();
                            loadingDialog.dismiss();
                        }
                    }
                });







        totalItems.setText("Price("+totalitem+" items)");
        totalAmount.setText(totalammount);

        totalItemsPrice.setText("Rs."+ totalitemprice +"/-");
        if (deliverypricee.equals("FREE")){
            deliveryPrice.setText("FREE");

        }else {
            deliveryPrice.setText("Rs." + deliverypricee + "/-");
        }
              savedAmount.setText("You saved Rs."+ savedammount +" on this order.");

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}

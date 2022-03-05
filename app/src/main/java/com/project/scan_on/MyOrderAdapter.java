package com.project.scan_on;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.content.Context.CLIPBOARD_SERVICE;

public class MyOrderAdapter extends RecyclerView.Adapter<MyOrderAdapter.Viewholder> {


    private List<MyOrderMainModel> myOrderMainModelList;
    private Dialog loadingDialog;

    public MyOrderAdapter(List<MyOrderMainModel> myOrderItemModelList,Dialog loadingDialog) {
        this.myOrderMainModelList = myOrderItemModelList;
        this.loadingDialog = loadingDialog;
    }

    @NonNull
    @Override
    public MyOrderAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.my_order_main_item_layout,viewGroup,false);
        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyOrderAdapter.Viewholder viewholder, int position) {

        String orderid= myOrderMainModelList.get(position).getOrderId();
        String deliveryprice= myOrderMainModelList.get(position).getDeliveryPrice();
        String paymentstatus= myOrderMainModelList.get(position).getPaymentStatus();
        String totalammount= String.valueOf(myOrderMainModelList.get(position).getTotalAmmount());
        String totalitem= String.valueOf(myOrderMainModelList.get(position).getTotalItem());
        String totalitemprice= String.valueOf(myOrderMainModelList.get(position).getTotalItemPrice());
        String orderStatus = myOrderMainModelList.get(position).getOrderStatus();
        long savedammount = myOrderMainModelList.get(position).getSavedAmmount();
        String paymentmethod = myOrderMainModelList.get(position).getPaymentMethod();
        Date date = myOrderMainModelList.get(position).getOrderDate();


        viewholder.setData(orderid,deliveryprice,paymentstatus,totalammount,totalitem,totalitemprice,orderStatus,date,savedammount,paymentmethod);
    }

    @Override
    public int getItemCount() {
        return myOrderMainModelList.size();
    }

    class Viewholder extends RecyclerView.ViewHolder

    {


        private ImageView orderIndicator,copyorderid;
        private TextView orderId,TotalAmmount,totalItem;
        private TextView orderstatus;
        private TextView orderDate;


        public Viewholder(@NonNull final View itemView) {
            super(itemView);
            copyorderid = itemView.findViewById(R.id.copyorderid);
            orderId = itemView.findViewById(R.id.order_id);
            TotalAmmount = itemView.findViewById(R.id.totalAmmount);
            totalItem = itemView.findViewById(R.id.totalItem2);
            orderDate = itemView.findViewById(R.id.payment_status);
            orderIndicator = itemView.findViewById(R.id.order_indicator);
            orderstatus = itemView.findViewById(R.id.order_status2);

        }


        public void setData(final String orderid, final String deliveryprice, String paymentstatus, final String totalammount, final String totalitem, final String totalitemprice, final String orderStatus, final Date date, final long savedammount, String paymentmethod) {

            if (orderStatus.equals("Cancelled")) {
                orderIndicator.setImageTintList(ColorStateList.valueOf(itemView.getContext().getResources().getColor(R.color.copenRed)));
            }else {
                orderIndicator.setImageTintList(ColorStateList.valueOf(itemView.getContext().getResources().getColor(R.color.successGreen)));
            }
            final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, dd MM yy hh:mm", Locale.getDefault());
            orderDate.setText(String.valueOf(simpleDateFormat.format(date)));

            orderId.setText("Order Id: "+orderid);
            TotalAmmount.setText(totalammount);
            totalItem.setText(totalitem);
            orderstatus.setText(orderStatus);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent orderDetailsIntent = new Intent(itemView.getContext(), OrderDetailsActivity.class);
                    orderDetailsIntent.putExtra("orderid",orderid);
                    orderDetailsIntent.putExtra("totalitemprice",totalitemprice);
                    orderDetailsIntent.putExtra("totalammount",totalammount);
                    orderDetailsIntent.putExtra("deliveryprice",deliveryprice);
                    orderDetailsIntent.putExtra("totalitem",totalitem);
                    orderDetailsIntent.putExtra("savedammount",savedammount);
                    orderDetailsIntent.putExtra("orderstatus",orderStatus);
                    orderDetailsIntent.putExtra("paymentmethod",paymentmethod);
                    itemView.getContext().startActivity(orderDetailsIntent);
                }
            });

            copyorderid.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ClipboardManager manager = (ClipboardManager) itemView.getContext().getSystemService(CLIPBOARD_SERVICE);
                    ClipData clipData = ClipData.newPlainText("text", orderid);
                    manager.setPrimaryClip(clipData);
                    Toast.makeText(itemView.getContext(), "Order ID Copied!", Toast.LENGTH_SHORT).show();

                }
            });
        }
    }}

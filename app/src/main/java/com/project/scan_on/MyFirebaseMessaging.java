package com.project.scan_on;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import com.project.scan_on.NotificationHelper;
import com.project.scan_on.Main2Activity;
import com.project.scan_on.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MyFirebaseMessaging extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            sendNotificationAPI26(this,remoteMessage,true,"","");
        else
            sendNotification(this,remoteMessage,true,"","");
    }

    public static void sendNotificationAPI26(Context context, RemoteMessage remoteMessage,boolean fromservice,String titlee,String message) {
        String title;
        String content;

        if (fromservice){
            RemoteMessage.Notification notification = remoteMessage.getNotification();
            title = notification.getTitle();
        content = notification.getBody();
       }else {
           title = titlee;
           content = message;
       }
        Intent intent = new Intent(context, Main2Activity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationHelper helper = new NotificationHelper(context);
        android.app.Notification.Builder builder = helper.getiDeliveryChannelNotification(title,content,pendingIntent,defaultSoundUri);
        //get random ID for notification to show all notifications
        helper.getManager().notify(new Random().nextInt(), builder.build());
    }

    public static void sendNotification(Context context, RemoteMessage remoteMessage,boolean fromservice,String titlee, String message) {
        Intent intent = new Intent(context, Main2Activity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        String title;
        String body;
        if (fromservice){
            RemoteMessage.Notification notification = remoteMessage.getNotification();
            title = notification.getTitle();
            body = notification.getBody();
        }else {
            title = titlee;
            body = message;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,"")
                .setSmallIcon(R.mipmap.app_round_icon)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager noti = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        noti.notify(0, builder.build());
    }

    @Override
    public void onNewToken(@NonNull String tokenRefreshed) {
        super.onNewToken(tokenRefreshed);
        updateTokenToFirebase(tokenRefreshed);

    }
    private void updateTokenToFirebase(String tokenRefreshed){
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            Map<String,Object> map = new HashMap<>();
            map.put("deviceTocken",tokenRefreshed);
            FirebaseFirestore.getInstance().collection("USERS").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).update(map);

        }
    }
}

package com.project.scan_on;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

//import com.github.javiersantos.bottomdialogs.BottomDialog;
import com.github.javiersantos.bottomdialogs.BottomDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.project.scan_on.Helper.BaseActivity;
import com.project.scan_on.Helper.MyUploadService;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import me.jagar.chatvoiceplayerlibrary.VoicePlayerView;


import static com.paytm.pgsdk.easypay.manager.PaytmAssist.getContext;

public class RecordActivity extends BaseActivity implements View.OnClickListener {

    private ImageButton recordBtn;
    private TextView filenameText;
    private boolean isRecording = false;
    private String recordPermission = Manifest.permission.RECORD_AUDIO;
    private int PERMISSION_CODE = 21;
    private MediaRecorder mediaRecorder;
    private String recordFile;
    private Chronometer timer;
    private static final String TAG = "Storage#RecordActivity";
    private static final int RC_TAKE_PICTURE = 101;
    private static final String KEY_FILE_URI = "key_file_uri";
    private static final String KEY_DOWNLOAD_URL = "key_download_url";
    private BroadcastReceiver mBroadcastReceiver;
    private FirebaseAuth mAuth;
    private Uri mDownloadUrl = null;
    private Uri mFileUri = null;
    private ProgressBar progressBar;
    private ImageButton backarroww;
    BottomDialog dialog;
    VoicePlayerView voicePlayerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        recordBtn = findViewById(R.id.record_btn);
        timer = findViewById(R.id.record_timer);
        filenameText = findViewById(R.id.record_filename);
        recordBtn.setOnClickListener(this::onClick);
        mAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressBar);
        if (savedInstanceState != null) {
            mFileUri = savedInstanceState.getParcelable(KEY_FILE_URI);
            mDownloadUrl = savedInstanceState.getParcelable(KEY_DOWNLOAD_URL);
        }
        backarroww = findViewById(R.id.backarrow);

        backarroww.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        onNewIntent(getIntent());
        // Local broadcast receiver
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "onReceive:" + intent);
                hideProgressBar();

                switch (intent.getAction()) {
                    case MyUploadService.UPLOAD_COMPLETED:
                    {
                        resetActivity();
                    }
                    case MyUploadService.UPLOAD_ERROR:
                        if (dialog!=null && intent.getParcelableExtra(MyUploadService.EXTRA_FILE_URI)!=null){
                           mFileUri = intent.getParcelableExtra(MyUploadService.EXTRA_FILE_URI);
                           if (voicePlayerView!=null){
                               voicePlayerView.setAudio(mFileUri.getPath());
                               //dialog.show();
                           }

                        }

                        onUploadResultIntent(intent);
                        break;
                }
            }
        };

    }
    private void onUploadResultIntent(Intent intent) {
        showProgressBar("caption");
        // Got a new intent from MyUploadService with a success or failure
        mDownloadUrl = intent.getParcelableExtra(MyUploadService.EXTRA_DOWNLOAD_URL);
        mFileUri = intent.getParcelableExtra(MyUploadService.EXTRA_FILE_URI);
        Map<String,Object> map = new HashMap<>();
        map.put("UserId",FirebaseAuth.getInstance().getCurrentUser().getUid());
        map.put("Mp3Media",mDownloadUrl.toString());
        map.put("UserNumber",FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
        FirebaseFirestore.getInstance().collection("AUDIO_ORDERS").document().set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
           if (task.isSuccessful()){
               hideProgressBar();
               Toast.makeText(RecordActivity.this, "Audio Received Successfully", Toast.LENGTH_SHORT).show();
           } else {
               hideProgressBar();
               Toast.makeText(RecordActivity.this, ""+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
           }
            }
        });

    }


    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // Check if this Activity was launched by clicking on an upload notification
        if (intent.hasExtra(MyUploadService.EXTRA_DOWNLOAD_URL)) {
            onUploadResultIntent(intent);
        }

    }


    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.registerReceiver(mBroadcastReceiver, MyUploadService.getIntentFilter());
    }





    @Override
    public void onClick(View v) {
        /*  Check, which button is pressed and do the task accordingly
         */
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.bottomdialogview, null);

        voicePlayerView = (VoicePlayerView) customView.findViewById(R.id.voicePlayerView);
        Button deletebtn =  customView.findViewById(R.id.delete);
        Button  sendBtn =  customView.findViewById(R.id.send);

        switch (v.getId()) {

            case R.id.record_btn:
                if(isRecording) {
                    //Stop Recording
                    stopRecording();

                    // Change button image and set Recording state to false
                    recordBtn.setImageDrawable(getResources().getDrawable(R.drawable.record_btn_stopped, null));
                    isRecording = false;
                    String path = getExternalFilesDir("/").getAbsolutePath();
                    final File directory = new File(path + "/" + recordFile);


                    voicePlayerView.setAudio(directory.getAbsolutePath());
                    voicePlayerView.setVisibility(View.VISIBLE);


                    dialog = new BottomDialog.Builder(this)
                            .setTitle("Send Audio Message")
                            .setCustomView(customView)
                            .setCancelable(false)
                            // You can also show the custom view with some padding in DP (left, top, right, bottom)
                            //.setCustomView(customView, 20, 20, 20, 0)
                            .show();


                    deletebtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            timer.setBase(SystemClock.elapsedRealtime());
                            timer.stop();
                            voicePlayerView.getMediaPlayer().stop();
                            directory.delete();
                            dialog.dismiss();
                            filenameText.setText("Press the mic button \n to start recording");
                            Toast.makeText(RecordActivity.this, "Recording Deleted!", Toast.LENGTH_SHORT).show();
                        }
                    });

                    sendBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final File filecurrent = new File(path + "/" + recordFile);
                            dialog.dismiss();
                            Toast.makeText(RecordActivity.this, "Dont Close app from background until apload complete!", Toast.LENGTH_SHORT).show();
                            mFileUri = Uri.fromFile(filecurrent);
                            uploadFromUri(mFileUri);


                        }
                    });

                } else {

                    //Check permission to record audio
                    if(checkPermissions()) {
                        //Start Recording
                        startRecording();

                        // Change button image and set Recording state to false
                        recordBtn.setImageDrawable(getResources().getDrawable(R.drawable.record_btn_recording, null));
                        isRecording = true;
                    }
                }
                break;
        }
    }
    private void uploadFromUri(Uri fileUri) {
        Log.d(TAG, "uploadFromUri:src:" + fileUri.toString());

        // Save the File URI
        mFileUri = fileUri;

        // Clear the last download, if any
        mDownloadUrl = null;

        // Start MyUploadService to upload the file, so that the file is uploaded
        // even if this Activity is killed or put in the background
        startService(new Intent(this, MyUploadService.class)
                .putExtra(MyUploadService.EXTRA_FILE_URI, fileUri)
                .setAction(MyUploadService.ACTION_UPLOAD));

        // Show loading spinner
        showProgressBar(getString(R.string.progress_uploading));
    }


    private void showProgressBar(String caption) {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
     progressBar.setVisibility(View.GONE);
    }



    private void stopRecording() {
        //Stop Timer, very obvious
        timer.stop();
        //Change text on page to file saved
        filenameText.setText("Recording Stopped, File Saved : " + recordFile);
        //Stop media recorder and set it to null for further use to record new audio
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
    }

    private void startRecording() {
        timer.setBase(SystemClock.elapsedRealtime());
        timer.start();
        //Get app external directory path
        String recordPath = getExternalFilesDir("/").getAbsolutePath();
        //Get current date and time
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss", Locale.CANADA);
        Date now = new Date();
        //initialize filename variable with date and time at the end to ensure the new file wont overwrite previous file
        recordFile = "Recording_" + formatter.format(now) + ".mp3";
        filenameText.setText("Recording, File Name : " + recordFile);
        //Setup Media Recorder for recording
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setOutputFile(recordPath + "/" + recordFile);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Start Recording
        mediaRecorder.start();
    }

    private boolean checkPermissions() {
        //Check permission
        if (ActivityCompat.checkSelfPermission(this, recordPermission) == PackageManager.PERMISSION_GRANTED) {
            //Permission Granted
            return true;
        } else {
            //Permission not granted, ask for permission
            ActivityCompat.requestPermissions(this, new String[]{recordPermission}, PERMISSION_CODE);
            return false;
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        if(isRecording){
            stopRecording();
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);

    }


    @Override
    public void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putParcelable(KEY_FILE_URI, mFileUri);
        out.putParcelable(KEY_DOWNLOAD_URL, mDownloadUrl);
    }




   private void resetActivity(){
        if (timer!=null) {
            timer.setBase(SystemClock.elapsedRealtime());
            timer.stop();
        }

       filenameText.setText("Press the mic button \n to start recording");
       Toast.makeText(RecordActivity.this, "File Uploaded Successfully", Toast.LENGTH_SHORT).show();
   }




}

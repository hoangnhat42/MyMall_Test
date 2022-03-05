package com.project.scan_on;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

//import com.github.javiersantos.bottomdialogs.BottomDialog;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

//import me.jagar.chatvoiceplayerlibrary.VoicePlayerView;


/**
 * A simple {@link Fragment} subclass.
 */
public class AudioRecordFragment extends Fragment implements View.OnClickListener {


    private ImageButton recordBtn;
    private TextView filenameText;
    private boolean isRecording = false;

    private String recordPermission = Manifest.permission.RECORD_AUDIO;
    private int PERMISSION_CODE = 21;

    private MediaRecorder mediaRecorder;
    private String recordFile;
    private Chronometer timer;
    public AudioRecordFragment() {
        // Required empty public constructor
    }

    private static final String TAG = "Storage#MainActivity";

    private static final int RC_TAKE_PICTURE = 101;

    private static final String KEY_FILE_URI = "key_file_uri";
    private static final String KEY_DOWNLOAD_URL = "key_download_url";

    private BroadcastReceiver mBroadcastReceiver;
    private FirebaseAuth mAuth;

    private Uri mDownloadUrl = null;
    private Uri mFileUri = null;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_audio_record, container, false);


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recordBtn = view.findViewById(R.id.record_btn);
        timer = view.findViewById(R.id.record_timer);
        filenameText = view.findViewById(R.id.record_filename);
        recordBtn.setOnClickListener(this::onClick);
        mAuth = FirebaseAuth.getInstance();

        if (savedInstanceState != null) {
            mFileUri = savedInstanceState.getParcelable(KEY_FILE_URI);
            mDownloadUrl = savedInstanceState.getParcelable(KEY_DOWNLOAD_URL);
        }



    }

    @Override
    public void onClick(View v) {
        /*  Check, which button is pressed and do the task accordingly
         */
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.bottomdialogview, null);

        //VoicePlayerView voicePlayerView = (VoicePlayerView) customView.findViewById(R.id.voicePlayerView);
        Button deletebtn =  customView.findViewById(R.id.delete);
        Button  sendBtn =  customView.findViewById(R.id.send);



       // final BottomDialog dialog;

        switch (v.getId()) {

            case R.id.record_btn:
                if(isRecording) {
                    //Stop Recording
                    stopRecording();

                    // Change button image and set Recording state to false
                    recordBtn.setImageDrawable(getResources().getDrawable(R.drawable.record_btn_stopped, null));
                    isRecording = false;
                    String path = getActivity().getExternalFilesDir("/").getAbsolutePath();
                    final File directory = new File(path + "/" + recordFile);

                   // voicePlayerView.setAudio(directory.getAbsolutePath());
                   // voicePlayerView.setVisibility(View.VISIBLE);

                    //  voicePlayerView.getImgShare().setImageResource(null);


//                    dialog = new BottomDialog.Builder(getContext())
//                            .setTitle("Send Audio Message")
//                            .setCustomView(customView)
//                            .setCancelable(false)
//                            // You can also show the custom view with some padding in DP (left, top, right, bottom)
//                            //.setCustomView(customView, 20, 20, 20, 0)
//                            .show();


                    deletebtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            timer.setBase(SystemClock.elapsedRealtime());
                            timer.stop();
                            directory.delete();
                            //dialog.dismiss();
                            filenameText.setText("Press the mic button \n to start recording");
                            Toast.makeText(getContext(), "Recording Deleted!", Toast.LENGTH_SHORT).show();
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
        String recordPath = getActivity().getExternalFilesDir("/").getAbsolutePath();
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
        if (ActivityCompat.checkSelfPermission(getContext(), recordPermission) == PackageManager.PERMISSION_GRANTED) {
            //Permission Granted
            return true;
        } else {
            //Permission not granted, ask for permission
            ActivityCompat.requestPermissions(getActivity(), new String[]{recordPermission}, PERMISSION_CODE);
            return false;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(isRecording){
            stopRecording();
        }
    }


}

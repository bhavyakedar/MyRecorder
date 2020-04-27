package com.example.myrecorder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    Button save;
    ToggleButton record;
    ListView listView;
    MediaPlayer mediaPlayer;
    MediaRecorder mediaRecorder;
    String pathNew;
    TextView textView;
    int seconds = 0;
    boolean recording = false;
    ArrayList<String> myFiles = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getPermissions();
        record = findViewById(R.id.record);
        save = findViewById(R.id.save);
        listView = findViewById(R.id.listView);
        textView = findViewById(R.id.textView);
        mediaPlayer = new MediaPlayer();
        final File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/MyRecorder");
        if(!file.exists())
            file.mkdir();
        getFiles(file);
        final ArrayAdapter arrayAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,myFiles);
        listView.setAdapter(arrayAdapter);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaRecorder!=null) {
                    mediaRecorder.stop();
                    Toast.makeText(MainActivity.this, "Files saved successfully at :" + pathNew, Toast.LENGTH_LONG).show();
                    recording = false;
                    seconds = 0;
                    textView.setText("00:00:00");
                    mediaRecorder = null;
                    getFiles(file);
                    arrayAdapter.notifyDataSetChanged();
                }
            }
        });
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                int hours = seconds/3600;
                int mins = (seconds - hours*3600)/60;
                int secs = seconds - hours*3600 - mins*60;
                textView.setText(String.format("%02d:%02d:%02d",hours,mins,secs));
                if(recording)
                {
                    seconds++;
                }
                handler.postDelayed(this, 1000);
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyRecorder/" + myFiles.get(position));
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                }
                catch (IOException e)
                {
                    Toast.makeText(MainActivity.this, "Couldn't play "+myFiles.get(position)+" . It may have been deleted.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void onRecord(View view)
    {
        boolean isOn = ((ToggleButton)view).isChecked();
        if(isOn)
        {
            try
            {
                if(mediaRecorder == null) {
                    int num = 100000;
                    Random random = new Random();
                    num = random.nextInt(num);
                    pathNew = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyRecorder/My_Recorder_" + num + ".3gpp";
                    mediaRecorder = new MediaRecorder();
                    mediaRecorder.reset();
                    mediaRecorder.setAudioEncodingBitRate(16);
                    mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
                    mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
                    mediaRecorder.setOutputFile(pathNew);
                    mediaRecorder.prepare();
                    mediaRecorder.start();
                    recording = true;
                }
                else{
                    mediaRecorder.resume();
                    recording = true;
                }
                Toast.makeText(this, "Recording...", Toast.LENGTH_SHORT).show();
            }
            catch(IOException e)
            {
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            save.setEnabled(false);
        }
        else
        {
            save.setEnabled(true);
            mediaRecorder.pause();
            recording = false;
            Toast.makeText(this, "Recording paused...", Toast.LENGTH_SHORT).show();
        }
    }

    public void getPermissions()
    {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO},0);
            getPermissions();
        }
    }

    public void getFiles(File file)
    {
        myFiles.clear();
        File[] allFiles = file.listFiles();
        if(allFiles.length > 0)
        {
            for(int i = 0 ; i < allFiles.length ; i++)
            {
                if(allFiles[i].getName().endsWith(".3gpp") && allFiles[i].getName().startsWith("My_Recorder_"))
                    myFiles.add(allFiles[i].getName());
            }
        }
    }
}

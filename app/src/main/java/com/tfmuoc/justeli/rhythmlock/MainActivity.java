package com.tfmuoc.justeli.rhythmlock;

import android.app.Dialog;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.HashMap;


public class MainActivity extends ActionBarActivity {

    //Menu options
    private static final int SIMPLE_COMP = Menu.FIRST;
    private static final int NORM_COMP = SIMPLE_COMP + 1;
    // 1 for SIMPLE (by default), 2 for NORMALIZED TIME
    int comparsionMode=1;

    HashMap<Integer,EventData> data;

    int counter = 1;
    boolean isRecording = false;
    boolean loaded = false;

    // in milliseconds
    int threshold=50;

    int[] soundPathId = new int[4];

    //Create the soundpool
    SoundPool soundpool = new SoundPool(5,AudioManager.STREAM_MUSIC,0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        View currentView = getWindow().getDecorView().findViewById(android.R.id.content);

        data = new HashMap<Integer,EventData>();

        SeekBar seekbarThreshold = (SeekBar)this.findViewById(R.id.seekBarThreshold);

        ImageView square1 = (ImageView)this.findViewById(R.id.square1);
        ImageView square2 = (ImageView)this.findViewById(R.id.square2);
        ImageView square3 = (ImageView)this.findViewById(R.id.square3);
        ImageView square4 = (ImageView)this.findViewById(R.id.square4);
        final TextView txtThreshold = (TextView)this.findViewById(R.id.textThreshold);
        txtThreshold.setText("T: "+threshold);

        //To change threshold value
        seekbarThreshold.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                threshold = seekBar.getProgress();
                txtThreshold.setText("T: "+threshold);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        seekbarThreshold.setProgress(50);

        //Define Record and Compare buttons
        final ToggleButton toggleRecordButton = (ToggleButton)this.findViewById(R.id.recordButton);
        final ToggleButton compButton = (ToggleButton)this.findViewById(R.id.buttonCompare);

        toggleRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (toggleRecordButton.isChecked()) {
                    System.out.println("RECORDING! \n");
                    isRecording = true;
                    counter = 1;
                    //Deactivate the other button. Cannot record and compare on same time
                    compButton.setChecked(false);
                } else {
                    toggleRecordButton.setChecked(false);
                    System.out.println("NOT RECORDING! \n");
                    isRecording = false;

                    //If any event took place
                    if (data.size()!=0) {
                        // When the user ends the pattern, the app stores it
                        EventData ev = new EventData();

                        //ev.storeEvent(data);  --> simpleNormalizator function already stores the output
                        data = ev.simpleNormalizator(data);

                        //Store the normalized data into the file system
                        ev.storeEvent(data);
                        Toast.makeText(getApplicationContext(), "Rhythm saved", Toast.LENGTH_SHORT).show();
                    }
                    //Clear the variable
                    data.clear();

                }
            }
        });

        compButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (compButton.isChecked()) {
                    System.out.println("COMPARING! \n");
                    isRecording = true;
                    counter=1;
                    //Deactivate the other button. Cannot record and compare on same time
                    toggleRecordButton.setChecked(false);
                } else {
                    compButton.setChecked(false);
                    isRecording = false;
                    System.out.printf("END OF PATTERN FOR COMPARISON \n");

                    //When clicked, compare the new source with the recorded one
                    PatternCheck comparator = new PatternCheck();

                    //Normalize the new data
                    EventData ev = new EventData();
                    data=ev.simpleNormalizator(data);

                    //Will return the value of the comparison. False by default
                    boolean matching = false;
                    switch (comparsionMode){
                        case 1:
                            //Let's compare using simple comparison
                            matching = comparator.SimpleComparator(threshold, data);
                            break;
                        case 2:
                            //Let's compare using an improved comparison
                            data=comparator.tempoNormalizator(data);
                            matching = comparator.SimpleComparator(threshold, data);
                            break;
                    }

                    //If there is matching...
                    if (matching)
                        Toast.makeText(getApplicationContext(), "Matching!", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getApplicationContext(), "Keep trying!", Toast.LENGTH_SHORT).show();

                    //Clear the variable
                    data.clear();
                }
            }
        });


        //Pre-charge sounds on memory to be played
        loadSound();

        square1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                recordData(event, 1);
                playSound(soundPathId[0]);
                return false;
            }
        });

        square2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                recordData(event, 2);
                playSound(soundPathId[1]);
                return false;
            }
        });

        square3.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                recordData(event,3);
                playSound(soundPathId[2]);
                return false;
            }
        });

        square4.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                recordData(event,4);
                playSound(soundPathId[3]);
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.removeItem(R.id.action_settings);
        menu.add(0, SIMPLE_COMP, 0, "Simple Comparison");
        menu.add(0, NORM_COMP, 0, "Normalized Comparison");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // Handle item selection
        switch (id) {
            case (SIMPLE_COMP):
                comparsionMode=1;
                Toast toast = Toast.makeText(this, "SIMPLE COMPARISON IS USED!", Toast.LENGTH_SHORT);
                toast.show();
                return true;

            case (NORM_COMP):
                comparsionMode=2;
                Toast toast2 = Toast.makeText(this, "BINARY NORMALIZED COMPARISON IS USED!", Toast.LENGTH_SHORT);
                toast2.show();
                return true;
/*
            case (R.id.action_settings):
                Toast toast3 = Toast.makeText(this, "Coming soon!", Toast.LENGTH_SHORT);
                toast3.show();
                return true;
*/
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Records the event and from where (which square) it comes (id)
    public void recordData(MotionEvent event, int id){

        // If there is a press event event.getAction() is 0;
        if (event.getAction()==0 && isRecording ){
            // Get the event
            EventData recentData = new EventData();
            recentData.setId(id);
            recentData.setTouchTime(event.getEventTime());

            System.out.printf("Square #:  %d \n", id);
            System.out.printf("Time:  %d \n", event.getEventTime());

            // Log it into "data".
            data.put(counter,recentData);

            // Add 1 to the counter
            System.out.printf("Count of events #:  %d \n", counter);
            counter++;

        }

    }

    //This method pre-load all the sounds
    public void loadSound(){

        soundpool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                loaded=true;
            }
        });

        soundPathId[0] = soundpool.load(this, R.raw.sound1, 1);
        soundPathId[1] = soundpool.load(this, R.raw.sound2, 1);
        soundPathId[2] = soundpool.load(this, R.raw.sound3, 1);
        soundPathId[3] = soundpool.load(this, R.raw.sound4, 1);
    }

    //This method plays all the sounds when any square is clicked
    public void playSound(int soundPathId){

        //Get the max volume from system
        AudioManager audioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
        float volume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        //System.out.printf("Sound #: %d \n", soundPathId);
        if (loaded)
            soundpool.play(soundPathId, volume, volume, 0, 0, 0);
    }

}


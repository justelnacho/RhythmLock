package com.tfmuoc.justeli.rhythmlock;

import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;

/**
 * Created by justeli on 28/03/2015.
 */
public class PatternCheck {

    //Lineal and simple comparison between values
    //threshold variable is in milliseconds unit
    public boolean SimpleComparator(int threshold, HashMap<Integer,EventData> newData){
        Log.i("COMPARATOR","USING SIMPLE COMPARATOR");

        HashMap<Integer,EventData> storedData;
        EventData ev = new EventData();
        try {
            storedData = ev.readEvent();
            long aboveValue;
            long belowValue;
            long currentTime;
            int squareNum = storedData.get(1).getId();

            //Normalize the new data
            newData = ev.simpleNormalizator(newData);

            //If even sizes are not equal, then no matching
            int maxSize = storedData.size();
            if (maxSize != newData.size()) {
                Log.i("NOT MATCHING","FAIL COMPARING SIZES");
                return false;
            }

            if (squareNum!=newData.get(1).getId()){
                Log.i("NOT MATCHING","FAIL COMPARING SQUARE 1");
                return false;
            }

            for (int i = 2; i <= maxSize; i++) {
                aboveValue=storedData.get(i).getTouchTime()+threshold;
                belowValue=storedData.get(i).getTouchTime()-threshold;

                // To catch corner cases
                if (belowValue<0)
                        belowValue=0;

                currentTime = newData.get(i).getTouchTime();
                squareNum = storedData.get(i).getId();

                //System.out.printf("Stored Square:  %d \n", squareNum);
                //System.out.printf("New Square:  %d \n", newData.get(i).getId());

                if ( (currentTime>aboveValue || currentTime <belowValue) || (squareNum!=newData.get(i).getId()) ){
                    Log.i("NOT MATCHING","FAIL COMPARING TIMING OR SQUARE");
                    return false;
                }
            }
        } catch (Exception e){
            e.printStackTrace();
            //Toast.makeText(getApplicationContext(),"Please record a rhythm before...",Toast.LENGTH_SHORT).show();
        }

        Log.i("MATCHING","YOU GOT IT BOY!");
        return true;
    }

    //Normalizes the time of the newest rhythm sample
    public HashMap<Integer,EventData> tempoNormalizator(HashMap<Integer,EventData> newData){

        HashMap<Integer,EventData> storedData;
        EventData ev = new EventData();

        try {
            int counter;
            storedData = ev.readEvent();

            long durationStored = storedData.get(storedData.size()).getTouchTime();
            long durationNew = newData.get(newData.size()).getTouchTime();
            double factor = ((double)durationStored)/durationNew;

            for (counter=1;counter<=storedData.size();counter++){
                long newTime= (long)((double)newData.get(counter).getTouchTime()*factor);
                newData.get(counter).setTouchTime(newTime);
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return newData;
    }

}

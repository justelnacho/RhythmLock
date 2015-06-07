package com.tfmuoc.justeli.rhythmlock;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by justeli on 26/03/2015.
 * Id = Square1 / Square2 / Square3 / Square4
 */

public class EventData implements Serializable{

    //touchID = square touched
    //touchTime = time when thel event happened.

    int touchId;
    long  touchTime;

    public void EventData(int touch, long time){
        touchId=touch;
        touchTime=time;
    }

    public int getId(){
        return this.touchId;
    }

    public long getTouchTime(){
        return this.touchTime;
    }

    public void setId(int touch){
        touchId=touch;
    }

    public void setTouchTime(long time){
        touchTime=time;
    }

    public boolean storeEvent(HashMap<Integer,EventData> data){
        // Returns true if possible, false if error!
        FileOutputStream fileOutputStream = null;
        try {
            // Create the directory
            File newD = new File(Environment.getExternalStorageDirectory() + File.separator + "rhythmLock");
            if(!newD.exists()){
                newD.mkdirs();
            }
            // Creates the file
            File file = new File(Environment.getExternalStorageDirectory() + File.separator + "rhythmLock" + File.separator + "rhythm.pass");
            if (file.exists()){
                file.delete();
            }
            fileOutputStream = new FileOutputStream(file);
            ObjectOutputStream objectOutputStream= new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(data);
            objectOutputStream.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException a) {
            a.printStackTrace();
        }
        return false;
    }

    public HashMap<Integer,EventData> readEvent(){
        // Returns Hash if possible, null if error!
        FileOutputStream fileOutputStream = null;
        try {
            File file = new File(Environment.getExternalStorageDirectory() + File.separator + "rhythmLock" + File.separator + "rhythm.pass");
            FileInputStream fileInputStream  = new FileInputStream(file);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            HashMap readedMap = (HashMap) objectInputStream.readObject();
            objectInputStream.close();
            return readedMap;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException a) {
            a.printStackTrace();
        } catch (ClassNotFoundException c) {
            c.printStackTrace();
        }
        return null;
    }

    //Normalize the values in relation to the first touch.
    //Gets recentData as raw data, normalizes it and store it again.
    public HashMap<Integer,EventData> simpleNormalizator(HashMap<Integer,EventData> recentData){

        try {
            long timeZero = recentData.get(1).getTouchTime();
            recentData.get(1).setTouchTime(0);

            //i is the counter of the number of events
            for (int i=2;i<=recentData.size();i++) {

                //Calculates the difference between each event and the first one, and stores it
                long difference;
                difference = Math.abs(recentData.get(i).getTouchTime() - timeZero);
                recentData.get(i).setTouchTime(difference);
                //Store the normalized data into the file system
                //storeEvent(recentData);
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return recentData;
    }


}

package com.app.model;

import java.util.Date;

/**
 *
 * @author G4-T6
 */
public class Activity {
    
    private Date startTime;
    private Date endTime;
    private String currentLocation;
    private String nextLocation;//null means leaving SIS building

    /**
     * Constructs a Activity Object with the specific initial parameters
     * 
     * @param startTime the start time of the activity
     * @param endTime the end time of the activity
     * @param currentLocation current location of the activity
     * @param nextLocation the location of next activity
     */
    public Activity(Date startTime, Date endTime, String currentLocation, String nextLocation) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.currentLocation = currentLocation;
        this.nextLocation = nextLocation;
    }

    /**
     * Return the start time of an instance of an Activity Object
     * 
     * @return a Date Object representing the start time of an activity
     */
    public Date getStartTime() {
        return startTime;
    }

    /**
     * Return the end time of an instance of an Activity Object
     * 
     * @return a Date Object representing the end time of an activity
     */
    public Date getEndTime() {
        return endTime;
    }

    /**
     * Return the current location of an instance of an Activity Object
     * 
     * @return a String Object representing current location
     */
    public String getCurrentLocation() {
        return currentLocation;
    }

    /**
     * Return the next location of an instance of an Activity Object
     *
     * @return a String object representing nextLocation
     */
    public String getNextLocation() {
        return nextLocation;
    }
    
    /**
     * Return the duration of an instance of an Activity Object
     *
     * @return the duration of the whole activity which belongs to primitive type of long
     */
    public long getDuration(){
        long start = startTime.getTime();
        long end  = startTime.getTime();
        return end - start;
    }

    /**
     * Set the end time of an instance of an Activity Object
     *
     * @param endTime a Date
     */
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    /**
     * Set the next location where the user will move to after current activity finishes
     *
     * @param nextLocation a String
     */
    public void setNextLocation(String nextLocation) {
        this.nextLocation = nextLocation;
    }
    
    /**
     * Return a Pair Object that contains the start time and end time of the period when two activities have overlap
     *
     * @param anotherActivity another specific Activity Object
     * @return a pair Object of the start time and end time of the overlapped period using a Pair Object, null if two activities are not overlapped
     */
    public Pair<Date, Date> overlap(Activity anotherActivity){//return null if do not overlap
        Pair<Date, Date> timePair= null;
        Date sTime = anotherActivity.getStartTime();
        Date eTime = anotherActivity.getEndTime();
        if(!(endTime.getTime() <= sTime.getTime() || startTime.getTime() >= eTime.getTime())){
            Date d1;
            Date d2;
            if(sTime.after(startTime)){
                d1 = sTime;
            }else{
                d1 = startTime;
            }
            if(eTime.before(endTime)){
                d2 = eTime;
            }else{
                d2 = endTime;
            }
            timePair = new Pair(d1, d2);
        }
        return timePair;
    }
    
    /**
     * Compare the start time of this Activity Object against the start time of another specific Activity Object.Return true if the start times are the same
     *
     * @param anotherActivity another specific Activity Object
     * @return true if the start times are the same, false otherwise
     */
    public boolean startTogether(Activity anotherActivity){
        Date anotherStartTime = anotherActivity.getStartTime();
        if(startTime.getTime() == anotherStartTime.getTime()){
            return true;
        }else{
            return false;
        }
    }
    
    /**
     * Compare the end time of this Activity Object against the end time of another specific Activity Object.Return true if the end times are the same
     *
     * @param anotherActivity another specific Activity Object
     * @return true if the end times are the same, false otherwise
     */
    public boolean endTogether(Activity anotherActivity){
        Date anotherEndTime = anotherActivity.getEndTime();
        if(endTime.getTime() == anotherEndTime.getTime()){
            return true;
        }else{
            return false;
        }
    }
    
    /**
     * Check whether this Activity is staying in the same group with another specific Activity
     *
     * @param anotherActivity another specific Activity Object
     * @return true if two activities stay in a group, false otherwise
     */
    public boolean stayAsGroup(Activity anotherActivity){
        String anotherLocation = anotherActivity.getCurrentLocation();
        if(currentLocation.equals(anotherLocation) && overlap(anotherActivity) != null){
            Pair<Date, Date> timePair = overlap(anotherActivity);
            long duration = timePair.getSecond().getTime() - timePair.getFirst().getTime();
            if(duration >= 300000){
                return true;
            }
            return false;
        }else{
            return false;
        }
    }
    
    /**
     * Check whether this Activity move to another location together with another specific Activity
     *
     * @param anotherActivity another specific Activity Object
     * @return true if two activities move together, false otherwise
     */
    public boolean moveTogether(Activity anotherActivity){
        String anotherCurrent = anotherActivity.getCurrentLocation();
        String anotherNext = anotherActivity.getNextLocation();
        if(nextLocation != null && anotherNext != null && currentLocation.equals(anotherCurrent) && nextLocation.equals(anotherNext) && endTogether(anotherActivity)){
            return true;
        }else{
            return false;
        }
    }
}

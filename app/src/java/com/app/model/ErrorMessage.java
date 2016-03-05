/*
 * ErrorMessage is an Object which tracks the error of csv files after all kinds of validtions
 */
package com.app.model;

/**
 *
 * @author G4T6
 */
public class ErrorMessage implements Comparable<ErrorMessage>{
    private String fileName;
    private int rowNo;
    private String[] message;

    /**
     * Construct an ErrorMessage Object consists of the following parameters
     *
     * @param fileName the file name of the error appears
     * @param rowNo the row number of the error appears
     * @param message the specific error message of the error
     */
    public ErrorMessage(String fileName, int rowNo, String message[]) {
        this.fileName = fileName;
        this.rowNo = rowNo;
        this.message = message;
    }

    /**
     * Get the file name of the error appears
     *
     * @return a String value represents the file name of the error appears
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Get the row number of the error appears
     *
     * @return an integer represents the row umber of the error appears
     */
    public int getRowNo() {
        return rowNo;
    }

    /**
     * Get the specific error messages
     *
     * @return a String array represents the specific error messages
     */
    public String[] getMessage() {
        return message;
    }
    
    @Override
    public int compareTo(ErrorMessage other){
        ErrorMessage otherError  = (ErrorMessage)other;
        int fileCompare = getFileName().compareTo(otherError.getFileName());
        int rowCompare = getRowNo() - otherError.getRowNo();
        if(fileCompare != 0){
            return fileCompare;
        } else {
            return rowCompare;
        }
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.app.model;

import com.opencsv.CSVReader;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.servlet.http.Part;

/**
 *
 * @author G4T6
 */
public class ZipFileReader {
    /**
     *Construct a ZipFileReader Object with the following parameters
     */
    public ZipFileReader(){
    
    }
    /**
     *Reads the file
     * @param filepart file chosen for uploading
     * @return a Pair of HashMap of files uploaded and List of data deleted
     * @throws IOException
     */
    public Pair<HashMap<String, List<String[]>>, List<List<String[]>>> readFile(Part filepart) throws IOException {
        //2D array list
        HashMap<String, List<String[]>> content = new HashMap<>();
        List<List<String[]>> deletion = new ArrayList<>();
        InputStream is = filepart.getInputStream();
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));
        ZipEntry ze = null;

        while ((ze = zis.getNextEntry()) != null) {
            String csvname = ze.getName();
            InputStreamReader isr = new InputStreamReader(zis, "UTF-8");
            CSVReader reader = new CSVReader(isr);
            
            if (csvname.equals("location-delete.csv")) {
                deletion.add(reader.readAll());
            } else {
                content.put(csvname, reader.readAll());
            }
        }
        
        Pair<HashMap<String, List<String[]>>, List<List<String[]>>> pair = new Pair(content, deletion);
        return pair;

    }
}

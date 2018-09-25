package org.wind.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

public class FileHandle {
    public static List<String> getDicList(String destFilename) {
        List<String> dicList =new ArrayList<>();
        try {
            FileReader fileReader =new FileReader(destFilename);
            BufferedReader buffReader =new BufferedReader(fileReader);
            String line;
            while((line = buffReader.readLine()) != null) {
                if(line.length() > 0){
                    dicList.add(line);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return dicList;
    }

    public static int getFileLineNum(String destFileName) {
        int linenumber = 0;
        try {
            File file = new File(destFileName);
            if (file.exists()) {
                FileReader fr = new FileReader(file);
                LineNumberReader lnr = new LineNumberReader(fr);
                while (lnr.readLine() != null) {
                    linenumber++;
                }
                lnr.close();
            } else {
                System.out.println("File does not exists!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return linenumber;
    }
}

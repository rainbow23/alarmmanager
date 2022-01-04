package com.codechacha.alarmmanager;

import android.content.Context;
import android.util.Log;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Locale;

class InternalFileReadWrite {

    private Context context;
    private String FILE_NAME = "log.txt";
    private StringBuffer stringBuffer;

    InternalFileReadWrite(Context context){
        this.context = context;

    }

    void clearFile(){
        // ファイル削除
        context.deleteFile(FILE_NAME);
        // StringBuffer clear
        stringBuffer.setLength(0);
    }

    // ファイルを保存
    void writeFile() {

        stringBuffer = new StringBuffer();

        long currentTime = System.currentTimeMillis();
        SimpleDateFormat dataFormat =
                new SimpleDateFormat("hh:mm:ss", Locale.US);
        String cTime = dataFormat.format(currentTime);
        Log.d("debug", cTime);

        stringBuffer.append(cTime);
        stringBuffer.append(System.getProperty("line.separator"));// 改行

        // try-with-resources
        try (FileOutputStream fileOutputstream =
                     context.openFileOutput(FILE_NAME,
                             Context.MODE_APPEND)){

            fileOutputstream.write(stringBuffer.toString().getBytes());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ファイルを読み出し
    String readFile() {

        stringBuffer = new StringBuffer();

        // try-with-resources
        try (FileInputStream fileInputStream = context.openFileInput(FILE_NAME);
             BufferedReader reader= new BufferedReader(
                     new InputStreamReader(fileInputStream, StandardCharsets.UTF_8))
        ) {

            String lineBuffer;

            while( (lineBuffer = reader.readLine()) != null ) {
                stringBuffer.append(lineBuffer);
                stringBuffer.append(System.getProperty("line.separator"));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return stringBuffer.toString();
    }
}
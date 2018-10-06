package com.fahim.fileuploadphp.Utils;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

/**
 * Created by root on 22/7/18.
 */

public class ProgressRequest extends RequestBody {

    private File file;
    private UploadCallBacks listener;
    private static final int Default_Buffer_size = 4096;

    public ProgressRequest(File file, UploadCallBacks listener) {
        this.file = file;
        this.listener = listener;
    }

    @Nullable
    @Override
    public MediaType contentType() {
        return MediaType.parse("image/*");
    }

    @Override
    public long contentLength() throws IOException {
        return file.length();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {

        long filelength=file.length();
        byte[] buffer=new byte[Default_Buffer_size];
        FileInputStream fileInputStream=new FileInputStream(file);
        long uploaded=0;

        try{
            int read;
            Handler handler=new Handler(Looper.getMainLooper());
            while ((read=fileInputStream.read(buffer))!=-1){

                handler.post( new ProgressUpdater(uploaded,filelength));
                uploaded+=read;
                sink.write(buffer,0,read);

            }
        }finally {
            fileInputStream.close();
        }


    }


    private class ProgressUpdater implements Runnable {
        private long uploaded;
        private long filelength;
        public ProgressUpdater(long uploaded, long filelength) {
            this.uploaded=uploaded;
            this.filelength=filelength;
        }

        @Override
        public void run() {
            listener.onProgressUpdate((int) (100*uploaded/filelength));
        }
    }
}

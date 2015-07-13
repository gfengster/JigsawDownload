package com.abc.download;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.abc.util.Observable;

class Downloader extends Observable<Long> implements Runnable {
    
    private static final int WRITE_TIMEOUT = 100;
    
    private final URL mUrl;
    private final long mStart;
    private final long mEnd;
    private final int mId;
    private final AsynchronousFileChannel mOutput;
    
    private long mDownloadedSize;
    
    Downloader(int id, AsynchronousFileChannel output, URL url, long start, long end){
        mUrl = url;
        mStart = start;
        mEnd = end;
        mOutput = output;
        
        mId = id;
    }
    
    @Override
    public void run() {
        InputStream instream  = null;
        
        try {
            final URLConnection connection = mUrl.openConnection();
            
            final String byteRange = mStart + "-" + mEnd;
            connection.setRequestProperty("Range", "bytes=" + byteRange);
            connection.connect();
            
            instream = connection.getInputStream();
            
            byte[] buf = new byte[2048];
            int len;
            
            while( (len = instream.read(buf)) != -1) {
                
                Future<Integer> result = mOutput.write(ByteBuffer.wrap(buf, 0, len), mStart + mDownloadedSize);
                mDownloadedSize += len;
                
                mOutput.force(true);
                int count =0;
                while(!result.isDone()) {
                    if ((count++) > WRITE_TIMEOUT)
                        break;
                    Thread.sleep(1);
                }
                
                if (len != result.get()){
                    throw new WriteException(mId + " " + byteRange);
                }
                
                super.setChanged();
                super.notifyObservers((long)len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (WriteException e) {
            e.printStackTrace();
        } finally {
            if (instream != null)
                try {
                    instream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }
    
    private static class WriteException extends Exception{
        private static final long serialVersionUID = -2702394489755157875L;

        WriteException(String msg){
            super(msg);
        }
    }
}
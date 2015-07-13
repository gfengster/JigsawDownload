/**
 * Copyright (C) 2015 Guangjie Feng
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.abc.download;

import static java.lang.System.out;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import com.abc.download.management.Monitoring;
import com.abc.util.Observable;
import com.abc.util.Observer;

public final class DownloadManager implements Observer<Long>, DownloadManagerMBean {
    public static DownloadManager getManager(String out, String address, int numThread)
            throws IOException, InterruptedException {
        final File file = new File(out);
        final File outfile;
        if (file.isDirectory()) {
            final String name = address.substring(address.lastIndexOf('/'), address.length());
            if (name.trim().isEmpty())
                throw new IllegalArgumentException(address + " is not valid file.");
                
            outfile = new File(file, name);
           
        } else {
            file.delete();
            outfile = file;
        }
        
        DownloadManager mgr = new DownloadManager(outfile, address, numThread);
        try {
            MBeanServer svr =  Monitoring.getMonitorServer();
            svr.registerMBean(mgr, new ObjectName("com.abc.download:type=Manager"));
        } catch (InstanceAlreadyExistsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MBeanRegistrationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NotCompliantMBeanException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MalformedObjectNameException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return mgr;
    }
    
    public static DownloadManager getManager(String out, String address)
            throws IOException, InterruptedException {
        return getManager(out, address, Runtime.getRuntime().availableProcessors());
    }
        
    private final String mAddress;
    private final int mNumThread;
    
    private final ExecutorService mPool;
    
    private long mTotalSize;
    
    private volatile long mDownloadedSize = 0;
    
    private final ReentrantLock mLock = new ReentrantLock();
    
    private volatile Status mStatus = Status.STOPPED;
    
    private final File mOutfile;
    
    private DownloadManager(File outfile, String address, int numThread) {
        mStatus = Status.STARTING;
        
        mAddress = address;
        mNumThread = numThread;
        mPool = Executors.newFixedThreadPool(mNumThread);
        
        mOutfile = outfile;

    }
    
    public void download() throws IOException, InterruptedException {
        final AsynchronousFileChannel output = AsynchronousFileChannel.open(
                Paths.get(mOutfile.toURI()), 
                StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        
        final URL url = new URL(mAddress);
        
        final URLConnection conn = url.openConnection();
        mTotalSize = conn.getContentLengthLong();
        
        out.println("total download size: " + mTotalSize);
        
        final int range = Math.round(mTotalSize / (float)mNumThread);
        
        out.println("range: " + range);
        
        mStatus = Status.STARTED;
        
        for (int i = 0; i < mNumThread; i++) {
            long start = i*range;
            long end;
            
            if (i != (mNumThread-1)) {
                end = start + range - 1;
            } else {
                end = mTotalSize;
            }

            //out.println(i + " " + start + " - " + end);

            final Downloader d = new Downloader(i, output, url, start, end);
            d.addObserver(this);
            
            mPool.execute(d);
            
            mStatus = Status.DOWNLOADING;
        }
        
        mPool.shutdown(); // Disable new tasks from being submitted
        try {
          // Wait a while for existing tasks to terminate
          if (!mPool.awaitTermination(1, TimeUnit.HOURS)) {
              mPool.shutdownNow(); // Cancel currently executing tasks
            // Wait a while for tasks to respond to being cancelled
            if (!mPool.awaitTermination(1, TimeUnit.HOURS))
                System.err.println("Pool did not terminate");
          }
        } catch (InterruptedException ie) {
          // (Re-)Cancel if current thread also interrupted
            mPool.shutdownNow();
          // Preserve interrupt status
          Thread.currentThread().interrupt();
        } finally {
            output.close();
        
            mStatus = Status.FINISHED;
        }
        
        out.println("download finish");
    }
    
    @Override
    public void update(Observable<Long> o, Long arg) {
        
        try{ 
            if (mLock.tryLock() || mLock.tryLock(1, TimeUnit.HOURS)) {
            
                mDownloadedSize += arg;

//                System.out.print(Thread.currentThread().getId() + " ");
//                System.out.println("Total Size: " + getTotalSize() 
//                        + " Downloaded: " + getDownloadedSize() + " " 
//                        + String.format("%2.2f", 
//                                Math.floor(10000*getDownloadedSize()/(double)getTotalSize())/100) + "%");
            
              //  Thread.sleep(100000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (mLock.isHeldByCurrentThread())
                mLock.unlock();
        }
    }

    @Override
    public long getTotalSize(){
        return mTotalSize;
    }
    
    @Override
    public int getDownloadThread() {
        return mNumThread;
    }
    
    @Override
    public String getStatus(){
        String ret = "";
       
        try {
            if (mLock.tryLock() || mLock.tryLock(1, TimeUnit.HOURS)) {
                ret = mStatus.name();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (mLock.isHeldByCurrentThread())
                mLock.unlock();
           
        }
        return ret;
    }

    @Override
    public long getDownloadedSize(){
        long ret = 0;
        
        try {
            if (mLock.tryLock() || mLock.tryLock(1, TimeUnit.HOURS)) {
                ret = mDownloadedSize;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (mLock.isHeldByCurrentThread())
                mLock.unlock();
           
        }
        return ret;
    }
}

package com.abc.download;

public interface DownloadManagerMBean {
    public int getDownloadThread();
    public String getStatus();
    public long getTotalSize();
    public long getDownloadedSize();
}

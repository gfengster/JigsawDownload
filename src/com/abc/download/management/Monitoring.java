package com.abc.download.management;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;

public class Monitoring {
    public static MBeanServer getMonitorServer(){
        Monitoring mon = new Monitoring();
        
        return mon.getServer();
    }
    
    private final MBeanServer mbs;
    
    private Monitoring() {
        mbs = ManagementFactory.getPlatformMBeanServer();
        
    }
    
    private MBeanServer getServer(){
        return mbs;
    }
}

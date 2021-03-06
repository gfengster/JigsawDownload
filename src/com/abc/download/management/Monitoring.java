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

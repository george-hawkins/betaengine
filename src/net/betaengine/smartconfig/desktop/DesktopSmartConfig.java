/**
 * Copyright 2013 George C. Hawkins
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.betaengine.smartconfig.desktop;

import java.util.List;

public interface DesktopSmartConfig
{
    public interface FinishedCallback
    {
        enum Result
        {
            SUCCESS,
            ERROR,
            TIMEOUT
        };
        
        void finished(Result result, Exception e);
    };
    
    String getNetworkName();
    
    String getGateway();
    
    List<String> getWarnings();
    
    // Note: there are no guarantees as to the thread from which the callback will be invoked.
    void startBroadcasting(FinishedCallback callback, String networkName, String password, String gateway, String aesKey, String deviceName);
    
    void stopBroadcasting();
}

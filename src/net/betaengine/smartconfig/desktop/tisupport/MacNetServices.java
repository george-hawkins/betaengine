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
package net.betaengine.smartconfig.desktop.tisupport;

import java.net.NetworkInterface;

import com.ti.smartconfig.SmartConfigOsNetServices;

public class MacNetServices implements SmartConfigOsNetServices
{
    private final static String AIRPORT_CMD = "/System/Library/PrivateFrameworks/Apple80211.framework/Versions/Current/Resources/airport";

    private NetworkInterface networkInterface;
    private String device;
    private String ssid;
    private String gateway;
    private boolean is5GHz;

    public MacNetServices()
    {
        try
        {
            String output = CommandExecutor.execute(AIRPORT_CMD + " -I");
            
            int offset = output.indexOf(" SSID: ");
            
            if (offset == -1)
            {
                System.err.println("Error: wifi is absent or not enabled.");
                return;
            }
            
            offset += 7;
            
            int end = lineEnd(output, offset);
            
            ssid = output.substring(offset, end);

            offset = output.indexOf("channel: ", end);
            offset += 9;

            end = lineEnd(output, offset);

            int channel = Integer.parseInt(output.substring(offset, end));

            is5GHz = channel > 14; // Channels 1 to 14 are the 2.4GHz band.

            // ---------------------------------------------------------
            
            output = CommandExecutor.execute("networksetup -listallhardwareports");
            offset = output.indexOf("Hardware Port: Wi-Fi");

            offset = output.indexOf("Device: ", offset);
            offset += 8;
            end = lineEnd(output, offset);

            device = output.substring(offset, end);

            networkInterface = NetworkInterface.getByName(device);

            // ---------------------------------------------------------

            output = CommandExecutor.execute("route -n get -ifscope " + device + " default");

            offset = output.indexOf("gateway: ");
            offset += 9;

            end = lineEnd(output, offset);

            gateway = output.substring(offset, end);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    private int lineEnd(String s, int offset)
    {
        return s.indexOf('\n', offset);
    }

    @Override
    public String getInterfaceName()
    {
        return device;
    }
    
    @Override
    public String getSSID(String interfaceName)
    {
        return ssid;
    }

    @Override
    public String getDefaultGateWay(String interfaceName)
    {
        return gateway;
    }

    @Override
    public boolean get5GHz(String interfaceName)
    {
        return is5GHz;
    }

    @Override
    public NetworkInterface getNetworkInterface()
    {
        return this.networkInterface;
    }

    @Override
    public String getMTU(String interfaceName)
    {
        // There's no need for OS specific logic to retrieve the MTU as this can be done
        // in plain Java using NetworkInterface.
        // See net.betaengine.smartconfig.desktop.tisupport.SafeNetServices.checkMtu(...).
        throw new UnsupportedOperationException();
    }
}

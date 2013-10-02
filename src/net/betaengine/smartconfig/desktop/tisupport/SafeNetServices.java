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

import java.util.ArrayList;
import java.util.List;

import net.betaengine.smartconfig.desktop.TextBundle;

import com.ti.smartconfig.SmartConfigOsNetServices;
import com.ti.smartconfig.SmartConfigOsServicesException;
import com.ti.smartconfig.VoidOsNetServices;
import com.ti.smartconfig.Windows7NetServices;

// This class provides a less exception laden and simpler interface to the TI
// SmartConfigOsNetServices implementations.
// In this class I use the names I've used throughout my application rather than the
// names TI uses in SmartConfigOsNetServices, e.g. networkName instead of ssid etc.
public class SafeNetServices
{
    private final static int MIN_MTU_SIZE = 1500;
    
    private final String networkName;
    private final String gateway;
    private final List<String> warnings = new ArrayList<>();
    
    public SafeNetServices()
    {
        SmartConfigOsNetServices services = getNetServices();
        String interfaceName = getInterfaceName(services);
        
        if (interfaceName != null)
        {
            networkName = getNetworkName(services, interfaceName);
            gateway = getGateway(services, interfaceName);
            
            checkBand(services, interfaceName);
            checkMtu(services, interfaceName);
        }
        else
        {
            warnings.add(TextBundle.INTERFACE_WARNING);
            
            networkName = null;
            gateway = null;
        }
    }
    
    public List<String> getWarnings()
    {
        return warnings;
    }
    
    public String getNetworkName()
    {
        return networkName;
    }

    public String getGateWay()
    {
        return gateway;
    }

    private String getInterfaceName(SmartConfigOsNetServices services)
    {
        try
        {
            return emptyToNull(services.getInterfaceName());
        }
        catch (SmartConfigOsServicesException e)
        {
            e.printStackTrace();
            return null;
        }
    }
    
    private String getNetworkName(SmartConfigOsNetServices services, String interfaceName)
    {
        try
        {
            return emptyToNull(services.getSSID(interfaceName));
        }
        catch (SmartConfigOsServicesException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    private String getGateway(SmartConfigOsNetServices services, String interfaceName)
    {
        try
        {
            return emptyToNull(services.getDefaultGateWay(interfaceName));
        }
        catch (SmartConfigOsServicesException e)
        {
            e.printStackTrace();
            return null;
        }
    }
    
    private void checkBand(SmartConfigOsNetServices services, String interfaceName)
    {
        try
        {
            boolean is5GHz = services.get5GHz(interfaceName);
            
            if (is5GHz)
            {
                warnings.add(TextBundle.BAND_WARNING);
            }
        }
        catch (SmartConfigOsServicesException e)
        {
            e.printStackTrace();
        }
    }
    
    private void checkMtu(SmartConfigOsNetServices services, String interfaceName)
    {
        try
        {
            int mtu = services.getNetworkInterface().getMTU();
            
            if (mtu < MIN_MTU_SIZE)
            {
                warnings.add(TextBundle.format(TextBundle.MTU_WARNING, MIN_MTU_SIZE));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    private SmartConfigOsNetServices getNetServices()
    {
        String os = System.getProperty("os.name").toLowerCase();
        
        if (os.contains("windows"))
        {
            return new Windows7NetServices();
        }
        else if (os.contains("mac"))
        {
            return new MacNetServices();
        }
        else if (os.contains("linux"))
        {
            return new BugfixLinuxNetServices();
        }
        else
        {
            return new VoidOsNetServices();
        }
    }
    
    // TI code uses a confused mix of null and empty strings.
    private String emptyToNull(String s)
    {
        return s == null ? null : (s.isEmpty() ? null : s);
    }
}

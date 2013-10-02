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

import java.net.DatagramSocket;
import java.nio.charset.Charset;
import java.util.List;

import net.betaengine.smartconfig.desktop.DesktopSmartConfig;

import com.ti.smartconfig.FirstTimeConfig;
import com.ti.smartconfig.FirstTimeConfigListener;

public class DesktopSmartConfigImpl implements DesktopSmartConfig
{
    public static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");
    
    // Unusual constants used by TI to indicate no password or AES key.
    // The values are odd, "KEY" for password and "ENC" for AES key, along with inconsistent
    // use of case - "No"/"NO". Many people would have just stuck with using null instead.
    private final static String NO_PASSWORD = "^^No-KEY^^";
    private final static String NO_AES_KEY = "^^NO-ENC^^";
    
    private final SafeNetServices netServices = new SafeNetServices();
    
    private FirstTimeConfig config;
    
    public DesktopSmartConfigImpl()
    {
        System.setProperty("java.net.preferIPv4Stack", "true");
    }
    
    @Override
    public String getNetworkName()
    {
        return netServices.getNetworkName();
    }
    
    @Override
    public String getGateway()
    {
        return netServices.getGateWay();
    }
    
    @Override
    public List<String> getWarnings()
    {
        return netServices.getWarnings();
    }
    
    @Override
    public void startBroadcasting(FinishedCallback callback, String networkName, String password, String gateway, String aesKey, String deviceName)
    {
        try
        {
            if (password == null)
            {
                password = NO_PASSWORD;
            }
            if (aesKey == null)
            {
                aesKey = NO_AES_KEY;
            }

            // TI just call getBytes() without a charset in their applet version - a clear problem.
            // I don't know what the correct charset is but will assume ISO-8859-1 until corrected.
            config = new FirstTimeConfig(
                wrap(callback),
                password, aesKey.getBytes(ISO_8859_1),
                gateway, networkName, deviceName);
            
            config.setTransmitDataGramSocket(new DatagramSocket());
            config.transmitSettings();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    @Override
    public void stopBroadcasting()
    {
        try
        {
            config.stopTransmitting();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    private FirstTimeConfigListener wrap(final FinishedCallback callback)
    {
        return new FirstTimeConfigListener()
        {
            @Override
            public void onFirstTimeConfigEvent(FtcEvent event, Exception e)
            {
                // Bizarrely the TI logic doesn't actually stop sending under various
                // conditions, e.g. TIMEOUT, so ensure that it is always stopped.
                stopBroadcasting();
                
                FinishedCallback.Result result;
                
                switch (event)
                {
                case FTC_SUCCESS:
                    result = FinishedCallback.Result.SUCCESS;
                    break;
                case FTC_TIMEOUT:
                    result = FinishedCallback.Result.TIMEOUT;
                    break;
                case FTC_ERROR:
                    result = FinishedCallback.Result.ERROR;
                    break;
                default:
                    System.err.println("Error: unknown FtcEvent type " + event);
                    result = FinishedCallback.Result.ERROR;
                }
                
                callback.finished(result, e);
            }
        };
    }
}

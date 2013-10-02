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
package net.betaengine.smartconfig.desktop.ui;

import java.util.List;

import javax.swing.SwingUtilities;

import net.betaengine.smartconfig.desktop.DesktopSmartConfig;

public class SwingSafeSmartConfig implements DesktopSmartConfig
{
    private final DesktopSmartConfig delegate;
    
    public SwingSafeSmartConfig(DesktopSmartConfig delegate)
    {
        this.delegate = delegate;
    }
    
    @Override
    public String getNetworkName()
    {
        return delegate.getNetworkName();
    }

    @Override
    public String getGateway()
    {
        return delegate.getGateway();
    }

    @Override
    public List<String> getWarnings()
    {
        return delegate.getWarnings();
    }

    @Override
    public void startBroadcasting(final FinishedCallback callback,
        String networkName, String password, String gateway,
        String aesKey, String deviceName)
    {
        // Ensure two things:
        // 1. That we get called back on the event thread.
        // 2. That we don't get called back from within the startBroadcasting(...) logic.
        FinishedCallback wrapper = new FinishedCallback()
        {
            @Override
            public void finished(final FinishedCallback.Result result, final Exception e)
            {
                SwingUtilities.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        callback.finished(result, e);
                    }
                });
            }
        };
        
        delegate.startBroadcasting(wrapper, networkName, password, gateway, aesKey, deviceName);
    }

    @Override
    public void stopBroadcasting()
    {
        delegate.stopBroadcasting();
    }
}

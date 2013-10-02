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

import com.ti.smartconfig.LinuxNetServices;

// The TI LinuxNetServices assumptions about how the nm-tool output is formatted do
// not hold true when trying to retrieve the network frequency on Ubuntu 12.04.
public class BugfixLinuxNetServices extends LinuxNetServices
{
    // This over complex sed pattern is just trying to be robust in the face of any formatting.
    private final static String[] GET_FREQUENCY = 
    {
        "bash",
        "-c",
        "nm-tool | sed -n 's/^\\s*\\*.*\\<Freq\\s*\\([0-9]\\+\\).*/\\1/p'"
    };
    
    @Override
    public boolean get5GHz(String interfaceName)
    {
        String frequency = CommandExecutor.execute(GET_FREQUENCY);
        
        if (frequency != null)
        {
            try
            {
                return Integer.parseInt(frequency.trim()) > 4000;
            }
            catch (NumberFormatException e)
            {
                e.printStackTrace();
            }
        }
        
        // Try the superclass method _maybe_ it will work.
        return super.get5GHz(interfaceName);
    }
}

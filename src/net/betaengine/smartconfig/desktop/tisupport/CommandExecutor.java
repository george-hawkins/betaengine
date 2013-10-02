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

import java.io.IOException;

public class CommandExecutor
{
    // This version of execute will split up command at spaces.
    public static String execute(String command)
    {
        try
        {
            return getOutput(Runtime.getRuntime().exec(command));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
    
    // This version of execute expects the caller to have split up the command.
    public static String execute(String... command)
    {
        try
        {
            return getOutput(Runtime.getRuntime().exec(command));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    private static String getOutput(Process process)
    throws InterruptedException, IOException
    {
        process.waitFor();

        StringBuilder builder = new StringBuilder();
        int i;

        while ((i = process.getInputStream().read()) != -1)
        {
            builder.append((char)i);
        }

        process.destroy();

        return builder.toString();
    }
}

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
package net.betaengine.smartconfig.device.decoder;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;

public class Consumer
{
    // Currently we ignore the BSSID and channel information in positions 0 and 1.
    private final static int SOURCE = 2;
    private final static int DESTINATION = 3;
    private final static int LENGTH = 4;
    
    private final Analyzer analyzer = new Analyzer();

    public static void main(String[] args)
    {
        try
        {
            Consumer consumer = new Consumer();
            Reader input = (args.length == 0) ? new InputStreamReader(System.in) : new FileReader(args[0]);
            
            consumer.consume(input);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private void consume(Reader input)
    {
        try
        {
            BufferedReader reader = new BufferedReader(input);
            String line;
            boolean found = false;
            
            while ((line = reader.readLine()) != null)
            {
                if (handle(line))
                {
                    found = true;
                    break;
                }
            }
            
            System.err.println("Scan " + (found ? "succeeded" : "failed"));
            
            reader.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private boolean handle(String frame)
    {
        String[] tokens = frame.split("\\t");
        
        if (tokens.length != 5) return false;
        
        for (String s : tokens)
        {
            if (s.isEmpty()) return false;
        }
        
        int length = Integer.parseInt(tokens[LENGTH]);
        
        return analyzer.process(tokens[SOURCE], tokens[DESTINATION], length);
    }
}

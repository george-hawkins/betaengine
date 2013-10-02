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
package net.betaengine.jarjar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Formatter;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.tonicsystems.jarjar.Main;

public class Repackage
{
    private final static Pattern PROPERTY_PATTERN = Pattern.compile("#.+=.+");
    
    private Exception failure;
    
    public boolean repackage()
    {
        try
        {
            File rules = getRulesFile();
            File parent = rules.getParentFile();
            Properties properties = getProperties(rules);
            String inputName = properties.getProperty("input.jar");
            String outputName = properties.getProperty("output.jar");
            
            if (inputName == null || outputName == null)
            {
                throw new RepackageException("rules file did not contain input and/or output jar properies");
            }
            
            Main jarjar = new Main();
            File input = new File(parent, inputName);
            File output = new File(parent, outputName);
            
            jarjar.process(rules, input, output);
            
            return true;
        }
        catch (Exception e)
        {
            failure = e;
            
            return false;
        }
    }
    
    public Exception getException()
    {
        return failure;
    }
    
    private Properties getProperties(File file) throws IOException
    {
        StringBuilder builder = new StringBuilder();
        Formatter formatter = new Formatter(builder);
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        
        while ((line = reader.readLine()) != null)
        {
            if (PROPERTY_PATTERN.matcher(line).matches())
            {
                line = line.substring(1); // Remove leading hash.
                formatter.format("%s%n", line); // %n is a nice way to get platform line separator.
            }
        }
        
        reader.close();
        formatter.close();
        
        Properties properties = new Properties();
        
        properties.load(new StringReader(builder.toString()));
        
        return properties;
    }
    
    private File getRulesFile() throws URISyntaxException
    {
        URL url = getClass().getProtectionDomain().getCodeSource().getLocation();
        File file = new File(url.toURI());
        
        if (file.isFile())
        {
            // Assume this is a jar and get the containing directory.
            file = file.getParentFile();
        }
        
        if (!file.canWrite())
        {
            throw new RepackageException(file + " is not a writeable directory");
        }
        
        return new File(file, "rules.txt");
    }
    
    @SuppressWarnings("serial")
    private static class RepackageException extends RuntimeException
    {
        public RepackageException(String message) { super(message); }
    }
    
    private static void userInterface(final boolean ok, final String message)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                
                String title = ok ? "Info" : "Error";
                int type = ok ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE;
                
                JOptionPane.showMessageDialog(null, message, title, type);
                System.exit(0);
            }
        });
    }
    
    public static void main(String[] args)
    {
        boolean useUserInterface = args.length != 1 || !args[0].equals("-noui");
        Repackage repackage = new Repackage();
        boolean ok = repackage.repackage();
        String message = ok ?
            "Repackaging was successful." : "Repackaging failed (" + repackage.getException() + ").";
        
        if (useUserInterface)
        {
            userInterface(ok, message);
        }
        else
        {
            System.err.println((ok ? "Info" : "Error") + ": " + message);
        }
    }
}

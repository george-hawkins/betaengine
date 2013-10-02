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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;

import net.betaengine.smartconfig.desktop.TextBundle;

public class BroadcastPane
{
    private final static long BROADCASE_TIMEOUT = 5 * 60 * 1000; // 5 minutes in milliseconds - set by TI.
    private final static int PROGRESS_BAR_MAX = 500;
    
    private final JDialog dialog;
    private final CanceledCallback callback;
    private Timer timer;
    
    public interface CanceledCallback
    {
        void canceled();
    }
    
    public BroadcastPane(JFrame frame, String message, String buttonText, CanceledCallback callback)
    {
        this.callback = callback;
        
        String[] options = { buttonText };
        
        JOptionPane pane = new JOptionPane(
            getBroadcastPanel(message),
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.PLAIN_MESSAGE,
            null,
            options, options[0]);
        
        pane.addPropertyChangeListener(new ValueSetListener(pane)
        {
            @Override
            public void valueSet(Object value)
            {
                canceled();
            }
        });
        
        dialog = new JDialog(frame, TextBundle.BROADCASTING_TITLE, true);
        
        dialog.setResizable(false);
        dialog.setContentPane(pane);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                canceled();
            }
        });
        
        dialog.pack();
        dialog.setLocationRelativeTo(frame);
    }
    
    public void show() { dialog.setVisible(true); }
    
    public void stop() { timer.stop(); }
    
    public void hide()
    {
        stop();
        dialog.setVisible(false);
    }
    
    private void canceled()
    {
        hide();
        callback.canceled();
    }
    
    private JPanel getBroadcastPanel(String message)
    {
        JPanel panel = new JPanel();
        
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        JLabel label = new JLabel(message);
        
        final JProgressBar progressBar = new JProgressBar(0, PROGRESS_BAR_MAX);
        
        progressBar.setValue(PROGRESS_BAR_MAX);
        
        final long start = System.currentTimeMillis();

        timer = new Timer(200, new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                long diff = System.currentTimeMillis() - start;
                
                diff *= PROGRESS_BAR_MAX;
                diff /= BROADCASE_TIMEOUT;
                
                progressBar.setValue(PROGRESS_BAR_MAX - (int)diff);
            }
        });
        
        timer.start();
        
        panel.add(label, BorderLayout.CENTER);
        panel.add(Box.createVerticalStrut(12));
        panel.add(progressBar, BorderLayout.SOUTH);
        
        return panel;
    }
}

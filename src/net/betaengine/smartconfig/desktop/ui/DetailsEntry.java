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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URL;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.text.AbstractDocument;
import javax.swing.text.JTextComponent;

import net.betaengine.smartconfig.desktop.DesktopSmartConfig;
import net.betaengine.smartconfig.desktop.DesktopSmartConfig.FinishedCallback;
import net.betaengine.smartconfig.desktop.TextBundle;

public class DetailsEntry
{
    private final static int NETWORK_NAME_MAX_LEN = 32;
    private final static int PASSWORD_MAX_LEN = 32;
    private final static int GATEWAY_MAX_LEN = 15; // XXX.XXX.XXX.XXX
    private final static int AES_KEY_LEN = 16; // TI library enforced length.
    private final static int DEVICE_NAME_MAX_LEN = 63; // RFC 1034 section 3.1.
    
    private final static String DEFAULT_DEVICE_NAME = "CC3000"; // Chosen by TI.
 
    private final static String SEND_OPTION = TextBundle.SEND;
    private final static String WIFI_60_IMAGE = "images/wifi-60.png";
    
    private final static Preferences PREFS = Preferences.userNodeForPackage(DetailsEntry.class);
    private final static String NO_DEVICE_NAME_PREF = "no.device.name";
    
    private final JFrame frame = new JFrame(TextBundle.MAIN_TITLE);
    
    private final JTextField networkNameField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JTextField gatewayField = new JTextField(20);
    private final JPasswordField aesKeyField = new JPasswordField(20);
    private final JTextField deviceNameField = new JTextField(20);
    
    private final DesktopSmartConfig smartConfig;
    
    private BroadcastPane broadcastPane;
    
    public DetailsEntry(DesktopSmartConfig smartConfig)
    {
        this.smartConfig = new SwingSafeSmartConfig(smartConfig);
    }
    
    public void show()
    {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        
        JOptionPane pane = createOptionPane(smartConfig.getNetworkName(), smartConfig.getGateway());
        
        frame.getContentPane().add(pane);

        frame.pack();
        frame.setLocationRelativeTo(null); // Center.
        frame.setVisible(true);
        
        showSmartConfigWarnings();
    }
    
    private void showSmartConfigWarnings()
    {
        for (String warning : smartConfig.getWarnings())
        {
            showWarning(warning);
        }
    }
    
    private JOptionPane createOptionPane(String networkName, String gateway)
    {
        Object[] options = { SEND_OPTION };
        
        final JOptionPane pane = new JOptionPane(createFieldsPanel(networkName, gateway),
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.INFORMATION_MESSAGE,
            getIconImage(WIFI_60_IMAGE),
            options,
            options[0]);
        
        pane.addPropertyChangeListener(new ValueSetListener(pane)
        {
            @Override
            public void valueSet(Object value)
            {
                if (value.equals(SEND_OPTION))
                {
                    doSend();
                }
                else if (value.equals(-1)) // The value is set to -1 if escape is pressed.
                {
                    System.exit(0);
                }
            }
        });
        
        return pane;
    }
    
    private void doSend()
    {
        String networkName = getText(networkNameField);
        String password = getText(passwordField);
        String gateway = getText(gatewayField);
        String aesKey = getText(aesKeyField);
        String deviceName = getText(deviceNameField);
        
        if (networkName == null)
        {
            showError(TextBundle.NO_NETWORK_NAME_ERROR);
            networkNameField.requestFocusInWindow();
            return;
        }
        
        // Note: a full AES-128 key would require 32 hex digits but TI's FirstTimeConfig
        // class requires a key of arbitrary characters of exactly length 16.
        if (aesKey != null && aesKey.length() != AES_KEY_LEN)
        {
            showError(TextBundle.format(TextBundle.AES_KEY_LENGTH_ERROR, AES_KEY_LEN));
            aesKeyField.requestFocusInWindow();
            return;
        }
        
        if (deviceName == null && PREFS.getBoolean(NO_DEVICE_NAME_PREF, true))
        {
            showWarning(getDeviceNameWarningPanel());
        }
        
        FinishedCallback finishedCallback =  new FinishedCallback()
        {
            @Override
            public void finished(final FinishedCallback.Result result, final Exception e)
            {
                doFinished(result, e);
            }
        };
        
        smartConfig.startBroadcasting(finishedCallback, networkName, password, gateway, aesKey, deviceName);
        
        BroadcastPane.CanceledCallback canceledCallback = new BroadcastPane.CanceledCallback()
        {
            @Override
            public void canceled()
            {
                smartConfig.stopBroadcasting();
            }
        };

        if (deviceName != null)
        {
            broadcastPane = new BroadcastPane(frame,
                TextBundle.BROADCASTING_MONITORING, TextBundle.CANCEL, canceledCallback);
        }
        else
        {
            broadcastPane = new BroadcastPane(frame,
                TextBundle.BROADCASTING_MONITORING, TextBundle.STOP, canceledCallback);
        }
        
        broadcastPane.show();
    }
    
    private void doFinished(final FinishedCallback.Result result, final Exception e)
    {
        broadcastPane.stop();
        
        if (result == FinishedCallback.Result.SUCCESS)
        {
            showInformation(TextBundle.BROADCAST_SUCCESS);
        }
        else if (result == FinishedCallback.Result.TIMEOUT)
        {
            // Note: everything could be fine if the problem is an incorrect device name.
            showError(TextBundle.BROADCAST_TIMEOUT);
        }
        else if (result == FinishedCallback.Result.ERROR)
        {
            if (e != null)
            {
                e.printStackTrace();
                showError(TextBundle.format(TextBundle.BROADCAST_ERROR, e));
            }
            else
            {
                showError(TextBundle.BROADCAST_UNDEFINED_ERROR);
                // TODO: maybe capture stderr so at least that can be displayed for the undefined error case.
            }
        }
        broadcastPane.hide();
    }
    
    private JPanel getDeviceNameWarningPanel()
    {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        JLabel label = new JLabel(TextBundle.NO_DEVICE_NAME_WARNING);
        JCheckBox preference = new JCheckBox(TextBundle.NO_DEVICE_NAME_WARNING_DISABLE);
        
        preference.addItemListener(new ItemListener()
        {
            @Override
            public void itemStateChanged(ItemEvent e)
            {
                boolean selected = e.getStateChange() == ItemEvent.SELECTED;
                
                PREFS.putBoolean(NO_DEVICE_NAME_PREF, !selected);
            }
        });
        
        panel.add(label, BorderLayout.CENTER);
        panel.add(preference, BorderLayout.SOUTH);
        
        return panel;
    }
    
    // The TI logic expects nulls rather than empty strings.
    private String getText(JTextField field)
    {
        String s = field.getText();
        
        if (s != null)
        {
            s = s.trim();
            
            if (s.isEmpty())
            {
                s = null;
            }
        }
        
        return s;
    }
    
    private JPanel createFieldsPanel(String networkName, String gateway)
    {
        JPanel panel = new JPanel(new GridBagLayout());
        
        networkNameField.setText(networkName);
        gatewayField.setText(gateway);
        deviceNameField.setText(DEFAULT_DEVICE_NAME);
        
        setSizeLimit(networkNameField, NETWORK_NAME_MAX_LEN);
        setSizeLimit(passwordField, PASSWORD_MAX_LEN);
        setSizeLimit(gatewayField, GATEWAY_MAX_LEN);
        setSizeLimit(aesKeyField, AES_KEY_LEN);
        setSizeLimit(aesKeyField, DEVICE_NAME_MAX_LEN);
        
        addLabelAndField(panel, 0, TextBundle.NETWORK_NAME, networkNameField);
        addLabelAndField(panel, 1, TextBundle.PASSWORD, passwordField);
        addHideCheckBox(panel, 2, TextBundle.HIDE_PASSWORD, passwordField);
        
        // The TI apps all refer to gateway IP address but the TI library will accept a name or address.
        addLabelAndField(panel, 3, TextBundle.GATEWAY, gatewayField);
        
        addLabelAndField(panel, 4, TextBundle.AES_KEY, aesKeyField);
        addHideCheckBox(panel, 5, TextBundle.HIDE_AES_KEY, aesKeyField);
        addLabelAndField(panel, 6, TextBundle.DEVICE_NAME, deviceNameField);
        
        return panel;
    }
    
    private ImageIcon getIconImage(String name)
    {
        URL url = getClass().getResource(name);
        
        return new ImageIcon(url);
    }
    
    private void setSizeLimit(JTextComponent field, int limit)
    {
        AbstractDocument document = (AbstractDocument)field.getDocument();
        document.setDocumentFilter(new DocumentSizeFilter(limit));
    }
    
    private void addHideCheckBox(JPanel panel, int gridy, String hide, final JPasswordField field)
    {
        field.setEchoChar('\0'); // By default show password.
        
        JCheckBox hideCheckBox = new JCheckBox(hide);
        
        hideCheckBox.addItemListener(new ItemListener()
        {
            @Override
            public void itemStateChanged(ItemEvent e)
            {
                boolean selected = e.getStateChange() == ItemEvent.SELECTED;
                final int position = field.getCaretPosition();
                
                field.setEchoChar(selected ? '*' : 0);
                // Move focus to the field and restore the old cursor position rather than
                // selecting all the text (a behavior that only occurs on particular platforms).
                field.addFocusListener(new FocusAdapter()
                {
                    @Override
                    public void focusGained(FocusEvent e)
                    {
                        field.removeFocusListener(this);
                        field.setCaretPosition(position);
                    }
                });
                field.requestFocusInWindow();
            }
        });
        
        GridBagConstraints c = new GridBagConstraints();
        
        c.gridx = 1;
        c.gridy = gridy;
        c.anchor = GridBagConstraints.WEST;
        
        panel.add(hideCheckBox, c);
    }

    private void addLabelAndField(JPanel panel, int gridy, String name, JTextField field)
    {
        JLabel label = new JLabel(name);
        
        GridBagConstraints c = new GridBagConstraints();
        
        c.gridy = gridy;
        
        c.gridx = 0;
        c.anchor = GridBagConstraints.EAST;
        panel.add(label, c);
        
        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.insets = new Insets(1, 4, 0, 0);
        panel.add(field, c);
    }
    
    private void showInformation(Object message)
    {
        JOptionPane.showMessageDialog(frame,
            message,
            TextBundle.INFORMATION_TITLE,
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showWarning(Object message)
    {
        JOptionPane.showMessageDialog(frame,
            message,
            TextBundle.WARNING_TITLE,
            JOptionPane.WARNING_MESSAGE);
    }
    
    private void showError(Object message)
    {
        JOptionPane.showMessageDialog(frame,
            message,
            TextBundle.ERROR_TITLE,
            JOptionPane.ERROR_MESSAGE);
    }
    
    static
    {
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}

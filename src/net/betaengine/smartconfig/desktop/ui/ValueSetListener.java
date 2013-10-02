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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JOptionPane;

public abstract class ValueSetListener implements PropertyChangeListener
{
    private final JOptionPane optionPane;
    
    public ValueSetListener(JOptionPane optionPane)
    {
        this.optionPane = optionPane;
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent e)
    {
        String p = e.getPropertyName();

        if (p.equals(JOptionPane.VALUE_PROPERTY) && !e.getNewValue().equals(JOptionPane.UNINITIALIZED_VALUE))
        {
            valueSet(e.getNewValue());
            
            // If we don't reset the value we won't get called back if the same button is pressed a second time.
            optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
        }
    }
    
    public abstract void valueSet(Object value);
}

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

import java.awt.Toolkit;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

// See http://docs.oracle.com/javase/tutorial/uiswing/components/generaltext.html#filter
public class DocumentSizeFilter extends DocumentFilter
{
    private final int maxChars;

    public DocumentSizeFilter(int maxChars)
    {
        this.maxChars = maxChars;
    }

    @Override
    public void insertString(FilterBypass bypass, int offset, String s, AttributeSet a)
    throws BadLocationException
    {
        s = shrink(s, bypass.getDocument().getLength());
        super.insertString(bypass, offset, s, a);
    }

    @Override
    public void replace(FilterBypass bypass, int offset, int len, String s, AttributeSet a)
    throws BadLocationException
    {
        s = shrink(s, (bypass.getDocument().getLength() - len));
        super.replace(bypass, offset, len, s, a);
    }
    
    private String shrink(String s, int len)
    {
        int chop = (len + s.length()) - maxChars;
        
        if (chop > 0)
        {
            Toolkit.getDefaultToolkit().beep();
            s = s.substring(0, s.length() - chop);
        }
        
        return s;
    }
}

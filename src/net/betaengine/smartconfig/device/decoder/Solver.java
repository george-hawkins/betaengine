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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.common.base.Charsets;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;

public class Solver
{
    private final Multiset<Integer> lengths = HashMultiset.create();
    private final List<List<Integer>> alternatives = new ArrayList<>();
    private final String name;
    private boolean solved = false;
    
    public Solver(String name) { this.name = name; }

    public boolean isSolved() { return solved; }

    public void process(EncodedData encodedData)
    {
        if (solved) return;
        
        lengths.addAll(encodedData.getLengths());
        alternatives.add(encodedData.getData());
        
        place();
    }
    
    private void place()
    {
        int nibbleCount = getNibbleCount();
        
        if (nibbleCount == 0)
        {
            solved(Collections.<Integer>emptyList());
            return;
        }
        
        Set<Integer>[] sequence = createSequence(nibbleCount);
        
        for (List<Integer> data : alternatives)
        {
            place(nibbleCount, sequence, data);
        }
        
        boolean hasHoles = false;
        
        for (Set<Integer> element : sequence)
        {
            if (element.isEmpty())
            {
                hasHoles = true;
                break;
            }
        }
        
        if (!hasHoles)
        {
            LinkedList<Integer> result = new LinkedList<>();
            
            walk(sequence, result);
        }
    }

    private void place(int nibbleCount, Set<Integer>[] sequence, List<Integer> data)
    {
        if (data.isEmpty())
        {
            return;
        }
        
        placeFirst(sequence, data.get(0), data.size() > 1 ? data.get(1) : -1);
        placeRemainder(nibbleCount, sequence, data);
    }

    private void placeFirst(Set<Integer>[] sequence, int first, int second)
    {
        if ((first & 0xF0) == 0)
        {
            sequence[0].add(first);
        }
        else if (second != -1)
        {
            // Get the index of the *next* element, then subtract 1 to get our index.
            int index = getIndex(1, second, first);
            
            if (index > 0)
            {
                add(sequence, index - 1, first);
            }
        }
    }
    
    private void placeRemainder(int nibbleCount, Set<Integer>[] sequence, List<Integer> data)
    {
        Iterator<Integer> i = data.iterator();
        int previous = i.next();
        float factor = nibbleCount / (float)data.size();
        int pos = 1;
        
        while (i.hasNext())
        {
            int current = i.next();
            int expectedIndex = Math.round(pos++ * factor);
            
                int index = getIndex(expectedIndex, current, previous);
                
                add(sequence, index, current);
            
                previous = current;
        }
    }
    
    private void add(Set<Integer>[] sequence, int index, int value)
    {
        if (index >= 0 && index < sequence.length)
        {
            sequence[index].add(value);
        }
    }

    private int getIndex(int expectedIndex, int current, int previous)
    {
        int previousNibble = 0x0F & previous;
        int currentIndex = 0xF0 & current;
        
        currentIndex >>= 4;
        currentIndex ^= previousNibble;
        
        while (expectedIndex - currentIndex > 8)
        {
            currentIndex += 16;
        }

        return currentIndex;
    }
    
    private void walk(Set<Integer>[] sequence, LinkedList<Integer> result)
    {
        if (result.size() == sequence.length)
        {
            // Note: there may be multiple valid solutions so we don't stop on finding the first one.
            solved(result);
            return;
        }
        
        int index = result.size();
        Set<Integer> candidates = sequence[index];
        
        if (index > 0)
        {
            candidates = prune(index, candidates, result.getLast());
        }
        
        for (int candidate : candidates)
        {
            result.addLast(candidate);
            
            walk(sequence, result);
            
            result.removeLast();
        }
    }
    
    private Set<Integer> prune(int index, Set<Integer> candidates, Integer previous)
    {
        index %= 16;
        
        int highNibble = (0x0F & previous) ^ index;
        
        highNibble <<= 4;
        
        Set<Integer> result = new HashSet<>();
        
        for (int candidate : candidates)
        {
            if ((candidate & 0xF0) == highNibble)
            {
                result.add(candidate);
            }
        }
        
        return result;
    }
    
    private void solved(List<Integer> result)
    {
        System.err.print("Solved " + name + ": ");
        print(result);
        solved = true;
    }

    private void print(List<Integer> result)
    {
        System.err.print('[');
        
        int count = result.size() / 2;
        byte[] data = new byte[count];
        Iterator<Integer> i = result.iterator();
        
        for (int j = 0; j < count; j++)
        {
            int highNibble = i.next() & 0x0F;
            int lowNibble = i.next() & 0x0F;
            
            data[j] = (byte)((highNibble << 4) | lowNibble);
        }
        
        // Experimentation shows that the Android and iOS Smart Config apps
        // send data as UTF-8, while the TI Java applet library just uses the
        // default character set of the platform that it's running on.
        String s = new String(data, Charsets.UTF_8);

        System.err.println(escape(s) + ']');
    }
    
    // Replaces any characters that are not printable ISO 8859-1 characters with a Unicode escape.
    private String escape(String s)
    {
        StringBuilder builder = new StringBuilder();
        char[] chars = s.toCharArray();
        
        for (char c : chars)
        {
            if ((c >= ' ' && c <= '~') || (c >= '\u00A1' && c <= '\u00FF'))
            {
                builder.append(c);
            }
            else
            {
                builder.append(String.format("\\u%04X", (int)c));
            }
        }
        
        return builder.toString();
    }

    private Set<Integer>[] createSequence(int count)
    {
        @SuppressWarnings("unchecked")
        Set<Integer>[] result = new Set[count];
        
        for (int i = 0; i < result.length; i++)
        {
            result[i] = new HashSet<Integer>();
        }
        
        return result;
    }

    // If we've got multiple potential length values we sort them by frequency
    // and chose the most frequently seen value, if there's a tie we arbitrarily
    // choose the shortest value.
    private int getNibbleCount()
    {
        Iterator<Multiset.Entry<Integer>> i = Multisets.copyHighestCountFirst(lengths).entrySet().iterator();
        Multiset.Entry<Integer> entry = i.next();
        int minLength = entry.getElement();
        int count = entry.getCount();

        while (i.hasNext())
        {
            entry = i.next();
            if (entry.getCount() < count)
            {
                break;
            }
            minLength = Math.min(minLength, entry.getElement());
        }
        
        return minLength * 2;
    }
}

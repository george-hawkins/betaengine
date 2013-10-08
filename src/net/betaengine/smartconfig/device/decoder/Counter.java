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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class Counter<T>
{
    private final Map<T, Integer> counts = new HashMap<>();
    
    public void add(T value)
    {
        Integer count = counts.get(value);
        
        count = (count == null) ? 1 : (count + 1);
        
        counts.put(value, count);
    }
    
    public Set<T> getMaxCountValues()
    {
        if (counts.isEmpty()) return Collections.emptySet();
        
        Set<T> result = new HashSet<>();
        List<Entry<T, Integer>> sorted = getSortedCounts();
        
        ListIterator<Map.Entry<T, Integer>> i = sorted.listIterator(sorted.size());
        int count = sorted.get(sorted.size() - 1).getValue();
        
        while (i.hasPrevious())
        {
            Map.Entry<T, Integer> entry = i.previous();
            
            if (count != entry.getValue())
            {
                break;
            }
            
            result.add(entry.getKey());
        }
        
        return result;
    }
    
    private List<Map.Entry<T, Integer>> getSortedCounts()
    {
        List<Map.Entry<T, Integer>> entries = new ArrayList<>(counts.entrySet());

        Collections.sort(entries, new Comparator<Map.Entry<T, Integer>>()
        {
            @Override
            public int compare(Map.Entry<T, Integer> o1, Map.Entry<T, Integer> o2)
            {
                return o1.getValue() - o2.getValue();
            }
        });
        
        return entries;
    }
}

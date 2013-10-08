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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import com.google.common.collect.EvictingQueue;

public class LengthDecoder
{
    public final static int SEPARATOR_START = 3;
    public final static int SEPARATOR_END = 23;
    
    private final static int MAX_SIZES = 512; // Might need to be higher for busy n/w.
    private final static int SSID_TAG = 0x577;
    private final static int KEYPHRASE_TAG = 0x5b3;
    
    private final static int DATA_MIN = 593;
    private final static int MAX_UNSIGNED_BYTE = 255;
    private final static int DATA_MAX = DATA_MIN + MAX_UNSIGNED_BYTE;
    
    private final static int LEN_MIN = 28;
    private final static int MAX_SEQUENCE_LEN = 32;
    private final static int LEN_MAX = LEN_MIN + MAX_SEQUENCE_LEN;
    
    private final EvictingQueue<Integer> sizes = EvictingQueue.create(MAX_SIZES);
    private final int offset;
    private final Solver ssidSolver = new Solver("SSID");
    private final Solver keyphraseSolver = new Solver("keyphrase");
    
    private boolean ssidTagSeen = false;
    private boolean keyphraseTagSeen = false;
    
    public LengthDecoder(int offset, Iterable<Integer> previousLengths)
    {
        this.offset = offset;
        
        // The decoder gets created when we see a potential SEPARATOR_END. If it is the real
        // thing then SEPARATOR_START, a tag and a a length are probably in the preceding values.
        for (int length : previousLengths)
        {
            sizes.add(length - offset);
        }
    }

    public boolean add(int length)
    {
        int size = length - offset;
        
        if (size == SSID_TAG)
        {
            ssidTagSeen = true;

            solve(keyphraseTagSeen, keyphraseSolver, KEYPHRASE_TAG);
        }
        else if (size == KEYPHRASE_TAG)
        {
            keyphraseTagSeen = true;
            
            solve(ssidTagSeen, ssidSolver, SSID_TAG);
        }
        
        sizes.add(size);
        
        return ssidSolver.isSolved() && keyphraseSolver.isSolved();
    }
    
    private void solve(boolean otherSeen, Solver solver, int separator)
    {
        if (otherSeen)
        {
            List<Set<Integer>> chunks = getChunks(separator);
            
            if (chunks != null)
            {
                solver.process(chunks);
            }
        }
    }
    
    private List<Set<Integer>> getChunks(int tag)
    {
        Statistics statistics = new Statistics();
        List<Integer> list = new ArrayList<>(sizes); // A bit horrible.
        ListIterator<Integer> i = list.listIterator(list.size());
        boolean inSeparator = false;
        boolean foundSeparator = false;
        LinkedList<Integer> chunk = new LinkedList<>();
        LinkedList<Set<Integer>> result = new LinkedList<>();
        boolean start = true;
        
        // Important: we work *backwards* towards the opening tag.
        while (i.hasPrevious())
        {
            int size = i.previous();
            
            if (inSeparator) // We're in between chunks.
            {
                statistics.analyzeSeparator(size);
                
                if (size == SEPARATOR_START)
                {
                    inSeparator = false;
                }
            }
            else
            {
                if (size == SEPARATOR_END) // We've hit a separator.
                {
                    inSeparator = true;
                    
                    if (start)
                    {
                        start = false;
                    }
                    else
                    {
                        Set<Integer> dataChunk = getDataChunk(chunk);
                        
                        statistics.analyzeChunk(chunk);
                        
                        if (!chunk.isEmpty())
                        {
                            result.addFirst(dataChunk);
                        }
                    }
                    
                    chunk.clear();
                }
                else if (size == tag) // We've hit the start of the sequence.
                {
                    Set<Integer> lenChunk = getLenChunk(chunk);
                    
                    if (!lenChunk.isEmpty())
                    {
                        result.addFirst(lenChunk);
                        foundSeparator = true;
                        break;
                    }
                }
                else
                {
                    chunk.addFirst(size);
                }
            }
        }
        
        statistics.print(foundSeparator);
        
        return foundSeparator ? result : null;
    }
    
    private Set<Integer> getLenChunk(LinkedList<Integer> chunk)
    {
        Set<Integer> result = new HashSet<>();
        
        for (int len : chunk)
        {
            if (len >= LEN_MIN && len <= LEN_MAX)
            {
                result.add(len - LEN_MIN);
            }
        }
        
        return result;
    }
    
    private Set<Integer> getDataChunk(LinkedList<Integer> chunk)
    {
        Set<Integer> result = new HashSet<>();
        
        for (int len : chunk)
        {
            if (len >= DATA_MIN && len <= DATA_MAX)
            {
                result.add(len - DATA_MIN);
            }
        }
        
        return result;
    }
}

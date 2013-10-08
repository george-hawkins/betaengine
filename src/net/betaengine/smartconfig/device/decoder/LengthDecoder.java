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
import java.util.List;

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
            EncodedData encodedData = getEncodedData(separator);
            
            if (encodedData != null)
            {
                solver.process(encodedData);
            }
        }
    }
    
    private EncodedData getEncodedData(int tag)
    {
        List<Integer> sequence = new ArrayList<>();
        boolean foundTag = false;
        
        for (int size : sizes)
        {
            if (size == tag)
            {
                foundTag = true;
                // We may find the tag multiple times.
                // We only want the values from the last instance of tag onwards.
                sequence.clear();
            }
            else if (foundTag)
            {
                sequence.add(size);
            }
        }
        
        if (sequence.isEmpty())
        {
            return null;
        }
        
        List<Integer> lengths = new ArrayList<>();
        List<Integer> data = new ArrayList<>();
        
        boolean start = true;
        
        // Clean up the sequence - we want at least one potential length value,
        // followed by zero or more data values.
        for (int size : sequence)
        {
            if (start && isValidLenValue(size))
            {
                lengths.add(size - LEN_MIN);
            }
            else if (!lengths.isEmpty())
            {
                start = false;
                
                if (isValidDataValue(size))
                {
                    data.add(size - DATA_MIN);
                }
            }
        }
        
        return lengths.isEmpty() ? null : new EncodedData(lengths, data);
    }
    
    private boolean isValidLenValue(int len)
    {
        return len >= LEN_MIN && len <= LEN_MAX;
    }
    
    private boolean isValidDataValue(int len)
    {
        return len >= DATA_MIN && len <= DATA_MAX;
    }
}

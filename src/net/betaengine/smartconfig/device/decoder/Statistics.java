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

import java.util.LinkedList;

public class Statistics
{
    private int emptyChunks = 0;
    private int spammedChunks = 0;
    private int maxChunkSpamCount = 0;
    private int chunkCount = 0;
    private int separatorSpamCount = 0;
    private int spammedSeparators = 0;
    private int maxSeparatorSpamCount = 0;
    
    public void analyzeSeparator(int len)
    {
        if (len == LengthDecoder.SEPARATOR_START)
        {
            if (separatorSpamCount > 0)
            {
                spammedSeparators++;
                if (separatorSpamCount > maxSeparatorSpamCount)
                {
                    maxSeparatorSpamCount = separatorSpamCount;
                }
                separatorSpamCount = 0;
            }
        }
        else
        {
            separatorSpamCount++;
        }
    }
    
    public void analyzeChunk(LinkedList<Integer> chunk)
    {
        chunkCount++;

        // Note: here we're just looking at spam that wasn't already removed by cleanupChunk(...).
        if (chunk.isEmpty())
        {
            emptyChunks++;
        }
        else if (chunk.size() > 1)
        {
            spammedChunks++;
            
            int chunkSpamCount = chunk.size() - 1;
            
            if (chunkSpamCount > maxChunkSpamCount)
            {
                maxChunkSpamCount = chunkSpamCount;
            }
        }
        
    }
    
    public void print(boolean foundSeparator)
    {
        if (chunkCount > 0)
        {
            System.err.println(foundSeparator ? "Found separator" : "Did not find separator");
            System.err.println("Chunks: " + chunkCount);
            System.err.println("Empty chunks: " + emptyChunks);
            System.err.println("Spammed chunks: " + spammedChunks);
            if (spammedChunks > 0)
            {
                System.err.println("Most spam in a single chunk: " + maxChunkSpamCount);
            }
            System.err.println("Spammed seperators: " + spammedSeparators);
            if (spammedSeparators > 0)
            {
                System.err.println("Most spam in a single separator: " + maxSeparatorSpamCount);
            }
        }
        else
        {
            System.err.println("No chunks found");
        }
        System.err.println("----");
        
    }
}
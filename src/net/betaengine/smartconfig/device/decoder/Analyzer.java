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

import java.util.HashMap;
import java.util.Map;

public class Analyzer
{
    private final Map<Link, LinkManager> linkManagers = new HashMap<>();

    public boolean process(String source, String destination, int length)
    {
        Link link = new Link(source, destination);
        LinkManager manager = linkManagers.get(link);

        if (manager == null)
        {
            manager = new LinkManager();
            linkManagers.put(link, manager);
        }

        return manager.add(length);
    }

    private static class LinkManager
    {
        private final static int DIFF =
            LengthDecoder.SEPARATOR_END - LengthDecoder.SEPARATOR_START;

        private final static int MAX_PREVIOUS_LENGTHS = 16; // Might need to be higher for busy n/w.

        private final Evictor<Integer> previousLengths = new Evictor<>(MAX_PREVIOUS_LENGTHS);
        private final Map<Integer, LengthDecoder> decoders = new HashMap<>();

        public boolean add(int length)
        {
            // Look out for packets with data lengths that differ by the same amount
            // as the difference between SEPARATOR_END and SEPARATOR_START.
            for (int oldLength : previousLengths.getList())
            {
                if (length - oldLength == DIFF)
                {
                    // Offset is essentially the consistent change in size introduced by encryption.
                    int offset = oldLength - LengthDecoder.SEPARATOR_START;

                    if (!decoders.containsKey(offset))
                    {
                        decoders.put(offset, new LengthDecoder(offset, previousLengths.getList()));
                    }
                }
            }
            
            // Note: a smarter algorithm that was also handling channel hopping
            // would presumably focus on a given channel on finding enough data
            // that looked promising (and go back to hopping if it didn't find a
            // solution within a given time limit).

            for (LengthDecoder decoder : decoders.values())
            {
                if (decoder.add(length))
                {
                    return true;
                }
            }

            previousLengths.add(length);

            return false;
        }
    }
}

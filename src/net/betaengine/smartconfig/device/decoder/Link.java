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

import java.util.Objects;

public class Link
{
    private final String source;
    private final String destination;
    
    public Link(String source, String destination)
    {
        this.source = source;
        this.destination = destination;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (o != null && o.getClass() == getClass())
        {
            Link l = (Link)o;
            
            return l.source.equals(source) && l.destination.equals(destination);
        }
        else return false;
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(source, destination);
    }
    
    @Override
    public String toString()
    {
        return source + " -> " + destination;
    }
}
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
import java.util.List;

// Enforces a maximum length on an underlying list (as long as one doesn't add
// elements to the underlying list directly).
// A better alternative would be to depend on Guava and use the EvictingQueue.
public class Evictor<E>
{
    private final LinkedList<E> list = new LinkedList<E>();
    private final int limit;

    public Evictor(int limit)
    {
        this.limit = limit;
    }
    
    public List<E> getList() { return list; }

    public void add(E o)
    {
        list.addLast(o);
        while (list.size() > limit) list.removeFirst();
    }
}

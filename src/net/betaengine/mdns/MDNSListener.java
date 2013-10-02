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
package net.betaengine.mdns;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.xbill.DNS.Message;

public class MDNSListener
{
    private final static DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    
    public void listen()
    {
        // mDNS _device_info expires quickly despite long TTL.
        try
        {
            Map<String, Integer> seen = new HashMap<>();
            int index = 1;
            
            MulticastSocket socket = new MulticastSocket(5353);
            InetAddress group = InetAddress.getByName("224.0.0.251");
            socket.joinGroup(group);

            byte[] buf = new byte[2048];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            
            for (int i = 0; i < Integer.MAX_VALUE; i++) {
                socket.receive(packet);

                byte[] data = packet.getData();
                Message message = new Message(data);
                String result = message.toString();
                Integer oldIndex = seen.get(result);
                
                if (oldIndex == null)
                {
                    seen.put(result, index);

                    // dnsjava only understands standard DNS. mDNS introduces a cache-flush bit
                    // to the CLASS value - see http://tools.ietf.org/html/rfc6762 section 10.2.
                    // This replaceAll(...) is a cheap way of handling that for CLASS IN.
                    result = result.replaceAll("\\bCLASS32769\\b", "IN/cf");
                    
                    String source = packet.getAddress().getHostName();
                    String title = index + ". source=" + source;

                    System.err.println(title);
                    System.err.println(now());
                    System.err.println("================================");
                    System.err.println(result);
                    
                    index++;
                }
                else
                {
                    // Don't print traffic that's just repeating messages we've already seen.
                    System.err.println(now() + " same as " + oldIndex);
                }
                System.err.println("------------------------------------------------------------------------");
                System.err.println("------------------------------------------------------------------------");
            }

            socket.leaveGroup(group);
            socket.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private String now() { return DATE_FORMAT.format(new Date()); }
    
    public static void main(String[] args)
    {
        MDNSListener listener = new MDNSListener();
        
        listener.listen();
    }
}

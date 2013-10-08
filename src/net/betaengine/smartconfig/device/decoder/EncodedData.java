package net.betaengine.smartconfig.device.decoder;

import java.util.List;

public class EncodedData
{
    private final List<Integer> lengths;
    private final List<Integer> data;
    
    public EncodedData(List<Integer> lengths, List<Integer> data)
    {
        this.lengths = lengths;
        this.data = data;
    }
    
    public List<Integer> getLengths() { return lengths; }
    
    public List<Integer> getData() { return data; }
}
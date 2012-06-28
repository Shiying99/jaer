/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.unizh.ini.jaer.projects.integrateandfire;

import java.util.ArrayList;
import jspikestack.SpikeStack;
import net.sf.jaer.event.BasicEvent;

/**
 * This class contains an array of spiking networks and enables events to be
 * routed to them.  It's expecially useful when you do something like routing 
 * events to a different network based on cluster membership.
 * 
 * @author oconnorp
 */
public class NetworkList<NetType extends SpikeStack> 
{
    NetType initialNet;     // The network off which other networks are based.
    Remapper R;             // The remapper object
    
    ArrayList<SpikeStackWrapper<NetType>> nets;     // The list of networks to feed events to
    
    
    /** Initialize the network array given an initial network */
    public NetworkList(NetType initNet)
    {
        initialNet=initNet;
        
        
    }
    
    /** Initialize the network array given an initial network */
    public NetworkList(NetType initNet, Remapper R)
    {
        this(initNet);
        setRemapper(R);
    }
    
    /** Sets the network.  If nets is empty, this adds the network as the first
     * element.  If not, it replaces all elements in the nets array with copies 
     * of the network.
     * @param network 
     */
    public void setNetwork(NetType network)
    {           
        int len=nets.size();
        
        if (len==0)
            len=1;
                
        nets.clear();
        
        initialNet=network;
        setNetCount(len);
        
    }
    
    final public void setRemapper(Remapper rem)
    {   R=rem;
        for (SpikeStackWrapper n:nets)
            n.R=R;        
    }
    
    /** Retrieve a network */
    public SpikeStackWrapper<NetType> getNet(int ix)
    {   return nets.get(ix);
    }
    
    /** Reset all networks */
    public void reset() 
    {   for (SpikeStackWrapper n:nets)
            n.reset();
    }
    
    /** Route Events to network */
    public void routeTo(BasicEvent ev,int index)
    {   
        nets.get(index).addToQueue(ev);
    }
    
    
    /** Change the number of networks.  Increasing the number will cause new 
     copies of the base network to be made. */
    public void setNetCount(int number)
    {   if (nets.size()>number) // Trim extra nets
        {   for (int i=number; i<nets.size(); i++)
                nets.remove(number);
        }
        else if (nets.size()<number) // Make copies and expand
        {   for (int i=nets.size(); i<number; i++)
                nets.add(newWrappedNet());
        }
    }
    
    public SpikeStackWrapper newWrappedNet()
    {
        return new SpikeStackWrapper(initialNet.copy(),R);
        
    }
    
    /** Route Cluster Events to network by cluster id */
    public void routeTo(ClusterEvent ev)
    {           
        nets.get(ev.clusterid).addToQueue(ev);
    }
    
    /** Compute Events in all networks.  TODO: parallelize! */
    public void crunch()
    {
        for (SpikeStackWrapper n:nets)
            n.eatEvents();
    }
    
    
}
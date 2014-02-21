package edu.berkeley.path.beats.actuator;

/**
 * Created by gomes on 2/21/14.
 */
// Each signal communicates with links via a SignalPhaseController.
// Phase controller does two things: a) it registers the signal control,
// and b) it implements the phase indication.

import edu.berkeley.path.beats.simulator.BeatsException;
import edu.berkeley.path.beats.simulator.Controller;
import edu.berkeley.path.beats.simulator.Link;

import java.util.HashMap;

/** XXX.
 * YYY
 *
 * @author Gabriel Gomes (gomes@path.berkeley.edu)
 */
public class SignalPhaseController extends Controller {

    private HashMap<Link,Integer> target2index;
    private HashMap<ActuatorSignal.NEMA,Integer[]> nema2indices;

    public SignalPhaseController(ActuatorSignal mySignal){

//			super();
//
//			int i,j;
//
//			// populate target2index
//			int index = 0;
//			target2index = new HashMap<Link,Integer>();
//			for(i=0;i<mySignal.phase.length;i++)
//				for(j=0;j<mySignal.phase[i].getTargetlinks().length;j++)
//					target2index.put(mySignal.phase[i].getTargetlinks()[j],index++);
//
//			// populate nema2indices
//			nema2indices = new HashMap<ActuatorSignal.NEMA,Integer[]>();
//			for(i=0;i<mySignal.phase.length;i++){
//				Integer [] indices = new Integer[mySignal.phase[i].getTargetlinks().length];
//				for(j=0;j<mySignal.phase[i].getTargetlinks().length;j++)
//					indices[j] = target2index.get(mySignal.phase[i].getTargetlinks()[j]);
//				nema2indices.put(mySignal.phase[i].getNEMA(),indices);
//			}
//
//			control_maxflow = new double[target2index.size()];
    }

    @Override
    public void populate(Object jaxbobject) {}

    @Override
    public void update() throws BeatsException {}

//		@Override
//		public boolean register() {
//	        for(Link link : target2index.keySet())
//	        	if (null != link && !link.registerFlowController(this,target2index.get(link)))
//	        		return false;
//			return true;
//		}

//		@Override
//		public boolean deregister() {
//	        for(Link link : target2index.keySet())
//	        	if(!link.deregisterFlowController(this))
//	        		return false;
//			return true;
//		}

    protected void setPhaseColor(ActuatorSignal.NEMA nema,ActuatorSignal.BulbColor color){

//			Integer [] indices = nema2indices.get(nema);
//			if(indices==null)
//				return;
//
//			double maxflow;
//			switch(color){
//				case GREEN:
//				case YELLOW:
//					maxflow = Double.POSITIVE_INFINITY;
//					break;
//				case RED:
//				case DARK:
//					maxflow = 0d;
//					break;
//				default:
//					maxflow = 0d;
//					break;
//			}
//
//			for(Integer index:indices)
//				this.setControl_maxflow(index, maxflow);

    }

}
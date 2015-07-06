package edu.berkeley.path.beats.control;

import edu.berkeley.path.beats.actuator.ActuatorCommodity;
import edu.berkeley.path.beats.simulator.*;
import edu.berkeley.path.beats.simulator.utils.Table;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by gomes on 6/4/2015.
 */
public class Controller_Commodity_Swapper extends Controller {

    private HashMap<Long, LinkReference> linkRefs;

    /////////////////////////////////////////////////////////////////////
    // Construction
    /////////////////////////////////////////////////////////////////////

    public Controller_Commodity_Swapper(Scenario myScenario, edu.berkeley.path.beats.jaxb.Controller c) {
        super(myScenario,c,Algorithm.Commodity_Swapper);
    }


    /////////////////////////////////////////////////////////////////////
    // populate / validate / reset  / update
    /////////////////////////////////////////////////////////////////////

    @Override
    protected void populate(Object jaxbO) {

        super.populate(jaxbO);

        linkRefs = new HashMap<Long, LinkReference>();

        Table splitstable = getTables().get("splits");
        for(Table.Row row : splitstable.getRows()){
            Long link_id = Long.parseLong(row.get_value_for_column_name("link_id"));
            Integer comm_in = Integer.parseInt(row.get_value_for_column_name("comm_in"));
            Integer comm_out = Integer.parseInt(row.get_value_for_column_name("comm_out"));
//            Long target_sink = Long.parseLong(row.get_value_for_column_name("target_sink"));
//            Long target_source = Long.parseLong(row.get_value_for_column_name("target_source"));
            String time_delay_series = row.get_value_for_column_name("time_delay");
            Double time_delay;
            if(time_delay_series == null){
                time_delay = new Double(0);
            } else{
                time_delay = Double.parseDouble(time_delay_series);
            }

            Link actuating_link = myScenario.get.linkWithId(link_id);

//            Link source_link = myScenario.get.linkWithId(target_source);
//            Link sink_link = myScenario.get.linkWithId(target_sink);
//            SplitRatioProfile reference_split_profile = sink_link.getBegin_node().getSplitRatioProfile();

            if(linkRefs.containsKey(link_id)) {
//                linkRefs.get(link_id).append(source_link, sink_link, reference_split_profile, comm_in, comm_out);
            } else {
//                linkRefs.put(link_id, new LinkReference(this, actuating_link, source_link, sink_link,
//                        comm_in, comm_out, reference_split_profile, myScenario));
            }
        }

        System.out.println(jaxbO);
    }

    @Override
    protected void validate() {
        super.validate();
    }

    @Override
    protected void reset()  {
        super.reset();
    }

    protected LinkReference getLinkReferenceWithId(Long id) {
        return linkRefs.get(id);
    }

    private class LinkReference {
        ActuatorCommodity myActuator;
        Link myLink;
        int[] InputCommodities;
        int[] OutputCommodities;

        public LinkReference(Controller parent, Link myLink, Link source_link, Link sink_link,
                             int comm_in, int comm_out, SplitRatioProfile SRP, Scenario scenario) {

        }

        public void append(Link source_link, Link sink_link, SplitRatioProfile SRP, int comm_in, int comm_out){

        }

        public Link getMyLink() {
            return myLink;
        }
    }

}

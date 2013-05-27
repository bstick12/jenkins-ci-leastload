package org.bstick12.jenkinsci.plugins.leastload;

import hudson.model.LoadBalancer;
import hudson.model.Computer;
import hudson.model.Queue.Task;
import hudson.model.queue.MappingWorksheet;
import hudson.model.queue.MappingWorksheet.ExecutorChunk;
import hudson.model.queue.MappingWorksheet.Mapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import static java.util.logging.Level.*;
import java.util.logging.Logger;

public class LeastLoadBalancer extends LoadBalancer {

	private static final Logger LOGGER = Logger.getLogger(LeastLoadBalancer.class.getCanonicalName());
	
	private static final Comparator<ExecutorChunk> EXECUTOR_CHUNK_COMPARATOR = Collections.reverseOrder(new ExecutorChunkComparator());
	
	private final LoadBalancer fallback;
	
	public LeastLoadBalancer(LoadBalancer fallback) {
		this.fallback = fallback;
	}
	
	@Override
	public Mapping map(Task task, MappingWorksheet ws) {
		
		try {

			List<ExecutorChunk> useableChunks = new ArrayList<ExecutorChunk>();
            for (int i=0; i<ws.works.size(); i++) {
                for (ExecutorChunk ec : ws.works(i).applicableExecutorChunks()) {
                	useableChunks.add(ec);
                }
            }

            Collections.sort(useableChunks, EXECUTOR_CHUNK_COMPARATOR);

            Mapping m = ws.new Mapping();
            if (assignGreedily(m,task,useableChunks,0)) {
                assert m.isCompletelyValid();
                return m;
            } else {
            	LOGGER.log(INFO, "Least load balancer was unable to define mapping. Falling back to double check");
            	return getFallBackLoadBalancer().map(task, ws);
            }
		} catch (Exception e) {
			LOGGER.log(WARNING, "Least load balancer failed falling back", e);
			return getFallBackLoadBalancer().map(task, ws);
		}
		
	}

    private boolean assignGreedily(Mapping m, Task task, List<ExecutorChunk> executors, int i) {
        
    	if (executors.size() == i || m.size() == i) {
    		return true;
        }
        
        m.assign(i,executors.get(i));

        if (m.isPartiallyValid() && assignGreedily(m,task,executors,i+1)) {
            return true;
        } else {
	        m.assign(i,null);
	        return false;
        }
        
    }
	
	public LoadBalancer getFallBackLoadBalancer() {
		return fallback;
	}
	
	public static class ExecutorChunkComparator implements Comparator<ExecutorChunk> {

		public int compare(ExecutorChunk ec1, ExecutorChunk ec2) {

			if(ec1 == ec2) {
				return 0;
			}
			
			Computer com1 = ec1.computer;
			Computer com2 = ec2.computer;
			
			if(com1.isIdle() && !com2.isIdle()) {
				return 1;
			} else if (com2.isIdle() && !com1.isIdle()) {
				return -1;
			} else {
				return com1.countIdle() - com2.countIdle();
			}
			
		}
		
	}
	
}

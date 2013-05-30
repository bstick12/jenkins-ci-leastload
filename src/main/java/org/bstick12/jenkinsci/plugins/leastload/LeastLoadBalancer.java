package org.bstick12.jenkinsci.plugins.leastload;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import hudson.model.LoadBalancer;
import hudson.model.AbstractProject;
import hudson.model.Computer;
import hudson.model.Executor;
import hudson.model.Queue.Task;
import hudson.model.queue.MappingWorksheet;
import hudson.model.queue.MappingWorksheet.ExecutorChunk;
import hudson.model.queue.MappingWorksheet.Mapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

/**
 * 
 * A {@link LoadBalancer} implementation that the leastload plugin uses to replace the default 
 * Jenkins {@link LoadBalancer}
 * 
 * <p>The {@link LeastLoadBalancer} chooses {@link Executor}s that have the least load. An {@link Executor} is defined
 * as having the least load if it is idle or has the more available {@link Executor}s
 * 
 * <p>If for any reason we are unsuccessful in creating a {@link Mapping} we fall back on the default Jenkins 
 * {@link LoadBalancer#CONSISTENT_HASH} and try to use that.
 * 
 * @author brendan.nolan@gmail.com
 *
 */
public class LeastLoadBalancer extends LoadBalancer {

	private static final Logger LOGGER = Logger.getLogger(LeastLoadBalancer.class.getCanonicalName());
	
	private static final Comparator<ExecutorChunk> EXECUTOR_CHUNK_COMPARATOR = Collections.reverseOrder(new ExecutorChunkComparator());
	
	private final LoadBalancer fallback;
	
	/**
	 * 
	 * Create the {@link LeastLoadBalancer} with a fallback that will be 
	 * used in case of any failures.
	 * 
	 */
	public LeastLoadBalancer(LoadBalancer fallback) {
		this.fallback = fallback;
	}
	
	@Override
	public Mapping map(Task task, MappingWorksheet ws) {
		
		try {

			if(!isDisabled(task)) {
	
				List<ExecutorChunk> useableChunks = getApplicableSortedByLoad(ws);	
	            Mapping m = ws.new Mapping();
	            if (assignGreedily(m,useableChunks,0)) {
	                assert m.isCompletelyValid();
	                return m;
	            } else {
	            	LOGGER.log(INFO, "Least load balancer was unable to define mapping. Falling back to double check");
	            	return getFallBackLoadBalancer().map(task, ws);
	            }
	            
			} else {
				return getFallBackLoadBalancer().map(task, ws);
			}
            
		} catch (Exception e) {
			LOGGER.log(WARNING, "Least load balancer failed falling back", e);
			return getFallBackLoadBalancer().map(task, ws);
		}		
	}

	/**
	 * 
	 * Extract a list of applicable {@link ExecutorChunk}s sorted in least loaded order
	 * 
	 * @param ws - The mapping worksheet
	 * @return -A list of ExecutorChunk in least loaded order
	 */
	private List<ExecutorChunk> getApplicableSortedByLoad(MappingWorksheet ws) {
		
		List<ExecutorChunk> chunks = new ArrayList<ExecutorChunk>();
		for (int i=0; i<ws.works.size(); i++) {
		    for (ExecutorChunk ec : ws.works(i).applicableExecutorChunks()) {
		    	chunks.add(ec);
		    }
		}
		Collections.sort(chunks, EXECUTOR_CHUNK_COMPARATOR);
		return chunks;
		
	} 

    @SuppressWarnings("rawtypes")
	private boolean isDisabled(Task task) {

    	if(task instanceof AbstractProject) {
    		AbstractProject project = (AbstractProject) task;
    		@SuppressWarnings("unchecked")
    		LeastLoadDisabledProperty property = (LeastLoadDisabledProperty) project.getProperty(LeastLoadDisabledProperty.class);
    		return property.isLeastLoadDisabled();
    	} else {
    		return false;
    	}
    	
    }

	private boolean assignGreedily(Mapping m, List<ExecutorChunk> executors, int i) {
        
    	if (m.size() == i) {
    		return true;
        }
        
    	for(ExecutorChunk ec : executors) {    	
    		m.assign(i,ec);
	        if (m.isPartiallyValid() && assignGreedily(m,executors,i+1)) {
	            return true;
	        }
        } 
	    
    	m.assign(i,null);
        return false;
        
    }
	
    /**
     * 
     * Retrieves the fallback {@link LoadBalancer}  
     * 
     * @return
     */
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

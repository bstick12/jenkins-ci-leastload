package org.bstick12.jenkinsci.plugins.leastload;

import jenkins.model.Jenkins;
import hudson.Plugin;
import hudson.model.LoadBalancer;

public class PluginImpl extends Plugin {
	
	@Override
	public void start() throws Exception {

		LoadBalancer currentLoadBalancer = Jenkins.getInstance().getQueue().getLoadBalancer();
		LoadBalancer leastLoadBalancer = new LeastLoadBalancer(currentLoadBalancer);
		Jenkins.getInstance().getQueue().setLoadBalancer(leastLoadBalancer);

	}
	
}

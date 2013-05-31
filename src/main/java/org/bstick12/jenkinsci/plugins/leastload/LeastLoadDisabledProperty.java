package org.bstick12.jenkinsci.plugins.leastload;

import hudson.Extension;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import hudson.model.AbstractProject;

import org.kohsuke.stapler.DataBoundConstructor;

public class LeastLoadDisabledProperty extends JobProperty<AbstractProject<?,?>> {

	private final boolean leastLoadDisabled;
	
	@DataBoundConstructor
	public LeastLoadDisabledProperty(boolean leastLoadDisabled) {
		this.leastLoadDisabled = leastLoadDisabled;
	}
	
	public boolean isLeastLoadDisabled() {
		return leastLoadDisabled;
	}
	
	@Extension
	public static class DescriptorImpl extends JobPropertyDescriptor {

		@Override
		public String getDisplayName() {
			return "Least Load Disabled Property";
		}

	}
	
}

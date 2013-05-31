package org.bstick12.jenkinsci.plugins.leastload;

import static org.junit.Assert.assertEquals;
import hudson.model.LoadBalancer;
import hudson.model.AbstractProject;
import hudson.model.Queue.Task;
import hudson.model.queue.MappingWorksheet;

import org.junit.Test;
import org.mockito.Mockito;



public class LeastLoadBalancerTest {

	protected LoadBalancer mockFallback = Mockito.mock(LoadBalancer.class);
	
	@SuppressWarnings("rawtypes")
	protected AbstractProject mockAbstractProject = Mockito.mock(AbstractProject.class);
	
	protected MappingWorksheet mockMappingWorksheet = Mockito.mock(MappingWorksheet.class);
	
	@Test(expected=NullPointerException.class)
	public void testConstructorFailure() {
		new LeastLoadBalancer(null);
	}
	
	@Test
	public void testConstructor() {
		LeastLoadBalancer llb = new LeastLoadBalancer(mockFallback);
		assertEquals(mockFallback, llb.getFallBackLoadBalancer());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testFallbackOnFailure() {
		LeastLoadBalancer llb = new LeastLoadBalancer(mockFallback);
		LeastLoadDisabledProperty lldp = new LeastLoadDisabledProperty(false);
		Mockito.doReturn(lldp).when(mockAbstractProject).getProperty(LeastLoadDisabledProperty.class);		
		llb.map(mockAbstractProject, mockMappingWorksheet);
		Mockito.verify(mockFallback).map(mockAbstractProject, mockMappingWorksheet);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testFallbackOnPropertyDisable() {
		
		LeastLoadBalancer llb = new LeastLoadBalancer(mockFallback);		
		LeastLoadDisabledProperty lldp = new LeastLoadDisabledProperty(true);
		Mockito.doReturn(lldp).when(mockAbstractProject).getProperty(LeastLoadDisabledProperty.class);		
		llb.map(mockAbstractProject, mockMappingWorksheet);
		Mockito.verify(mockFallback).map(mockAbstractProject, mockMappingWorksheet);
		
	}
	
	@Test
	public void testFallbackWhenNotAbstractProject() {
		
		LeastLoadBalancer llb = new LeastLoadBalancer(mockFallback);
		Task mockTask = Mockito.mock(Task.class);
		llb.map(mockTask, mockMappingWorksheet);
		Mockito.verify(mockFallback).map(mockTask, mockMappingWorksheet);
		
	}
	
}
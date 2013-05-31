package org.bstick12.jenkinsci.plugins.leastload;

import org.junit.Assert;
import org.junit.Test;

public class LeastLoadDisabledPropertyTest {

	@Test
	public void testConstructor() {
		
		LeastLoadDisabledProperty  lldp = new LeastLoadDisabledProperty(true);
		Assert.assertTrue(lldp.isLeastLoadDisabled());
		lldp = new LeastLoadDisabledProperty(false);
		Assert.assertFalse(lldp.isLeastLoadDisabled());
		
	}
	
	
}

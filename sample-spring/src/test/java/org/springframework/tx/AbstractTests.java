package org.springframework.tx;

import org.junit.jupiter.api.BeforeEach;

class AbstractTests {

	protected String configLocation;

	@BeforeEach
	public void beforeEach() {
		configLocation = getClass().getName().replaceAll("\\.", "/") + "-context.xml";
	}

}

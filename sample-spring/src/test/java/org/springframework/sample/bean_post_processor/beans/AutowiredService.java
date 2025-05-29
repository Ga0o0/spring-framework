package org.springframework.sample.bean_post_processor.beans;

import org.springframework.beans.factory.annotation.Autowired;

public class AutowiredService {

	@Autowired
	// @Inject
	// @Value(value = "")
	private Dao dao;

	public Dao getDao() {
		return dao;
	}
}
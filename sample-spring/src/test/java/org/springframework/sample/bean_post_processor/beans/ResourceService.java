package org.springframework.sample.bean_post_processor.beans;

import jakarta.annotation.Resource;

public class ResourceService {
	@Resource
	private Dao dao1;
	@Resource
	private Dao dao;

	public Dao getDao() {
		return dao;
	}

	public Dao getDao1() {
		return dao1;
	}
}

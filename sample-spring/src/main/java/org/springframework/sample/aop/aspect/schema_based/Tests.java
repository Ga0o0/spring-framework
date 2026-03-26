package org.springframework.sample.aop.aspect.schema_based;

import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.sample.aop.aspect.TargetService;

/**
 * 用 XML 配置声明一个 Aspect
 */
public class Tests {
	static final String LOCATION = SchemaAspect.class.getSimpleName() + "-aspect.xml";

	/**
	 * Test
	 */
	public static void main(String[] args) {
		GenericXmlApplicationContext context = new GenericXmlApplicationContext();
		context.load(LOCATION);
		context.refresh();

		// Test
		TargetService service = context.getBean(TargetService.class);
		service.doMethod1();
		service.doMethod2();
		try {
			service.doMethod3();
		} catch (Exception ignore) {}
	}
}

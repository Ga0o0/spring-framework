package org.springframework.sample.bean_post_processor.annotation_autowired;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.ClassUtils;

import java.lang.annotation.Annotation;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @see AutowiredAnnotationBeanPostProcessor#AutowiredAnnotationBeanPostProcessor()
 *
 * @see org.springframework.beans.factory.annotation.Autowired
 * @see org.springframework.beans.factory.annotation.Value
 * @see jakarta.inject.Inject
 * @see javax.inject.Inject
 */
public class CodeAnalysis01_AutowiredAnnotationBeanPostProcessor {
	static class CodeAnalysis_AutowiredAnnotationBeanPostProcessor extends AutowiredAnnotationBeanPostProcessor {
		private final Set<Class<? extends Annotation>> autowiredAnnotationTypes = new LinkedHashSet<>(4);

		/**
		 * @see AutowiredAnnotationBeanPostProcessor#AutowiredAnnotationBeanPostProcessor()
		 */
		// 为 Spring 的标准 {@link Autowired @Autowired} 和 {@link Value @Value} 注解创建一个新的 {@code AutowiredAnnotationBeanPostProcessor}。
		// <p>还支持常见的 {@link jakarta.inject.Inject @Inject} 注解（如果可用）以及原始的 {@code javax.inject.Inject} 变体。
		@SuppressWarnings("unchecked")
		public CodeAnalysis_AutowiredAnnotationBeanPostProcessor() {
			this.autowiredAnnotationTypes.add(Autowired.class); // important -> go
			this.autowiredAnnotationTypes.add(Value.class); // important -> go

			ClassLoader classLoader = AutowiredAnnotationBeanPostProcessor.class.getClassLoader();
			try {
				this.autowiredAnnotationTypes.add((Class<? extends Annotation>)
						ClassUtils.forName("jakarta.inject.Inject", classLoader)); // important -> go
				logger.trace("'jakarta.inject.Inject' annotation found and supported for autowiring");
			}
			catch (ClassNotFoundException ex) {
				// jakarta.inject API not available - simply skip.
			}

			try {
				this.autowiredAnnotationTypes.add((Class<? extends Annotation>)
						ClassUtils.forName("javax.inject.Inject", classLoader)); // important -> go
				logger.trace("'javax.inject.Inject' annotation found and supported for autowiring");
			}
			catch (ClassNotFoundException ex) {
				// javax.inject API not available - simply skip.
			}
		}
	}
}

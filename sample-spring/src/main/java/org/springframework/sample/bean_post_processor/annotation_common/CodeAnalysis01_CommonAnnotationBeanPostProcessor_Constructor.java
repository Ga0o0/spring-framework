package org.springframework.sample.bean_post_processor.annotation_common;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.annotation.CommonAnnotationBeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.jndi.support.SimpleJndiBeanFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

import java.lang.annotation.Annotation;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @see CommonAnnotationBeanPostProcessor#CommonAnnotationBeanPostProcessor()
 *
 * @see jakarta.annotation.Resource
 * @see javax.annotation.Resource
 * @see jakarta.ejb.EJB
 *
 * @see jakarta.annotation.PostConstruct
 * @see jakarta.annotation.PreDestroy
 * @see javax.annotation.PostConstruct
 * @see javax.annotation.PreDestroy
 */
public class CodeAnalysis01_CommonAnnotationBeanPostProcessor_Constructor {
	static class CommonAnnotationBeanPostProcessor_Constructor extends CommonAnnotationBeanPostProcessor {
		private static final long serialVersionUID = 1L;
		// 对 JDK 9+ 的 JNDI API 的防御性引用（可选的 java.naming 模块）
		private static final boolean jndiPresent = ClassUtils.isPresent(
				"javax.naming.InitialContext", CommonAnnotationBeanPostProcessor.class.getClassLoader());
		private static final Set<Class<? extends Annotation>> resourceAnnotationTypes = new LinkedHashSet<>(4);
		private static final Class<? extends Annotation> jakartaResourceType;
		private static final Class<? extends Annotation> javaxResourceType;
		private static final Class<? extends Annotation> ejbAnnotationType;
		private transient BeanFactory jndiFactory;
		static {
			jakartaResourceType = loadAnnotationType("jakarta.annotation.Resource"); // important -> go
			if (jakartaResourceType != null) {
				resourceAnnotationTypes.add(jakartaResourceType);
			}

			javaxResourceType = loadAnnotationType("javax.annotation.Resource"); // important -> go
			if (javaxResourceType != null) {
				resourceAnnotationTypes.add(javaxResourceType);
			}

			ejbAnnotationType = loadAnnotationType("jakarta.ejb.EJB"); // important -> go
			if (ejbAnnotationType != null) {
				resourceAnnotationTypes.add(ejbAnnotationType);
			}
		}

		// 创建一个新的 CommonAnnotationBeanPostProcessor，
		// 并将 init 和 destroy 注释类型分别设置为 {@link jakarta.annotation.PostConstruct}
		// 和 {@link jakarta.annotation.PreDestroy}。
		public CommonAnnotationBeanPostProcessor_Constructor() {
			setOrder(Ordered.LOWEST_PRECEDENCE - 3);

			// Jakarta EE 9 的 jakarta.annotation 包中的注解集
			addInitAnnotationType(loadAnnotationType("jakarta.annotation.PostConstruct")); // important -> go
			addDestroyAnnotationType(loadAnnotationType("jakarta.annotation.PreDestroy")); // important -> go

			// 允许 javax.annotation 包中存在遗留的 JSR-250 注解
			addInitAnnotationType(loadAnnotationType("javax.annotation.PostConstruct")); // important -> go
			addDestroyAnnotationType(loadAnnotationType("javax.annotation.PreDestroy")); // important -> go

			// JDK 9+ 中是否存在 java.naming 模块？
			if (jndiPresent) {
				this.jndiFactory = new SimpleJndiBeanFactory();
			}
		}

		@SuppressWarnings("unchecked")
		@Nullable
		private static Class<? extends Annotation> loadAnnotationType(String name) {
			try {
				return (Class<? extends Annotation>)
						ClassUtils.forName(name, CommonAnnotationBeanPostProcessor.class.getClassLoader());
			}
			catch (ClassNotFoundException ex) {
				return null;
			}
		}
	}
}

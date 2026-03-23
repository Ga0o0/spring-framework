package org.springframework.sample.bean_post_processor.annotation_common.jakarta_resource.dependency_types;

/*
AutowireCandidateResolver 												[interface]
	\-- impl---- SimpleAutowireCandidateResolver 						[class]
		\--extends-- GenericTypeAwareAutowireCandidateResolver 			[class]
			\--extends-- QualifierAnnotationAutowireCandidateResolver 	[class]		@Qualifier/Value
				\--extends-- ContextAnnotationAutowireCandidateResolver [class]		@Lazy
**/

/**
 * LazyResolution
 *
 * @see org.springframework.beans.factory.support.AutowireCandidateResolver
 * @see org.springframework.beans.factory.support.SimpleAutowireCandidateResolver
 * @see org.springframework.beans.factory.support.GenericTypeAwareAutowireCandidateResolver
 * @see org.springframework.beans.factory.annotation.QualifierAnnotationAutowireCandidateResolver
 * @see org.springframework.context.annotation.ContextAnnotationAutowireCandidateResolver
 *
 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#resolveDependency(org.springframework.beans.factory.config.DependencyDescriptor, java.lang.String, java.util.Set, org.springframework.beans.TypeConverter)
 * @see org.springframework.beans.factory.config.DependencyDescriptor.supportsLazyResolution()
 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#getAutowireCandidateResolver()
 * @see org.springframework.beans.factory.support.AutowireCandidateResolver#getLazyResolutionProxyIfNecessary(org.springframework.beans.factory.config.DependencyDescriptor, String)
 */
public class DT10_LazyResolution {
	// todo
}

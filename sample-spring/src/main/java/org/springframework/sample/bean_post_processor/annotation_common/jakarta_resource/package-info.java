/**
 * @Resource
 *
 * @see jakarta.annotation.Resource
 * @see org.springframework.context.annotation.CommonAnnotationBeanPostProcessor
 *
 * @see org.springframework.sample.bean_post_processor.annotation_common.CodeAnalysis50_ResourceElement_getResourceToInject
 *
 * @see org.springframework.context.annotation.CommonAnnotationBeanPostProcessor.ResourceElement#ResourceElement(java.lang.reflect.Member, java.lang.reflect.AnnotatedElement, java.beans.PropertyDescriptor)
 * @see org.springframework.context.annotation.CommonAnnotationBeanPostProcessor#autowireResource(org.springframework.beans.factory.BeanFactory, org.springframework.context.annotation.CommonAnnotationBeanPostProcessor.LookupElement, java.lang.String)
 */
package org.springframework.sample.bean_post_processor.annotation_common.jakarta_resource;

/*		@Resource
********************************* Class API Docs *********************************
Resource 注解用于标记应用程序所需的资源。此注解可以应用于应用程序组件类，也可以应用于组件类的字段或方法。
当注解应用于字段或方法时，容器会在组件初始化时将所请求资源的实例注入到应用程序组件中。
如果注解应用于组件类，则该注解声明了一个应用程序在运行时查找的资源。

即使此注解未标记为 Inherited，部署工具也需要检查任何组件类的所有父类，以发现所有父类中此注解的用法。
所有此类注解实例都指定了应用程序组件所需的资源。
请注意，此注解也可能出现在父类的私有字段和方法上；在这种情况下，容器也需要执行注入操作。

********************************* Class Definition *********************************
@Target({TYPE, FIELD, METHOD})
@Retention(RUNTIME)
@Repeatable(Resources.class)
public @interface Resource {
	String name() default "";
	String lookup() default "";
	Class<?> type() default java.lang.Object.class;
	Resource.AuthenticationType authenticationType() default Resource.AuthenticationType.CONTAINER;
	boolean shareable() default true;
	String mappedName() default "";
	String description() default "";

	enum AuthenticationType {
		CONTAINER,
		APPLICATION
	}
}
**/


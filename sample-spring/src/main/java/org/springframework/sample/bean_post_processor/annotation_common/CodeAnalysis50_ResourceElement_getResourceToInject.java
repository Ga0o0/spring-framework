package org.springframework.sample.bean_post_processor.annotation_common;

import lombok.Getter;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.InjectionMetadata;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.context.annotation.CommonAnnotationBeanPostProcessor;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.Nullable;
import org.springframework.sample.bean_post_processor.annotation_autowired.CodeAnalysis70_AutowireCapableBeanFactory_resolveDependency;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.StringValueResolver;

import java.beans.PropertyDescriptor;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * ResourceElement#inject(...)
 *
 * @see org.springframework.context.annotation.CommonAnnotationBeanPostProcessor.ResourceElement#inject(java.lang.Object, java.lang.String, org.springframework.beans.PropertyValues)
 * @see org.springframework.beans.factory.annotation.InjectionMetadata.InjectedElement#inject(java.lang.Object, java.lang.String, org.springframework.beans.PropertyValues)
 * @see org.springframework.beans.factory.annotation.InjectionMetadata.InjectedElement#getResourceToInject(java.lang.Object, java.lang.String)
 * @see org.springframework.context.annotation.CommonAnnotationBeanPostProcessor.ResourceElement#getResourceToInject(java.lang.Object, java.lang.String)
 *
 * ## 1. 懒加载处理：如果标记了 @Lazy，返回代理对象 -> buildLazyResourceProxy(this, requestingBeanName)
 *
 * @see org.springframework.context.annotation.CommonAnnotationBeanPostProcessor#buildLazyResourceProxy(org.springframework.context.annotation.CommonAnnotationBeanPostProcessor.LookupElement, java.lang.String)
 * @see org.springframework.aop.framework.ProxyFactory#getProxy(java.lang.ClassLoader)
 *
 * ## 2. 非懒加载：通过 BeanFactory 查找实际对象 -> getResource(this, requestingBeanName)
 *
 * @see org.springframework.context.annotation.CommonAnnotationBeanPostProcessor#getResource(org.springframework.context.annotation.CommonAnnotationBeanPostProcessor.LookupElement, java.lang.String)
 * @see org.springframework.context.annotation.CommonAnnotationBeanPostProcessor#autowireResource(org.springframework.beans.factory.BeanFactory, org.springframework.context.annotation.CommonAnnotationBeanPostProcessor.LookupElement, java.lang.String)
 *
 * ### 2.1. factory instanceof AutowireCapableBeanFactory == true
 *
 * #### 2.1.1. 支持回退到默认类型匹配 && @Resource.name 无值 && factory 不包含名称为 name 的 bean 时，执行的操作
 *
 * @see org.springframework.beans.factory.config.AutowireCapableBeanFactory#resolveDependency(org.springframework.beans.factory.config.DependencyDescriptor, java.lang.String, java.util.Set, org.springframework.beans.TypeConverter)
 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#resolveDependency(org.springframework.beans.factory.config.DependencyDescriptor, java.lang.String, java.util.Set, org.springframework.beans.TypeConverter)
 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#doResolveDependency(org.springframework.beans.factory.config.DependencyDescriptor, java.lang.String, java.util.Set, org.springframework.beans.TypeConverter)
 *
 * @see CodeAnalysis70_AutowireCapableBeanFactory_resolveDependency
 *
 * #### 2.1.2. 非 2.1.1 的情况；例如：@Resource.name 有值
 *
 * @see org.springframework.beans.factory.config.AutowireCapableBeanFactory#resolveBeanByName(java.lang.String, org.springframework.beans.factory.config.DependencyDescriptor)
 * @see org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory#resolveBeanByName(java.lang.String, org.springframework.beans.factory.config.DependencyDescriptor)
 * @see org.springframework.beans.factory.support.AbstractBeanFactory#getBean(java.lang.String, java.lang.Class)
 *
 * ### 2.2. factory instanceof AutowireCapableBeanFactory == false
 *
 * @see org.springframework.beans.factory.BeanFactory#getBean(java.lang.String, java.lang.Class)
 */
public class CodeAnalysis50_ResourceElement_getResourceToInject {

	/**
	 * @see org.springframework.beans.factory.annotation.InjectionMetadata.InjectedElement#inject(java.lang.Object, java.lang.String, org.springframework.beans.PropertyValues)
	 */
	static class CodeAnalysis01_InjectionMetadata extends InjectionMetadata.InjectedElement {
		protected CodeAnalysis01_InjectionMetadata(Member member, PropertyDescriptor pd) {
			super(member, pd);
		}
		// 需要覆盖这个或 {@link #getResourceToInject}。
		public void inject(Object target, @Nullable String requestingBeanName, @Nullable PropertyValues pvs)
				throws Throwable {

			if (!shouldInject(pvs)) {
				return;
			}
			if (this.isField) {// 如果是字段注入
				Field field = (Field) this.member;
				ReflectionUtils.makeAccessible(field);
				// getResourceToInject(target, requestingBeanName) -> 获取要注入的值
				field.set(target, getResourceToInject(target, requestingBeanName)); // important -> go
			}
			else { // 如果是方法注入（setter方法）
				try {
					Method method = (Method) this.member;
					ReflectionUtils.makeAccessible(method);
					method.invoke(target, getResourceToInject(target, requestingBeanName)); // important -> go
				}
				catch (InvocationTargetException ex) {
					throw ex.getTargetException();
				}
			}
		}
	}

	/**
	 * @see org.springframework.context.annotation.CommonAnnotationBeanPostProcessor.ResourceElement#getResourceToInject(java.lang.Object, java.lang.String)
	 */
	static class CodeAnalysis02_CommonAnnotationBeanPostProcessor extends CommonAnnotationBeanPostProcessor {
		private static final long serialVersionUID = 1L;
		private transient StringValueResolver embeddedValueResolver;
		private transient BeanFactory beanFactory;
		private transient BeanFactory resourceFactory;
		private boolean alwaysUseJndiLookup = false;
		private transient BeanFactory jndiFactory;
		private boolean fallbackToDefaultTypeMatch = true;

		// 表示有关注释字段或 setter 方法的注入信息的类，支持 @Resource 注释。
		private class ResourceElement extends LookupElement {
			private final boolean lazyLookup;
			public ResourceElement(Member member, AnnotatedElement ae, @Nullable PropertyDescriptor pd) {
				super(member, pd);
				jakarta.annotation.Resource resource = ae.getAnnotation(jakarta.annotation.Resource.class);
				String resourceName = resource.name(); 	 // @Resource#name
				Class<?> resourceType = resource.type(); // @Resource#type
				// @Resource#name 为空；则采用字段和方法名
				this.isDefaultName = !StringUtils.hasLength(resourceName);
				if (this.isDefaultName) {
					// 获取字段或方法名
					resourceName = this.member.getName();
					// 如果是方法名是并以 set 开头，则去除 set 并将其剩余部分按照字段命名的格式（首字母小写的驼峰命名）来处理
					if (this.member instanceof Method && resourceName.startsWith("set") && resourceName.length() > 3) {
						resourceName = StringUtils.uncapitalizeAsProperty(resourceName.substring(3));
					}
				}
				else if (embeddedValueResolver != null) {
					// 解析给定的字符串值，例如解析占位符。
					resourceName = embeddedValueResolver.resolveStringValue(resourceName);
				}
				if (Object.class != resourceType) {
					checkResourceType(resourceType); // 检查 resourceType
				}
				else {
					// No resource type specified... check field/method. --> 译文：未指定资源类型...检查字段/方法。
					resourceType = getResourceType();	// 获取 resourceType
				}
				this.name = (resourceName != null ? resourceName : "");
				this.lookupType = resourceType;
				String lookupValue = resource.lookup();
				this.mappedName = (StringUtils.hasLength(lookupValue) ? lookupValue : resource.mappedName());
				Lazy lazy = ae.getAnnotation(Lazy.class);
				this.lazyLookup = (lazy != null && lazy.value());
			}

			@Override
			protected Object getResourceToInject(Object target, @Nullable String requestingBeanName) {
				// 1. 懒加载处理：如果标记了 @Lazy，返回代理对象 -> buildLazyResourceProxy(this, requestingBeanName)
				// 2. 非懒加载：通过 BeanFactory 查找实际对象 -> getResource(this, requestingBeanName)
				return (this.lazyLookup ? buildLazyResourceProxy(this, requestingBeanName) :// important -> go
						getResource(this, requestingBeanName));// important -> go
			}
			// ...
		}

		// 获取给定名称和类型的资源代理，并在收到方法调用时按需委托给 {@link #getResource}。
		protected Object buildLazyResourceProxy(LookupElement element, @Nullable String requestingBeanName) {
			// 创建目标对象的工厂
			TargetSource ts = new TargetSource() {
				@Override
				public Class<?> getTargetClass() {
					return element.lookupType;
				}
				@Override
				public Object getTarget() {
					return getResource(element, requestingBeanName);
				} // 真正查找的时刻：首次调用代理对象方法时
			};

			// 创建代理对象
			ProxyFactory pf = new ProxyFactory();
			pf.setTargetSource(ts);
			if (element.lookupType.isInterface()) {
				pf.addInterface(element.lookupType);
			}
			ClassLoader classLoader = (this.beanFactory instanceof ConfigurableBeanFactory configurableBeanFactory ?
					configurableBeanFactory.getBeanClassLoader() : null);
			return pf.getProxy(classLoader);// important -> go
		}

		// 获取指定名称和类型的资源对象。
		// @param element 被注解的字段/方法的描述符
		// @param requestingBeanName 请求 Bean 的名称
		// @return 资源对象（永不返回 null）
		// 如果未找到相应的目标资源，则抛出 NoSuchBeanDefinitionException
		protected Object getResource(LookupElement element, @Nullable String requestingBeanName)
				throws NoSuchBeanDefinitionException {

			// 1. 执行 JNDI 查找
			// JNDI lookup to perform? --> 译文：要执行 JNDI 查找吗？
			String jndiName = null;
			if (StringUtils.hasLength(element.mappedName)) {
				jndiName = element.mappedName;
			}
			else if (this.alwaysUseJndiLookup) {
				jndiName = element.name;
			}
			if (jndiName != null) {
				if (this.jndiFactory == null) {
					// 未配置 JNDI 工厂 - 指 定“jndiFactory” 属性
					throw new NoSuchBeanDefinitionException(element.lookupType,
							"No JNDI factory configured - specify the 'jndiFactory' property");
				}
				return this.jndiFactory.getBean(jndiName, element.lookupType);
			}

			// Regular resource autowiring --> 译文：常规资源自动装配
			if (this.resourceFactory == null) {
				// 未配置资源工厂 - 指定 “resourceFactory” 属性
				throw new NoSuchBeanDefinitionException(element.lookupType,
						"No resource factory configured - specify the 'resourceFactory' property");
			}
			// 2. 自动装配 Resource
			return autowireResource(this.resourceFactory, element, requestingBeanName); // important -> go
		}

		// 通过基于给定工厂的自动装配，获取给定名称和类型的资源对象。
		// @param factory 要自动装配的工厂
		// @param element 被注解的字段/方法的描述符
		// @param requestingBeanName 请求 Bean 的名称
		// @return 资源对象（永不返回 {@code null}）
		// 如果未找到相应的目标资源，则抛出 NoSuchBeanDefinitionException
		protected Object autowireResource(BeanFactory factory, LookupElement element, @Nullable String requestingBeanName)
				throws NoSuchBeanDefinitionException {

			Object resource;
			Set<String> autowiredBeanNames;
			String name = element.name;// 获取 @Resource 的 name 属性

			if (factory instanceof AutowireCapableBeanFactory autowireCapableBeanFactory) {
				// ============ 情况 1：未显式指定 name ============
				// 是否支持回退到默认类型匹配 && @Resource.name 无值 && factory 不包含名称为 name 的 bean
				if (this.fallbackToDefaultTypeMatch && element.isDefaultName && !factory.containsBean(name)) {
					autowiredBeanNames = new LinkedHashSet<>();
					// 使用 DependencyDescriptor 进行类型匹配
					resource = autowireCapableBeanFactory.resolveDependency(
							element.getDependencyDescriptor(), requestingBeanName, autowiredBeanNames, null); // important -> go
					if (resource == null) {
						throw new NoSuchBeanDefinitionException(element.getLookupType(), "No resolvable resource object");
					}
				}
				// ============ 情况 2：显式指定了 name ============
				else {
					// 严格按名称查找
					// 最终执行 AbstractBeanFactory.getBean(java.lang.String, java.lang.Class<T>) 方法
					resource = autowireCapableBeanFactory.resolveBeanByName(name, element.getDependencyDescriptor());// important -> go
					autowiredBeanNames = Collections.singleton(name);
				}
			}
			else {
				resource = factory.getBean(name, element.lookupType); // important -> go
				autowiredBeanNames = Collections.singleton(name);
			}

			// 注册依赖关系（用于销毁时的顺序控制）
			if (factory instanceof ConfigurableBeanFactory configurableBeanFactory) {
				for (String autowiredBeanName : autowiredBeanNames) {
					if (requestingBeanName != null && configurableBeanFactory.containsBean(autowiredBeanName)) {
						configurableBeanFactory.registerDependentBean(autowiredBeanName, requestingBeanName); // important -> go
					}
				}
			}

			return resource;
		}


		// ---------------------------------------------------------------------------------------------------------------
		// ---------------------------------  		不太重要的				  --------------------------------------------
		// ---------------------------------------------------------------------------------------------------------------
		// 表示有关注释字段或 setter 方法的通用注入信息的类，支持 @Resource 和相关注释。
		@Getter
		protected abstract static class LookupElement extends InjectionMetadata.InjectedElement {
			protected String name = "";
			protected boolean isDefaultName = false;
			protected Class<?> lookupType = Object.class;
			protected String mappedName;
			public LookupElement(Member member, @Nullable PropertyDescriptor pd) {
				super(member, pd);
			}
			public final Class<?> getLookupType() {
				return this.lookupType;
			}
			// ...
			// 为底层字段/方法构建 DependencyDescriptor。
			public final DependencyDescriptor getDependencyDescriptor() {
				// ...
				return null;
			}
			//...
		}
	}
}
package org.springframework.sample.cache._mine.code_analysis;

import org.springframework.cache.annotation.CachingConfigurationSelector;
import org.springframework.cache.annotation.ProxyCachingConfiguration;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.AdviceModeImportSelector;
import org.springframework.context.annotation.AutoProxyRegistrar;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 * EnableCaching
 *
 * @see org.springframework.cache.annotation.EnableCaching
 * @see org.springframework.cache.annotation.CachingConfigurationSelector
 *
 * @see org.springframework.cache.annotation.CachingConfigurationSelector#selectImports(org.springframework.core.type.AnnotationMetadata)
 * @see org.springframework.context.annotation.AdviceModeImportSelector#selectImports(org.springframework.core.type.AnnotationMetadata)
 * @see org.springframework.context.annotation.AdviceModeImportSelector#selectImports(org.springframework.context.annotation.AdviceMode)
 * @see org.springframework.cache.annotation.CachingConfigurationSelector#selectImports(org.springframework.context.annotation.AdviceMode)
 *
 * ## 1. 针对 EnableCaching#mode() 的 PROXY 值返回 ProxyCachingConfiguration。也可能包含相应的 JCache 配置。
 *
 * @see org.springframework.cache.annotation.CachingConfigurationSelector#getProxyImports()
 *
 * ### 1.1. 默认导入
 *
 * @see org.springframework.context.annotation.AutoProxyRegistrar
 * @see org.springframework.cache.annotation.ProxyCachingConfiguration
 *
 * ### 1.2. javax.cache.Cache 和 org.springframework.cache.jcache.config.ProxyJCacheConfiguration 存在时的导入
 *
 * @see org.springframework.cache.jcache.config.ProxyJCacheConfiguration
 *
 * ## 2. 针对 EnableCaching#mode() 的 ASPECTJ 值返回 AspectJCachingConfiguration。也可能包含相应的 JCache 配置。
 *
 * @see org.springframework.cache.annotation.CachingConfigurationSelector#getAspectJImports()
 *
 * ### 2.1. 默认导入
 *
 * @see org.springframework.cache.aspectj.AspectJCachingConfiguration
 *
 * ### 2.2. javax.cache.Cache 和 org.springframework.cache.jcache.config.ProxyJCacheConfiguration 存在时的导入
 *
 * @see org.springframework.cache.aspectj.AspectJJCacheConfiguration
 */
public class CodeAnalysis01_AtEnableCaching {

	// ...
	@Import(CachingConfigurationSelector.class)
	@interface EnableCaching {
		// 指示是否要创建基于子类（CGLIB）的代理，而不是基于标准 Java 接口的代理。默认值为 {@code false}。
		// <strong>仅当 {@link #mode()} 设置为 {@link AdviceMode#PROXY}</strong> 时适用。
		//
		// <p>请注意，将此属性设置为 {@code true} 将影响所有需要代理的 Spring 管理 bean，而不仅仅是那些标记了 {@code @Cacheable} 的 bean。
		// 例如，其他标记了 Spring 的 {@code @Transactional} 注解的 bean 也将同时升级为子类代理。
		// 除非明确预期使用某种类型的代理（例如在测试中），否则这种方法在实践中不会产生任何负面影响。
		boolean proxyTargetClass() default false;

		// 指定缓存建议的应用方式。
		//
		// <p><b>默认值为 {@link AdviceMode#PROXY}。</b>请注意，代理模式仅允许拦截通过代理发起的调用。
		// 同一类内的本地调用无法通过此方式拦截；本地调用中此类方法的缓存注解将被忽略，因为 Spring 的拦截器在这种运行时场景下根本不会生效。
		// 如需更高级的拦截模式，请考虑将其切换为 {@link AdviceMode#ASPECTJ}。
		AdviceMode mode() default AdviceMode.PROXY;

		// 指定在特定连接点应用多个缓存建议时，缓存建议的执行顺序。
		// <p>默认值为 {@link Ordered#LOWEST_PRECEDENCE}。
		int order() default Ordered.LOWEST_PRECEDENCE;
	}


	/**
	 * @see CachingConfigurationSelector#selectImports(AnnotationMetadata)
	 * @see org.springframework.context.annotation.AdviceModeImportSelector#selectImports(AnnotationMetadata)
	 */
	// public abstract class AdviceModeImportSelector<A extends Annotation> implements ImportSelector { ... }
	static abstract class CA01_CachingConfigurationSelector<A extends Annotation> implements ImportSelector {
		// 默认建议模式属性名称。
		public static final String DEFAULT_ADVICE_MODE_ATTRIBUTE_NAME = "mode";
		// 泛型类型 {@code A} 指定的注解的 {@link AdviceMode} 属性名称。
		// 默认值为 {@value #DEFAULT_ADVICE_MODE_ATTRIBUTE_NAME}，但子类可以重写该属性以进行自定义。
		protected String getAdviceModeAttributeName() {
			return DEFAULT_ADVICE_MODE_ATTRIBUTE_NAME;
		}

		@Override
		public final String[] selectImports(AnnotationMetadata importingClassMetadata) {
			Class<?> annType = GenericTypeResolver.resolveTypeArgument(getClass(), AdviceModeImportSelector.class);
			// ... Assert.state(...)
			// AnnotationAttributes attributes = AnnotationConfigUtils.attributesFor(importingClassMetadata, annType);
			AnnotationAttributes attributes = CA03_AnnotationConfigUtils.attributesFor(importingClassMetadata, annType);
			// ... throw ex

			AdviceMode adviceMode = attributes.getEnum(getAdviceModeAttributeName()); // getAdviceModeAttributeName() -> 获取 mode 属性
			String[] imports = selectImports(adviceMode); // important -> go
			// ... throw ex

			return imports;
		}

		protected abstract String[] selectImports(AdviceMode adviceMode);
	}

	/**
	 * @see org.springframework.cache.annotation.CachingConfigurationSelector#selectImports(org.springframework.context.annotation.AdviceMode)
	 */
	// 根据导入的 {@code @Configuration} 类中 {@link EnableCaching#mode} 的值，
	// 选择应使用的 {@link AbstractCachingConfiguration} 实现。
	//
	// <p>检测是否存在 JSR-107，并相应地启用 JCache 支持。</p>
	// public class CachingConfigurationSelector extends AdviceModeImportSelector<EnableCaching> { ... }
	static class CA02_CachingConfigurationSelector extends CA01_CachingConfigurationSelector<EnableCaching> {
		private static final String PROXY_JCACHE_CONFIGURATION_CLASS = "org.springframework.cache.jcache.config.ProxyJCacheConfiguration";
		private static final String CACHE_ASPECT_CONFIGURATION_CLASS_NAME = "org.springframework.cache.aspectj.AspectJCachingConfiguration";
		private static final String JCACHE_ASPECT_CONFIGURATION_CLASS_NAME = "org.springframework.cache.aspectj.AspectJJCacheConfiguration";
		private static final boolean jsr107Present;
		private static final boolean jcacheImplPresent;
		static {
			ClassLoader classLoader = CachingConfigurationSelector.class.getClassLoader();
			jsr107Present = ClassUtils.isPresent("javax.cache.Cache", classLoader);
			// PROXY_JCACHE_CONFIGURATION_CLASS = org.springframework.cache.jcache.config.ProxyJCacheConfiguration
			jcacheImplPresent = ClassUtils.isPresent(PROXY_JCACHE_CONFIGURATION_CLASS, classLoader);
		}

		// 分别针对 {@link EnableCaching#mode()} 的 {@code PROXY} 和 {@code ASPECTJ} 值
		// 返回 {@link ProxyCachingConfiguration} 或 {@code AspectJCachingConfiguration}。也可能包含相应的 JCache 配置。
		@Override
		public String[] selectImports(AdviceMode adviceMode) {
			return switch (adviceMode) {
				case PROXY -> getProxyImports();  // important -> go
				case ASPECTJ -> getAspectJImports();  // important -> go
			};
		}

		// 如果 {@link AdviceMode} 设置为 {@link AdviceMode#PROXY}，则返回要使用的导入。
		// <p>如果存在，请注意添加必要的 JSR-107 导入。
		private String[] getProxyImports() {
			List<String> result = new ArrayList<>(3);
			result.add(AutoProxyRegistrar.class.getName());  // important -> go
			result.add(ProxyCachingConfiguration.class.getName());  // important -> go
			if (jsr107Present && jcacheImplPresent) {
				result.add(PROXY_JCACHE_CONFIGURATION_CLASS);  // important -> go
			}
			return StringUtils.toStringArray(result);
		}

		// 如果 {@link AdviceMode} 设置为 {@link AdviceMode#ASPECTJ}，则返回要使用的导入。
		// <p>如果存在，请确保添加必要的 JSR-107 导入。</p>
		private String[] getAspectJImports() {
			List<String> result = new ArrayList<>(2);
			result.add(CACHE_ASPECT_CONFIGURATION_CLASS_NAME);  // important -> go
			if (jsr107Present && jcacheImplPresent) {
				result.add(JCACHE_ASPECT_CONFIGURATION_CLASS_NAME);  // important -> go
			}
			return StringUtils.toStringArray(result);
		}
	}

	static abstract class CA03_AnnotationConfigUtils {
		static AnnotationAttributes attributesFor(AnnotatedTypeMetadata metadata, Class<?> annotationType) {
			return attributesFor(metadata, annotationType.getName());
		}
		static AnnotationAttributes attributesFor(AnnotatedTypeMetadata metadata, String annotationTypeName) {
			return AnnotationAttributes.fromMap(metadata.getAnnotationAttributes(annotationTypeName));
		}
	}
}

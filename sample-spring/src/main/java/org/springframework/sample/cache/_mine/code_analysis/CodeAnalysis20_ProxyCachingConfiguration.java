package org.springframework.sample.cache._mine.code_analysis;

/**
 * ProxyCachingConfiguration
 *
 * @see org.springframework.cache.annotation.ProxyCachingConfiguration
 *
 * ## 1. CacheOperationSource
 *
 * @see org.springframework.cache.annotation.ProxyCachingConfiguration#cacheOperationSource()
 * @see org.springframework.cache.annotation.AnnotationCacheOperationSource
 *
 * ## 2. CacheInterceptor
 *
 * @see org.springframework.cache.annotation.ProxyCachingConfiguration#cacheInterceptor(org.springframework.cache.interceptor.CacheOperationSource)
 * @see org.springframework.cache.interceptor.CacheInterceptor
 * @see org.springframework.cache.interceptor.CacheAspectSupport#setCacheOperationSource(org.springframework.cache.interceptor.CacheOperationSource)
 *
 * ## 3. BeanFactoryCacheOperationSourceAdvisor
 *
 * @see org.springframework.cache.annotation.ProxyCachingConfiguration#cacheAdvisor(org.springframework.cache.interceptor.CacheOperationSource, org.springframework.cache.interceptor.CacheInterceptor)
 * @see org.springframework.cache.interceptor.BeanFactoryCacheOperationSourceAdvisor
 * @see org.springframework.cache.interceptor.BeanFactoryCacheOperationSourceAdvisor#setCacheOperationSource(org.springframework.cache.interceptor.CacheOperationSource)
 * @see org.springframework.aop.support.AbstractBeanFactoryPointcutAdvisor#setAdvice(org.aopalliance.aop.Advice)
 */
public class CodeAnalysis20_ProxyCachingConfiguration {
}

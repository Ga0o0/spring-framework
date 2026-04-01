/**
 * EnableCaching
 *
 * @see org.springframework.cache.annotation.EnableCaching
 */
package org.springframework.sample.cache._mine.code_analysis;

/**
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

/**
 * AutoProxyRegistrar
 *
 * @see org.springframework.cache.annotation.CachingConfigurationSelector#getProxyImports()
 * @see org.springframework.context.annotation.AutoProxyRegistrar
 * @see org.springframework.context.annotation.AutoProxyRegistrar#registerBeanDefinitions(org.springframework.core.type.AnnotationMetadata, org.springframework.beans.factory.support.BeanDefinitionRegistry)
 *
 * ## 1. @EnableCaching#mode == AdviceMode.PROXY -> 注册一个名为 org.springframework.aop.config.internalAutoProxyCreator，类型为 InfrastructureAdvisorAutoProxyCreator 的 BeanDefinition
 *
 * @see org.springframework.aop.config.AopConfigUtils#registerAutoProxyCreatorIfNecessary(org.springframework.beans.factory.support.BeanDefinitionRegistry)
 * @see org.springframework.aop.config.AopConfigUtils#registerAutoProxyCreatorIfNecessary(org.springframework.beans.factory.support.BeanDefinitionRegistry, java.lang.Object)
 * @see org.springframework.aop.config.AopConfigUtils#registerOrEscalateApcAsRequired(java.lang.Class, org.springframework.beans.factory.support.BeanDefinitionRegistry, java.lang.Object)
 * @see org.springframework.aop.config.AopConfigUtils#AUTO_PROXY_CREATOR_BEAN_NAME
 * @see org.springframework.aop.framework.autoproxy.InfrastructureAdvisorAutoProxyCreator
 * @see org.springframework.beans.factory.support.BeanDefinitionRegistry#registerBeanDefinition(java.lang.String, org.springframework.beans.factory.config.BeanDefinition)
 *
 * ### 1.1. @EnableCaching#proxyTargetClass == true -> 为上面（步骤 1 中）的 BeanDefinition 注册一个属性 proxyTargetClass，值为 true
 *
 * @see org.springframework.aop.config.AopConfigUtils#forceAutoProxyCreatorToUseClassProxying(org.springframework.beans.factory.support.BeanDefinitionRegistry)
 */

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


/**
 * CacheInterceptor
 *
 * @see org.springframework.cache.interceptor.CacheInterceptor
 * @see org.springframework.cache.interceptor.CacheInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
 * @see org.springframework.cache.interceptor.CacheAspectSupport#execute(org.springframework.cache.interceptor.CacheOperationInvoker, java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
 *
 * ## 1. 获取方法调用的缓存操作
 *
 * @see org.springframework.cache.interceptor.AbstractFallbackCacheOperationSource#getCacheOperations(java.lang.reflect.Method, java.lang.Class)
 * @see org.springframework.cache.interceptor.AbstractFallbackCacheOperationSource#computeCacheOperations(java.lang.reflect.Method, java.lang.Class)
 *
 * ### 1.1. 首先尝试的是目标类中的方法
 *
 * @see org.springframework.cache.annotation.AnnotationCacheOperationSource#findCacheOperations(java.lang.reflect.Method)
 * @see org.springframework.cache.annotation.AnnotationCacheOperationSource#determineCacheOperations(org.springframework.cache.annotation.AnnotationCacheOperationSource.CacheOperationProvider)
 * @see org.springframework.cache.annotation.AnnotationCacheOperationSource.CacheOperationProvider#getCacheOperations(org.springframework.cache.annotation.CacheAnnotationParser)
 * @see org.springframework.cache.annotation.SpringCacheAnnotationParser#parseCacheAnnotations(java.lang.reflect.Method)
 * @see org.springframework.cache.annotation.SpringCacheAnnotationParser#parseCacheAnnotations(org.springframework.cache.annotation.SpringCacheAnnotationParser.DefaultCacheConfig, java.lang.reflect.AnnotatedElement)
 *
 * #### 1.1.1. 找到多个操作 -> 本地声明会覆盖接口声明的操作；先进行 localOnly = false 查找，如果找到多个操作，则进行 localOnly = true 查找，并覆盖 localOnly = false 查找
 *		localOnly = false  		-> 对整个类型层次结构（包括超类和已实现的接口）进行全面搜索
 * 		localOnly = true		-> 查找所有直接声明的注释以及任何 @Inherited 超类注释
 *
 * @see org.springframework.cache.annotation.SpringCacheAnnotationParser#parseCacheAnnotations(org.springframework.cache.annotation.SpringCacheAnnotationParser.DefaultCacheConfig, java.lang.reflect.AnnotatedElement, boolean)
 *
 * ### 1.2. 第二次尝试是对目标类进行缓存操作 -> SpringCacheAnnotationParser#parseCacheAnnotations(...) 之后执行 【1.1.1】 相同操作
 *
 * @see org.springframework.cache.annotation.AnnotationCacheOperationSource#findCacheOperations(java.lang.Class)
 * @see org.springframework.cache.annotation.AnnotationCacheOperationSource#determineCacheOperations(org.springframework.cache.annotation.AnnotationCacheOperationSource.CacheOperationProvider)
 * @see org.springframework.cache.annotation.AnnotationCacheOperationSource.CacheOperationProvider#getCacheOperations(org.springframework.cache.annotation.CacheAnnotationParser)
 * @see org.springframework.cache.annotation.SpringCacheAnnotationParser#parseCacheAnnotations(Class)
 * @see org.springframework.cache.annotation.SpringCacheAnnotationParser#parseCacheAnnotations(org.springframework.cache.annotation.SpringCacheAnnotationParser.DefaultCacheConfig, java.lang.reflect.AnnotatedElement)
 *
 * ### 1.3. 如果【1.1 和 1.2】都未找到。则方法可能位于接口，则按顺序对其接口进行 【1.1 和 1.2】 的操作；即：退而求其次的方法是查看原始方法 ->  最后一种回退机制是使用原始方法的类
 *
 * ## 2. 步骤【1】查询到的数据不为空时，进行以下操作
 *
 * @see org.springframework.cache.interceptor.CacheAspectSupport.CacheOperationContexts#CacheOperationContexts(java.util.Collection, java.lang.reflect.Method, java.lang.Object[], java.lang.Object, java.lang.Class)
 * @see org.springframework.cache.interceptor.CacheAspectSupport#execute(org.springframework.cache.interceptor.CacheOperationInvoker, java.lang.reflect.Method, org.springframework.cache.interceptor.CacheAspectSupport.CacheOperationContexts)
 *
 * ### 2.1. 处理任何提前驱逐
 *
 * @see org.springframework.cache.interceptor.CacheAspectSupport#processCacheEvicts(java.util.Collection, boolean, java.lang.Object)
 * @see org.springframework.cache.interceptor.CacheAspectSupport#performCacheEvicts(java.util.List, java.lang.Object)
 *
 * #### 2.1.1. 移除缓存中的所有条目
 *
 * @see org.springframework.cache.interceptor.AbstractCacheInvoker#doClear(org.springframework.cache.Cache, boolean)
 * @see org.springframework.cache.Cache#invalidate()	-> 在调用方法之前执行回收操作
 * @see org.springframework.cache.Cache#clear()			-> 不在调用方法之前执行回收操作
 *
 * #### 2.2. 移除缓存中的指定条目
 *
 * @see org.springframework.cache.interceptor.AbstractCacheInvoker#doEvict(org.springframework.cache.Cache, java.lang.Object, boolean)
 * @see org.springframework.cache.Cache#evictIfPresent(java.lang.Object) 	-> 在调用方法之前执行回收操作
 * @see org.springframework.cache.Cache#evict(java.lang.Object)				-> 不在调用方法之前执行回收操作
 *
 * ### 2.2. 检查我们是否有符合条件的缓存值
 *
 * @see org.springframework.cache.interceptor.CacheAspectSupport#findCachedValue(org.springframework.cache.interceptor.CacheOperationInvoker, java.lang.reflect.Method, org.springframework.cache.interceptor.CacheAspectSupport.CacheOperationContexts)
 * @see org.springframework.cache.interceptor.CacheAspectSupport#generateKey(org.springframework.cache.interceptor.CacheAspectSupport.CacheOperationContext, java.lang.Object)
 * @see org.springframework.cache.interceptor.CacheAspectSupport#findInCaches(org.springframework.cache.interceptor.CacheAspectSupport.CacheOperationContext, java.lang.Object, org.springframework.cache.interceptor.CacheOperationInvoker, java.lang.reflect.Method, org.springframework.cache.interceptor.CacheAspectSupport.CacheOperationContexts)
 *
 * ### 2.3. 缓存没有值 或 判断结果是 Cache.ValueWrapper 实例，执行以下操作
 *
 * @see org.springframework.cache.interceptor.CacheAspectSupport#evaluate(java.lang.Object, org.springframework.cache.interceptor.CacheOperationInvoker, java.lang.reflect.Method, org.springframework.cache.interceptor.CacheAspectSupport.CacheOperationContexts)
 *
 * #### 2.3.1. 缓存有值 且 没有 CachePut，直接使用缓存命中
 *
 * @see org.springframework.cache.interceptor.CacheAspectSupport#unwrapCacheValue(java.lang.Object)
 * @see org.springframework.cache.interceptor.CacheAspectSupport#wrapCacheValue(java.lang.reflect.Method, java.lang.Object)
 *
 * #### 2.3.2. 其他情况 -> 执行底层操作（通常在缓存未命中时执行），并返回调用结果
 *
 * @see org.springframework.cache.interceptor.CacheAspectSupport#invokeOperation(org.springframework.cache.interceptor.CacheOperationInvoker)
 * @see org.aopalliance.intercept.Joinpoint#proceed()
 * @see org.springframework.cache.interceptor.CacheAspectSupport#unwrapReturnValue(java.lang.Object)
 *
 * #### 2.3.3. 收集所有 @Cacheable 未命中导致的 puts 请求 和 收集所有显式的 @CachePuts 请求。
 *
 * @see org.springframework.cache.interceptor.CacheAspectSupport#collectPutRequests(java.util.Collection, java.lang.Object, java.util.Collection)
 *
 * #### 2.3.4. 处理所有收集到的 put 请求，无论是来自 @CachePut 还是 @Cacheable 未命中
 *
 * @see org.springframework.cache.interceptor.CacheAspectSupport.CachePutRequest#apply(java.lang.Object)
 * @see org.springframework.cache.interceptor.CacheAspectSupport.CachePutRequest#performCachePut(java.lang.Object)
 * @see org.springframework.cache.interceptor.AbstractCacheInvoker#doPut(org.springframework.cache.Cache, java.lang.Object, java.lang.Object)
 * @see org.springframework.cache.Cache#put(java.lang.Object, java.lang.Object)
 *
 * #### 2.3.5. 处理所有逾期驱逐案件 -> 执行【2.1】操作
 *
 * @see org.springframework.cache.interceptor.CacheAspectSupport#processCacheEvicts(java.util.Collection, boolean, java.lang.Object)
 */


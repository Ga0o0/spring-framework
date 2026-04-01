package org.springframework.sample.cache._mine.code_analysis;

import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.cache.interceptor.CacheAspectSupport;
import org.springframework.cache.interceptor.CacheOperation;
import org.springframework.cache.interceptor.CacheOperationInvoker;
import org.springframework.cache.interceptor.CacheOperationSource;
import org.springframework.context.expression.CachedExpressionEvaluator;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;

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
public class CodeAnalysis21_CacheInterceptor {


	static class CA01_CacheAspectSupport extends CacheAspectSupport {
		private boolean initialized = false;

		@Nullable
		protected Object execute(CacheOperationInvoker invoker, Object target, Method method, Object[] args) {
			// Check whether aspect is enabled (to cope with cases where the AJ is pulled in automatically)
			// --> 译文：检查是否启用了 aspect（以应对自动导入 AJ 的情况）
			if (this.initialized) {
				Class<?> targetClass = AopProxyUtils.ultimateTargetClass(target);
				CacheOperationSource cacheOperationSource = getCacheOperationSource();
				if (cacheOperationSource != null) {
					Collection<CacheOperation> operations = cacheOperationSource.getCacheOperations(method, targetClass);
					if (!CollectionUtils.isEmpty(operations)) {
						return execute(invoker, method,
								// new CacheAspectSupport.CacheOperationContexts(operations, method, args, target, targetClass));
								new CacheOperationContexts(operations, method, args, target, targetClass));
					}
				}
			}

			return invokeOperation(invoker);
		}

		@Nullable
		private Object execute(CacheOperationInvoker invoker, Method method, CacheOperationContexts contexts) {
			/*
			if (contexts.isSynchronized()) {
				// 同步调用的特殊处理
				return executeSynchronized(invoker, method, contexts);
			}

			// Process any early evictions --> 译文：处理任何提前驱逐
			processCacheEvicts(contexts.get(CacheEvictOperation.class), true,
					CacheOperationExpressionEvaluator.NO_RESULT);

			// Check if we have a cached value matching the conditions --> 译文：检查我们是否有符合条件的缓存值
			Object cacheHit = findCachedValue(invoker, method, contexts);
			if (cacheHit == null || cacheHit instanceof Cache.ValueWrapper) {
				return evaluate(cacheHit, invoker, method, contexts);
			}
			return cacheHit;*/
			return null;
		}

		@Nullable
		private Object processCacheEvicts(Collection<CacheOperationContext> contexts, boolean beforeInvocation, @Nullable Object result) {
			/*if (contexts.isEmpty()) {
				return null;
			}
			List<CacheOperationContext> applicable = contexts.stream()
					.filter(context -> (context.metadata.operation instanceof CacheEvictOperation evict &&
							beforeInvocation == evict.isBeforeInvocation())).toList();
			if (applicable.isEmpty()) {
				return null;
			}

			if (result instanceof CompletableFuture<?> future) {
				return future.whenComplete((value, ex) -> {
					if (ex == null) {
						performCacheEvicts(applicable, value);
					}
				});
			}
			if (this.reactiveCachingHandler != null) {
				Object returnValue = this.reactiveCachingHandler.processCacheEvicts(applicable, result);
				if (returnValue != ReactiveCachingHandler.NOT_HANDLED) {
					return returnValue;
				}
			}
			performCacheEvicts(applicable, result);*/
			return null;
		}

		@Nullable
		private Object evaluate(@Nullable Object cacheHit, CacheOperationInvoker invoker, Method method,
								// CacheAspectSupport.CacheOperationContexts contexts) {
								CacheOperationContexts contexts) {
			/*
			// 在响应式管道中，延迟缓存命中判定后是否需要重新调用？
			if (contexts.processed) {
				return cacheHit;
			}

			Object cacheValue;
			Object returnValue;

			if (cacheHit != null && !hasCachePut(contexts)) {
				// 如果没有 PUT 请求，则直接使用缓存命中。
				cacheValue = unwrapCacheValue(cacheHit);
				returnValue = wrapCacheValue(method, cacheValue);
			}
			else {
				// 如果没有缓存命中，则调用该方法
				returnValue = invokeOperation(invoker);
				cacheValue = unwrapReturnValue(returnValue);
			}

			// 如果未找到缓存值，则收集所有 @Cacheable 未命中导致的 puts 请求。
			List<CachePutRequest> cachePutRequests = new ArrayList<>(1);
			if (cacheHit == null) {
				collectPutRequests(contexts.get(CacheableOperation.class), cacheValue, cachePutRequests);
			}

			// 收集所有显式的 @CachePuts 请求。
			collectPutRequests(contexts.get(CachePutOperation.class), cacheValue, cachePutRequests);

			// 处理所有收集到的 put 请求，无论是来自 @CachePut 还是 @Cacheable 未命中。
			for (CachePutRequest cachePutRequest : cachePutRequests) {
				Object returnOverride = cachePutRequest.apply(cacheValue);
				if (returnOverride != null) {
					returnValue = returnOverride;
				}
			}

			// 处理所有逾期驱逐案件
			Object returnOverride = processCacheEvicts(
					contexts.get(CacheEvictOperation.class), false, returnValue);
			if (returnOverride != null) {
				returnValue = returnOverride;
			}

			// 标记为已处理，以便在延迟缓存命中判定后重新调用
			contexts.processed = true;

			return returnValue;*/
			return null;
		}

		private class CacheOperationContexts {
			private final MultiValueMap<Class<? extends CacheOperation>, CacheOperationContext> contexts;
			private final boolean sync;
			boolean processed;
			public CacheOperationContexts(Collection<? extends CacheOperation> operations, Method method,
										  Object[] args, Object target, Class<?> targetClass) {

				this.contexts = new LinkedMultiValueMap<>(operations.size());
				for (CacheOperation op : operations) {
					this.contexts.add(op.getClass(), getOperationContext(op, method, args, target, targetClass));
				}
				// this.sync = determineSyncFlag(method);
				this.sync = false; // 上一行代码报错覆盖
			}
			public Collection<CacheOperationContext> get(Class<? extends CacheOperation> operationClass) {
				Collection<CacheOperationContext> result = this.contexts.get(operationClass);
				return (result != null ? result : Collections.emptyList());
			}
			// ...
			public boolean isSynchronized() {
				return this.sync;
			}
			// ...
		}
	}

	static class CacheOperationExpressionEvaluator extends CachedExpressionEvaluator {
		public static final Object NO_RESULT = new Object();
		// ...
	}
}

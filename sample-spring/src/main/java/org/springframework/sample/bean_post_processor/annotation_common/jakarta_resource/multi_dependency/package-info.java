/**
 * 多实例无法确定 - 问题描述与解决
 *
 * ## 示例 - 问题描述
 *
 * @see org.springframework.sample.bean_post_processor.annotation_common.jakarta_resource.multi_dependency.AJR01_AtJakartaResource_MultiDependency_ExceptionDesc
 *
 * ## 示例 - 问题解决
 *
 * @see org.springframework.sample.bean_post_processor.annotation_common.jakarta_resource.multi_dependency.AJR02_AtJakartaResource_MultiDependency_AtPrimary
 * @see org.springframework.sample.bean_post_processor.annotation_common.jakarta_resource.multi_dependency.AJR03_AtJakartaResource_MultiDependency_AtPriority
 *
 * ## 原理 - 问题解决（源码）
 *
 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#determineAutowireCandidate(java.util.Map, org.springframework.beans.factory.config.DependencyDescriptor)
 */
package org.springframework.sample.bean_post_processor.annotation_common.jakarta_resource.multi_dependency;
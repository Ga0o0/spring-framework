/*
 * Copyright 2002-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.core.io.support;

import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

/**
 * Region of a {@link Resource} implementation, materialized by a {@code position}
 * within the {@link Resource} and a byte {@code count} for the length of that region.
 *
 * @author Arjen Poutsma
 * @since 4.3
 */
// {@link Resource} 实现中的区域，由 {@link Resource} 中的 {@code position} 和该区域长度的字节 {@code count} 来具体化。
public class ResourceRegion {

	private final Resource resource;

	private final long position;

	private final long count;


	/**
	 * Create a new {@code ResourceRegion} from a given {@link Resource}.
	 * This region of a resource is represented by a start {@code position}
	 * and a byte {@code count} within the given {@code Resource}.
	 * @param resource a Resource
	 * @param position the start position of the region in that resource
	 * @param count the byte count of the region in that resource
	 */
	// 从给定的 {@link Resource} 创建一个新的 {@code ResourceRegion}。
	// 该资源区域由起始 {@code position} 和在给定 {@code Resource} 中的字节数 {@code count} 表示。
	// @param resource 一个 Resource 对象
	// @param position 该区域在该资源中的起始位置
	// @param count 该区域在该资源中的字节数
	public ResourceRegion(Resource resource, long position, long count) {
		Assert.notNull(resource, "Resource must not be null");
		Assert.isTrue(position >= 0, "'position' must be greater than or equal to 0");
		Assert.isTrue(count >= 0, "'count' must be greater than or equal to 0");
		this.resource = resource;
		this.position = position;
		this.count = count;
	}


	/**
	 * Return the underlying {@link Resource} for this {@code ResourceRegion}.
	 */
	// 返回此 {@code ResourceRegion} 的底层 {@link Resource}。
	public Resource getResource() {
		return this.resource;
	}

	/**
	 * Return the start position of this region in the underlying {@link Resource}.
	 */
	// 返回此区域在底层 {@link Resource} 中的起始位置。
	public long getPosition() {
		return this.position;
	}

	/**
	 * Return the byte count of this region in the underlying {@link Resource}.
	 */
	// 返回底层 {@link Resource} 中此区域的字节数。
	public long getCount() {
		return this.count;
	}

}

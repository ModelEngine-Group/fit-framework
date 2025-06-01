/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an interface as a client proxy for HTTP requests.
 * This annotation is used to indicate that an interface should be treated as a proxy for making HTTP requests.
 * Interfaces annotated with {@code @HttpProxy} will be processed by the framework to generate dynamic proxy objects
 * that can be used to invoke HTTP endpoints.
 *
 * @author 王攀博
 * @since 2025-01-13
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface HttpProxy {}
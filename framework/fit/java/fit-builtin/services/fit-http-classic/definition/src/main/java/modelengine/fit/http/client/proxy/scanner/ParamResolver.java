/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.client.proxy.scanner;

import modelengine.fit.http.client.proxy.support.setter.DestinationSetterInfo;

/**
 * Defines a contract for resolving annotations into destination setter information.
 * Implementations of this interface are responsible for parsing specific annotations
 * and converting them into {@link DestinationSetterInfo} objects that can be used to
 * set properties on HTTP request objects.
 *
 * @param <T> The type of annotation that this resolver handles.
 * @author 王攀博
 * @since 2025-02-10
 */
public interface ParamResolver<T> {
    /**
     * Resolves the given annotation into a destination setter information object.
     *
     * @param annotation The annotation to resolve.
     * @param jsonPath The JSON path associated with the annotation.
     * @return A {@link DestinationSetterInfo} object containing the resolved information.
     */
    DestinationSetterInfo resolve(T annotation, String jsonPath);
}
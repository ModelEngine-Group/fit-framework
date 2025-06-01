/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.client.proxy.scanner.resolver;

import modelengine.fit.http.annotation.PathVariable;
import modelengine.fit.http.client.proxy.scanner.ParamResolver;
import modelengine.fit.http.client.proxy.support.setter.DestinationSetterInfo;
import modelengine.fit.http.client.proxy.support.setter.PathVariableDestinationSetter;

/**
 * Resolves the {@link PathVariable} annotation into a destination setter information object.
 * This class implements the {@link ParamResolver} interface and is responsible for parsing
 * the {@link PathVariable} annotation and converting it into a {@link DestinationSetterInfo}
 * object that can be used to set path variables on HTTP request objects.
 *
 * @author 王攀博
 * @since 2025-02-10
 */
public class PathVariableResolver implements ParamResolver<PathVariable> {
    @Override
    public DestinationSetterInfo resolve(PathVariable annotation, String jsonPath) {
        return new DestinationSetterInfo(new PathVariableDestinationSetter(annotation.name()), jsonPath);
    }
}
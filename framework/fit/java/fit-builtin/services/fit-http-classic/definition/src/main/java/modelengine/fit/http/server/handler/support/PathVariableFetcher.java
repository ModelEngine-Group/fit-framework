/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.server.handler.support;

import static modelengine.fit.http.HttpClassicRequestAttribute.PATH_PATTERN;
import static modelengine.fitframework.inspection.Validation.notBlank;
import static modelengine.fitframework.util.ObjectUtils.cast;

import modelengine.fit.http.server.HttpClassicServerRequest;
import modelengine.fit.http.server.HttpClassicServerResponse;
import modelengine.fit.http.server.handler.RequestMappingException;
import modelengine.fit.http.server.handler.SourceFetcher;
import modelengine.fit.http.server.handler.exception.RequestParamFetchException;
import modelengine.fitframework.inspection.Validation;
import modelengine.fitframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 表示从 Http 请求路径中获取值的 {@link SourceFetcher}。
 *
 * @author 季聿阶
 * @since 2022-08-28
 */
public class PathVariableFetcher implements SourceFetcher {
    private static final char PATH_SEPARATOR = '/';
    private static final char PATH_VARIABLE_PREFIX = '{';
    private static final char PATH_VARIABLE_SUFFIX = '}';

    private final String variableName;

    /**
     * 通过路径变量名来实例化 {@link PathVariableFetcher}。
     *
     * @param variableName 表示路径变量名的 {@link String}。
     * @throws IllegalArgumentException 当 {@code variableName} 为 {@code null} 或空白字符串时。
     */
    public PathVariableFetcher(String variableName) {
        this.variableName = notBlank(variableName, () -> new RequestParamFetchException(
                "PathVariable 必须指定参数名。" +
                "可以通过以下方式之一指定：" +
                "1) 使用 name 属性：@PathVariable(name = \"id\")，" +
                "2) 使用 value 属性：@PathVariable(value = \"id\")，" +
                "3) 编译时添加 -parameters 参数，然后使用参数名：@PathVariable String id（id 将作为参数名）"));
    }

    /**
     * 通过路径变量参数元数据来实例化 {@link PathVariableFetcher}。
     *
     * @param paramValue 表示路径变量参数元数据的 {@link String}。
     * @throws IllegalArgumentException 当 {@code variableName} 为 {@code null} 或空白字符串时。
     */
    public PathVariableFetcher(ParamValue paramValue) {
        this.variableName = notBlank(paramValue.name(), () -> new RequestParamFetchException(
                "PathVariable 必须指定参数名。" +
                "可以通过以下方式之一指定：" +
                "1) 使用 name 属性：@PathVariable(name = \"id\")，" +
                "2) 使用 value 属性：@PathVariable(value = \"id\")，" +
                "3) 编译时添加 -parameters 参数，然后使用参数名：@PathVariable String id（id 将作为参数名）"));
    }

    @Override
    public Object get(HttpClassicServerRequest request, HttpClassicServerResponse response) {
        String pathPattern = cast(request.attributes()
                .get(PATH_PATTERN.key())
                .orElseThrow(() -> new IllegalStateException(StringUtils.format("No path pattern. [path={0}]",
                        request.path()))));
        List<String> partPatterns =
                StringUtils.split(pathPattern, PATH_SEPARATOR, ArrayList::new, StringUtils::isNotBlank);
        int pathVariableIndex = this.findPathVariableIndex(partPatterns);
        Validation.between(pathVariableIndex,
                0,
                partPatterns.size() - 1,
                () -> new RequestMappingException(StringUtils.format(
                        "No path variable in path pattern. [pattern={0}, variable={1}]",
                        pathPattern,
                        this.variableName)));
        List<String> partPath =
                StringUtils.split(request.path(), PATH_SEPARATOR, ArrayList::new, StringUtils::isNotBlank);
        Validation.equals(partPath.size(),
                partPatterns.size(),
                () -> new RequestMappingException(StringUtils.format(
                        "The http request path does not match the path pattern. [pattern={0}, path={1}]",
                        pathPattern,
                        request.path())));
        return partPath.get(pathVariableIndex);
    }

    private int findPathVariableIndex(List<String> partPatterns) {
        for (int i = 0; i < partPatterns.size(); i++) {
            if (Objects.equals(partPatterns.get(i),
                    StringUtils.surround(this.variableName, PATH_VARIABLE_PREFIX, PATH_VARIABLE_SUFFIX))) {
                return i;
            }
        }
        return -1;
    }
}

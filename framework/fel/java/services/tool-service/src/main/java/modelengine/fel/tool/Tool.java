/*
 * Copyright (c) 2025-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fel.tool;

import modelengine.fel.core.tool.ToolInfo;
import modelengine.fel.tool.support.MethodToolMetadata;
import modelengine.fel.tool.support.SchemaToolMetadata;
import modelengine.fitframework.pattern.builder.BuilderFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 表示大模型的工具。
 *
 * @author 季聿阶
 * @since 2024-04-05
 */
public interface Tool {
    /**
     * 获取商品信息。
     *
     * @return 表示商品信息的 {@link Info}。
     */
    Info info();

    /**
     * 获取工具的元数据。
     *
     * @return 表示工具元数据的 {@link Metadata}。
     */
    Metadata metadata();

    /**
     * 调用工具。
     *
     * @param args 表示调用工具的参数列表的 {@link Object}{@code []}。
     * @return 表示调用工具的结果的 {@link Object}。
     */
    Object execute(Object... args);

    /**
     * 使用 Json 格式的参数调用工具。
     * <p>该调用的参数实际上是将 {@link #executeWithJsonObject(Map)} 方法的参数进行了一次 Json 序列化。</p>
     * <p>该调用的返回值实际上是将工具调用的原始返回值进行了一次 Json 序列化。</p>
     *
     * @param jsonArgs 表示调用工具的 Json 格式的参数的 {@link String}。
     * @return 表示调用工具的 Json 格式的结果的 {@link Object}。
     */
    Object executeWithJson(String jsonArgs);

    /**
     * 使用 Json 格式的对象参数调用工具。
     * <p>该调用的参数实际上是将 {@link #execute(Object...)} 方法的参数按照参数名加值的方式，拼装成了一个对象。</p>
     * <p>该调用的返回值就是工具调用的原始返回值。</p>
     *
     * @param jsonObject 表示调用工具的 Json 格式的对象参数的 {@link Map}{@code <}{@link String}{@code , }{@link
     * Object}{@code >}。
     * @return 表示调用工具的结果的 {@link Object}。
     */
    Object executeWithJsonObject(Map<String, Object> jsonObject);

    /**
     * 调用工具，并默认使用 json 序列化器对结果进行序列化。
     *
     * @param args 表示调用工具的参数列表的 {@link Object}{@code []}。
     * @return 表示调用工具的结果的 {@link Object}。
     */
    String prettyExecute(Object... args);

    /**
     * 使用 Json 格式的参数调用工具，并默认使用 json 序列化器对结果进行序列化。
     * <p>该调用的参数实际上是将 {@link #executeWithJsonObject(Map)} 方法的参数进行了一次 Json 序列化。</p>
     * <p>该调用的返回值实际上是将工具调用的原始返回值进行了一次 Json 序列化。</p>
     *
     * @param jsonArgs 表示调用工具的 Json 格式的参数的 {@link String}。
     * @return 表示调用工具的 Json 格式的结果的 {@link Object}。
     */
    String prettyExecuteWithJson(String jsonArgs);

    /**
     * 使用 Json 格式的对象参数调用工具，并默认使用 json 序列化器对结果进行序列化。
     * <p>该调用的参数实际上是将 {@link #execute(Object...)} 方法的参数按照参数名加值的方式，拼装成了一个对象。</p>
     * <p>该调用的返回值就是工具调用的原始返回值。</p>
     *
     * @param jsonObject 表示调用工具的 Json 格式的对象参数的 {@link Map}{@code <}{@link String}{@code , }{@link
     * Object}{@code >}。
     * @return 表示调用工具的结果的 {@link Object}。
     */
    String prettyExecuteWithJsonObject(Map<String, Object> jsonObject);

    /**
     * 调用工具，并使用指定的转换工具对结果进行格式化。
     *
     * @param converter 表示转换工具的 {@link Tool}。
     * @param args 表示调用工具的参数列表的 {@link Object}{@code []}。
     * @return 表示调用工具的结果的 {@link Object}。
     */
    String prettyExecute(Tool converter, Object... args);

    /**
     * 使用 Json 格式的参数调用工具，并使用指定的转换工具对结果进行格式化。
     * <p>该调用的参数实际上是将 {@link #executeWithJsonObject(Map)} 方法的参数进行了一次 Json 序列化。</p>
     * <p>该调用的返回值实际上是将工具调用的原始返回值进行了一次 Json 序列化。</p>
     *
     * @param converter 表示转换工具的 {@link Tool}。
     * @param jsonArgs 表示调用工具的 Json 格式的参数的 {@link String}。
     * @return 表示调用工具的 Json 格式的结果的 {@link Object}。
     */
    String prettyExecuteWithJson(Tool converter, String jsonArgs);

    /**
     * 使用 Json 格式的对象参数调用工具，并使用指定的转换工具对结果进行格式化。
     * <p>该调用的参数实际上是将 {@link #execute(Object...)} 方法的参数按照参数名加值的方式，拼装成了一个对象。</p>
     * <p>该调用的返回值就是工具调用的原始返回值。</p>
     *
     * @param converter 表示转换工具的 {@link Tool}。
     * @param jsonObject 表示调用工具的 Json 格式的对象参数的 {@link Map}{@code <}{@link String}{@code , }{@link
     * Object}{@code >}。
     * @return 表示调用工具的结果的 {@link Object}。
     */
    String prettyExecuteWithJsonObject(Tool converter, Map<String, Object> jsonObject);

    /**
     * 表示工具信息。
     */
    interface Info extends ToolInfo {
        /**
         * 获取工具的唯一名字。
         *
         * @return 表示工具的唯一名字的 {@link String}。
         */
        String uniqueName();

        /**
         * 获取工具组的名字。
         *
         * @return 表示工具组名字的 {@link String}。
         */
        String groupName();

        /**
         * 获取工具定义名。
         *
         * @return 表示获取工具定义名 {@link String}。
         */
        String definitionName();

        /**
         * 获取工具定义组名。
         *
         * @return 表示获取工具定义组名 {@link String}。
         */
        String definitionGroupName();

        /**
         * 获取工具的格式规范描述。
         *
         * @return 表示工具的格式规范描述的 {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}。
         */
        Map<String, Object> schema();

        /**
         * 获取工具的运行规范描述。
         *
         * @return 表示工具的格式规范描述的 {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}。
         */
        Map<String, Object> runnables();

        /**
         * 获取工具的版本。
         *
         * @return 表示工具的版本的 {@link String}。
         */
        String version();

        /**
         * 获取当前版本工具是否最新的状态。
         *
         * @return 表示当前版本工具是否最新的状态的 {@code boolean}。
         */
        boolean isLatest();

        /**
         * 获取输出转换器的工具方法。
         *
         * @return 表示输出转换器工具方法的 {@link String}。
         */
        String returnConverter();

        /**
         * 获取所有的默认参数值。
         *
         * @return 表示参数默认值的{@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}。
         */
        Map<String, Object> defaultParameterValues();

        /**
         * {@link Info} 的构建器。
         */
        interface Builder extends ToolInfo.Builder<Builder> {
            /**
             * 向当前构建器中设置工具的唯一名字。
             *
             * @param uniqueName 表示待设置的工具唯一名字的 {@link String}。
             * @return 表示当前构建器的 {@link Builder}。
             */
            Builder uniqueName(String uniqueName);

            /**
             * 向当前构建器中设置工具名字。
             *
             * @param groupName 表示待设置的工具组名字的 {@link String}。
             * @return 表示当前构建器的 {@link Builder}。
             */
            Builder groupName(String groupName);

            /**
             * 向当前构建器中设置工具定义名称。
             *
             * @param definitionName 表示待设置的当前工具定义名称的 {@link String}。
             * @return 表示当前构建器的 {@link Builder}。
             */
            Builder definitionName(String definitionName);

            /**
             * 向当前构建器中设置工具定义组名称。
             *
             * @param definitionGroupName 表示待设置的当前工具定义组名称的 {@link String}。
             * @return 表示当前构建器的 {@link Builder}。
             */
            Builder definitionGroupName(String definitionGroupName);

            /**
             * 向当前构建器中设置工具的格式规范描述。
             *
             * @param schema 表示工具格式规范描述的 {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}。
             * @return 表示当前构建器的 {@link Builder}。
             */
            Builder schema(Map<String, Object> schema);

            /**
             * 向当前构建器中设置工具的运行规范描述。
             *
             * @param runnables 表示工具格式规范描述的 {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}。
             * @return 表示当前构建器的 {@link Builder}。
             */
            Builder runnables(Map<String, Object> runnables);

            /**
             * 向当前构建器中设置工具的版本。
             *
             * @param version 表示待设置的工具版本的 {@link String}。
             * @return 表示当前构建器的 {@link Builder}。
             */
            Builder version(String version);

            /**
             * 向当前构建器中设置当前工具版本是否最新。
             *
             * @param isLatest 表示待设置的当前工具版本是否最新的 {@code boolean}。
             * @return 表示当前构建器的 {@link Builder}。
             */
            Builder isLatest(boolean isLatest);

            /**
             * 向当前构建器中设置输出转换器工具方法。
             *
             * @param returnConverter 表示待设置的输出转换器工具方法的 {@link String}。
             * @return 表示当前构建器的 {@link Builder}。
             */
            Builder returnConverter(String returnConverter);

            /**
             * 向当前构建器中设置默认参数值。
             *
             * @param defaultParameterValues 表示待设置的参数默认值的
             * {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}。
             * @return 表示当前构建器的 {@link Builder}。
             */
            Builder defaultParameterValues(Map<String, Object> defaultParameterValues);

            /**
             * 构建对象。
             *
             * @return 表示构建出来的对象的 {@link Info}。
             */
            Info build();
        }

        /**
         * 获取 {@link Info} 的构建器。
         *
         * @return 表示 {@link Info} 的构建器的 {@link Builder}。
         */
        static Builder custom() {
            return custom(null);
        }

        /**
         * 获取 {@link Info} 的构建器，同时将指定对象的值进行填充。
         *
         * @param value 表示指定对象的 {@link Info}。
         * @return 表示 {@link Info} 的构建器的 {@link Builder}。
         */
        static Builder custom(Info value) {
            return BuilderFactory.get(Info.class, Builder.class).create(value);
        }
    }

    /**
     * 表示工具的元数据信息。
     */
    interface Metadata {
        /**
         * 获取工具的参数类型列表。
         *
         * @return 表示工具的参数类型列表的 {@link List}{@code <}{@link Type}{@code >}。
         */
        List<Type> parameterTypes();

        /**
         * 获取工具的参数名字序列，按照参数定义顺序排序。
         *
         * @return 表示工具的参数名字列表的 {@link List}{@code <}{@link String}{@code >}。
         */
        List<String> parameterOrder();

        /**
         * 获取工具参数列表中指定名字的参数的默认值。
         *
         * @param name 表示指定参数名字的 {@link String}。
         * @return 表示指定名字参数的默认值的 {@link Object}。
         */
        Object parameterDefaultValue(String name);

        /**
         * 获取工具参数列表中指定名字的参数的位置下标。
         *
         * @param name 表示指定参数名字的 {@link String}。
         * @return 表示指定名字参数的位置下标的 {@code int}。
         */
        int parameterIndex(String name);

        /**
         * 获取工具的参数列表中，必须的参数名字列表。
         *
         * @return 表示必须的参数名字列表的 {@link Set}{@code <}{@link String}{@code >}。
         */
        Set<String> requiredParameters();

        /**
         * 获取工具的返回值类型。
         *
         * @return 表示工具返回值类型 JsonSchema 格式的 {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}。
         */
        Map<String, Object> returnType();

        /**
         * 获取输出转换器的工具方法。
         *
         * @return 表示输出转换器工具方法的 {@link String}。
         */
        String returnConverter();

        /**
         * 获取工具内执行的方法。
         *
         * @return 表示工具内执行的方法类型的 {@link Optional}{@code <}{@link Method}{@code >}。
         */
        Optional<Method> getMethod();

        /**
         * 获取工具的格式规范描述。
         *
         * @return 表示工具的格式规范描述的 {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}。
         */
        Map<String, Object> schema();

        /**
         * 获取工具定义名。
         *
         * @return 表示获取工具定义名 {@link String}。
         */
        String definitionName();

        /**
         * 获取工具定义组名。
         *
         * @return 表示获取工具定义组名 {@link String}。
         */
        String definitionGroupName();

        /**
         * 获取工具定义描述。
         *
         * @return 表示工具定义的描述信息 {@link String}。
         */
        String description();

        /**
         * 通过本地方法创建一个可配置的元数据信息。
         *
         * @param method 表示本地方法的 {@link Method}。
         * @return 表示通过本地方法创建一个可配置的元数据信息的 {@link Metadata}。
         * @throws IllegalArgumentException 当 {@code method} 为 {@code null} 时。
         */
        static Metadata fromMethod(Method method) {
            return new MethodToolMetadata(method);
        }

        /**
         * 通过工具的格式规范创建一个可配置的元数据信息。
         *
         * @param definitionGroupName 表示定义组名 {@link String}。
         * @param schema 表示工具的格式规范的 {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}。
         * @return 表示通过工具的格式规范创建一个可配置的元数据信息的 {@link Metadata}。
         * @throws IllegalArgumentException 当 {@code schema} 为 {@code null} 时。
         */
        static Metadata fromSchema(String definitionGroupName, Map<String, Object> schema) {
            return new SchemaToolMetadata(definitionGroupName, schema);
        }
    }
}

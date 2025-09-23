/*
 * Copyright (c) 2024-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fitframework.parameterization.support;

import modelengine.fitframework.parameterization.ParameterizationMode;
import modelengine.fitframework.parameterization.ParameterizedString;
import modelengine.fitframework.parameterization.ParameterizedStringResolver;
import modelengine.fitframework.parameterization.ResolvedParameter;
import modelengine.fitframework.parameterization.StringFormatException;
import modelengine.fitframework.util.CollectionUtils;
import modelengine.fitframework.util.MapUtils;
import modelengine.fitframework.util.ObjectUtils;
import modelengine.fitframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 为 {@link ParameterizedString} 提供默认实现。
 *
 * @author 梁济时
 * @author 季聿阶
 * @since 2020-07-24
 */
class DefaultParameterizedString implements ParameterizedString {
    private final ParameterizedStringResolver resolver;
    private final String originalString;
    private String escapedString;
    private final List<DefaultResolvedParameter> parameters;
    private final ParameterizationMode mode;

    /**
     * 使用源字符串及解析到的参数信息的集合初始化 {@link DefaultParameterizedString} 类的新实例。
     *
     * @param resolver 表示解析得到当前参数化字符串的解析器的 {@link DefaultParameterizedStringResolver}。
     * @param originalString 表示源字符串的 {@link String}。
     * @param mode 表示描述参数化字符串的解析模式的 {@link ParameterizationMode}。
     */
    private DefaultParameterizedString(ParameterizedStringResolver resolver, String originalString,
            ParameterizationMode mode) {
        this.resolver = resolver;
        this.originalString = originalString;
        this.parameters = new ArrayList<>();
        this.mode = mode;
    }

    @Override
    public ParameterizedStringResolver getResolver() {
        return this.resolver;
    }

    @Override
    public String getOriginalString() {
        return this.originalString;
    }

    @Override
    public List<ResolvedParameter> getParameters() {
        return this.parameters.stream().map(ObjectUtils::<ResolvedParameter>cast).collect(Collectors.toList());
    }

    @Override
    public String format(Map<?, ?> args) {
        return this.format(args, null);
    }

    @Override
    public String format(Map<?, ?> args, String defaultValue) {
        Map<?, ?> actualArgs = ObjectUtils.nullIf(args, Collections.EMPTY_MAP);
        long count = this.getParameters().stream().map(ResolvedParameter::getName).distinct().count();

        // 根据模式检查参数数量。
        if (this.mode.isRequireExactParameterCount()) {
            if (MapUtils.count(actualArgs) != count) {
                throw new StringFormatException("The provided args is not match the required args.");
            }
        } else {
            if (MapUtils.count(actualArgs) < count) {
                // 在非严格模式下，如果缺失参数策略是抛异常，仍然检查。
                if (this.mode.getMissingParameterBehavior()
                        == ParameterizationMode.MissingParameterBehavior.THROW_EXCEPTION) {
                    throw new StringFormatException("Required parameters are missing.");
                }
            }
        }

        if (CollectionUtils.isEmpty(this.getParameters())) {
            return this.escapedString;
        } else {
            List<DefaultResolvedParameter> sortedParameters =
                    this.parameters.stream().sorted(Comparator.comparingInt(ResolvedParameter::getPosition)).toList();
            int index = 0;
            int affixLength = this.measureAffix();
            StringBuilder builder = new StringBuilder(this.escapedString.length() << 1);
            for (DefaultResolvedParameter parameter : sortedParameters) {
                builder.append(this.escapedString, index, parameter.getEscapedPosition());
                builder.append(StringUtils.normalize(getParameterValue(actualArgs, parameter.getName(), defaultValue)));
                index = parameter.getEscapedPosition() + affixLength + measure(parameter.getName());
            }
            builder.append(this.escapedString.substring(index));
            return builder.toString();
        }
    }

    /**
     * 获取指定名称的参数的值。
     *
     * @param args 表示参数映射的 {@link Map}。
     * @param name 表示所需参数的名称的 {@link String}。
     * @param defaultValue 表示当参数缺失时使用的默认值的 {@link String}。
     * @return 表示参数的值的 {@link String}。
     */
    private String getParameterValue(Map<?, ?> args, String name, String defaultValue) {
        if (!args.containsKey(name)) {
            return switch (this.mode.getMissingParameterBehavior()) {
                case THROW_EXCEPTION -> throw new StringFormatException(StringUtils.format(
                        "Parameter '{0}' required but not supplied.",
                        name));
                case USE_EMPTY_STRING -> StringUtils.EMPTY;
                case USE_DEFAULT_VALUE -> StringUtils.normalize(defaultValue);
                case KEEP_PLACEHOLDER ->
                        this.getResolver().getParameterPrefix() + name + this.getResolver().getParameterSuffix();
            };
        }
        return ObjectUtils.toString(args.get(name));
    }

    /**
     * 测量前后缀的长度和，并返回结果。
     *
     * @return 表示前后缀的长度和的32位整数。
     */
    private int measureAffix() {
        return measure(this.getResolver().getParameterPrefix()) + measure(this.getResolver().getParameterSuffix());
    }

    /**
     * 测量指定字符串。
     *
     * @param string 表示待测量的字符串的 {@link String}。
     * @return 表示字符串的长度的32位整数。
     */
    private static int measure(String string) {
        return string.length();
    }

    @Override
    public String toString() {
        return StringUtils.format("[originalString={0}, variables={1}]",
                this.getOriginalString(),
                this.getParameters());
    }

    /**
     * 根据指定的解析器，解析指定的字符串。
     *
     * @param resolver 表示用以解析字符串的解析器的 {@link DefaultParameterizedStringResolver}。
     * @param originalString 表示待解析的字符串的 {@link String}。
     * @param mode 表示描述参数化字符串的解析模式的 {@link ParameterizationMode}。
     * @return 表示解析后得到的参数化字符串的 {@link ParameterizedString}。
     * @throws StringFormatException 当源字符串格式不满足解析器的要求时。
     */
    static DefaultParameterizedString resolve(DefaultParameterizedStringResolver resolver, String originalString,
            ParameterizationMode mode) {
        DefaultParameterizedString parameterizedString = new DefaultParameterizedString(resolver, originalString, mode);
        parameterizedString.new Resolver(mode).resolve();
        return parameterizedString;
    }

    /**
     * 为解析字符串提供工具。
     * <p>解析工具必然属于一个{@link ParameterizedString 参数化字符串}，并且在调用 {@link Resolver#resolve()}
     * 方法时对所属的参数化字符串产生影响。</p>
     *
     * @author 梁济时
     * @since 2020-07-24
     */
    private class Resolver {
        private final StringBuilder escaped;
        private final ParameterizationMode mode;
        private int position;
        private StringBuilder parameter;
        private int parameterEscapedCharacters;

        /**
         * 初始化一个 {@link Resolver} 类的新实例。
         *
         * @param mode 表示描述参数化字符串的解析模式的 {@link ParameterizationMode}。
         */
        Resolver(ParameterizationMode mode) {
            this.escaped = new StringBuilder(this.getOriginalString().length());
            this.mode = mode;
        }

        /**
         * 解析字符串，并将结果应用到所属的参数化字符串中。
         */
        void resolve() {
            while (this.position < this.getOriginalString().length()) {
                if (this.isEscapeCharacter()) {
                    this.resolveEscapeCharacter();
                    continue;
                }
                if (this.parameter == null) {
                    if (this.isPrefix()) {
                        this.resolvePrefix();
                        continue;
                    }
                    if (this.isSuffix()) {
                        this.resolveSuffix();
                        continue;
                    }
                } else {
                    if (this.isSuffix()) {
                        this.resolveSuffix();
                        continue;
                    }
                    if (this.isPrefix()) {
                        this.resolvePrefix();
                        continue;
                    }
                }
                {
                    this.resolveCharacter();
                }
            }
            // 此时若字数构建器未被重置，则说明源字符串结尾存在未关闭的参数声明。
            if (this.parameter != null) {
                throw new StringFormatException(StringUtils.format("Incomplete parameter. [string={0}, position={1}]",
                        this.getOriginalString(),
                        this.position - this.parameter.length() - this.getResolver().getParameterPrefix().length()));
            }
            DefaultParameterizedString.this.escapedString = this.escaped.toString();
        }

        /**
         * 获取源字符串。源字符串保存在所属的参数化字符串中。
         *
         * @return 表示源字符串的 {@link String}。
         */
        private String getOriginalString() {
            return DefaultParameterizedString.this.getOriginalString();
        }

        /**
         * 获取所属的解析器。根据所属参数化字符串向上查找。
         *
         * @return 表示解析器的 {@link ParameterizedStringResolver}。
         */
        private ParameterizedStringResolver getResolver() {
            return DefaultParameterizedString.this.getResolver();
        }

        /**
         * 检查当前字符是否是一个转义字符。
         *
         * @return 若当前字符是转义字符，则为 {@code true}；否则为 {@code false}。
         */
        private boolean isEscapeCharacter() {
            return this.getOriginalString().charAt(this.position) == this.getResolver().getEscapeCharacter();
        }

        /**
         * 处理转义字符。
         */
        private void resolveEscapeCharacter() {
            if (this.position < this.getOriginalString().length() - 1) {
                this.position++;
                // 若正在收集参数，则需记录参数包含的转义字符的数量，以便于准确计算参数位置。
                if (this.parameter != null) {
                    this.parameterEscapedCharacters++;
                }
                this.resolveCharacter();
            } else {
                throw new StringFormatException(StringUtils.format(
                        "Invalid escape character position. [string={0}, position={1}]",
                        this.getOriginalString(),
                        this.position));
            }
        }

        /**
         * 检查源字符串的当前位置是否是参数前缀。
         *
         * @return 若是参数前缀，则为 {@code true}；否则为 {@code false}。
         */
        private boolean isPrefix() {
            return matchString(this.getOriginalString(), this.position, this.getResolver().getParameterPrefix());
        }

        /**
         * 解析参数前缀。应首先判断{@link Resolver#isPrefix() 当前位置是参数前缀}。
         */
        private void resolvePrefix() {
            if (this.parameter != null) {
                throw new StringFormatException(StringUtils.format(
                        "Invalid prefix position. [string={0}, " + "position={1}]",
                        this.getOriginalString(),
                        this.position));
            }
            this.parameter = new StringBuilder();
            this.position += this.getResolver().getParameterPrefix().length();
        }

        /**
         * 检查源字符串的当前位置是否是参数后缀。
         *
         * @return 若是参数后缀，则为 {@code true}；否则为 {@code false}。
         */
        private boolean isSuffix() {
            return matchString(this.getOriginalString(), this.position, this.getResolver().getParameterSuffix());
        }

        /**
         * 解析参数后缀。应首先判断{@link Resolver#isSuffix() 当前位置是参数后缀}。
         */
        private void resolveSuffix() {
            if (this.parameter == null) {
                if (this.mode.isStrictSyntax()) {
                    throw new StringFormatException(StringUtils.format(
                            "Invalid suffix position. [string={0}, position={1}]",
                            this.getOriginalString(),
                            this.position));
                }
                this.resolveCharacter();
                return;
            }
            String parameterName = this.parameter.toString();
            this.parameter = null;
            int parameterPosition = this.getParameterPosition(parameterName);
            this.position += this.getResolver().getParameterSuffix().length();
            int length = this.position - parameterPosition;
            DefaultResolvedParameter resolvedParameter =
                    new DefaultResolvedParameter(parameterName, parameterPosition, length, this.escaped.length());
            this.escaped.append(this.getResolver().getParameterPrefix())
                    .append(parameterName)
                    .append(this.getResolver().getParameterSuffix());
            DefaultParameterizedString.this.parameters.add(resolvedParameter);
        }

        /**
         * 解析普通字符。在当前字符位置不为特殊字符时进行常规化操作。
         * <p>若正在收集参数名称，则暂不将字符纳入已转义字符串中，在参数关闭时统一纳入。</p>
         */
        private void resolveCharacter() {
            ObjectUtils.nullIf(this.parameter, this.escaped).append(this.getOriginalString().charAt(this.position));
            this.position++;
        }

        /**
         * 获取当前参数的位置。
         * <p>获取参数位置后，会清理已记录的参数包含的转义字符的数量。</p>
         *
         * @param parameterName 表示当前参数的名称的 {@link String}。
         * @return 表示参数位置的32位整数。
         */
        private int getParameterPosition(String parameterName) {
            int actualPosition = this.position;
            actualPosition -= parameterName.length();
            actualPosition -= this.getResolver().getParameterPrefix().length();
            actualPosition -= this.parameterEscapedCharacters;
            this.parameterEscapedCharacters = 0;
            return actualPosition;
        }
    }

    /**
     * 检查指定字符串的指定索引处是否匹配指定的占位符字符串。
     *
     * @param s 表示待检查的字符串的 {@link String}。
     * @param start 表示源字符串中待检测位置的索引的32位整数。
     * @param placeholder 表示待匹配的占位符的 {@link String}。
     * @return 若源字符串从指定索引处与占位符匹配，则为 {@code true}；否则为 {@code false}。
     */
    private static boolean matchString(String s, int start, String placeholder) {
        if (s.length() - placeholder.length() < start) {
            return false;
        }
        for (int i = start, j = 0; j < placeholder.length(); i++, j++) {
            if (s.charAt(i) != placeholder.charAt(j)) {
                return false;
            }
        }
        return true;
    }
}

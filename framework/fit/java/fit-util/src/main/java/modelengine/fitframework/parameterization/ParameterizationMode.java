/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fitframework.parameterization;

/**
 * 参数化字符串的格式化模式。
 *
 * @author 季聿阶
 * @since 2025-09-23
 */
public enum ParameterizationMode {
    /**
     * 严格模式：
     * <ul>
     *     <li>参数数量必须完全匹配</li>
     *     <li>语法错误抛异常</li>
     *     <li>缺失参数抛异常</li>
     * </ul>
     */
    STRICT(true, true, MissingParameterBehavior.THROW_EXCEPTION),

    /**
     * 宽松模式 - 使用空字符串：
     * <ul>
     *     <li>允许多余参数</li>
     *     <li>语法错误当普通字符处理</li>
     *     <li>缺失参数使用空字符串</li>
     * </ul>
     */
    LENIENT_EMPTY(false, false, MissingParameterBehavior.USE_EMPTY_STRING),

    /**
     * 宽松模式 - 使用默认值：
     * <ul>
     *     <li>允许多余参数</li>
     *     <li>语法错误当普通字符处理</li>
     *     <li>缺失参数使用默认值</li>
     * </ul>
     */
    LENIENT_DEFAULT(false, false, MissingParameterBehavior.USE_DEFAULT_VALUE),

    /**
     * 宽松模式 - 保持占位符：
     * <ul>
     *     <li>允许多余参数</li>
     *     <li>语法错误当普通字符处理</li>
     *     <li>缺失参数保持原占位符</li>
     * </ul>
     */
    LENIENT_KEEP_PLACEHOLDER(false, false, MissingParameterBehavior.KEEP_PLACEHOLDER),

    /**
     * 混合模式：
     * <ul>
     *     <li>允许多余参数</li>
     *     <li>语法错误当普通字符处理</li>
     *     <li>但缺失参数仍抛异常（便于调试）</li>
     * </ul>
     */
    LENIENT_STRICT_PARAMETERS(false, false, MissingParameterBehavior.THROW_EXCEPTION);

    private final boolean requireExactParameterCount;
    private final boolean strictSyntax;
    private final MissingParameterBehavior missingParameterBehavior;

    ParameterizationMode(boolean requireExactParameterCount, boolean strictSyntax,
            MissingParameterBehavior missingParameterBehavior) {
        this.requireExactParameterCount = requireExactParameterCount;
        this.strictSyntax = strictSyntax;
        this.missingParameterBehavior = missingParameterBehavior;
    }

    /**
     * 是否要求参数数量必须完全匹配。
     *
     * @return 如果要求参数数量必须完全匹配，则返回 {@code true}，否则，返回 {@code false}。
     */
    public boolean isRequireExactParameterCount() {
        return this.requireExactParameterCount;
    }

    /**
     * 是否要求语法错误抛异常。
     *
     * @return 如果要求语法错误抛异常，则返回 {@code true}，否则，返回 {@code false}。
     */
    public boolean isStrictSyntax() {
        return this.strictSyntax;
    }

    /**
     * 获取缺失参数处理行为。
     *
     * @return 表示缺失参数处理行为的 {@link MissingParameterBehavior}。
     */
    public MissingParameterBehavior getMissingParameterBehavior() {
        return this.missingParameterBehavior;
    }

    /**
     * 缺失参数处理行为。
     */
    public enum MissingParameterBehavior {
        /**
         * 抛异常。
         */
        THROW_EXCEPTION,

        /**
         * 使用空字符串。
         */
        USE_EMPTY_STRING,

        /**
         * 使用默认值。
         */
        USE_DEFAULT_VALUE,

        /**
         * 保持原占位符。
         */
        KEEP_PLACEHOLDER
    }
}

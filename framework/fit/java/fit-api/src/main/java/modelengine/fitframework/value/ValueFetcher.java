/*
 * Copyright (c) 2024-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fitframework.value;

/**
 * 表示值的获取工具。
 *
 * @author 季聿阶
 * @since 2022-08-04
 */
public interface ValueFetcher {
    /**
     * 从指定对象中按照属性的路径获取其值。
     * <p>获取属性值的规则如下：</p>
     * <ol>
     *     <li>当 {@code object} 为 {@code null} 时，返回 {@code null}。</li>
     *     <li>当 {@code propertyPath} 为 {@code null} 或空白字符串时，返回 {@code object}。</li>
     *     <li>当 {@code propertyPath} 不为 {@code null} 时，必须是一个以 {@code '.'} 作为分隔符的字符串，且首尾不能是 {@code '.'}。
     *     将 {@code propertyPath} 以 {@code '.'} 进行切割，如果 {@code propertyPath} 是以 {@code '$'} 开头，则忽略切割后的第一部
     *     分。切割后的部分相当于是 {@code object} 对象内的逐级键，其值就是按照逐级键一层一层获取后的最后一部分值。</li>
     * </ol>
     * 
     * <p>例如：</p>
     * <pre>
     * +---------------------+--------------+---------------------+
     * |        object       | propertyPath |        value        |
     * +---------------------|--------------+---------------------+
     * | {"k1": {"k2": "v"}} | (empty)      | {"k1": {"k2": "v"}} |
     * | {"k1": {"k2": "v"}} | k1           | {"k2": "v"}         |
     * | {"k1": {"k2": "v"}} | k1.k2        | "v"                 |
     * | {"k1": {"k2": "v"}} | k2           | null                |
     * | {"k1": {"k2": "v"}} | $.k1.k2      | "v"                 |
     * | {"k1": {"k2": "v"}} | $.k2         | null                |
     * | null                | k            | null                |
     * | "v"                 | (empty)      | "v"                 |
     * +---------------------+--------------+---------------------+
     * </pre>
     * 任意输入对象都可以转换为键值对的形式。
     * <p><strong>注意：</strong>当 {@code object} 为 {@link String} 时，默认按普通字符串处理，不会自动解析 JSON 文本。
     * 如果需要从 JSON 字符串中取值，请调用方先使用 {@link modelengine.fitframework.serialization.ObjectSerializer}
     * 将其反序列化为对象，再调用本方法。</p>
     * 
     * @param object 表示指定对象的 {@link Object}。
     * @param propertyPath 表示待获取属性的路径的 {@link String}。
     * @return 表示获取到的属性值的 {@link Object}。
     */
    Object fetch(Object object, String propertyPath);
}

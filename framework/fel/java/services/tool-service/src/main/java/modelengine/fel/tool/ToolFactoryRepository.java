/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool;

import java.util.Optional;
import java.util.Set;

/**
 * 表示创建工具的工厂接口。
 *
 * @author 王攀博
 * @since 2024-04-23
 */
public interface ToolFactoryRepository {
    /**
     * 注册工具工厂。
     *
     * @param factory 表示注册的工具工厂的 {@link ToolFactory}。
     */
    void register(ToolFactory factory);

    /**
     * 反注册工具工厂。
     *
     * @param factory 表示要反注册的工具工厂的 {@link ToolFactory}。
     */
    void unregister(ToolFactory factory);

    /**
     * 根据运行规范标签匹配一个工厂。
     *
     * @param runnables 表示运行规范集合的 {@link Set}{@code <}{@link String}{@code >}。
     * @return 表示返回的工厂的 {@link Optional}{@code <}{@link ToolFactory}{@code >}，否则为 {@link Optional#empty()}。
     */
    Optional<ToolFactory> match(Set<String> runnables);
}

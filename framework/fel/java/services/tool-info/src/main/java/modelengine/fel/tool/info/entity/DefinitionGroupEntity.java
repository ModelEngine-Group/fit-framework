/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.info.entity;

import java.util.List;

/**
 * 表示定义组的实体类。
 *
 * @since 2024-10-26
 */
public class DefinitionGroupEntity extends GroupEntity {
    private List<DefinitionEntity> definitions;

    /**
     * 获取定义组中的所有定义。
     *
     * @return 表示定义组的 {@link List}{@code <}{@link DefinitionEntity}{@code >}。
     */
    public List<DefinitionEntity> getDefinitions() {
        return this.definitions;
    }

    /**
     * 设置定义组中的所有定义。
     *
     * @param definitions 表示定义组的 {@link List}{@code <}{@link DefinitionEntity}{@code >}。
     */
    public void setDefinitions(List<DefinitionEntity> definitions) {
        this.definitions = definitions;
    }
}

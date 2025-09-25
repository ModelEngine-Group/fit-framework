/*
 * Copyright (c) 2025-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fel.tool.model.transfer;

import java.util.List;

/**
 * 表示定义组的基本内容。
 *
 * @author 王攀博
 * @since 2024-10-25
 */
public class DefinitionGroupData extends GroupData {
    private List<DefinitionData> definitions;

    /**
     * 获取定义列表。
     *
     * @return 表示定义列表的 {@link List}{@code <}{@link DefinitionData}{@code >}。
     */
    public List<DefinitionData> getDefinitions() {
        return this.definitions;
    }

    /**
     * 设置定义列表。
     *
     * @param definitions 表示要设置的定义列表的 {@link List}{@code <}{@link DefinitionData}{@code >}。
     */
    public void setDefinitions(List<DefinitionData> definitions) {
        this.definitions = definitions;
    }
}

/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.waterflow.domain.utils;

import lombok.Getter;
import lombok.Setter;

/**
 * ID生成器抽象类
 *
 * @author 高诗意
 * @since 1.0
 */
public abstract class IdGenerator implements Identity {
    /**
     * id
     */
    @Getter
    @Setter
    protected String id;

    /**
     * 构造函数
     */
    public IdGenerator() {
        this(UUIDUtil.uuid());
    }

    /**
     * 构造函数
     *
     * @param id ID
     */
    public IdGenerator(String id) {
        this.id = id;
    }
}

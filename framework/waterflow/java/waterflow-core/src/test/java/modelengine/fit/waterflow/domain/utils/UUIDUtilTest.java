/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.waterflow.domain.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * {@link UUIDUtil} 的测试。
 *
 * @author 宋永坦
 * @since 2025-09-18
 */
class UUIDUtilTest {
    @Test
    void shouldGetIdWhenExecuteUuidGivenNewIdGenerator() {
        AtomicInteger idBase = new AtomicInteger(1);
        Supplier<String> oldGenerator = UUIDUtil.setUuidGenerator(() -> Integer.toString(idBase.getAndIncrement()));

        String uuid1 = UUIDUtil.uuid();
        String uuid2 = UUIDUtil.uuid();
        UUIDUtil.setUuidGenerator(oldGenerator);

        Assertions.assertEquals("1", uuid1);
        Assertions.assertEquals("2", uuid2);
    }
}
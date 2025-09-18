/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.waterflow.domain.utils;

import modelengine.fitframework.inspection.Validation;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Uuid的Utils类。
 *
 * @author 孙怡菲
 * @since 1.0
 */
public class UUIDUtil {
    private static Supplier<String> uuidGenerator = () -> UUID.randomUUID().toString().replace("-", "");

    /**
     * 随机生成uuid。
     *
     * @return 随机生成的uuid的 {@link String}。
     */
    public static String uuid() {
        return uuidGenerator.get();
    }

    /**
     * 全局替换 uuid 的生成器。
     *
     * @param uuidGenerator 表示要设置的 uuid 生成器的 {@link Supplier}{@code <}{@link String}{@code >}。
     * @return 表示设置前使用的 uuid 生成器的 {@link Supplier}{@code <}{@link String}{@code >}。
     */
    public static synchronized Supplier<String> setUuidGenerator(Supplier<String> uuidGenerator) {
        Validation.notNull(uuidGenerator, "The uuid generator should not be null.");
        Supplier<String> oldUuidGenerator = UUIDUtil.uuidGenerator;
        UUIDUtil.uuidGenerator = uuidGenerator;
        return oldUuidGenerator;
    }
}

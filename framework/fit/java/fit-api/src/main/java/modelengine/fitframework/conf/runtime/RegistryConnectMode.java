/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fitframework.conf.runtime;

import static modelengine.fitframework.inspection.Validation.notBlank;

import modelengine.fitframework.util.StringUtils;

import java.util.Arrays;

/**
 * 注册中心连接模式枚举，用于标识客户端连接注册中心的方式。
 *
 * <p>支持的连接模式包括：</p>
 * <ul>
 *     <li>{@code DIRECT}：直连注册中心，不经过代理。</li>
 *     <li>{@code PROXY}：通过本地设置的代理连接注册中心。</li>
 * </ul>
 *
 * @author 董智豪
 * @since 2025-08-04
 */
public enum RegistryConnectMode {
    /** 直连注册中心模式（不经过代理）。 */
    DIRECT("DIRECT"),

    /** 通过代理连接注册中心（例如本地 Socks/HTTP 代理）。 */
    PROXY("PROXY");

    /** 模式标识符字符串（如：DIRECT、PROXY）。 */
    private final String mode;

    /**
     * 构造函数，初始化连接模式标识符。
     *
     * @param mode 注册中心连接模式的标识符（不能为空）。
     */
    RegistryConnectMode(String mode) {
        this.mode = notBlank(mode, "The registry connect mode cannot be blank.");
    }

    /**
     * 根据字符串标识获取对应的枚举值。
     *
     * @param mode 字符串标识（如 "DIRECT"、"PROXY"）。
     * @return 匹配的 {@link RegistryConnectMode} 枚举值；如果无匹配项则返回 {@code DIRECT}，默认为直连模式。
     */
    public static RegistryConnectMode fromMode(String mode) {
        return Arrays.stream(RegistryConnectMode.values())
                .filter(registryConnectMode -> StringUtils.equals(registryConnectMode.mode, mode))
                .findFirst()
                .orElse(DIRECT);
    }
}
/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.example.client;

/**
 * 鉴权测试接口定义。
 * 包含各种鉴权场景的测试方法。
 *
 * @author 季聿阶
 * @since 2025-01-01
 */
public interface TestAuthInterface {
    /**
     * 测试Bearer Token静态鉴权。
     *
     * @return 鉴权测试结果
     */
    String testBearerStatic();

    /**
     * 测试Bearer Token参数驱动鉴权。
     *
     * @param token Bearer Token
     * @return 鉴权测试结果
     */
    String testBearerDynamic(String token);

    /**
     * 测试Basic Auth静态鉴权。
     *
     * @return 鉴权测试结果
     */
    String testBasicStatic();

    /**
     * 测试API Key Header静态鉴权。
     *
     * @return 鉴权测试结果
     */
    String testApiKeyHeaderStatic();

    /**
     * 测试API Key Query参数静态鉴权。
     *
     * @return 鉴权测试结果
     */
    String testApiKeyQueryStatic();

    /**
     * 测试API Key参数驱动鉴权。
     *
     * @param apiKey API Key值
     * @return 鉴权测试结果
     */
    String testApiKeyDynamic(String apiKey);

    /**
     * 测试动态Provider鉴权。
     *
     * @return 鉴权测试结果
     */
    String testDynamicProvider();

    /**
     * 测试自定义签名Provider鉴权。
     *
     * @return 鉴权测试结果
     */
    String testCustomProvider();

    /**
     * 测试方法级别覆盖接口级别鉴权。
     *
     * @return 鉴权测试结果
     */
    String testMethodOverride();

    /**
     * 测试多种鉴权组合（虽然我们说不支持多重鉴权，但可能有组合场景）。
     *
     * @param userToken 用户Token
     * @return 鉴权测试结果
     */
    String testCombinedAuth(String userToken);
}
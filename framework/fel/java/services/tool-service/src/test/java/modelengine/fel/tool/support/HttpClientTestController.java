/*
 * Copyright (c) 2025-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fel.tool.support;

import modelengine.fel.tool.support.entity.Address;
import modelengine.fel.tool.support.entity.Education;
import modelengine.fit.http.annotation.GetMapping;
import modelengine.fit.http.annotation.PathVariable;
import modelengine.fit.http.annotation.PostMapping;
import modelengine.fit.http.annotation.RequestBean;
import modelengine.fit.http.annotation.RequestBody;
import modelengine.fit.http.annotation.RequestForm;
import modelengine.fit.http.annotation.RequestHeader;
import modelengine.fit.http.annotation.RequestMapping;
import modelengine.fit.http.annotation.RequestQuery;
import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.annotation.Property;
import modelengine.fitframework.util.MapBuilder;

import org.jetbrains.annotations.NotNull;

import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * 表示测试用的模拟 HTTP 服务端。
 *
 * @author 何天放
 * @since 2024-06-15
 */
@Component
@RequestMapping(group = "http-tool-test")
public class HttpClientTestController {
    /**
     * 表示返回值为 {@link Map} 的 HTTP 服务端接口。
     *
     * @param name 表示姓名的 {@link String}。
     * @param age 表示年龄的 {@link Integer}。
     * @param address 表示地址的 {@link Address}
     * @param education 表示教育信息的 {@link Education}。
     * @param phoneNumbers 表示电话号码列表的 {@link List}{@code <}{@link String}{@code >}。
     * @return 表示结果的 {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}。
     */
    @PostMapping(path = "/test/return/map/{name}")
    public Map<String, Object> returnMap(@PathVariable("name") @Property(description = "表示姓名") String name,
            @RequestHeader("age") @Property(description = "表示年龄") Integer age,
            @RequestBody @Property(description = "表示地址") Address address,
            @RequestBean @Property(description = "表示教育经历") Education education,
            @RequestQuery("phoneNumbers") @Property(description = "表示电话列表") List<String> phoneNumbers) {
        return MapBuilder.<String, Object>get()
                .put("name", name)
                .put("age", age)
                .put("address", address)
                .put("education", education)
                .put("phoneNumbers", phoneNumbers)
                .build();
    }

    /**
     * 表示返回值为 {@link String} 的 HTTP 服务端接口。
     *
     * @param values 表示字符串列表的 {@link List}{@code <}{@link String}{@code >}。
     * @return 表示结果的 {@link String}。
     */
    @PostMapping(path = "/test/return/string")
    public String returnString(@RequestForm("values") @Property(description = "表示字符串列表") List<String> values) {
        return String.join(",", values);
    }

    /**
     * 表示返回值为 {@link Integer} 的 HTTP 服务端接口。
     *
     * @param values 表示整形数字列表的 {@link List}{@code <}{@link Integer}{@code >}。
     * @return 表示结果的 {@link Integer}。
     */
    @PostMapping(path = "/test/return/integer")
    public Integer returnInteger(@RequestQuery("values") @Property(description = "表示数字列表") List<Integer> values) {
        return values.stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * 表示返回值为 {@code null} 的 HTTP 服务端接口。
     */
    @PostMapping(path = "/test/return/void")
    public void returnVoid() {}

    /**
     * 表示对 basic 鉴权的校验。
     *
     * @param authorization 表示 Http 通信时的鉴权信息的 {@link String}。
     * @return 表示是否鉴权成功 {@link Boolean}。
     */
    @GetMapping(path = "/authorization/basic")
    public Boolean basicAuthorization(@RequestHeader("Authorization") String authorization) {
        if (authorization == null || !authorization.startsWith("Basic ")) {
            return false;
        }
        String base64Credentials = authorization.substring(6);
        String credentials = new String(Base64.getDecoder().decode(base64Credentials));
        String[] values = credentials.split(":", 2);

        String username = values[0];
        String password = values[1];

        // 验证用户名和密码
        if ("testuser".equals(username) && "testpass".equals(password)) {
            return true;
        }
        return false;
    }

    /**
     * 表示对 api key 鉴权的校验。
     *
     * @param authorization 表示 Http 通信时的鉴权信息的 {@link String}。
     * @return 表示是否鉴权成功 {@link Boolean}。
     */
    @GetMapping(path = "/authorization/apikey/header")
    public Boolean apiKeyAuthorization(@RequestHeader("ApiKey") String authorization) {
        return checkApiKeyAuthorization(authorization);
    }

    /**
     * 表示对 api key 鉴权的校验。
     *
     * @param authorization 表示 Http 通信时的鉴权信息的 {@link String}。
     * @return 表示是否鉴权成功 {@link Boolean}。
     */
    @GetMapping(path = "/authorization/apikey/query")
    public Boolean apiKeyQueryAuthorization(@RequestQuery("ApiKey") String authorization) {
        return checkApiKeyAuthorization(authorization);
    }

    @NotNull
    private Boolean checkApiKeyAuthorization(String authorization) {
        if (authorization == null) {
            return false;
        }
        // 检查 ApiKey 的值是否有效
        if (!"ApiKeyValue".equals(authorization)) {
            return false;
        }
        return true;
    }

    /**
     * 表示对 bearer 鉴权的校验。
     *
     * @param authorization 表示 Http 通信时的鉴权信息的 {@link String}。
     * @return 表示是否鉴权成功 {@link Boolean}。
     */
    @GetMapping(path = "/authorization/bearer")
    public Boolean bearerAuthorization(@RequestHeader("Authorization") String authorization) {
        // 校验 Authorization 是否存在，并且以 Bearer 开头
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return false;
        }

        // 提取 Bearer Token
        String token = authorization.substring(7); // 去掉 "Bearer " 部分
        // 校验 Bearer Token 是否有效
        if (!"test666666666".equals(token)) {
            return false;
        }
        return true;
    }
}

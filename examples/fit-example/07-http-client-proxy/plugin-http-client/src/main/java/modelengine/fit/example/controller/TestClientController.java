/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.example.controller;

import modelengine.fit.example.client.TestAuthClient;
import modelengine.fit.example.client.TestInterface;
import modelengine.fit.example.client.TestRequestAddress;
import modelengine.fit.example.client.TestRequestAddressClass;
import modelengine.fit.example.client.TestRequestAddressInClassMapping;
import modelengine.fit.example.client.TestRequestAddressInMethodMapping;
import modelengine.fit.example.entity.Education;
import modelengine.fit.http.annotation.GetMapping;
import modelengine.fit.http.annotation.RequestMapping;
import modelengine.fit.http.annotation.RequestQuery;
import modelengine.fitframework.annotation.Component;

import java.util.Arrays;

/**
 * Controller for handling HTTP client test operations.
 * This class provides endpoints for testing various HTTP client proxy implementations.
 *
 * @author 季聿阶
 * @since 2025-06-01
 */
@Component
@RequestMapping(path = "/http-client")
public class TestClientController {
    private final TestRequestAddress t1;
    private final TestRequestAddressClass t2;
    private final TestRequestAddressInClassMapping t3;
    private final TestRequestAddressInMethodMapping t4;
    private final TestAuthClient authClient;

    /**
     * Constructs a TestClientController with the specified test interfaces.
     *
     * @param t1 The TestRequestAddress interface.
     * @param t2 The TestRequestAddressClass interface.
     * @param t3 The TestRequestAddressInClassMapping interface.
     * @param t4 The TestRequestAddressInMethodMapping interface.
     * @param authClient The TestAuthClient interface for auth testing.
     */
    public TestClientController(TestRequestAddress t1, TestRequestAddressClass t2, TestRequestAddressInClassMapping t3,
            TestRequestAddressInMethodMapping t4, TestAuthClient authClient) {
        this.t1 = t1;
        this.t2 = t2;
        this.t3 = t3;
        this.t4 = t4;
        this.authClient = authClient;
    }

    /**
     * Endpoint for running HTTP client tests.
     * This method allows testing different HTTP client proxy implementations and methods.
     *
     * @param type The type of test interface to use (t1, t2, t3, or t4).
     * @param method The method to invoke on the selected test interface.
     * @return The result of the invoked method.
     */
    @GetMapping(path = "/test")
    public Object test(@RequestQuery("type") String type, @RequestQuery("method") String method) {
        TestInterface testClass = switch (type) {
            case "t1" -> t1;
            case "t2" -> t2;
            case "t3" -> t3;
            case "t4" -> t4;
            default -> throw new IllegalArgumentException("Invalid type: " + type);
        };
        switch (method) {
            case "requestBean":
                Education education = new Education();
                education.setBachelor("PKU");
                education.setMaster("THU");
                return testClass.requestBean(education);
            case "pathVariable":
                return testClass.pathVariable("variable");
            case "header":
                return testClass.header("header", Arrays.asList(1, 2, 3));
            case "cookie":
                return testClass.cookie("cookie");
            case "query":
                return testClass.query("query");
            case "requestBody":
                return testClass.requestBody("requestBody");
            case "form":
                return testClass.form("form");
            default:
                throw new IllegalArgumentException("Invalid method: " + method);
        }
    }

    /**
     * Endpoint for running HTTP client auth tests.
     * This method allows testing different authentication scenarios.
     *
     * @param method The auth test method to invoke.
     * @param token Optional token parameter for dynamic auth tests.
     * @return The result of the invoked auth method.
     */
    @GetMapping(path = "/auth-test")
    public Object authTest(@RequestQuery("method") String method,
            @RequestQuery(value = "token", required = false) String token,
            @RequestQuery(value = "username", required = false) String username,
            @RequestQuery(value = "password", required = false) String password) {
        switch (method) {
            case "bearerStatic":
                return authClient.testBearerStatic();
            case "bearerDynamic":
                return authClient.testBearerDynamic(token != null ? token : "dynamic-test-token");
            case "basicStatic":
                return authClient.testBasicStatic();
            case "basicDynamicUsername":
                return authClient.testBasicDynamicUsername(username != null ? username : "testuser");
            case "basicDynamicBoth":
                return authClient.testBasicDynamicBoth(username != null ? username : "testuser",
                        password != null ? password : "testpass");
            case "apiKeyHeaderStatic":
                return authClient.testApiKeyHeaderStatic();
            case "apiKeyQueryStatic":
                return authClient.testApiKeyQueryStatic();
            case "apiKeyDynamic":
                return authClient.testApiKeyDynamic(token != null ? token : "dynamic-api-key");
            case "dynamicProvider":
                return authClient.testDynamicProvider();
            case "customProvider":
                return authClient.testCustomProvider();
            case "methodOverride":
                return authClient.testMethodOverride();
            case "combinedAuth":
                return authClient.testCombinedAuth(token != null ? token : "user-context-token");
            default:
                throw new IllegalArgumentException("Invalid auth method: " + method);
        }
    }
}
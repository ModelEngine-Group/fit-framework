/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.example.client;

import modelengine.fit.example.entity.Education;

import java.util.List;

/**
 * This interface defines a set of methods for testing HTTP client proxy functionality.
 * Each method corresponds to a specific HTTP request type and parameter binding scenario.
 *
 * @author 季聿阶
 * @since 2025-06-01
 */
public interface TestInterface {
    /**
     * Tests request bean binding by sending an Education object in the request.
     *
     * @param education The Education object to be sent in the request.
     * @return The modified Education object received in the response.
     */
    Education requestBean(Education education);

    /**
     * Tests path variable binding by extracting a path variable from the URL.
     *
     * @param variable The path variable extracted from the URL.
     * @return A string containing the path variable value.
     */
    String pathVariable(String variable);

    /**
     * Tests header binding by extracting header values from the request.
     *
     * @param header The value of the "header" header.
     * @param headers The list of values for the "headers" header.
     * @return A string containing the header values.
     */
    String header(String header, List<Integer> headers);

    /**
     * Tests cookie binding by extracting a cookie value from the request.
     *
     * @param cookieValue The value of the "cookie" cookie.
     * @return A string containing the cookie value.
     */
    String cookie(String cookieValue);

    /**
     * Tests query parameter binding by extracting a query parameter from the request.
     *
     * @param query The value of the "query" query parameter.
     * @return A string containing the query parameter value.
     */
    String query(String query);

    /**
     * Tests request body binding by extracting the request body content.
     *
     * @param requestBody The content of the request body.
     * @return A string containing the request body content.
     */
    String requestBody(String requestBody);

    /**
     * Tests form data binding by extracting a form field value from the request.
     *
     * @param form The value of the "form" form field.
     * @return A string containing the form field value.
     */
    String form(String form);
}
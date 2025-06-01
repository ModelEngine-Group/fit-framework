/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.example.controller;

import modelengine.fit.example.entity.Education;
import modelengine.fit.http.annotation.GetMapping;
import modelengine.fit.http.annotation.PatchMapping;
import modelengine.fit.http.annotation.PathVariable;
import modelengine.fit.http.annotation.PostMapping;
import modelengine.fit.http.annotation.PutMapping;
import modelengine.fit.http.annotation.RequestBean;
import modelengine.fit.http.annotation.RequestBody;
import modelengine.fit.http.annotation.RequestCookie;
import modelengine.fit.http.annotation.RequestForm;
import modelengine.fit.http.annotation.RequestHeader;
import modelengine.fit.http.annotation.RequestMapping;
import modelengine.fit.http.annotation.RequestQuery;
import modelengine.fitframework.annotation.Component;

import java.util.List;

/**
 * Controller for handling HTTP requests related to test server operations.
 * This class provides endpoints for testing various HTTP request types and parameter bindings.
 *
 * @author 季聿阶
 * @since 2025-06-01
 */
@Component
@RequestMapping(path = "/http-server")
public class TestServerController {
    /**
     * Endpoint for testing request bean binding.
     * This method receives an Education object as a request bean and modifies its master degree field.
     *
     * @param education The Education object received in the request.
     * @return The modified Education object.
     */
    @PostMapping(path = "/request-bean")
    public Education requestBean(@RequestBean Education education) {
        education.setMaster("SJTU");
        return education;
    }

    /**
     * Endpoint for testing path variable binding.
     * This method retrieves a path variable from the URL and returns it in a response.
     *
     * @param variable The path variable extracted from the URL.
     * @return A string containing the path variable value.
     */
    @GetMapping(path = "/path-variable/{variable}")
    public String pathVariable(@PathVariable(name = "variable") String variable) {
        return "PathVariable: " + variable;
    }

    /**
     * Endpoint for testing header binding.
     * This method retrieves header values from the request and returns them in a response.
     *
     * @param header The value of the "header" header.
     * @param headers The list of values for the "headers" header.
     * @return A string containing the header values.
     */
    @GetMapping(path = "/header")
    public String header(@RequestHeader(name = "header") String header,
            @RequestHeader(name = "headers") List<Integer> headers) {
        return "Header: " + header + ", Headers: " + headers;
    }

    /**
     * Endpoint for testing cookie binding.
     * This method retrieves a cookie value from the request and returns it in a response.
     *
     * @param cookieValue The value of the "cookie" cookie.
     * @return A string containing the cookie value.
     */
    @GetMapping(path = "/cookie")
    public String cookie(@RequestCookie(name = "cookie") String cookieValue) {
        return "Cookie: " + cookieValue;
    }

    /**
     * Endpoint for testing query parameter binding.
     * This method retrieves a query parameter from the request and returns it in a response.
     *
     * @param query The value of the "query" query parameter.
     * @return A string containing the query parameter value.
     */
    @GetMapping(path = "/query")
    public String query(@RequestQuery(name = "query") String query) {
        return "Query: " + query;
    }

    /**
     * Endpoint for testing request body binding.
     * This method retrieves the request body as a string and returns it in a response.
     *
     * @param requestBody The content of the request body.
     * @return A string containing the request body content.
     */
    @PatchMapping(path = "/request-body")
    public String requestBody(@RequestBody String requestBody) {
        return "RequestBody: " + requestBody;
    }

    /**
     * Endpoint for testing form data binding.
     * This method retrieves a form field value from the request and returns it in a response.
     *
     * @param form The value of the "form" form field.
     * @return A string containing the form field value.
     */
    @PutMapping(path = "/form")
    public String form(@RequestForm(name = "form") String form) {
        return "Form: " + form;
    }
}
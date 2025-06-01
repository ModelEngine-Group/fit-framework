/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.example.client;

import modelengine.fit.example.entity.Education;
import modelengine.fit.http.annotation.GetMapping;
import modelengine.fit.http.annotation.HttpProxy;
import modelengine.fit.http.annotation.PatchMapping;
import modelengine.fit.http.annotation.PathVariable;
import modelengine.fit.http.annotation.PostMapping;
import modelengine.fit.http.annotation.PutMapping;
import modelengine.fit.http.annotation.RequestBean;
import modelengine.fit.http.annotation.RequestBody;
import modelengine.fit.http.annotation.RequestCookie;
import modelengine.fit.http.annotation.RequestForm;
import modelengine.fit.http.annotation.RequestHeader;
import modelengine.fit.http.annotation.RequestQuery;

import java.util.List;

/**
 * This interface defines a set of methods for testing HTTP client proxy functionality.
 * It extends the TestInterface and provides specific annotations for configuring the HTTP request details.
 * The interface is marked with @HttpProxy to indicate it's a proxy for HTTP requests.
 * Each method in this interface specifies the full URL for the HTTP request using the @GetMapping, @PostMapping, etc.
 * annotations.
 *
 * @author 季聿阶
 * @since 2025-06-01
 */
@HttpProxy
public interface TestRequestAddressInMethodMapping extends TestInterface {
    @Override
    @PostMapping(path = "http://localhost:8080/http-server/request-bean")
    Education requestBean(@RequestBean Education education);

    @Override
    @GetMapping(path = "http://localhost:8080/http-server/path-variable/{variable}")
    String pathVariable(@PathVariable(name = "variable") String variable);

    @Override
    @GetMapping(path = "http://localhost:8080/http-server/header")
    String header(@RequestHeader(name = "header") String header,
            @RequestHeader(name = "headers") List<Integer> headers);

    @Override
    @GetMapping(path = "http://localhost:8080/http-server/cookie")
    String cookie(@RequestCookie(name = "cookie") String cookieValue);

    @Override
    @GetMapping(path = "http://localhost:8080/http-server/query")
    String query(@RequestQuery(name = "query") String query);

    @Override
    @PatchMapping(path = "http://localhost:8080/http-server/request-body")
    String requestBody(@RequestBody String requestBody);

    @Override
    @PutMapping(path = "http://localhost:8080/http-server/form")
    String form(@RequestForm(name = "form") String form);
}
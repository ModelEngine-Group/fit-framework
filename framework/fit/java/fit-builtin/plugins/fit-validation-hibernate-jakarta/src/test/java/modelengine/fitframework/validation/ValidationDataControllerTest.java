/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fitframework.validation;

import static org.assertj.core.api.Assertions.assertThat;

import modelengine.fit.http.client.HttpClassicClientResponse;
import modelengine.fitframework.annotation.Fit;
import modelengine.fitframework.test.annotation.MvcTest;
import modelengine.fitframework.test.domain.mvc.MockMvc;
import modelengine.fitframework.test.domain.mvc.request.MockMvcRequestBuilders;
import modelengine.fitframework.test.domain.mvc.request.MockRequestBuilder;
import modelengine.fitframework.validation.data.Company;
import modelengine.fitframework.validation.data.Employee;
import modelengine.fitframework.validation.data.ValidationDataController;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;

/**
 * {@link ValidationDataController} 的测试集。
 *
 * @author 阮睿
 * @since 2025-07-18
 */
@MvcTest(classes = {ValidationDataController.class})
@DisplayName("测试 EvalDataController")
public class ValidationDataControllerTest {
    @Fit
    private MockMvc mockMvc;

    private HttpClassicClientResponse<?> response;

    @AfterEach
    void teardown() throws IOException {
        if (this.response != null) {
            this.response.close();
        }
    }

    @Test
    @DisplayName("合法 Company 对象校验")
    void shouldOKWhenCreateValidCompany() {
        Employee validEmployee = new Employee("John", 25);
        Company validCompany = new Company(Collections.singletonList(validEmployee));
        MockRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/validation/company/default")
                .jsonEntity(validCompany)
                .responseType(Void.class);
        this.response = this.mockMvc.perform(requestBuilder);
        assertThat(this.response.statusCode()).isEqualTo(200);
    }

    @Test
    @DisplayName("不合法 Company 对象校验")
    void shouldFailedWhenCreateInvalidCompany() {
        Company invalidCompany = new Company(null);
        MockRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/validation/company/default")
                .jsonEntity(invalidCompany)
                .responseType(Void.class);
        this.response = this.mockMvc.perform(requestBuilder);
        assertThat(this.response.statusCode()).isEqualTo(500);
    }

    @Test
    @DisplayName("自定义分组校验 Company 对象")
    void shouldOKWhenCreateValidCompanyWithGroup() {
        Employee validEmployee = new Employee("Jane", 30);
        Company validCompany = new Company(Collections.singletonList(validEmployee));
        MockRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/validation/company/companyGroup")
                .jsonEntity(validCompany)
                .responseType(Void.class);
        this.response = this.mockMvc.perform(requestBuilder);
        assertThat(this.response.statusCode()).isEqualTo(200);
    }

    @Test
    @DisplayName("自定义分组校验 Company 对象")
    void shouldFailedWhenCreateInvalidCompanyWithGroup() {
        Employee invalidEmployee = new Employee("", 15);
        Company invalidCompany = new Company(Collections.singletonList(invalidEmployee));
        MockRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/validation/company/companyGroup")
                .jsonEntity(invalidCompany)
                .responseType(Void.class);
        this.response = this.mockMvc.perform(requestBuilder);
        assertThat(this.response.statusCode()).isEqualTo(500);
    }
}
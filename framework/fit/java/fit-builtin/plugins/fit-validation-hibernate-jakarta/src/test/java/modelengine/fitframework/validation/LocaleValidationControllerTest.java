/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fitframework.validation;

import modelengine.fit.http.client.HttpClassicClientResponse;
import modelengine.fit.http.entity.Entity;
import modelengine.fit.http.entity.ObjectEntity;
import modelengine.fitframework.annotation.Fit;
import modelengine.fitframework.test.annotation.MvcTest;
import modelengine.fitframework.test.domain.mvc.MockMvc;
import modelengine.fitframework.test.domain.mvc.request.MockMvcRequestBuilders;
import modelengine.fitframework.test.domain.mvc.request.MockRequestBuilder;
import modelengine.fitframework.validation.data.Company;
import modelengine.fitframework.validation.data.LocaleValidationController;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 表示评估国际化校验的测试类。
 *
 * @author 阮睿
 * @since 2025-08-01
 */
@MvcTest(classes = {LocaleValidationController.class})
@DisplayName("测试地区化验证消息功能")
public class LocaleValidationControllerTest {
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
    @DisplayName("测试法文地区的验证消息")
    void shouldReturnChineseValidationMessage() {
        Company invalidCompany = new Company(null);

        MockRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/validation/locale/simple")
                .header("Accept-Language", "fr")
                .jsonEntity(invalidCompany)
                .responseType(Map.class);

        this.response = this.mockMvc.perform(requestBuilder);
        // 获取JSON格式的错误信息
        String errorMessage = "";
        if (this.response.entity().isPresent()) {
            Entity entity = this.response.entity().get();
            if (entity instanceof ObjectEntity) {
                ObjectEntity<?> objectEntity = (ObjectEntity<?>) entity;
                Object errorObj = objectEntity.object();
                if (errorObj instanceof Map) {
                    Map<String, Object> errorMap = (Map<String, Object>) errorObj;
                    errorMessage =
                            errorMap.get("error") != null ? errorMap.get("error").toString() : errorMap.toString();
                } else {
                    errorMessage = errorObj.toString();
                }
            }
        }

        assertThat(errorMessage).isEqualTo("validateSimpleParam.company.employees: ne doit pas être nul");
        assertThat(this.response.statusCode()).isEqualTo(500);
    }

    @Test
    @DisplayName("测试英文地区的验证消息")
    void shouldReturnEnglishValidationMessage() {
        Company invalidCompany = new Company(null);

        MockRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/validation/locale/simple")
                .header("Accept-Language", "en-us")
                .jsonEntity(invalidCompany)
                .responseType(Map.class);

        this.response = this.mockMvc.perform(requestBuilder);
        // 获取JSON格式的错误信息
        String errorMessage = "";
        if (this.response.entity().isPresent()) {
            Entity entity = this.response.entity().get();
            if (entity instanceof ObjectEntity) {
                ObjectEntity<?> objectEntity = (ObjectEntity<?>) entity;
                Object errorObj = objectEntity.object();
                if (errorObj instanceof Map) {
                    Map<String, Object> errorMap = (Map<String, Object>) errorObj;
                    errorMessage =
                            errorMap.get("error") != null ? errorMap.get("error").toString() : errorMap.toString();
                } else {
                    errorMessage = errorObj.toString();
                }
            }
        }

        assertThat(errorMessage).isEqualTo("validateSimpleParam.company.employees: must not be null");
        assertThat(this.response.statusCode()).isEqualTo(500);
    }

    @Test
    @DisplayName("测试URL参数指定地区")
    void shouldUseLocaleFromUrlParam() {
        Company invalidCompany = new Company(null);

        MockRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/validation/locale/simple")
                .param("locale", "en-US")
                .jsonEntity(invalidCompany)
                .responseType(Map.class);

        this.response = this.mockMvc.perform(requestBuilder);

        // 获取JSON格式的错误信息
        String errorMessage = "";
        if (this.response.entity().isPresent()) {
            Entity entity = this.response.entity().get();
            if (entity instanceof ObjectEntity) {
                ObjectEntity<?> objectEntity = (ObjectEntity<?>) entity;
                Object errorObj = objectEntity.object();
                if (errorObj instanceof Map) {
                    Map<String, Object> errorMap = (Map<String, Object>) errorObj;
                    errorMessage =
                            errorMap.get("error") != null ? errorMap.get("error").toString() : errorMap.toString();
                } else {
                    errorMessage = errorObj.toString();
                }
            }
        }

        assertThat(errorMessage).isEqualTo("validateSimpleParam.company.employees: must not be null");
        assertThat(this.response.cookies().get("locale").isPresent());
        assertThat(this.response.cookies().get("locale").get().value()).isEqualTo("en-US");
        assertThat(this.response.statusCode()).isEqualTo(500);
    }
}








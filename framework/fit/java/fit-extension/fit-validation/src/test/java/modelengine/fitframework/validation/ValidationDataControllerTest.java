/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024-2025 Huawei Technologies Co., Ltd. All rights reserved.
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
import modelengine.fitframework.validation.data.Car;
import modelengine.fitframework.validation.data.Product;
import modelengine.fitframework.validation.data.ValidationDataController;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

/**
 * {@link ValidationDataController} 的测试集。
 *
 * @author 吕博文
 * @since 2024-08-15
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
    @DisplayName("合法 Car 对象校验")
    void shouldOKWhenCreateValidCar() {
        Car validCar = new Car(1, 1, "brand", "model", -1, -1);
        MockRequestBuilder requestBuilder =
                MockMvcRequestBuilders.post("/validation/car/default").jsonEntity(validCar).responseType(Void.class);
        this.response = this.mockMvc.perform(requestBuilder);
        assertThat(this.response.statusCode()).isEqualTo(200);
    }

    @Test
    @DisplayName("不合法 Car 对象校验")
    void shouldFailedWhenCreateInvalidCar() {
        Car invalidCar = new Car(0, 3, "", "abd", -1, -1);
        MockRequestBuilder requestBuilder =
                MockMvcRequestBuilders.post("/validation/car/default").jsonEntity(invalidCar).responseType(Void.class);
        this.response = this.mockMvc.perform(requestBuilder);
        assertThat(this.response.statusCode()).isEqualTo(500);
    }

    @Test
    @DisplayName("自定义分组校验 Car 对象")
    void shouldOKWhenCreateValidCarWithCarGroup() {
        Car invalidCar = new Car(0, 2, "", "abd", 2000, -1);
        MockRequestBuilder requestBuilder =
                MockMvcRequestBuilders.post("/validation/car/carGroup").jsonEntity(invalidCar).responseType(Void.class);
        this.response = this.mockMvc.perform(requestBuilder);
        assertThat(this.response.statusCode()).isEqualTo(200);
    }

    @Test
    @DisplayName("自定义分组校验 Car 对象")
    void shouldFailedWhenCreateInvalidCarWithCarGroup() {
        Car invalidCar = new Car(0, 3, "", "abd", -1, -1);
        MockRequestBuilder requestBuilder =
                MockMvcRequestBuilders.post("/validation/car/carGroup").jsonEntity(invalidCar).responseType(Void.class);
        this.response = this.mockMvc.perform(requestBuilder);
        assertThat(this.response.statusCode()).isEqualTo(500);
    }

    @Test
    @DisplayName("合法 Product 对象校验")
    void shouldOKWhenCreateValidProduct() {
        Product product = new Product("mac", 10499.0, 100, "computer");
        MockRequestBuilder requestBuilder =
                MockMvcRequestBuilders.post("/validation/product/default").jsonEntity(product).responseType(Void.class);
        this.response = this.mockMvc.perform(requestBuilder);
        assertThat(this.response.statusCode()).isEqualTo(200);
    }

    @Test
    @DisplayName("不合法 Product 对象校验")
    void shouldFailedWhenCreateInvalidProduct() {
        Product product = new Product("", 10499.0, -1, "computer");
        MockRequestBuilder requestBuilder =
                MockMvcRequestBuilders.post("/validation/product/default").jsonEntity(product).responseType(Void.class);
        this.response = this.mockMvc.perform(requestBuilder);
        assertThat(this.response.statusCode()).isEqualTo(500);
    }

    @Test
    @DisplayName("合法 Product-List<Car> 对象校验: cars == null")
    void shouldOkWhenCreateInvalidProductWithNoCar() {
        Product product = new Product("mac", 10499.0, 100, "computer", null);
        MockRequestBuilder requestBuilder =
                MockMvcRequestBuilders.post("/validation/product/default").jsonEntity(product).responseType(Void.class);
        this.response = this.mockMvc.perform(requestBuilder);
        assertThat(this.response.statusCode()).isEqualTo(200);
    }

    @Test
    @DisplayName("合法 Product-List<Car> 对象校验: cars.size() == 1")
    void shouldOkWhenCreateInvalidProductWith1Car() {
        Car validCar = new Car(1, 1, "brand", "model", 2000, 1999);
        Product product = new Product("mac", 10499.0, 100, "computer", Collections.singletonList(validCar));
        MockRequestBuilder requestBuilder =
                MockMvcRequestBuilders.post("/validation/product/default").jsonEntity(product).responseType(Void.class);
        this.response = this.mockMvc.perform(requestBuilder);
        assertThat(this.response.statusCode()).isEqualTo(200);
    }

    @Test
    @DisplayName("不合法 Product-List<Car> 对象校验: cars.size() == 2")
    void shouldFailedWhenCreateInvalidProductWith2Cars() {
        Car validCar = new Car(1, 1, "brand", "model", 2000, 1999);
        Product product = new Product("mac", 10499.0, 100, "computer", Arrays.asList(validCar, validCar));
        MockRequestBuilder requestBuilder =
                MockMvcRequestBuilders.post("/validation/product/default").jsonEntity(product).responseType(Void.class);
        this.response = this.mockMvc.perform(requestBuilder);
        assertThat(this.response.statusCode()).isEqualTo(500);
    }
}
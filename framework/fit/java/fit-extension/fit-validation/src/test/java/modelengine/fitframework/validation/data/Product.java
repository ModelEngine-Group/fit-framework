/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024-2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fitframework.validation.data;

import modelengine.fitframework.validation.annotation.MaxSize;
import modelengine.fitframework.validation.constraints.NotBlank;
import modelengine.fitframework.validation.constraints.Positive;

import java.util.List;

/**
 * 表示产品的数据类。
 *
 * @author 吕博文
 * @since 2024-08-02
 */
public class Product {
    @NotBlank(message = "产品名不能为空")
    private String name;

    @Positive(message = "产品价格必须为正")
    private Double price;

    @Positive(message = "产品数量必须为正")
    private Integer quantity;

    @NotBlank(message = "产品类别不能为空")
    private String category;

    @MaxSize(max = 2)
    private List<Car> cars;

    /**
     * Product 默认构造函数。
     */
    public Product() {}

    /**
     * 构造函数。
     *
     * @param name 表示名字的 {@link String}。
     * @param price 表示价格的 {@link Double}。
     * @param quantity 表示数量的 {@link Integer}。
     * @param category 表示类别的 {@link String}。
     */
    public Product(String name, Double price, Integer quantity, String category) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.category = category;
    }

    /**
     * 构造函数。
     *
     * @param name 表示名字的 {@link String}。
     * @param price 表示价格的 {@link Double}。
     * @param quantity 表示数量的 {@link Integer}。
     * @param category 表示类别的 {@link String}。
     * @param cars 表示汽车集合的 {@link List}{@code <}{@link Car}{@code >}。
     */
    public Product(String name, Double price, Integer quantity, String category, List<Car> cars) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.category = category;
        this.cars = cars;
    }
}

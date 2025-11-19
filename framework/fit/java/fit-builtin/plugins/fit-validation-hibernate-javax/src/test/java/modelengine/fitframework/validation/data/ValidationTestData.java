/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fitframework.validation.data;

import java.math.BigDecimal;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Negative;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

/**
 * 测试用的复杂验证数据类。
 *
 * @author 阮睿
 * @since 2025-07-18
 */
public class ValidationTestData {
    // 分组接口定义。
    public interface AdvancedGroup {}

    public interface StudentGroup {}

    public interface TeacherGroup {}

    @NotBlank(message = "名称不能为空")
    private String name;

    @Min(value = 0, message = "年龄必须大于等于0")
    @Max(value = 150, message = "年龄必须小于等于150")
    @Max(value = 200, message = "高级组年龄必须小于等于200", groups = AdvancedGroup.class)
    private Integer age;

    @NotBlank(message = "描述不能为空")
    private String description;

    @NotBlank(message = "内容不能为空白")
    private String content;

    @Positive(message = "数量必须是正数")
    private Integer quantity;

    @Negative(message = "折扣必须是负数")
    private BigDecimal discount;

    @AssertTrue(message = "必须同意条款")
    private Boolean agreed;

    /**
     * 构造函数。
     */
    public ValidationTestData() {}

    /**
     * 设置名称。
     *
     * @param name 表示名称的 {@link String}。
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 设置年龄。
     *
     * @param age 表示年龄的 {@link Integer}。
     */
    public void setAge(Integer age) {
        this.age = age;
    }

    /**
     * 设置描述。
     *
     * @param description 表示描述的 {@link String}。
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 设置内容。
     *
     * @param content 表示内容的 {@link String}。
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * 设置数量。
     *
     * @param quantity 表示数量的 {@link Integer}。
     */
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    /**
     * 设置折扣。
     *
     * @param discount 表示折扣的 {@link BigDecimal}。
     */
    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    /**
     * 确认是否同意。
     *
     * @param agreed 表示是否同意的 {@link Boolean}。
     */
    public void setAgreed(Boolean agreed) {
        this.agreed = agreed;
    }
}

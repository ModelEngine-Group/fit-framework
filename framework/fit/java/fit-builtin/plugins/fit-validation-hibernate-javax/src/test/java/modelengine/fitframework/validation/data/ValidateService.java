/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fitframework.validation.data;

import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.annotation.Fit;
import modelengine.fitframework.log.Logger;
import modelengine.fitframework.validation.Validated;

import org.hibernate.validator.constraints.Range;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.AssertFalse;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Email;
import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Negative;
import javax.validation.constraints.NegativeOrZero;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Past;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;

/**
 * 表示测试用校验服务。
 *
 * @author 阮睿
 * @since 2025-07-18
 */
@Component
@Validated
public class ValidateService {
    private static final Logger LOG = Logger.get(ValidateService.class);

    @Fit
    private GroupValidateService.StudentValidateService studentValidateService;

    @Fit
    private GroupValidateService.TeacherValidateService teacherValidateService;

    @Fit
    private GroupValidateService.AdvancedValidateService advancedValidateService;

    /**
     * 测试原始类型。
     *
     * @param num 表示输入的 {@code int}。
     */
    public void foo0(@Positive(message = "必须是正数") int num) {
        LOG.debug("{}", num);
    }

    /**
     * 测试结构体类型。
     *
     * @param employee 表示输入的 {@link Employee}。
     */
    public void foo1(@Valid Employee employee) {
        LOG.debug("{}", employee);
    }

    /**
     * 测试嵌套类型。
     *
     * @param company 表示输入的 {@link Company}。
     */
    public void foo2(@Valid Company company) {
        LOG.debug("{}", company);
    }

    /**
     * 测试 NotNull 约束注解。
     *
     * @param value 表示输入的 {@code String}。
     */
    public void testNotNull(@NotNull String value) {}

    /**
     * 测试 NotEmpty 约束注解。
     *
     * @param value 表示输入的 {@code String}。
     */
    public void testNotEmpty(@NotEmpty String value) {}

    /**
     * 测试 NotBlank 约束注解。
     *
     * @param value 表示输入的 {@code String}。
     */
    public void testNotBlank(@NotBlank String value) {}

    /**
     * 测试 Null 约束注解。
     *
     * @param value 表示输入的 {@code String}。
     */
    public void testNull(@Null String value) {}

    /**
     * 测试 Size 约束注解。
     *
     * @param value 表示输入的 {@code String}。
     */
    public void testSize(@Size(min = 2, max = 10) String value) {}

    /**
     * 测试 List 的 Size 约束注解。
     *
     * @param value 表示输入的 {@code List<String>}。
     */
    public void testSizeList(@Size(min = 1, max = 3) List<String> value) {}

    /**
     * 测试 Min 约束注解。
     *
     * @param value 表示输入的 {@code int}。
     */
    public void testMin(@Min(10) int value) {}

    /**
     * 测试 Max 约束注解。
     *
     * @param value 表示输入的 {@code int}。
     */
    public void testMax(@Max(100) int value) {}

    /**
     * 测试 DecimalMin 约束注解。
     *
     * @param value 表示输入的 {@code BigDecimal}。
     */
    public void testDecimalMin(@DecimalMin("10.5") BigDecimal value) {}

    /**
     * 测试 DecimalMax 约束注解。
     *
     * @param value 表示输入的 {@code BigDecimal}。
     */
    public void testDecimalMax(@DecimalMax("100.5") BigDecimal value) {}

    /**
     * 测试 Positive 约束注解。
     *
     * @param value 表示输入的 {@code int}。
     */
    public void testPositive(@Positive int value) {}

    /**
     * 测试 PositiveOrZero 约束注解。
     *
     * @param value 表示输入的 {@code int}。
     */
    public void testPositiveOrZero(@PositiveOrZero int value) {}

    /**
     * 测试 Negative 约束注解。
     *
     * @param value 表示输入的 {@code int}。
     */
    public void testNegative(@Negative int value) {}

    /**
     * 测试 NegativeOrZero 约束注解。
     *
     * @param value 表示输入的 {@code int}。
     */
    public void testNegativeOrZero(@NegativeOrZero int value) {}

    /**
     * 测试 Digits 约束注解。
     *
     * @param value 表示输入的 {@code BigDecimal}。
     */
    public void testDigits(@Digits(integer = 3, fraction = 2) BigDecimal value) {}

    /**
     * 测试 Past 约束注解。
     *
     * @param value 表示输入的 {@code LocalDate}。
     */
    public void testPast(@Past LocalDate value) {}

    /**
     * 测试 PastOrPresent 约束注解。
     *
     * @param value 表示输入的 {@code LocalDate}。
     */
    public void testPastOrPresent(@PastOrPresent LocalDate value) {}

    /**
     * 测试 Future 约束注解。
     *
     * @param value 表示输入的 {@code LocalDate}。
     */
    public void testFuture(@Future LocalDate value) {}

    /**
     * 测试 FutureOrPresent 约束注解。
     *
     * @param value 表示输入的 {@code LocalDate}。
     */
    public void testFutureOrPresent(@FutureOrPresent LocalDate value) {}

    /**
     * 测试 Pattern 约束注解。
     *
     * @param value 表示输入的 {@code String}。
     */
    public void testPattern(@Pattern(regexp = "^[a-zA-Z]+$") String value) {}

    /**
     * 测试 Email 约束注解。
     *
     * @param value 表示输入的 {@code String}。
     */
    public void testEmail(@Email String value) {}

    /**
     * 测试 AssertTrue 约束注解。
     *
     * @param value 表示输入的 {@code boolean}。
     */
    public void testAssertTrue(@AssertTrue boolean value) {}

    /**
     * 测试 AssertFalse 约束注解。
     *
     * @param value 表示输入的 {@code boolean}。
     */
    public void testAssertFalse(@AssertFalse boolean value) {}

    /**
     * 测试 Valid 对象验证。
     *
     * @param data 表示输入的 {@link ValidationTestData}。
     */
    public void testValidObject(@Valid ValidationTestData data) {}

    /**
     * 测试 Range 约束注解。
     *
     * @param value 表示输入的 {@code int}。
     */
    public void testRange(@Range(min = 10, max = 100, message = "需要在10和100之间") int value) {}

    /**
     * 测试 BigDecimal 类型的Range约束注解。
     *
     * @param value 表示输入的 {@code BigDecimal}。
     */
    public void testRangeBigDecimal(@Range(min = 10, max = 100, message = "需要在10和100之间") BigDecimal value) {}

    /**
     * 验证 Employee 对象。
     *
     * @param employee 表示输入的 {@link Employee}。
     */
    public void validateEmployee(@Valid Employee employee) {
        LOG.debug("Validating employee: {}", employee);
    }

    /**
     * 验证年龄是否为正数。
     *
     * @param age 表示输入的 {@code int}。
     */
    public void validateAge(@Positive(message = "必须是正数") int age) {
        LOG.debug("Validating age: {}", age);
    }

    /**
     * 验证姓名和年龄。
     *
     * @param name 表示输入的 {@code String}。
     * @param age 表示输入的 {@code int}。
     */
    public void validateNameAndAge(@NotBlank String name, @Positive int age) {
        LOG.debug("Validating name: {} and age: {}", name, age);
    }

    /**
     * 验证高级分组数据。
     *
     * @param data 表示输入的 {@link ValidationTestData}。
     */
    public void validateAdvancedGroup(ValidationTestData data) {
        if (advancedValidateService != null) {
            advancedValidateService.validateAdvancedGroup(data);
        }
    }

    /**
     * 验证学生年龄。
     *
     * @param age 表示输入的 {@code int}。
     */
    public void validateStudentAge(int age) {
        if (studentValidateService != null) {
            studentValidateService.validateStudentAge(age);
        }
    }

    /**
     * 验证教师年龄。
     *
     * @param age 表示输入的 {@code int}。
     */
    public void validateTeacherAge(int age) {
        if (teacherValidateService != null) {
            teacherValidateService.validateTeacherAge(age);
        }
    }

    /**
     * 验证公司对象。
     *
     * @param company 表示输入的 {@link Company}。
     */
    public void validateCompany(@Valid Company company) {
        LOG.debug("Validating company: {}", company);
    }

    /**
     * 验证员工列表。
     *
     * @param employees 表示输入的 {@link List<Employee>}。
     */
    public void validateEmployeeList(List<@Valid Employee> employees) {
        LOG.debug("Validating employee list: {}", employees);
    }

    /**
     * 验证嵌套员工列表。
     *
     * @param nestedList 表示输入的 {@link List<List<Employee>>}。
     */
    public void validateNestedEmployeeList(List<List<@Valid Employee>> nestedList) {
        LOG.debug("Validating nested employee list: {}", nestedList);
    }

    /**
     * 验证员工映射。
     *
     * @param employeeMap 表示输入的 {@link Map<String, Employee>}。
     */
    public void validateEmployeeMap(@Valid Map<String, Employee> employeeMap) {
        LOG.debug("Validating employee map: {}", employeeMap);
    }

    /**
     * 验证员工数据映射。
     *
     * @param map 表示输入的 {@link Map<Employee, ValidationTestData>}。
     */
    public void validateEmployeeDataMap(@Valid Map<Employee, ValidationTestData> map) {
        LOG.debug("Validating employee data map: {}", map);
    }

    /**
     * 验证嵌套员工数据映射。
     *
     * @param nestedMap 表示输入的 {@link Map<Employee, Map<String, ValidationTestData>>}。
     */
    public void validateNestedEmployeeDataMap(Map<@Valid Employee, Map<String, @Valid ValidationTestData>> nestedMap) {
        LOG.debug("Validating nested employee data map: {}", nestedMap);
    }

    /**
     * 验证员工映射列表。
     *
     * @param listOfMaps 表示输入的 {@link List<Map<String, Employee>>}。
     */
    public void validateEmployeeMapList(List<Map<String, @Valid Employee>> listOfMaps) {
        LOG.debug("Validating employee map list: {}", listOfMaps);
    }

    /**
     * 验证员工数据列表映射。
     *
     * @param map 表示输入的 {@link Map<Employee, List<ValidationTestData>>}。
     */
    public void validateEmployeeDataListMap(Map<@Valid Employee, List<@Valid ValidationTestData>> map) {
        LOG.debug("Validating employee data list map: {}", map);
    }

    /**
     * 验证公司列表。
     *
     * @param companies 表示输入的 {@link List<Company>}。
     */
    public void validateCompanyList(@Valid List<Company> companies) {
        LOG.debug("Validating company list: {}", companies);
    }

    /**
     * 验证混合类型数据。
     *
     * @param value 表示输入的 {@code int}。
     * @param employee 表示输入的 {@link Employee}。
     */
    public void validateMixed(@Positive int value, @Valid Employee employee) {
        LOG.debug("Validating mixed primitive {} and object {}", value, employee);
    }

    /**
     * 验证混合集合数据。
     *
     * @param list1 表示输入的 {@code List<String>}。
     * @param list2 表示输入的 {@code List<String>}。
     */
    public void validateMixedCollections(@NotNull List<String> list1, @NotEmpty List<String> list2) {
        LOG.debug("Validating mixed collections: {} and {}", list1, list2);
    }
}
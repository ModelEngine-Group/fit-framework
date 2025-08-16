/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fitframework.validation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.validation.ConstraintViolationException;
import modelengine.fitframework.annotation.Fit;
import modelengine.fitframework.test.annotation.FitTestWithJunit;
import modelengine.fitframework.validation.data.Company;
import modelengine.fitframework.validation.data.Employee;
import modelengine.fitframework.validation.data.GroupValidateService;
import modelengine.fitframework.validation.data.ValidateService;
import modelengine.fitframework.validation.data.ValidationTestData;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link ValidationHandler} 的单元测试。
 *
 * @author 阮睿
 * @since 2025-07-18
 */
@DisplayName("测试注解校验功能")
@FitTestWithJunit(includeClasses = {
        ValidateService.class, ValidationHandler.class, GroupValidateService.StudentValidateService.class,
        GroupValidateService.TeacherValidateService.class, GroupValidateService.AdvancedValidateService.class
})
public class ValidationHandlerTest {
    @Fit
    private ValidateService validateService;

    @Test
    @DisplayName("测试校验原始类型成功")
    void givePrimitiveThenValidateOk() {
        assertThatThrownBy(() -> this.validateService.foo0(-1)).isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("必须是正数");
    }

    @Test
    @DisplayName("测试校验结构体成功")
    void giveClassThenValidateOk() {
        assertThatThrownBy(() -> this.validateService.foo1(new Employee("sky", 17))).isInstanceOf(
                ConstraintViolationException.class).hasMessageContaining("年龄必须大于等于18");
    }

    @Test
    @DisplayName("测试嵌套结构体成功")
    void giveNestedClassThenValidateOk() {
        assertThatThrownBy(() -> {
            Employee employee = new Employee("sky", 17);
            this.validateService.foo2(new Company(Collections.singletonList(employee)));
        }).isInstanceOf(ConstraintViolationException.class).hasMessageContaining("年龄必须大于等于18");
    }

    @Test
    @DisplayName("测试@NotNull注解")
    void testNotNullValidation() {
        assertThatThrownBy(() -> this.validateService.testNotNull(null)).isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("不能为null");
    }

    @Test
    @DisplayName("测试@NotEmpty注解")
    void testNotEmptyValidation() {
        assertThatThrownBy(() -> this.validateService.testNotEmpty("")).isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("不能为空");
    }

    @Test
    @DisplayName("测试@NotBlank注解")
    void testNotBlankValidation() {
        assertThatThrownBy(() -> this.validateService.testNotBlank("   ")).isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("不能为空");
    }

    @Test
    @DisplayName("测试@Null注解")
    void testNullValidation() {
        assertThatThrownBy(() -> this.validateService.testNull("not null")).isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("必须为null");
    }

    @Test
    @DisplayName("测试@Size注解-字符串")
    void testSizeStringValidation() {
        assertThatThrownBy(() -> this.validateService.testSize("a")).isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("个数必须在2和10之间");
    }

    @Test
    @DisplayName("测试@Size注解-集合")
    void testSizeListValidation() {
        assertThatThrownBy(() -> this.validateService.testSizeList(Arrays.asList("1", "2", "3", "4"))).isInstanceOf(
                ConstraintViolationException.class).hasMessageContaining("个数必须在1和3之间");
    }

    @Test
    @DisplayName("测试@Min注解")
    void testMinValidation() {
        assertThatThrownBy(() -> this.validateService.testMin(5)).isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("最小不能小于10");
    }

    @Test
    @DisplayName("测试@Max注解")
    void testMaxValidation() {
        assertThatThrownBy(() -> this.validateService.testMax(150)).isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("最大不能超过100");
    }

    @Test
    @DisplayName("测试@DecimalMin注解")
    void testDecimalMinValidation() {
        assertThatThrownBy(() -> this.validateService.testDecimalMin(new BigDecimal("5.0"))).isInstanceOf(
                ConstraintViolationException.class).hasMessageContaining("必须大于");
    }

    @Test
    @DisplayName("测试@DecimalMax注解")
    void testDecimalMaxValidation() {
        assertThatThrownBy(() -> this.validateService.testDecimalMax(new BigDecimal("150.0"))).isInstanceOf(
                ConstraintViolationException.class).hasMessageContaining("必须小于");
    }

    @Test
    @DisplayName("测试@Positive注解")
    void testPositiveValidation() {
        assertThatThrownBy(() -> this.validateService.testPositive(0)).isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("必须是正数");
    }

    @Test
    @DisplayName("测试@PositiveOrZero注解")
    void testPositiveOrZeroValidation() {
        assertThatThrownBy(() -> this.validateService.testPositiveOrZero(-1)).isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("必须是正数或零");
    }

    @Test
    @DisplayName("测试@Negative注解")
    void testNegativeValidation() {
        assertThatThrownBy(() -> this.validateService.testNegative(1)).isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("必须是负数");
    }

    @Test
    @DisplayName("测试@NegativeOrZero注解")
    void testNegativeOrZeroValidation() {
        assertThatThrownBy(() -> this.validateService.testNegativeOrZero(1)).isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("必须是负数或零");
    }

    @Test
    @DisplayName("测试@Digits注解")
    void testDigitsValidation() {
        assertThatThrownBy(() -> this.validateService.testDigits(new BigDecimal("1234.567"))).isInstanceOf(
                ConstraintViolationException.class).hasMessageContaining("数字的值超出了允许范围");
    }

    @Test
    @DisplayName("测试@Past注解")
    void testPastValidation() {
        assertThatThrownBy(() -> this.validateService.testPast(LocalDate.now().plusDays(1))).isInstanceOf(
                ConstraintViolationException.class).hasMessageContaining("需要是一个过去的时间");
    }

    @Test
    @DisplayName("测试@PastOrPresent注解")
    void testPastOrPresentValidation() {
        assertThatThrownBy(() -> this.validateService.testPastOrPresent(LocalDate.now().plusDays(1))).isInstanceOf(
                ConstraintViolationException.class).hasMessageContaining("需要是一个过去或现在的时间");
    }

    @Test
    @DisplayName("测试@Future注解")
    void testFutureValidation() {
        assertThatThrownBy(() -> this.validateService.testFuture(LocalDate.now().minusDays(1))).isInstanceOf(
                ConstraintViolationException.class).hasMessageContaining("需要是一个将来的时间");
    }

    @Test
    @DisplayName("测试@FutureOrPresent注解")
    void testFutureOrPresentValidation() {
        assertThatThrownBy(() -> this.validateService.testFutureOrPresent(LocalDate.now().minusDays(1))).isInstanceOf(
                ConstraintViolationException.class).hasMessageContaining("需要是一个将来或现在的时间");
    }

    @Test
    @DisplayName("测试@Pattern注解")
    void testPatternValidation() {
        assertThatThrownBy(() -> this.validateService.testPattern("123")).isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("需要匹配正则表达式");
    }

    @Test
    @DisplayName("测试@Email注解")
    void testEmailValidation() {
        assertThatThrownBy(() -> this.validateService.testEmail("invalid-email")).isInstanceOf(
                ConstraintViolationException.class).hasMessageContaining("不是一个合法的电子邮件地址");
    }

    @Test
    @DisplayName("测试@AssertTrue注解")
    void testAssertTrueValidation() {
        assertThatThrownBy(() -> this.validateService.testAssertTrue(false)).isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("只能为true");
    }

    @Test
    @DisplayName("测试@AssertFalse注解")
    void testAssertFalseValidation() {
        assertThatThrownBy(() -> this.validateService.testAssertFalse(true)).isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("只能为false");
    }

    @Test
    @DisplayName("测试复杂对象校验")
    void testComplexObjectValidation() {
        ValidationTestData invalidData = new ValidationTestData();
        invalidData.setName(null); // 违反@NotNull
        invalidData.setAge(200); // 违反@Max(150)

        assertThatThrownBy(() -> this.validateService.testValidObject(invalidData)).isInstanceOf(
                ConstraintViolationException.class).hasMessageContaining("名称不能为空");
    }

    @Test
    @DisplayName("校验数据类，该数据类的 Constraint 字段会被校验，其余字段不会被校验")
    public void givenFieldsWithConstraintAnnotationThenValidateHappened() {
        Employee invalidEmployee = new Employee("", 150); // 空名字，年龄超限
        assertThatThrownBy(() -> validateService.validateEmployee(invalidEmployee)).isInstanceOf(
                ConstraintViolationException.class).hasMessageContaining("不能为空");
    }

    @Test
    @DisplayName("校验方法参数，该方法的 Constraint 参数会被校验，其余参数不会被校验")
    public void givenParametersWithConstraintAnnotationThenValidateHappened() {
        assertThatThrownBy(() -> validateService.validateAge(-1)).isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("必须是正数");
    }

    @Test
    @DisplayName("校验多个参数")
    public void givenMultipleParametersThenValidateHappened() {
        assertThatThrownBy(() -> validateService.validateNameAndAge("",
                -1)).isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    @DisplayName("校验数据类，只会校验与数据类的分组一致的字段分组")
    public void givenFieldsThenSameGroupValidateHappened() {
        ValidationTestData data = new ValidationTestData();
        data.setAge(300); // 违反高级分组的约束
        data.setName(""); // 违反默认分组约束

        assertThatThrownBy(() -> validateService.validateAdvancedGroup(data)).isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("高级组年龄必须小于等于200");
    }

    @Test
    @DisplayName("校验方法参数，分组约束测试")
    public void givenParametersThenGroupValidateHappened() {
        // 测试学生年龄验证 - 现在会抛出异常，因为使用了学生分组
        assertThatThrownBy(() -> validateService.validateStudentAge(25)).isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("范围要在7~20之内");
    }

    @Test
    @DisplayName("校验方法参数，教师年龄验证测试")
    public void givenParametersThenTeacherGroupValidateNotHappened() {
        // 测试教师年龄验证 - 现在会抛出异常，因为使用了教师分组
        assertThatThrownBy(() -> validateService.validateTeacherAge(15)).isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("范围要在22~65之内");
    }

    @Test
    @DisplayName("测试嵌套校验类 Company")
    void shouldReturnMsgWhenValidateCompany() {
        Employee invalidEmployee1 = new Employee("", 17); // 空名字，年龄不足
        Employee invalidEmployee2 = new Employee("John", 150); // 年龄超限
        Company company = new Company(Arrays.asList(invalidEmployee1, invalidEmployee2));

        assertThatThrownBy(() -> validateService.validateCompany(company)).isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    @DisplayName("测试 @Valid List<Employee>")
    void shouldReturnMsgWhenValidateList() {
        Employee validEmployee = new Employee("John", 25);
        Employee invalidEmployee1 = new Employee("", 17);
        Employee invalidEmployee2 = new Employee("Jane", 150);
        List<Employee> employees = Arrays.asList(validEmployee, invalidEmployee1, invalidEmployee2);

        assertThatThrownBy(() -> validateService.validateEmployeeList(employees)).isInstanceOf(
                ConstraintViolationException.class);
    }

    @Test
    @DisplayName("测试 @Valid List<List<Employee>>")
    void shouldReturnMsgWhenValidateListInList() {
        Employee validEmployee1 = new Employee("John", 25);
        Employee validEmployee2 = new Employee("John", 25);
        Employee invalidEmployee1 = new Employee("", 17);
        Employee invalidEmployee2 = new Employee("Jane", 150);

        List<Employee> employeeList1 = Arrays.asList(validEmployee1, invalidEmployee1);
        List<Employee> employeeList2 = Arrays.asList(validEmployee2, invalidEmployee2);
        List<List<Employee>> nestedList = Arrays.asList(employeeList1, employeeList2);

        assertThatThrownBy(() -> validateService.validateNestedEmployeeList(nestedList)).isInstanceOf(
                ConstraintViolationException.class);
    }

    @Test
    @DisplayName("测试 @Valid Map<String, Employee>")
    void shouldReturnMsgWhenValidateMapSimple() {
        Employee validEmployee = new Employee("John", 25);
        Employee invalidEmployee1 = new Employee("", 17);
        Employee invalidEmployee2 = new Employee("Jane", 150);

        Map<String, Employee> employeeMap = new HashMap<>();
        employeeMap.put("1", validEmployee);
        employeeMap.put("2", invalidEmployee1);
        employeeMap.put("3", invalidEmployee2);

        assertThatThrownBy(() -> validateService.validateEmployeeMap(employeeMap)).isInstanceOf(
                ConstraintViolationException.class);
    }

    @Test
    @DisplayName("测试 @Valid Map<Employee, ValidationTestData>")
    void shouldReturnMsgWhenValidateMapObj() {
        Employee validEmployee = new Employee("John", 25);
        Employee invalidEmployee = new Employee("", 17);

        ValidationTestData validData = new ValidationTestData();
        validData.setName("Test");
        validData.setAge(25);

        ValidationTestData invalidData = new ValidationTestData();
        invalidData.setName("");
        invalidData.setAge(-1);

        Map<Employee, ValidationTestData> map = new HashMap<>();
        map.put(validEmployee, validData);
        map.put(invalidEmployee, invalidData);

        assertThatThrownBy(() -> validateService.validateEmployeeDataMap(map)).isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    @DisplayName("测试 @Valid Map<Employee, Map<String, ValidationTestData>>")
    void shouldReturnMsgWhenValidateMapInMap() {
        Employee validEmployee = new Employee("John", 25);
        Employee invalidEmployee = new Employee("", 17);

        ValidationTestData validData = new ValidationTestData();
        validData.setName("Test");
        validData.setAge(25);

        ValidationTestData invalidData = new ValidationTestData();
        invalidData.setName("");
        invalidData.setAge(-1);

        Map<String, ValidationTestData> innerMap = new HashMap<>();
        innerMap.put("valid", validData);
        innerMap.put("invalid", invalidData);

        Map<Employee, Map<String, ValidationTestData>> nestedMap = new HashMap<>();
        nestedMap.put(validEmployee, innerMap);
        nestedMap.put(invalidEmployee, innerMap);

        assertThatThrownBy(() -> validateService.validateNestedEmployeeDataMap(nestedMap)).isInstanceOf(
                ConstraintViolationException.class);
    }

    @Test
    @DisplayName("测试 @Valid List<Map<String, Employee>>")
    void shouldReturnMsgWhenValidateMapInList() {
        Employee validEmployee = new Employee("John", 25);
        Employee invalidEmployee1 = new Employee("", 17);
        Employee invalidEmployee2 = new Employee("Jane", 150);

        Map<String, Employee> map1 = new HashMap<>();
        map1.put("valid", validEmployee);
        map1.put("invalid1", invalidEmployee1);

        Map<String, Employee> map2 = new HashMap<>();
        map2.put("valid", validEmployee);
        map2.put("invalid2", invalidEmployee2);

        List<Map<String, Employee>> listOfMaps = Arrays.asList(map1, map2);

        assertThatThrownBy(() -> validateService.validateEmployeeMapList(listOfMaps)).isInstanceOf(
                ConstraintViolationException.class);
    }

    @Test
    @DisplayName("测试 @Valid Map<Employee, List<ValidationTestData>>")
    void shouldReturnMsgWhenValidateListInMap() {
        Employee validEmployee = new Employee("John", 25);
        Employee invalidEmployee = new Employee("", 17);

        ValidationTestData validData = new ValidationTestData();
        validData.setName("Test");
        validData.setAge(25);

        ValidationTestData invalidData = new ValidationTestData();
        invalidData.setName("");
        invalidData.setAge(-1);

        List<ValidationTestData> dataList1 = Arrays.asList(validData, invalidData);
        List<ValidationTestData> dataList2 = Arrays.asList(validData);

        Map<Employee, List<ValidationTestData>> map = new HashMap<>();
        map.put(validEmployee, dataList1);
        map.put(invalidEmployee, dataList2);

        assertThatThrownBy(() -> validateService.validateEmployeeDataListMap(map)).isInstanceOf(
                ConstraintViolationException.class);
    }

    @Test
    @DisplayName("测试 @Valid List<Company>")
    void shouldReturnMsgWhenValidateListComplex() {
        Employee invalidEmployee = new Employee("", 17);
        Company invalidCompany = new Company(Arrays.asList(invalidEmployee));
        List<Company> companies = Arrays.asList(invalidCompany);

        assertThatThrownBy(() -> validateService.validateCompanyList(companies)).isInstanceOf(
                ConstraintViolationException.class);
    }

    @Test
    @DisplayName("测试@NotNull注解-成功场景")
    void testNotNullValidationSuccess() {
        validateService.testNotNull("valid value");
    }

    @Test
    @DisplayName("测试@Size注解-最小边界值")
    void testSizeStringValidationMinBoundary() {
        validateService.testSize("ab"); // 2个字符，最小边界
    }

    @Test
    @DisplayName("测试@Size注解-最大边界值")
    void testSizeStringValidationMaxBoundary() {
        validateService.testSize("abcdefghij"); // 10个字符，最大边界
    }

    @Test
    @DisplayName("测试@Min注解-边界值")
    void testMinValidationBoundary() {
        validateService.testMin(10); // 最小值边界
    }

    @Test
    @DisplayName("测试@Max注解-边界值")
    void testMaxValidationBoundary() {
        validateService.testMax(100); // 最大值边界
    }

    @Test
    @DisplayName("测试@Positive注解-边界值")
    void testPositiveValidationBoundary() {
        validateService.testPositive(1); // 最小正数
    }

    @Test
    @DisplayName("测试@PositiveOrZero注解-零值")
    void testPositiveOrZeroValidationZero() {
        validateService.testPositiveOrZero(0); // 零值
    }

    @Test
    @DisplayName("测试@Past注解-边界值")
    void testPastValidationBoundary() {
        validateService.testPast(LocalDate.now().minusDays(1)); // 昨天
    }

    @Test
    @DisplayName("测试@Future注解-边界值")
    void testFutureValidationBoundary() {
        validateService.testFuture(LocalDate.now().plusDays(1)); // 明天
    }

    @Test
    @DisplayName("测试有效的复杂对象")
    void testValidComplexObject() {
        ValidationTestData validData = new ValidationTestData();
        validData.setName("Test Name");
        validData.setAge(25);
        validData.setDescription("Test Description");
        validData.setContent("Test Content");
        validData.setQuantity(10);
        validData.setDiscount(new BigDecimal("-5.0"));
        validData.setAgreed(true);

        validateService.testValidObject(validData);
    }

    @Test
    @DisplayName("测试多个验证失败")
    void testMultipleValidationFailures() {
        ValidationTestData invalidData = new ValidationTestData();
        invalidData.setName(""); // 违反@NotBlank
        invalidData.setAge(-1); // 违反@Min(0)
        invalidData.setQuantity(-10); // 违反@Positive

        assertThatThrownBy(() -> validateService.testValidObject(invalidData)).isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    @DisplayName("测试混合原始类型和对象校验")
    void testMixedPrimitiveAndObjectValidation() {
        Employee invalidEmployee = new Employee("", 17);
        assertThatThrownBy(() -> validateService.validateMixed(-1, invalidEmployee)).isInstanceOf(
                ConstraintViolationException.class);
    }

    @Test
    @DisplayName("测试空集合和null值混合")
    void testEmptyCollectionAndNullMixed() {
        assertThatThrownBy(() -> validateService.validateMixedCollections(null, Collections.emptyList())).isInstanceOf(
                ConstraintViolationException.class);
    }

    @Test
    @DisplayName("测试@Range注解-最小值验证")
    void testRangeMinValidation() {
        assertThatThrownBy(() -> this.validateService.testRange(5)).isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("需要在10和100之间");
    }

    @Test
    @DisplayName("测试@Range注解-最大值验证")
    void testRangeMaxValidation() {
        assertThatThrownBy(() -> this.validateService.testRange(150)).isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("需要在10和100之间");
    }

    @Test
    @DisplayName("测试@Range注解-最小边界值")
    void testRangeMinBoundary() {
        validateService.testRange(10); // 最小边界值，应该通过
    }

    @Test
    @DisplayName("测试@Range注解-最大边界值")
    void testRangeMaxBoundary() {
        validateService.testRange(100); // 最大边界值，应该通过
    }

    @Test
    @DisplayName("测试@Range注解-中间值")
    void testRangeValidValue() {
        validateService.testRange(50); // 中间值，应该通过
    }

    @Test
    @DisplayName("测试@Range注解-BigDecimal类型")
    void testRangeBigDecimalValidation() {
        assertThatThrownBy(() -> this.validateService.testRangeBigDecimal(new BigDecimal("5.5"))).isInstanceOf(
                ConstraintViolationException.class).hasMessageContaining("需要在10和100之间");
    }

}
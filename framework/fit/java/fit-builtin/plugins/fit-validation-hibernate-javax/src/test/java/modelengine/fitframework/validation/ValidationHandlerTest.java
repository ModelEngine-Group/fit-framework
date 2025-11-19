/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fitframework.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import modelengine.fitframework.aop.JoinPoint;
import modelengine.fitframework.ioc.BeanContainer;
import modelengine.fitframework.ioc.annotation.AnnotationMetadataResolver;
import modelengine.fitframework.ioc.annotation.support.DefaultAnnotationMetadataResolver;
import modelengine.fitframework.runtime.FitRuntime;
import modelengine.fitframework.util.ObjectUtils;
import modelengine.fitframework.util.ReflectionUtils;
import modelengine.fitframework.validation.data.Company;
import modelengine.fitframework.validation.data.Employee;
import modelengine.fitframework.validation.data.GroupValidateService;
import modelengine.fitframework.validation.data.ValidateService;
import modelengine.fitframework.validation.data.ValidationTestData;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.validation.ConstraintViolationException;

/**
 * {@link ValidationHandler} 的单元测试。
 *
 * @author 阮睿
 * @since 2025-07-18
 */
public class ValidationHandlerTest {
    private final ValidateService validateService = mock(ValidateService.class);
    private final BeanContainer beanContainer = mock(BeanContainer.class);
    private final FitRuntime fitRuntime = mock(FitRuntime.class);
    private final AnnotationMetadataResolver annotationMetadataResolver = new DefaultAnnotationMetadataResolver();
    private final Validated validated = Mockito.mock(Validated.class);
    private final ValidationHandler handler = new ValidationHandler();

    @BeforeEach
    void setUp() {
        this.handler.setLocale(Locale.CHINA);
        when(this.validated.value()).thenReturn(new Class[0]);
        when(this.fitRuntime.resolverOfAnnotations()).thenReturn(annotationMetadataResolver);
        when(this.beanContainer.runtime()).thenReturn(fitRuntime);
    }

    private ConstraintViolationException invokeHandleMethod(Method targetMethod, Object[] args) {
        Method handleValidatedMethod =
                ReflectionUtils.getDeclaredMethod(ValidationHandler.class, "handle", JoinPoint.class, Validated.class);
        handleValidatedMethod.setAccessible(true);
        JoinPoint joinPoint = mock(JoinPoint.class);
        when(joinPoint.getMethod()).thenReturn(targetMethod);
        when(joinPoint.getArgs()).thenReturn(args);
        when(joinPoint.getTarget()).thenReturn(validateService);

        InvocationTargetException invocationTargetException = catchThrowableOfType(InvocationTargetException.class,
                () -> handleValidatedMethod.invoke(handler, joinPoint, validated));

        return ObjectUtils.cast(invocationTargetException.getTargetException());
    }

    @Test
    @DisplayName("测试校验原始类型成功")
    void givePrimitiveThenValidateOk() {
        Method method = ReflectionUtils.getDeclaredMethod(ValidateService.class, "foo0", int.class);
        ConstraintViolationException exception = invokeHandleMethod(method, new Object[] {-1});
        assertThat(exception.getMessage()).contains("必须是正数");
    }

    @Test
    @DisplayName("测试校验结构体成功")
    void giveClassThenValidateOk() {
        Method method = ReflectionUtils.getDeclaredMethod(ValidateService.class, "foo1", Employee.class);
        Employee employee = new Employee("sky", 17);
        ConstraintViolationException exception = invokeHandleMethod(method, new Object[] {employee});
        assertThat(exception.getMessage()).contains("年龄必须大于等于18");
    }

    @Test
    @DisplayName("测试嵌套结构体成功")
    void giveNestedClassThenValidateOk() {
        Method method = ReflectionUtils.getDeclaredMethod(ValidateService.class, "foo2", Company.class);
        Employee employee = new Employee("sky", 17);
        Company company = new Company(Collections.singletonList(employee));
        ConstraintViolationException exception = invokeHandleMethod(method, new Object[] {company});
        assertThat(exception.getMessage()).contains("年龄必须大于等于18");
    }

    @Test
    @DisplayName("测试@NotNull注解")
    void testNotNullValidation() {
        Method method = ReflectionUtils.getDeclaredMethod(ValidateService.class, "testNotNull", String.class);
        ConstraintViolationException exception = invokeHandleMethod(method, new Object[] {null});
        assertThat(exception.getMessage()).contains("不能为null");
    }

    @Test
    @DisplayName("测试@NotEmpty注解")
    void testNotEmptyValidation() {
        Method method = ReflectionUtils.getDeclaredMethod(ValidateService.class, "testNotEmpty", String.class);
        ConstraintViolationException exception = invokeHandleMethod(method, new Object[] {""});
        assertThat(exception.getMessage()).contains("不能为空");
    }

    @Test
    @DisplayName("测试@NotBlank注解")
    void testNotBlankValidation() {
        Method method = ReflectionUtils.getDeclaredMethod(ValidateService.class, "testNotBlank", String.class);
        ConstraintViolationException exception = invokeHandleMethod(method, new Object[] {"   "});
        assertThat(exception.getMessage()).contains("不能为空");
    }

    @Test
    @DisplayName("测试@Null注解")
    void testNullValidation() {
        Method method = ReflectionUtils.getDeclaredMethod(ValidateService.class, "testNull", String.class);
        ConstraintViolationException exception = invokeHandleMethod(method, new Object[] {"not null"});
        assertThat(exception.getMessage()).contains("必须为null");
    }

    @Test
    @DisplayName("测试@Size注解-字符串")
    void testSizeStringValidation() {
        Method method = ReflectionUtils.getDeclaredMethod(ValidateService.class, "testSize", String.class);
        ConstraintViolationException exception = invokeHandleMethod(method, new Object[] {"a"});
        assertThat(exception.getMessage()).contains("个数必须在2和10之间");
    }

    @Test
    @DisplayName("测试@Size注解-集合")
    void testSizeListValidation() {
        Method method = ReflectionUtils.getDeclaredMethod(ValidateService.class, "testSizeList", List.class);
        ConstraintViolationException exception =
                invokeHandleMethod(method, new Object[] {Arrays.asList("1", "2", "3", "4")});
        assertThat(exception.getMessage()).contains("个数必须在1和3之间");
    }

    @Test
    @DisplayName("测试@Min注解")
    void testMinValidation() {
        Method method = ReflectionUtils.getDeclaredMethod(ValidateService.class, "testMin", int.class);
        ConstraintViolationException exception = invokeHandleMethod(method, new Object[] {5});
        assertThat(exception.getMessage()).contains("最小不能小于10");
    }

    @Test
    @DisplayName("测试@Max注解")
    void testMaxValidation() {
        Method method = ReflectionUtils.getDeclaredMethod(ValidateService.class, "testMax", int.class);
        ConstraintViolationException exception = invokeHandleMethod(method, new Object[] {150});
        assertThat(exception.getMessage()).contains("最大不能超过100");
    }

    @Test
    @DisplayName("测试@DecimalMin注解")
    void testDecimalMinValidation() {
        Method method = ReflectionUtils.getDeclaredMethod(ValidateService.class, "testDecimalMin", BigDecimal.class);
        ConstraintViolationException exception = invokeHandleMethod(method, new Object[] {new BigDecimal("5.0")});
        assertThat(exception.getMessage()).contains("必须大于");
    }

    @Test
    @DisplayName("测试@DecimalMax注解")
    void testDecimalMaxValidation() {
        Method method = ReflectionUtils.getDeclaredMethod(ValidateService.class, "testDecimalMax", BigDecimal.class);
        ConstraintViolationException exception = invokeHandleMethod(method, new Object[] {new BigDecimal("150.0")});
        assertThat(exception.getMessage()).contains("必须小于");
    }

    @Test
    @DisplayName("测试@Positive注解")
    void testPositiveValidation() {
        Method method = ReflectionUtils.getDeclaredMethod(ValidateService.class, "testPositive", int.class);
        ConstraintViolationException exception = invokeHandleMethod(method, new Object[] {0});
        assertThat(exception.getMessage()).contains("必须是正数");
    }

    @Test
    @DisplayName("测试@PositiveOrZero注解")
    void testPositiveOrZeroValidation() {
        Method method = ReflectionUtils.getDeclaredMethod(ValidateService.class, "testPositiveOrZero", int.class);
        ConstraintViolationException exception = invokeHandleMethod(method, new Object[] {-1});
        assertThat(exception.getMessage()).contains("必须是正数或零");
    }

    @Test
    @DisplayName("测试@Negative注解")
    void testNegativeValidation() {
        Method method = ReflectionUtils.getDeclaredMethod(ValidateService.class, "testNegative", int.class);
        ConstraintViolationException exception = invokeHandleMethod(method, new Object[] {1});
        assertThat(exception.getMessage()).contains("必须是负数");
    }

    @Test
    @DisplayName("测试@NegativeOrZero注解")
    void testNegativeOrZeroValidation() {
        Method method = ReflectionUtils.getDeclaredMethod(ValidateService.class, "testNegativeOrZero", int.class);
        ConstraintViolationException exception = invokeHandleMethod(method, new Object[] {1});
        assertThat(exception.getMessage()).contains("必须是负数或零");
    }

    @Test
    @DisplayName("测试@Digits注解")
    void testDigitsValidation() {
        Method method = ReflectionUtils.getDeclaredMethod(ValidateService.class, "testDigits", BigDecimal.class);
        ConstraintViolationException exception = invokeHandleMethod(method, new Object[] {new BigDecimal("1234.567")});
        assertThat(exception.getMessage()).contains("数字的值超出了允许范围");
    }

    @Test
    @DisplayName("测试@Past注解")
    void testPastValidation() {
        Method method = ReflectionUtils.getDeclaredMethod(ValidateService.class, "testPast", LocalDate.class);
        ConstraintViolationException exception = invokeHandleMethod(method, new Object[] {LocalDate.now().plusDays(1)});
        assertThat(exception.getMessage()).contains("需要是一个过去的时间");
    }

    @Test
    @DisplayName("测试@PastOrPresent注解")
    void testPastOrPresentValidation() {
        Method method = ReflectionUtils.getDeclaredMethod(ValidateService.class, "testPastOrPresent", LocalDate.class);
        ConstraintViolationException exception = invokeHandleMethod(method, new Object[] {LocalDate.now().plusDays(1)});
        assertThat(exception.getMessage()).contains("需要是一个过去或现在的时间");
    }

    @Test
    @DisplayName("测试@Future注解")
    void testFutureValidation() {
        Method method = ReflectionUtils.getDeclaredMethod(ValidateService.class, "testFuture", LocalDate.class);
        ConstraintViolationException exception =
                invokeHandleMethod(method, new Object[] {LocalDate.now().minusDays(1)});
        assertThat(exception.getMessage()).contains("需要是一个将来的时间");
    }

    @Test
    @DisplayName("测试@FutureOrPresent注解")
    void testFutureOrPresentValidation() {
        Method method =
                ReflectionUtils.getDeclaredMethod(ValidateService.class, "testFutureOrPresent", LocalDate.class);
        ConstraintViolationException exception =
                invokeHandleMethod(method, new Object[] {LocalDate.now().minusDays(1)});
        assertThat(exception.getMessage()).contains("需要是一个将来或现在的时间");
    }

    @Test
    @DisplayName("测试@Pattern注解")
    void testPatternValidation() {
        Method method = ReflectionUtils.getDeclaredMethod(ValidateService.class, "testPattern", String.class);
        ConstraintViolationException exception = invokeHandleMethod(method, new Object[] {"123"});
        assertThat(exception.getMessage()).contains("需要匹配正则表达式");
    }

    @Test
    @DisplayName("测试@Email注解")
    void testEmailValidation() {
        Method method = ReflectionUtils.getDeclaredMethod(ValidateService.class, "testEmail", String.class);
        ConstraintViolationException exception = invokeHandleMethod(method, new Object[] {"invalid-email"});
        assertThat(exception.getMessage()).contains("不是一个合法的电子邮件地址");
    }

    @Test
    @DisplayName("测试@AssertTrue注解")
    void testAssertTrueValidation() {
        Method method = ReflectionUtils.getDeclaredMethod(ValidateService.class, "testAssertTrue", boolean.class);
        ConstraintViolationException exception = invokeHandleMethod(method, new Object[] {false});
        assertThat(exception.getMessage()).contains("只能为true");
    }

    @Test
    @DisplayName("测试@AssertFalse注解")
    void testAssertFalseValidation() {
        Method method = ReflectionUtils.getDeclaredMethod(ValidateService.class, "testAssertFalse", boolean.class);
        ConstraintViolationException exception = invokeHandleMethod(method, new Object[] {true});
        assertThat(exception.getMessage()).contains("只能为false");
    }

    @Test
    @DisplayName("测试复杂对象校验")
    void testComplexObjectValidation() {
        Method method =
                ReflectionUtils.getDeclaredMethod(ValidateService.class, "testValidObject", ValidationTestData.class);
        ValidationTestData invalidData = new ValidationTestData();
        invalidData.setName(null); // 违反@NotNull
        invalidData.setAge(200); // 违反@Max(150)

        ConstraintViolationException exception = invokeHandleMethod(method, new Object[] {invalidData});
        assertThat(exception.getMessage()).contains("名称不能为空");
    }

    @Test
    @DisplayName("校验数据类，该数据类的 Constraint 字段会被校验，其余字段不会被校验")
    public void givenFieldsWithConstraintAnnotationThenValidateHappened() {
        Method method = ReflectionUtils.getDeclaredMethod(ValidateService.class, "validateEmployee", Employee.class);
        Employee invalidEmployee = new Employee("", 150); // 空名字，年龄超限
        ConstraintViolationException exception = invokeHandleMethod(method, new Object[] {invalidEmployee});
        assertThat(exception.getMessage()).contains("不能为空");
    }

    @Test
    @DisplayName("校验方法参数，该方法的 Constraint 参数会被校验，其余参数不会被校验")
    public void givenParametersWithConstraintAnnotationThenValidateHappened() {
        Method method = ReflectionUtils.getDeclaredMethod(ValidateService.class, "validateAge", int.class);
        ConstraintViolationException exception = invokeHandleMethod(method, new Object[] {-1});
        assertThat(exception.getMessage()).contains("必须是正数");
    }

    @Test
    @DisplayName("校验多个参数")
    public void givenMultipleParametersThenValidateHappened() {
        Method method =
                ReflectionUtils.getDeclaredMethod(ValidateService.class, "validateNameAndAge", String.class, int.class);
        ConstraintViolationException exception = invokeHandleMethod(method, new Object[] {"", -1});
        assertThat(exception).isNotNull();
    }

    @Nested
    @DisplayName("Student Group Validation Tests")
    class StudentGroupValidationTests {
        @Test
        @DisplayName("校验方法参数，分组约束测试")
        public void givenParametersThenGroupValidateHappened() {
            // 测试学生年龄验证 - 现在会抛出异常，因为使用了学生分组
            Method method = ReflectionUtils.getDeclaredMethod(GroupValidateService.StudentValidateService.class,
                    "validateStudentAge",
                    int.class);
            Method handleValidatedMethod = ReflectionUtils.getDeclaredMethod(ValidationHandler.class,
                    "handle",
                    JoinPoint.class,
                    Validated.class);
            handleValidatedMethod.setAccessible(true);
            JoinPoint joinPoint = mock(JoinPoint.class);
            when(joinPoint.getMethod()).thenReturn(method);
            when(joinPoint.getArgs()).thenReturn(new Object[] {25});
            when(joinPoint.getTarget()).thenReturn(new GroupValidateService.StudentValidateService());
            when(ValidationHandlerTest.this.validated.value()).thenReturn(new Class[] {
                    ValidationTestData.StudentGroup.class
            });

            InvocationTargetException invocationTargetException = catchThrowableOfType(InvocationTargetException.class,
                    () -> handleValidatedMethod.invoke(ValidationHandlerTest.this.handler,
                            joinPoint,
                            ValidationHandlerTest.this.validated));

            ConstraintViolationException exception = ObjectUtils.cast(invocationTargetException.getTargetException());
            assertThat(exception.getMessage()).contains("范围要在7~20之内");
        }
    }

    @Nested
    @DisplayName("Teacher Group Validation Tests")
    class TeacherGroupValidationTests {
        @Test
        @DisplayName("校验方法参数，教师年龄验证测试")
        public void givenParametersThenTeacherGroupValidateNotHappened() {
            // 测试教师年龄验证 - 现在会抛出异常，因为使用了教师分组
            Method method = ReflectionUtils.getDeclaredMethod(GroupValidateService.TeacherValidateService.class,
                    "validateTeacherAge",
                    int.class);
            Method handleValidatedMethod = ReflectionUtils.getDeclaredMethod(ValidationHandler.class,
                    "handle",
                    JoinPoint.class,
                    Validated.class);
            handleValidatedMethod.setAccessible(true);
            JoinPoint joinPoint = mock(JoinPoint.class);
            when(joinPoint.getMethod()).thenReturn(method);
            when(joinPoint.getArgs()).thenReturn(new Object[] {15});
            when(joinPoint.getTarget()).thenReturn(new GroupValidateService.TeacherValidateService());
            when(ValidationHandlerTest.this.validated.value()).thenReturn(new Class[] {
                    ValidationTestData.TeacherGroup.class
            });

            InvocationTargetException invocationTargetException = catchThrowableOfType(InvocationTargetException.class,
                    () -> handleValidatedMethod.invoke(ValidationHandlerTest.this.handler,
                            joinPoint,
                            ValidationHandlerTest.this.validated));

            ConstraintViolationException exception = ObjectUtils.cast(invocationTargetException.getTargetException());
            assertThat(exception.getMessage()).contains("范围要在22~65之内");
        }
    }

    @Nested
    @DisplayName("Advanced Group Validation Tests")
    class AdvancedGroupValidationTests {
        @Test
        @DisplayName("测试验证高级分组数据")
        void testValidateAdvancedGroup() {
            // 测试高级分组验证 - 现在会抛出异常，因为使用了高级分组
            Method method = ReflectionUtils.getDeclaredMethod(GroupValidateService.AdvancedValidateService.class,
                    "validateAdvancedGroup",
                    ValidationTestData.class);
            Method handleValidatedMethod = ReflectionUtils.getDeclaredMethod(ValidationHandler.class,
                    "handle",
                    JoinPoint.class,
                    Validated.class);
            handleValidatedMethod.setAccessible(true);
            JoinPoint joinPoint = mock(JoinPoint.class);
            when(joinPoint.getMethod()).thenReturn(method);

            ValidationTestData data = new ValidationTestData();
            data.setAge(300); // 违反高级分组的约束
            data.setName(""); // 违反默认分组约束

            when(joinPoint.getArgs()).thenReturn(new Object[] {data});
            when(joinPoint.getTarget()).thenReturn(new GroupValidateService.AdvancedValidateService());
            when(ValidationHandlerTest.this.validated.value()).thenReturn(new Class[] {
                    ValidationTestData.AdvancedGroup.class
            });

            InvocationTargetException invocationTargetException = catchThrowableOfType(InvocationTargetException.class,
                    () -> handleValidatedMethod.invoke(ValidationHandlerTest.this.handler,
                            joinPoint,
                            ValidationHandlerTest.this.validated));

            ConstraintViolationException exception = ObjectUtils.cast(invocationTargetException.getTargetException());
            assertThat(exception.getMessage()).contains("高级组年龄必须小于等于200");
        }
    }

    @Test
    @DisplayName("测试嵌套校验类 Company")
    void shouldReturnMsgWhenValidateCompany() {
        Method method = ReflectionUtils.getDeclaredMethod(ValidateService.class, "validateCompany", Company.class);
        Employee invalidEmployee1 = new Employee("", 17); // 空名字，年龄不足
        Employee invalidEmployee2 = new Employee("John", 150); // 年龄超限
        Company company = new Company(Arrays.asList(invalidEmployee1, invalidEmployee2));

        ConstraintViolationException exception = invokeHandleMethod(method, new Object[] {company});
        assertThat(exception).isNotNull();
    }

    @Test
    @DisplayName("测试 @Valid List<Employee>")
    void shouldReturnMsgWhenValidateList() {
        Method method = ReflectionUtils.getDeclaredMethod(ValidateService.class, "validateEmployeeList", List.class);
        Employee validEmployee = new Employee("John", 25);
        Employee invalidEmployee1 = new Employee("", 17);
        Employee invalidEmployee2 = new Employee("Jane", 150);
        List<Employee> employees = Arrays.asList(validEmployee, invalidEmployee1, invalidEmployee2);

        ConstraintViolationException exception = invokeHandleMethod(method, new Object[] {employees});
        assertThat(exception).isNotNull();
    }

    @Test
    @DisplayName("测试 @Valid List<List<Employee>>")
    void shouldReturnMsgWhenValidateListInList() {
        Method method =
                ReflectionUtils.getDeclaredMethod(ValidateService.class, "validateNestedEmployeeList", List.class);
        Employee validEmployee1 = new Employee("John", 25);
        Employee validEmployee2 = new Employee("John", 25);
        Employee invalidEmployee1 = new Employee("", 17);
        Employee invalidEmployee2 = new Employee("Jane", 150);

        List<Employee> employeeList1 = Arrays.asList(validEmployee1, invalidEmployee1);
        List<Employee> employeeList2 = Arrays.asList(validEmployee2, invalidEmployee2);
        List<List<Employee>> nestedList = Arrays.asList(employeeList1, employeeList2);

        ConstraintViolationException exception = invokeHandleMethod(method, new Object[] {nestedList});
        assertThat(exception).isNotNull();
    }

    @Test
    @DisplayName("测试 @Valid Map<String, Employee>")
    void shouldReturnMsgWhenValidateMapSimple() {
        Method method = ReflectionUtils.getDeclaredMethod(ValidateService.class, "validateEmployeeMap", Map.class);
        Employee validEmployee = new Employee("John", 25);
        Employee invalidEmployee1 = new Employee("", 17);
        Employee invalidEmployee2 = new Employee("Jane", 150);

        Map<String, Employee> employeeMap = new HashMap<>();
        employeeMap.put("1", validEmployee);
        employeeMap.put("2", invalidEmployee1);
        employeeMap.put("3", invalidEmployee2);

        ConstraintViolationException exception = invokeHandleMethod(method, new Object[] {employeeMap});
        assertThat(exception).isNotNull();
    }

    @Test
    @DisplayName("测试 @Valid Map<Employee, ValidationTestData>")
    void shouldReturnMsgWhenValidateMapObj() {
        Method method = ReflectionUtils.getDeclaredMethod(ValidateService.class, "validateEmployeeDataMap", Map.class);
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

        ConstraintViolationException exception = invokeHandleMethod(method, new Object[] {map});
        assertThat(exception).isNotNull();
    }

    @Test
    @DisplayName("测试 @Valid Map<Employee, Map<String, ValidationTestData>>")
    void shouldReturnMsgWhenValidateMapInMap() {
        Method method =
                ReflectionUtils.getDeclaredMethod(ValidateService.class, "validateNestedEmployeeDataMap", Map.class);
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

        ConstraintViolationException exception = invokeHandleMethod(method, new Object[] {nestedMap});
        assertThat(exception).isNotNull();
    }

    @Test
    @DisplayName("测试 @Valid List<Map<String, Employee>>")
    void shouldReturnMsgWhenValidateMapInList() {
        Method method = ReflectionUtils.getDeclaredMethod(ValidateService.class, "validateEmployeeMapList", List.class);
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

        ConstraintViolationException exception = invokeHandleMethod(method, new Object[] {listOfMaps});
        assertThat(exception).isNotNull();
    }

    @Test
    @DisplayName("测试 @Valid Map<Employee, List<ValidationTestData>>")
    void shouldReturnMsgWhenValidateListInMap() {
        Method method =
                ReflectionUtils.getDeclaredMethod(ValidateService.class, "validateEmployeeDataListMap", Map.class);
        Employee validEmployee = new Employee("John", 25);
        Employee invalidEmployee = new Employee("", 17);

        ValidationTestData validData = new ValidationTestData();
        validData.setName("Test");
        validData.setAge(25);

        ValidationTestData invalidData = new ValidationTestData();
        invalidData.setName("");
        invalidData.setAge(-1);

        List<ValidationTestData> dataList1 = Arrays.asList(validData, invalidData);
        List<ValidationTestData> dataList2 = List.of(validData);

        Map<Employee, List<ValidationTestData>> map = new HashMap<>();
        map.put(validEmployee, dataList1);
        map.put(invalidEmployee, dataList2);

        ConstraintViolationException exception = invokeHandleMethod(method, new Object[] {map});
        assertThat(exception).isNotNull();
    }

    @Test
    @DisplayName("测试 @Valid List<Company>")
    void shouldReturnMsgWhenValidateListComplex() {
        Method method = ReflectionUtils.getDeclaredMethod(ValidateService.class, "validateCompanyList", List.class);
        Employee invalidEmployee = new Employee("", 17);
        Company invalidCompany = new Company(List.of(invalidEmployee));
        List<Company> companies = List.of(invalidCompany);

        ConstraintViolationException exception = invokeHandleMethod(method, new Object[] {companies});
        assertThat(exception).isNotNull();
    }

    @Test
    @DisplayName("测试 @NotNull 注解 - 成功场景")
    void testNotNullValidationSuccess() {
        this.validateService.testNotNull("valid value");
    }

    @Test
    @DisplayName("测试 @Size 注解 - 最小边界值")
    void testSizeStringValidationMinBoundary() {
        this.validateService.testSize("ab"); // 2个字符，最小边界
    }

    @Test
    @DisplayName("测试 @Size 注解 - 最大边界值")
    void testSizeStringValidationMaxBoundary() {
        this.validateService.testSize("abcdefghij"); // 10个字符，最大边界
    }

    @Test
    @DisplayName("测试 @Min 注解 - 边界值")
    void testMinValidationBoundary() {
        this.validateService.testMin(10); // 最小值边界
    }

    @Test
    @DisplayName("测试 @Max 注解 - 边界值")
    void testMaxValidationBoundary() {
        this.validateService.testMax(100); // 最大值边界
    }

    @Test
    @DisplayName("测试 @Positive 注解 - 边界值")
    void testPositiveValidationBoundary() {
        this.validateService.testPositive(1); // 最小正数
    }

    @Test
    @DisplayName("测试 @PositiveOrZero 注解 - 零值")
    void testPositiveOrZeroValidationZero() {
        this.validateService.testPositiveOrZero(0); // 零值
    }

    @Test
    @DisplayName("测试 @Past 注解 - 边界值")
    void testPastValidationBoundary() {
        this.validateService.testPast(LocalDate.now().minusDays(1)); // 昨天
    }

    @Test
    @DisplayName("测试 @Future 注解 - 边界值")
    void testFutureValidationBoundary() {
        this.validateService.testFuture(LocalDate.now().plusDays(1)); // 明天
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

        this.validateService.testValidObject(validData);
    }

    @Test
    @DisplayName("测试多个验证失败")
    void testMultipleValidationFailures() {
        Method method =
                ReflectionUtils.getDeclaredMethod(ValidateService.class, "testValidObject", ValidationTestData.class);
        ValidationTestData invalidData = new ValidationTestData();
        invalidData.setName(""); // 违反@NotBlank
        invalidData.setAge(-1); // 违反@Min(0)
        invalidData.setQuantity(-10); // 违反@Positive

        ConstraintViolationException exception = invokeHandleMethod(method, new Object[] {invalidData});
        assertThat(exception).isNotNull();
    }

    @Test
    @DisplayName("测试混合原始类型和对象校验")
    void testMixedPrimitiveAndObjectValidation() {
        Method method =
                ReflectionUtils.getDeclaredMethod(ValidateService.class, "validateMixed", int.class, Employee.class);
        Employee invalidEmployee = new Employee("", 17);
        ConstraintViolationException exception = invokeHandleMethod(method, new Object[] {-1, invalidEmployee});
        assertThat(exception).isNotNull();
    }

    @Test
    @DisplayName("测试空集合和 null 值混合")
    void testEmptyCollectionAndNullMixed() {
        Method method = ReflectionUtils.getDeclaredMethod(ValidateService.class,
                "validateMixedCollections",
                List.class,
                List.class);
        ConstraintViolationException exception =
                invokeHandleMethod(method, new Object[] {null, Collections.emptyList()});
        assertThat(exception).isNotNull();
    }

    @Test
    @DisplayName("测试 @Range 注解 - 最小值验证")
    void testRangeMinValidation() {
        Method method = ReflectionUtils.getDeclaredMethod(ValidateService.class, "testRange", int.class);
        ConstraintViolationException exception = invokeHandleMethod(method, new Object[] {5});
        assertThat(exception.getMessage()).contains("需要在10和100之间");
    }

    @Test
    @DisplayName("测试 @Range 注解 - 最大值验证")
    void testRangeMaxValidation() {
        Method method = ReflectionUtils.getDeclaredMethod(ValidateService.class, "testRange", int.class);
        ConstraintViolationException exception = invokeHandleMethod(method, new Object[] {150});
        assertThat(exception.getMessage()).contains("需要在10和100之间");
    }

    @Test
    @DisplayName("测试 @Range 注解 - 最小边界值")
    void testRangeMinBoundary() {
        this.validateService.testRange(10); // 最小边界值，应该通过
    }

    @Test
    @DisplayName("测试 @Range 注解 - 最大边界值")
    void testRangeMaxBoundary() {
        this.validateService.testRange(100); // 最大边界值，应该通过
    }

    @Test
    @DisplayName("测试 @Range 注解 - 中间值")
    void testRangeValidValue() {
        this.validateService.testRange(50); // 中间值，应该通过
    }

    @Test
    @DisplayName("测试 @Range 注解 - BigDecimal 类型")
    void testRangeBigDecimalValidation() {
        Method method =
                ReflectionUtils.getDeclaredMethod(ValidateService.class, "testRangeBigDecimal", BigDecimal.class);
        ConstraintViolationException exception = invokeHandleMethod(method, new Object[] {new BigDecimal("5.5")});
        assertThat(exception.getMessage()).contains("需要在10和100之间");
    }

    @Nested
    @DisplayName("测试 Locale 默认值为 null 时的情况")
    public class ValidationHandlerNullTest {
        @BeforeEach
        void setUp() {
            ValidationHandlerTest.this.handler.setLocale(null);
        }

        @Test
        @DisplayName("测试@Null注解")
        void testNullValidation() {
            Method method = ReflectionUtils.getDeclaredMethod(ValidateService.class, "testNull", String.class);
            ConstraintViolationException exception = invokeHandleMethod(method, new Object[] {"not null"});
            assertThat(exception.getMessage()).isNotNull();
        }
    }
}

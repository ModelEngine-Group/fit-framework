/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fitframework.validation.data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.log.Logger;
import modelengine.fitframework.validation.Validated;

/**
 * 表示测试用分组校验服务。
 *
 * @author 阮睿
 * @since 2025-07-18
 */
@Component
public class GroupValidateService {
    private static final Logger LOG = Logger.get(GroupValidateService.class);

    // 学生年龄验证服务
    @Component
    @Validated(ValidationTestData.StudentGroup.class)
    public static class StudentValidateService {
        public void validateStudentAge(
                @Min(value = 7, message = "范围要在7~20之内", groups = ValidationTestData.StudentGroup.class) @Max(
                        value = 20, message = "范围要在7~20之内",
                        groups = ValidationTestData.StudentGroup.class) int age) {
            LOG.debug("Validating student age: {}", age);
        }
    }

    // 教师年龄验证服务
    @Component
    @Validated(ValidationTestData.TeacherGroup.class)
    public static class TeacherValidateService {
        public void validateTeacherAge(
                @Min(value = 22, message = "范围要在22~65之内", groups = ValidationTestData.TeacherGroup.class) @Max(
                        value = 65, message = "范围要在22~65之内",
                        groups = ValidationTestData.TeacherGroup.class) int age) {
            LOG.debug("Validating teacher age: {}", age);
        }
    }

    // 高级分组验证服务
    @Component
    @Validated(ValidationTestData.AdvancedGroup.class)
    public static class AdvancedValidateService {
        public void validateAdvancedGroup(@Valid ValidationTestData data) {
            LOG.debug("Validating advanced group data: {}", data);
        }
    }
}

/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fitframework.validation.data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

/**
 * 员工实体类。
 *
 * @author 阮睿
 * @since 2025-07-18
 */
public class Employee {
    @NotBlank(message = "姓名不能为空")
    private String name;
    
    @Min(value = 18, message = "年龄必须大于等于18")
    private int age;
    
    public Employee() {}
    
    public Employee(String name, int age) {
        this.name = name;
        this.age = age;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public int getAge() {
        return age;
    }
    
    public void setAge(int age) {
        this.age = age;
    }
    
    @Override
    public String toString() {
        return "Employee{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}
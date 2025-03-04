/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.integration.mybatis.model;

/**
 * 测试用单词实体类。
 *
 * @author 李金绪
 * @since 2025-02-25
 */
public class WordDo {
    private String name;
    private String firstLetter;

    /**
     * 构造函数。
     *
     * @param name 表示单词名称的 {@link String}。
     * @param firstLetter 表示单词首字母的 {@link String}。
     */
    public WordDo(String name, String firstLetter) {
        this.name = name;
        this.firstLetter = firstLetter;
    }

    /**
     * 获取单词名称。
     *
     * @return 表示单词名称的 {@link String}。
     */
    public String getName() {
        return this.name;
    }

    /**
     * 设置单词名称。
     *
     * @param name 表示单词名称的 {@link String}。
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取单词首字母。
     *
     * @return 表示单词首字母的 {@link String}。
     */
    public String getFirstLetter() {
        return this.firstLetter;
    }

    /**
     * 设置单词首字母。
     *
     * @param firstLetter 表示单词首字母的 {@link String}。
     */

    public void setFirstLetter(String firstLetter) {
        this.firstLetter = firstLetter;
    }
}
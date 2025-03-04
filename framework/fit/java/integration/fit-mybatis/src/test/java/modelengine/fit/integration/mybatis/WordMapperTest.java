/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.integration.mybatis;

import static org.assertj.core.api.Assertions.assertThat;

import modelengine.fit.integration.mybatis.mapper.WordMapper;
import modelengine.fit.integration.mybatis.model.WordDo;
import modelengine.fitframework.annotation.Fit;
import modelengine.fitframework.conf.Config;
import modelengine.fitframework.test.annotation.MybatisTest;
import modelengine.fitframework.test.annotation.Sql;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 测试数据库层。
 *
 * @author 李金绪
 * @since 2025-02-25
 */
@MybatisTest(classes = {WordMapper.class})
@Sql(scripts = "sql/create/word.sql")
@DisplayName("测试自动转换驼峰形式")
public class WordMapperTest {
    @Fit
    private WordMapper mapper;
    @Fit
    private Config config;

    @Test
    @DisplayName("测试配置打开时,下划线正确转换至驼峰")
    void shouldOkWhenMapUnderscoreToCamelcase() {
        WordDo oriWord = this.mapper.get("hello");
        assertThat(oriWord).isNull();

        this.mapper.add(new WordDo("hello", "h"));

        WordDo curWord = this.mapper.get("hello");
        assertThat(curWord.getFirstLetter()).isEqualTo("h");
    }
}
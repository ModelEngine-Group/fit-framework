/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.source.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import modelengine.fel.core.document.Document;
import modelengine.fel.core.pattern.Source;
import modelengine.fitframework.util.FileUtils;
import modelengine.fitframework.util.MapBuilder;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.util.List;

/**
 * 表示 {@link TextFileSource} 的测试集。
 *
 * @author 易文渊
 * @since 2024-08-09
 */
@DisplayName("测试 TextFileSource")
public class TextFileSourceTest {
    @Test
    @DisplayName("测试读取文本文件成功，结果符合预期")
    void giveExistFileThenReturnOk() {
        Source<File> fileSource = new TextFileSource();
        URL url = this.getClass().getClassLoader().getResource("test.txt");
        File file = FileUtils.file(url);
        assertThat(file).isNotNull();
        List<Document> documents = fileSource.load(file);
        assertThat(documents).hasSize(1)
                .first()
                .extracting(Document::text, Document::metadata)
                .containsExactly("This is a test.",
                        MapBuilder.<String, Object>get().put("source", file.getName()).build());
    }

    @Test
    @DisplayName("测试文件不存在，读取失败")
    void giveNotExistFileThenReturnError() {
        assertThatThrownBy(() -> {
            Source<File> fileSource = new TextFileSource();
            fileSource.load(null);
        }).isInstanceOf(IllegalArgumentException.class);
    }
}
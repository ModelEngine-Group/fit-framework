/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.community.model.openai.entity.image;

import modelengine.fitframework.annotation.Property;
import modelengine.fitframework.exception.FitException;
import modelengine.fitframework.resource.web.Media;
import modelengine.fitframework.util.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * 表示 OpenAi 格式的图片。
 *
 * @author 何嘉斌
 * @since 2024-12-17
 */
public class OpenAiImage {
    @Property(name = "b64_json")
    private String b64Json;
    private String url;

    /**
     * 获取图片媒体资源。
     *
     * @return 表示图片媒体资源的 {@link Media}。
     */
    public Media media() {
        try {
            return StringUtils.isNotBlank(b64Json) ? new Media("image/jpeg", b64Json) : new Media(new URL(url));
        } catch (MalformedURLException ex) {
            throw new FitException(ex);
        }
    }
}
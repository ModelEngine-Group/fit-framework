/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.security.simple;

import modelengine.fit.security.Decryptor;
import modelengine.fit.security.Encryptor;
import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.util.StringUtils;

/**
 * 表示 {@link Decryptor} 的简单实现。
 *
 * @author 季聿阶
 * @since 2023-07-31
 */
@Component
public class SimpleCipher implements Encryptor, Decryptor {
    @Override
    public String encrypt(String decrypted) {
        return CIPHER_PREFIX + decrypted + CIPHER_SUFFIX;
    }

    @Override
    public String decrypt(String encrypted) {
        if (StringUtils.startsWithIgnoreCase(encrypted, CIPHER_PREFIX) && StringUtils.endsWithIgnoreCase(encrypted, CIPHER_SUFFIX)) {
            return encrypted.substring(CIPHER_PREFIX.length(), encrypted.length() - CIPHER_SUFFIX.length());
        }
        return encrypted;
    }
}

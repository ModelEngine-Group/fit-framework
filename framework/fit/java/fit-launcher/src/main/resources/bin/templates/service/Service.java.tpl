/*---------------------------------------------------------------------------------------------
 *  Copyright (c) {{YEAR}} FIT Framework Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package {{PACKAGE}};

import modelengine.fitframework.annotation.Genericable;

/**
 * {{SERVICE_NAME}} 服务接口（SPI）
 */
public interface {{SERVICE_NAME}} {
    /**
     * 执行服务操作
     *
     * @return 服务执行结果
     */
    @Genericable(id = "{{SERVICE_NAME}}")
    String execute();
}

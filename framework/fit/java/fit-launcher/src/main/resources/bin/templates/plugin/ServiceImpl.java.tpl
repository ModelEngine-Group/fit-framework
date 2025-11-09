/*---------------------------------------------------------------------------------------------
 *  Copyright (c) {{YEAR}} FIT Framework Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package {{PACKAGE}};

import {{SERVICE_PACKAGE}}.{{SERVICE_NAME}};
import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.annotation.Fitable;

/**
 * {{SERVICE_NAME}} 的默认实现
 */
@Component
public class Default{{SERVICE_NAME}} implements {{SERVICE_NAME}} {
    @Override
    @Fitable(id = "default-{{SERVICE_ID}}")
    public String execute() {
        return "{{SERVICE_NAME}} plugin is working successfully!";
    }
}

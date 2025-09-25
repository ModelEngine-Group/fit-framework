/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.maven.complie.plugin;

import modelengine.fitframework.plugin.maven.support.AbstractMojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.util.Objects;

/**
 * 对 Jar 包进行编译处理。
 *
 * @author 杭潇
 * @author 曹嘉美
 * @since 2024-10-26
 */
@Mojo(name = "build-tool", defaultPhase = LifecyclePhase.COMPILE,
        requiresDependencyResolution = ResolutionScope.RUNTIME)
public class BuildGroupPluginMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project.packaging}", readonly = true, required = true)
    private String packaging;

    @Override
    public void execute() throws MojoExecutionException {
        if (!Objects.equals(this.packaging, "jar")) {
            return;
        }
        GroupPluginCompiler compiler = new GroupPluginCompiler(this.project(), this.getLog());
        compiler.compile();
    }
}


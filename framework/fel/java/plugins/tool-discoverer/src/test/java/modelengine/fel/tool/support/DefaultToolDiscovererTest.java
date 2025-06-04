/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.support;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import modelengine.fel.tool.ToolSchema;
import modelengine.fel.tool.service.ToolRepository;
import modelengine.fit.serialization.json.jackson.JacksonObjectSerializer;
import modelengine.fitframework.plugin.Plugin;
import modelengine.fitframework.plugin.PluginCategory;
import modelengine.fitframework.plugin.PluginMetadata;
import modelengine.fitframework.resource.Resource;
import modelengine.fitframework.resource.support.FileResource;
import modelengine.fitframework.serialization.ObjectSerializer;
import modelengine.fitframework.util.FileUtils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * 表示 {@link DefaultToolDiscoverer} 的测试集。
 *
 * @author 易文渊
 * @since 2024-08-15
 */
@DisplayName("测试 DefaultToolDiscoverer")
public class DefaultToolDiscovererTest {
    private Plugin plugin;
    private ToolRepository toolRepository;

    @BeforeEach
    void setUp() throws IOException {
        this.plugin = mock(Plugin.class, RETURNS_DEEP_STUBS);
        this.toolRepository = mock(ToolRepository.class);
        URL url = this.getClass().getClassLoader().getResource(ToolSchema.TOOL_MANIFEST);
        Resource[] resources = {new FileResource(FileUtils.file(url))};
        when(this.plugin.metadata()).thenReturn(new TestPluginMetadata());
        when(this.plugin.resolverOfResources().resolve(ToolSchema.TOOL_MANIFEST)).thenReturn(resources);
    }

    @AfterEach
    void tearDown() {
        clearInvocations(this.plugin, this.toolRepository);
    }

    @Test
    @DisplayName("测试自动装载工具")
    void shouldOkWhenInstallTools() {
        doNothing().when(this.toolRepository).addTool(any());
        ObjectSerializer serializer = new JacksonObjectSerializer(null, null, null, true);
        DefaultToolDiscoverer toolDiscoverer = new DefaultToolDiscoverer(this.toolRepository, serializer, 64);
        toolDiscoverer.onPluginStarted(this.plugin);
        verify(this.toolRepository, times(1)).addTool(argThat(tool -> tool.name().equals("get_delivery_date")));
    }

    @Test
    @DisplayName("测试自动卸载工具")
    void shouldOkWhenUninstallTools() {
        doNothing().when(this.toolRepository).deleteTool(any(), any());
        ObjectSerializer serializer = new JacksonObjectSerializer(null, null, null, true);
        DefaultToolDiscoverer toolDiscoverer = new DefaultToolDiscoverer(this.toolRepository, serializer, 64);
        toolDiscoverer.onPluginStopping(this.plugin);
        verify(this.toolRepository, times(1)).deleteTool(argThat(group -> true),
                argThat(name -> name.equals("get_delivery_date")));
    }

    static class TestPluginMetadata implements PluginMetadata {
        static final String KEY = "test";

        @Override
        public String group() {
            return KEY;
        }

        @Override
        public String name() {
            return KEY;
        }

        @Override
        public String version() {
            return KEY;
        }

        @Override
        public URL location() {
            return null;
        }

        @Override
        public List<String> hierarchicalNames() {
            return null;
        }

        @Override
        public PluginCategory category() {
            return PluginCategory.USER;
        }

        @Override
        public int level() {
            return 0;
        }
    }
}
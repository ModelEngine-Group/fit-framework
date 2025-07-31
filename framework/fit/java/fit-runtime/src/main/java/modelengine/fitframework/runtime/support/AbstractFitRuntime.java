/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fitframework.runtime.support;

import modelengine.fitframework.broker.client.ioc.DynamicRoutingDependencyResolver;
import modelengine.fitframework.conf.Config;
import modelengine.fitframework.conf.ConfigChain;
import modelengine.fitframework.conf.ConfigLoadException;
import modelengine.fitframework.conf.ConfigLoader;
import modelengine.fitframework.conf.Configs;
import modelengine.fitframework.conf.support.DefaultConfigChain;
import modelengine.fitframework.event.EventPublisher;
import modelengine.fitframework.ioc.BeanFactory;
import modelengine.fitframework.ioc.BeanFactoryOrderComparator;
import modelengine.fitframework.ioc.BeanResolver;
import modelengine.fitframework.ioc.BeanResolvers;
import modelengine.fitframework.ioc.DependencyResolver;
import modelengine.fitframework.ioc.annotation.AnnotationMetadataResolver;
import modelengine.fitframework.ioc.annotation.AnnotationMetadataResolvers;
import modelengine.fitframework.log.Logger;
import modelengine.fitframework.log.Loggers;
import modelengine.fitframework.maven.MavenCoordinate;
import modelengine.fitframework.plugin.Plugin;
import modelengine.fitframework.plugin.RootPlugin;
import modelengine.fitframework.plugin.SharedJarRegistry;
import modelengine.fitframework.plugin.support.AbstractPlugin;
import modelengine.fitframework.protocol.jar.Jar;
import modelengine.fitframework.protocol.jar.JarLocation;
import modelengine.fitframework.resource.Resource;
import modelengine.fitframework.resource.ResourceResolver;
import modelengine.fitframework.runtime.FitRuntime;
import modelengine.fitframework.runtime.FitRuntimeStartedObserver;
import modelengine.fitframework.runtime.FitRuntimeStartupException;
import modelengine.fitframework.runtime.shared.ClassLoaderSharedJarRegistry;
import modelengine.fitframework.runtime.shared.SharedUrlClassLoader;
import modelengine.fitframework.util.ClassUtils;
import modelengine.fitframework.util.FileUtils;
import modelengine.fitframework.util.LockUtils;
import modelengine.fitframework.util.MapUtils;
import modelengine.fitframework.util.ObjectUtils;
import modelengine.fitframework.util.StringUtils;
import modelengine.fitframework.util.support.AbstractDisposable;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 为 {@link FitRuntime} 提供基类。
 *
 * @author 梁济时
 * @since 2023-01-31
 */
public abstract class AbstractFitRuntime extends AbstractDisposable implements FitRuntime {
    private static final String FRAMEWORK_VERSION = "3.5.0-M6";
    private static final String GLOBAL_CONFIG_NAME = "Global Config";
    private static final String EXTERNAL_CONFIG_CHAIN_NAME = "External Config Chain";
    private static final String BUILTIN_CONFIG_CHAIN_NAME = "Builtin Config Chain";
    private static final String IMPORTED_CONFIG_CHAIN_NAME = "Imported Config Chain";
    private static final String PROFILE_BUILTIN_CONFIG_NAME = "Builtin Config (Profile)";
    private static final String DEFAULT_BUILTIN_CONFIG_NAME = "Builtin Config (Default)";

    private static final String COMMAND_LINE_ARGUMENTS_CONFIG_NAME = "Command Line Arguments";
    private static final String SYSTEM_PROPERTIES_CONFIG_NAME = "System Properties";
    private static final String EXTERNAL_FILE_CONFIG_NAME = "External Config File";
    private static final String ENVIRONMENT_VARIABLES_CONFIG_NAME = "Environment Variables";
    private static final String SPECIFIC_ENVIRONMENT_VARIABLES_CONFIG_NAME = "Specific Environment Variables";

    private static final String EXTERNAL_CONFIG_FILE_KEY = "config-file";
    private static final String PROFILE_CONFIG_KEY = "fit.profiles.active";

    private static final String KEY_PREFIX = "--";

    private static final String UNKNOWN_VERSION = "<UNKNOWN>";
    private static final String FIT_SEPARATOR = "FIT_SEPARATOR";
    private static final String DOT_SEPARATOR = ".";

    private final Class<?> entry;
    private final String[] args;
    private final ResourceResolver resolverOfResources;
    private volatile boolean started;
    private final Object monitor;

    private final ConfigLoader loaderOfConfigs;
    private final BeanResolver resolverOfBeans;
    private final DependencyResolver resolverOfDependencies;
    private final AnnotationMetadataResolver resolverOfAnnotations;
    private final EventPublisher publisherOfEvents;

    private volatile URL location;
    private volatile SharedUrlClassLoader sharedClassLoader;
    private volatile SharedJarRegistry registryOfSharedJars;
    private volatile String profile;
    private volatile Config config;
    private volatile RootPlugin root;
    private volatile String version;

    /**
     * 使用入口类和命令行参数来初始化 {@link AbstractFitRuntime} 类的新实例。
     *
     * @param entry 表示入口类的 {@link Class}{@code <?>}。
     * @param args 表示命令行参数的 {@link String}{@code []}。
     */
    public AbstractFitRuntime(Class<?> entry, String[] args) {
        this.entry = entry;
        this.args = ObjectUtils.nullIf(args, StringUtils.EMPTY_ARRAY);
        this.started = false;
        this.monitor = LockUtils.newSynchronizedLock();

        ClassLoader loader = AbstractFitRuntime.class.getClassLoader();
        this.loaderOfConfigs = Configs.load(loader);
        this.resolverOfResources = ResourceResolver.forClassLoader(loader);
        this.resolverOfBeans = BeanResolvers.load(loader);
        this.resolverOfDependencies = new DynamicRoutingDependencyResolver();
        this.resolverOfAnnotations = AnnotationMetadataResolvers.create();
        this.publisherOfEvents = new FitRuntimeEventPublisher(this);
    }

    @Override
    public Class<?> entry() {
        return this.entry;
    }

    @Override
    public String[] argumentsFromCommandLine() {
        return Arrays.copyOf(this.args, this.args.length);
    }

    @Override
    public URL location() {
        return this.location;
    }

    @Override
    public String version() {
        if (this.version == null) {
            URL url = ClassUtils.locateOfProtectionDomain(AbstractFitRuntime.class);
            JarLocation jarLocation = JarLocation.parse(url);
            if (jarLocation.nests().isEmpty() && jarLocation.file().isDirectory()) {
                this.version = UNKNOWN_VERSION;
            } else {
                MavenCoordinate coordinate;
                try {
                    Jar jar = Jar.from(jarLocation);
                    coordinate = MavenCoordinate.read(jar);
                } catch (IOException ex) {
                    throw new IllegalStateException(StringUtils.format("Failed to read version of JAR. [url={0}]",
                            url));
                }
                this.version = coordinate.version();
            }
        }
        return this.version;
    }

    @Override
    public ClassLoader sharedClassLoader() {
        return this.sharedClassLoader;
    }

    @Override
    public SharedJarRegistry registryOfSharedJars() {
        return this.registryOfSharedJars;
    }

    @Override
    public Config config() {
        return this.config;
    }

    @Override
    public String profile() {
        return this.profile;
    }

    @Override
    public RootPlugin root() {
        return this.root;
    }

    @Override
    public List<Plugin> plugins() {
        List<Plugin> plugins = new LinkedList<>();
        Queue<Plugin> queue = new LinkedList<>();
        Optional.ofNullable(this.root()).ifPresent(queue::add);
        while (!queue.isEmpty()) {
            Plugin current = queue.poll();
            plugins.add(current);
            current.children().forEach(queue::add);
        }
        return new ArrayList<>(plugins);
    }

    @Override
    public Optional<Plugin> plugin(String name) {
        for (Plugin plugin : this.plugins()) {
            if (Objects.equals(plugin.metadata().name(), name)) {
                return Optional.of(plugin);
            }
        }
        return Optional.empty();
    }

    @Override
    public ConfigLoader loaderOfConfigs() {
        return this.loaderOfConfigs;
    }

    @Override
    public ResourceResolver resolverOfResources() {
        return this.resolverOfResources;
    }

    @Override
    public BeanResolver resolverOfBeans() {
        return this.resolverOfBeans;
    }

    @Override
    public DependencyResolver resolverOfDependencies() {
        return this.resolverOfDependencies;
    }

    @Override
    public AnnotationMetadataResolver resolverOfAnnotations() {
        return this.resolverOfAnnotations;
    }

    @Override
    public EventPublisher publisherOfEvents() {
        return this.publisherOfEvents;
    }

    @Override
    public boolean started() {
        return this.started;
    }

    @Override
    public void start() {
        if (this.started) {
            return;
        }
        synchronized (this.monitor) {
            if (!this.started) {
                this.start0();
                this.started = true;
            }
        }
    }

    private void start0() {
        long milliseconds = System.currentTimeMillis();
        try {
            this.location = this.locateRuntime();
            this.sharedClassLoader = this.obtainSharedClassLoader();
            this.registryOfSharedJars = new ClassLoaderSharedJarRegistry(this.sharedClassLoader);
            this.config = this.loadConfig();
            Loggers.initialize(this.config, this.getClass().getClassLoader());
            Logger log = Logger.get(this.getClass());
            log.info("Prepare to start FIT application... [version={}]", FRAMEWORK_VERSION);
            this.root = this.createRootPlugin();
            this.root.initialize();
            this.publisherOfEvents()
                    .publishEvent(Events.prepared(this, Duration.ofMillis(System.currentTimeMillis() - milliseconds)));
            this.root.start();
            this.root.container()
                    .all(FitRuntimeStartedObserver.class)
                    .stream()
                    .sorted(BeanFactoryOrderComparator.INSTANCE)
                    .map(BeanFactory::<FitRuntimeStartedObserver>get)
                    .forEach(observer -> observer.onRuntimeStarted(this));
            this.publisherOfEvents()
                    .publishEvent(Events.started(this, Duration.ofMillis(System.currentTimeMillis() - milliseconds)));
            log.info("FIT application started. [version={}]", FRAMEWORK_VERSION);
        } catch (Throwable cause) {
            this.publisherOfEvents().publishEvent(Events.failed(this, cause));
            throw cause;
        }
    }

    /**
     * 定位运行时的位置。
     *
     * @return 表示运行时位置的 {@link URL}。
     */
    protected abstract URL locateRuntime();

    /**
     * 获取用以加载公共类的加载程序。
     *
     * @return 表示公共类的加载程序的 {@link SharedUrlClassLoader}。
     */
    protected abstract SharedUrlClassLoader obtainSharedClassLoader();

    private Config loadConfig() {
        ConfigChain globalChain = new DefaultConfigChain(GLOBAL_CONFIG_NAME);
        this.addExternalConfigChain(globalChain);
        this.addBuiltinConfigChain(globalChain);
        this.addImportedConfigChain(globalChain);
        return globalChain;
    }

    private void addExternalConfigChain(ConfigChain globalChain) {
        ConfigChain chain = new DefaultConfigChain(EXTERNAL_CONFIG_CHAIN_NAME);
        globalChain.addConfig(chain);
        chain.addConfig(loadStartupConfig(this.args));
        chain.addConfig(Config.fromHierarchical(SYSTEM_PROPERTIES_CONFIG_NAME, System.getProperties()));
        chain.addConfig(Config.fromHierarchical(ENVIRONMENT_VARIABLES_CONFIG_NAME, System.getenv()));
        Map<String, String> specificEnv = this.loadSpecificEnv(System.getenv());
        if (MapUtils.isNotEmpty(specificEnv)) {
            chain.addConfig(Config.fromHierarchical(SPECIFIC_ENVIRONMENT_VARIABLES_CONFIG_NAME, specificEnv));
        }
        chain.addConfig(this.loadExternalConfig(this.getExternalConfigFileName(globalChain)));
    }

    private void addBuiltinConfigChain(ConfigChain globalChain) {
        ConfigChain chain = new DefaultConfigChain(BUILTIN_CONFIG_CHAIN_NAME);
        globalChain.addConfig(chain);
        this.loadDefaultBuiltConfig().ifPresent(chain::addConfig);
        this.profile = getActiveProfileName(globalChain);
        if (StringUtils.isNotBlank(this.profile)) {
            this.loadProfileBuiltinConfig().ifPresent(config -> chain.insertConfig(0, config));
        }
    }

    private void addImportedConfigChain(ConfigChain globalChain) {
        globalChain.addConfig(new DefaultConfigChain(IMPORTED_CONFIG_CHAIN_NAME));
    }

    private Optional<Config> loadDefaultBuiltConfig() {
        return this.loadEmbeddedConfig(this.resolverOfResources,
                DEFAULT_BUILTIN_CONFIG_NAME,
                AbstractPlugin.BUILTIN_CONFIG_PREFIX);
    }

    private Optional<Config> loadProfileBuiltinConfig() {
        String prefix = AbstractPlugin.BUILTIN_CONFIG_PREFIX + '-' + this.profile;
        return this.loadEmbeddedConfig(this.resolverOfResources, PROFILE_BUILTIN_CONFIG_NAME, prefix);
    }

    private static String getActiveProfileName(Config globalConfig) {
        String activeProfile = globalConfig.get(PROFILE_CONFIG_KEY, String.class);
        activeProfile = ObjectUtils.nullIf(StringUtils.trim(activeProfile), StringUtils.EMPTY);
        return activeProfile;
    }

    /**
     * 获取外部配置文件的文件名。
     *
     * @param config 表示当前系统已存在的配置的 {@link Config}。
     * @return 表示外部配置文件的文件名的 {@link String}。
     */
    protected String getExternalConfigFileName(Config config) {
        return config.get(EXTERNAL_CONFIG_FILE_KEY, String.class);
    }

    /**
     * 加载启动配置。
     * <p>启动配置主要包含三部分内容：
     * <ul>
     *     <li>启动时的命令行参数</li>
     *     <li>{@link System#getProperties()} 中的系统属性</li>
     *     <li>{@link System#getenv()} 中的环境变量</li>
     * </ul></p>
     *
     * @param args 表示命令行启动参数的 {@link String}{@code []}。
     * @return 表示启动配置的 {@link Config}。
     */
    private static Config loadStartupConfig(String[] args) {
        Map<String, Object> map = new HashMap<>(args.length);
        for (String argument : args) {
            int index = argument.indexOf('=');
            if (index < 0) {
                throw new IllegalArgumentException(StringUtils.format(
                        "The argument from command line must be key=value style. [argument={0}]",
                        argument));
            }
            String key = StringUtils.trim(argument.substring(0, index));
            String value = StringUtils.trim(argument.substring(index + 1));
            if (StringUtils.startsWithIgnoreCase(key, KEY_PREFIX)) {
                key = StringUtils.trim(key.substring(KEY_PREFIX.length()));
            }
            if (StringUtils.isEmpty(key)) {
                throw new IllegalArgumentException(StringUtils.format(
                        "The key of a argument config cannot be a blank string. [argument={0}]",
                        argument));
            }
            if (StringUtils.isEmpty(value)) {
                continue;
            }
            Object current = map.get(key);
            if (current == null) {
                map.put(key, value);
            } else if (current instanceof List) {
                List<String> values = ObjectUtils.cast(current);
                values.add(value);
            } else {
                List<String> values = new LinkedList<>();
                values.add(ObjectUtils.cast(current));
                values.add(value);
                map.put(key, values);
            }
        }
        return Config.fromHierarchical(COMMAND_LINE_ARGUMENTS_CONFIG_NAME, hierarchical(map));
    }

    private static Map<String, Object> hierarchical(Map<String, Object> values) {
        Map<String, Object> hierarchical = new HashMap<>();
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            String[] parts = entry.getKey().split("\\.");
            Map<String, Object> current = hierarchical;
            for (int i = 0; i < parts.length - 1; i++) {
                Object value = current.get(parts[i]);
                Map<String, Object> next;
                if (value instanceof Map) {
                    next = ObjectUtils.cast(value);
                } else {
                    next = new HashMap<>();
                    current.put(parts[i], next);
                }
                current = next;
            }
            String key = parts[parts.length - 1];
            Object value = current.get(key);
            if (!(value instanceof Map)) {
                current.put(key, entry.getValue());
            }
        }
        return hierarchical;
    }

    private Config loadExternalConfig(String externalConfigFileName) {
        String actualFileName = StringUtils.trim(externalConfigFileName);
        if (StringUtils.isEmpty(actualFileName)) {
            return null;
        }
        File file = new File(actualFileName);
        try {
            file = file.getCanonicalFile();
        } catch (IOException ex) {
            throw new FitRuntimeStartupException(StringUtils.format(
                    "The file name of external config is not canonical. [path={0}]",
                    FileUtils.path(file)), ex);
        }
        if (!file.exists()) {
            throw new FitRuntimeStartupException(StringUtils.format(
                    "The file of external config does not exist. [path={0}]",
                    FileUtils.path(file)));
        }
        if (!file.isFile()) {
            throw new FitRuntimeStartupException(StringUtils.format(
                    "The file of external config is not regular. [path={0}]",
                    FileUtils.path(file)));
        }
        Resource resource = Resource.fromFile(file);
        return ConfigLoader.loadConfig(this.loaderOfConfigs, resource, EXTERNAL_FILE_CONFIG_NAME);
    }

    /**
     * 加载特定的环境变量。
     *
     * @param envs 表示当前系统已存在的环境变量的 {@link Map}{@code <}{@link String}{@code , }{@link String}{@code >}。
     * @return 表示特定的环境变量的 {@link Map}{@code <}{@link String}{@code , }{@link String}{@code >}。
     */
    protected Map<String, String> loadSpecificEnv(Map<String, String> envs) {
        if (!envs.containsKey(FIT_SEPARATOR)) {
            return Collections.emptyMap();
        }
        String separator = envs.get(FIT_SEPARATOR);
        return envs.entrySet()
                .stream()
                .filter(entry -> entry.getKey().contains(separator))
                .collect(Collectors.toMap(entry -> entry.getKey().replace(separator, DOT_SEPARATOR),
                        Map.Entry::getValue,
                        (existingValue, newValue) -> existingValue));
    }

    @Override
    public Optional<Config> loadEmbeddedConfig(ResourceResolver resourceResolver, String name, String prefix) {
        List<Resource> resources = new LinkedList<>();
        Set<String> extensions = this.loaderOfConfigs.extensions();
        for (String extension : extensions) {
            String pattern = prefix + extension;
            try {
                resources.addAll(Arrays.asList(resourceResolver.resolve(pattern)));
            } catch (IOException e) {
                throw new ConfigLoadException(StringUtils.format("Failed to resolve resources of config. [pattern={0}]",
                        pattern), e);
            }
        }
        if (resources.size() > 1) {
            Config[] configs = resources.stream()
                    .map(resource -> ConfigLoader.loadConfig(this.loaderOfConfigs, resource))
                    .toArray(Config[]::new);
            ConfigChain chain = new DefaultConfigChain(name);
            chain.addConfigs(configs);
            return Optional.of(chain);
        } else if (!resources.isEmpty()) {
            return Optional.ofNullable(ConfigLoader.loadConfig(this.loaderOfConfigs, resources.get(0), name));
        } else {
            return Optional.empty();
        }
    }

    /**
     * 创建根插件实例。
     *
     * @return 表示根插件的 {@link RootPlugin}。
     */
    protected abstract RootPlugin createRootPlugin();
}

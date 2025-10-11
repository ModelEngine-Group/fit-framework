/*
 * Copyright (c) 2024-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fitframework.util.support;

import modelengine.fitframework.inspection.Validation;
import modelengine.fitframework.merge.ConflictResolutionPolicy;
import modelengine.fitframework.util.FileUtils;
import modelengine.fitframework.util.FunctionUtils;
import modelengine.fitframework.util.IoUtils;
import modelengine.fitframework.util.ObjectUtils;
import modelengine.fitframework.util.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * 为解压 {@code .zip} 格式的文件提供工具。
 *
 * @author 梁济时
 * @author 季聿阶
 * @since 2020-10-05
 */
public class Unzip extends AbstractZip<Unzip> {
    private File target;
    private Predicate<ZipEntry> predicate;
    private Security security = Security.DEFAULT;
    private final List<Function<ZipEntry, Redirect>> redirectors;
    private final List<Function<Conflict, ConflictResolutionPolicy>> conflictResolvers;
    private ConflictResolutionPolicy conflictResolutionPolicy;

    /**
     * 添加一个对解包文件内容项的过滤条件。
     *
     * @param zipFile 表示待解包的 {@code .zip} 文件的 {@link File}。
     * @param charset 表示待解包文件的字符集的 {@link Charset}。
     */
    public Unzip(File zipFile, Charset charset) {
        super(zipFile, charset);
        this.redirectors = new ArrayList<>();
        this.conflictResolvers = new ArrayList<>();
        this.conflictResolutionPolicy = ConflictResolutionPolicy.ABORT;
    }

    /**
     * 添加一个对解包文件内容项的过滤条件。
     *
     * @param predicate 表示解包过滤条件的 {@link Predicate}{@code <}{@link ZipEntry}{@code >}。
     * @return 表示解包的文件类的 {@link Unzip}。
     */
    public Unzip filter(Predicate<ZipEntry> predicate) {
        this.predicate = FunctionUtils.and(this.predicate, predicate);
        return this;
    }

    /**
     * 设置解包的重定向器。
     *
     * @param redirector 表示解包的重定向器的 {@link Function}{@code <}{@link ZipEntry}{@code , }{@link Redirect}{@code >}。
     * @return 表示解包的文件类的 {@link Unzip}。
     */
    public Unzip redirect(Function<ZipEntry, Redirect> redirector) {
        Validation.notNull(redirector, "The mapper to redirect output file cannot be null.");
        this.redirectors.add(redirector);
        return this;
    }

    /**
     * 设置解包的冲突解决策略。
     *
     * @param policy 表示解包的冲突解决策略的 {@link ConflictResolutionPolicy}。
     * @return 表示解包的文件类的 {@link Unzip}。
     */
    public Unzip resolveConflict(ConflictResolutionPolicy policy) {
        this.conflictResolutionPolicy = ObjectUtils.nullIf(policy, ConflictResolutionPolicy.ABORT);
        return this;
    }

    /**
     * 设置解包的冲突解决器。
     *
     * @param resolver 表示解包的冲突解决器的 {@link Function}{@code <}{@link Conflict}{@code , }
     * {@link ConflictResolutionPolicy}{@code >}。
     * @return 表示解包的文件类的 {@link Unzip}。
     */
    public Unzip resolveEntryConflict(Function<Conflict, ConflictResolutionPolicy> resolver) {
        Validation.notNull(resolver, "The conflict resolver for entry cannot be null.");
        this.conflictResolvers.add(resolver);
        return this;
    }

    /**
     * 设置解包的安全配置。
     *
     * @param security 表示解包的安全配置的 {@link Security}。
     * @return 表示解包的文件类的 {@link Unzip}。
     */
    public Unzip secure(Security security) {
        this.security = ObjectUtils.nullIf(security, Security.DEFAULT);
        return this;
    }

    @Override
    public void start() throws IOException {
        try (ZipFile zip = new ZipFile(this.file(), ZipFile.OPEN_READ, this.charset())) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            long maxSize = this.security.getCompressedTotalSize();
            long entryCount = 0L;
            while (entries.hasMoreElements()) {
                if (++entryCount > this.security.getEntryMaxCount()) {
                    throw new SecurityException(StringUtils.format(
                            "The file to unzip contains too many entries. [file={0}, max={1}]",
                            this.file().getName(),
                            this.security.getEntryMaxCount()));
                }
                ZipEntry entry = entries.nextElement();
                maxSize -= this.unzip(zip, entry, maxSize);
            }
        }
    }

    /**
     * 设置解包后的目标目录。
     *
     * @param target 表示解包后的目标目录的 {@link File}。
     * @return 表示解包的文件类的 {@link Unzip}。
     */
    public Unzip target(File target) {
        this.target = target;
        return this;
    }

    private void createDirectory(File target) throws IOException {
        if (!target.exists()) {
            this.createDirectory(target.getParentFile());
            return;
        }
        if (target.isDirectory()) {
            return;
        }
        if (this.override()) {
            Files.delete(target.toPath());
            Files.createDirectory(target.toPath());
        } else {
            throw new IOException(StringUtils.format("File already exists. Cannot create directory. [name={0}]",
                    target.getName()));
        }
    }

    private long decompress(ZipFile zip, ZipEntry entry, File target, long maxSize) throws IOException {
        byte[] buffer = new byte[IoUtils.DEFAULT_BUFFER_SIZE];
        long compressed = 0L;
        try (InputStream in = zip.getInputStream(entry); OutputStream out = new FileOutputStream(target, false)) {
            int part;
            while ((part = in.read(buffer, 0, buffer.length)) >= 0) {
                if ((compressed += part) > maxSize) {
                    String template = "The file to unzip is too large. [file={0}, max={1}]";
                    throw new SecurityException(StringUtils.format(template,
                            this.file().getName(),
                            this.security.getCompressedTotalSize()));
                }
                out.write(buffer, 0, part);
            }
        }
        return compressed;
    }

    private void deleteFile(File target) throws IOException {
        if (target.exists()) {
            if (this.override()) {
                Files.delete(target.toPath());
            } else {
                throw new IOException(StringUtils.format("File already exists. Cannot unzip entry. [name={0}]",
                        target.getName()));
            }
        }
    }

    private boolean filter(ZipEntry entry) {
        return FunctionUtils.test(this.predicate, entry, true);
    }

    private File getTarget(ZipEntry entry) {
        for (Function<ZipEntry, Redirect> redirector : this.redirectors) {
            Redirect redirect = redirector.apply(entry);
            if (redirect.redirected) {
                return this.getActualTarget(redirect.redirectedFile);
            }
        }

        String name = entry.getName();

        // 检查是否包含绝对路径字符(以'/'或驱动器字母开头)
        if (name.startsWith("/") || name.startsWith("\\") || (name.length() > 1 && name.charAt(1) == ':')) {
            if (!this.security.isCrossPath()) {
                throw new SecurityException(StringUtils.format("Detected a potential path traversal attack. [path={0}]",
                        name));
            }
        }

        Path targetDir = this.getTargetDirectory().toPath().normalize();
        Path targetPath = targetDir.resolve(name).normalize();

        // 使用Path.startsWith进行前缀检查(比String.startsWith更安全)
        if (!this.security.isCrossPath() && !targetPath.startsWith(targetDir)) {
            throw new SecurityException(StringUtils.format("Detected a potential path traversal attack. [path={0}]",
                    name));
        }

        return targetPath.toFile();
    }

    private File getActualTarget(File target) {
        // 如果是绝对路径,redirector明确指定,直接返回(redirector的职责)
        if (target.isAbsolute()) {
            return FileUtils.canonicalize(target);
        }

        Path targetDir = this.getTargetDirectory().toPath().normalize();
        Path actual = targetDir.resolve(target.toPath()).normalize();

        if (this.security.isCrossPath()) {
            return actual.toFile();
        }

        if (!actual.startsWith(targetDir)) {
            throw new SecurityException(StringUtils.format("Detected a potential path traversal attack. [path={0}]",
                    target.getPath()));
        }
        return actual.toFile();
    }

    private File getTargetDirectory() {
        return ObjectUtils.nullIf(this.target, this.file().getParentFile());
    }

    private ConflictResolutionPolicy resolveConflict(ZipEntry entry, File target) {
        if (!this.conflictResolvers.isEmpty()) {
            Conflict conflict = new Conflict(entry, target);
            for (Function<Conflict, ConflictResolutionPolicy> resolver : this.conflictResolvers) {
                ConflictResolutionPolicy policy = resolver.apply(conflict);
                if (policy != null) {
                    return policy;
                }
            }
        }
        return this.conflictResolutionPolicy;
    }

    private long unzip(ZipFile zip, ZipEntry entry, long maxSize) throws IOException {
        if (!this.filter(entry)) {
            return 0L;
        }
        File actualTarget = this.getTarget(entry);
        if (actualTarget.exists()) {
            ConflictResolutionPolicy policy = this.resolveConflict(entry, actualTarget);
            switch (policy) {
                case SKIP:
                    return 0L;
                case ABORT:
                    throw new IOException(StringUtils.format("File already exists. Cannot unzip entry. [entry={0}]",
                            entry.getName()));
                default:
                    // default to override
                    break;
            }
        }
        return this.unzipByOverride(zip, entry, maxSize, actualTarget);
    }

    private long unzipByOverride(ZipFile zip, ZipEntry entry, long maxSize, File actualTarget) throws IOException {
        FileUtils.ensureDirectory(actualTarget.getParentFile());
        if (entry.isDirectory()) {
            this.createDirectory(actualTarget);
            return 0L;
        } else {
            this.deleteFile(actualTarget);
            return this.decompress(zip, entry, actualTarget, maxSize);
        }
    }

    /**
     * 为 {@code .zip} 压缩文件提供安全配置。
     *
     * @author 梁济时
     * @since 2020-10-05
     */
    public static class Security {
        /**
         * 表示默认的安全配置。
         * <ul>
         *     <li>解包后的总大小不能超过 100MB。</li>
         *     <li>包含的文件数量不能超过 1024 个。</li>
         *     <li>不支持跨路径解压。</li>
         * </ul>
         */
        public static final Security DEFAULT = new Security(0x6400000, 1024, false);

        private final long compressedTotalSize;
        private final long entryMaxCount;
        private final boolean isCrossPath;

        /**
         * 使用解包到的最大尺寸及文件最大数量初始化 {@link Unzip.Security} 类的新实例。
         *
         * @param compressedTotalSize 表示允许解包到的最大尺寸的 64 位整数的 {@code long}。
         * @param entryMaxCount 表示允许解包到的文件的最大数量的 64 位整数 {@code long}。
         * @param isCrossPath 表示是否支持跨路径解压包 {@code boolean}。
         */
        public Security(long compressedTotalSize, long entryMaxCount, boolean isCrossPath) {
            this.compressedTotalSize = compressedTotalSize;
            this.entryMaxCount = entryMaxCount;
            this.isCrossPath = isCrossPath;
        }

        /**
         * 表示获取解包的最大尺寸。
         *
         * @return 允许解包到的最大尺寸的 64 位整数的 {@code long}。
         */
        public long getCompressedTotalSize() {
            return this.compressedTotalSize;
        }

        /**
         * 表示获取解压包的文件的最大数量。
         *
         * @return 允许解包到的文件的最大数量的 64 位整数 {@code long}。
         */
        public long getEntryMaxCount() {
            return this.entryMaxCount;
        }

        /**
         * 表示获取是否支持跨路径解压包。
         *
         * @return 是否支持跨路径解压包 {@code boolean}。
         */
        public boolean isCrossPath() {
            return this.isCrossPath;
        }

        @Override
        public String toString() {
            return StringUtils.format("[compressedTotalSize={0}, entryMaxCount={1}]",
                    this.getCompressedTotalSize(),
                    this.getEntryMaxCount());
        }
    }

    /**
     * 为正在被解包的项提供重定向能力。
     *
     * @author 梁济时
     * @author 季聿阶
     * @since 2020-10-05
     */
    public static class Redirect {
        private static final Redirect EMPTY = new Redirect(false, null);

        private final boolean redirected;
        private final File redirectedFile;

        private Redirect(boolean redirected, File redirectedFile) {
            this.redirected = redirected;
            this.redirectedFile = redirectedFile;
            if (this.redirected) {
                Validation.notNull(this.redirectedFile, "The redirected file cannot be null when redirected is true.");
            }
        }

        /**
         * 表示项将不被重定向。
         *
         * @return 表示不被重定向的结果的 {@link Redirect}。
         */
        public static Redirect unredirected() {
            return EMPTY;
        }

        /**
         * 表示项将被重定向。
         *
         * @param redirectedFile 表示将被重定向到的文件的 {@link File}。
         * @return 表示重定向的结果的 {@link Redirect}。
         */
        public static Redirect redirected(File redirectedFile) {
            return new Redirect(true, redirectedFile);
        }
    }

    /**
     * 为正在被解包的项提供冲突信息。
     *
     * @author 梁济时
     * @since 2020-10-06
     */
    public static class Conflict {
        private final ZipEntry entry;
        private final File target;

        /**
         * 使用指定的压缩文件项和目标文件来初始化 {@link Conflict} 的新实例。
         *
         * @param entry 表示压缩文件中的项的 {@link ZipEntry}。
         * @param target 表示目标文件的 {@link File}。
         */
        public Conflict(ZipEntry entry, File target) {
            this.entry = entry;
            this.target = target;
        }

        /**
         * 获取压缩文件中的项。
         *
         * @return 表示压缩文件中的项的 {@link ZipEntry}。
         */
        public ZipEntry getEntry() {
            return this.entry;
        }

        /**
         * 获取目标文件。
         *
         * @return 表示目标文件的 {@link File}。
         */
        public File getTarget() {
            return this.target;
        }
    }
}

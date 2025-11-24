ARG FIT_VERSION=3.5.3

# 使用Alpine官方镜像（如果镜像拉取失败，请先手动拉取：docker pull alpine:3.19）
FROM alpine:3.19

# 重新声明ARG以在FROM之后使用
ARG FIT_VERSION=3.5.3

LABEL maintainer="FIT Framework Team <support@fit-framework.org>"
LABEL description="FIT Framework Base Image - Alpine Linux (Minimal)"
LABEL org.opencontainers.image.source="https://github.com/ModelEngine-Group/fit-framework"
LABEL org.opencontainers.image.version="${FIT_VERSION}"
LABEL org.opencontainers.image.title="FIT Framework (Alpine)"

# 设置FIT Framework环境变量
ENV FIT_VERSION=${FIT_VERSION}
ENV FIT_HOME=/opt/fit-framework
ENV FIT_JAVA_DIR=${FIT_HOME}/java
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk
ENV PATH=${FIT_JAVA_DIR}/bin:${JAVA_HOME}/bin:${PATH}
ENV JAVA_OPTS="-Xms256m -Xmx1024m -Djava.awt.headless=true"

# 安装系统依赖和OpenJDK 17
RUN apk add --no-cache \
    openjdk17-jdk \
    curl \
    wget \
    unzip \
    bash \
    netcat-openbsd \
    shadow \
    su-exec \
    ca-certificates \
    tzdata \
    nodejs \
    npm && \
    ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime

# 创建fit用户（Alpine使用不同的命令）
RUN addgroup -g 2000 -S fit && \
    adduser -u 2000 -S -G fit -h ${FIT_HOME} -s /bin/bash fit

# 创建目录结构
RUN mkdir -p ${FIT_HOME} ${FIT_JAVA_DIR}/{bin,conf,lib,plugins,logs,data,dynamic-plugins} && \
    chown -R fit:fit ${FIT_HOME}

# 复制并安装FIT Framework
COPY ${FIT_VERSION}.zip /tmp/
RUN cd /tmp && \
    unzip -q "${FIT_VERSION}.zip" && \
    cp -r ${FIT_VERSION}/* ${FIT_JAVA_DIR}/ && \
    rm -rf "${FIT_VERSION}.zip" ${FIT_VERSION} && \
    chown -R fit:fit ${FIT_JAVA_DIR} && \
    chmod +x ${FIT_JAVA_DIR}/bin/*

# 复制脚本文件
COPY --chmod=755 common/docker-entrypoint.sh /usr/local/bin/
COPY --chmod=755 common/healthcheck.sh /usr/local/bin/

# 切换到fit用户
USER fit

# 设置工作目录
WORKDIR ${FIT_JAVA_DIR}/dynamic-plugins

# 暴露端口
EXPOSE 8080 8090

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
    CMD healthcheck.sh

# 设置入口点
ENTRYPOINT ["docker-entrypoint.sh"]
CMD ["fit", "start"]
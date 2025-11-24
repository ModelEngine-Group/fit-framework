ARG FIT_VERSION=3.5.3
FROM debian:12-slim

# 重新声明ARG以在FROM之后使用
ARG FIT_VERSION=3.5.3

LABEL maintainer="FIT Framework Team <support@fit-framework.org>"
LABEL description="FIT Framework Base Image - Debian 12 (Stable)"
LABEL org.opencontainers.image.source="https://github.com/ModelEngine-Group/fit-framework"
LABEL org.opencontainers.image.version="${FIT_VERSION}"
LABEL org.opencontainers.image.title="FIT Framework (Debian)"

# 避免交互式安装
ENV DEBIAN_FRONTEND=noninteractive
ENV TZ=Asia/Shanghai

# 设置FIT Framework环境变量
ENV FIT_VERSION=${FIT_VERSION}
ENV FIT_HOME=/opt/fit-framework
ENV FIT_JAVA_DIR=${FIT_HOME}/java
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV PATH=${FIT_JAVA_DIR}/bin:${JAVA_HOME}/bin:${PATH}
ENV JAVA_OPTS="-Xms256m -Xmx1024m -Djava.awt.headless=true"

# 配置APT使用阿里云镜像源
RUN sed -i 's@//.*deb.debian.org@//mirrors.ustc.edu.cn@g' /etc/apt/sources.list.d/debian.sources 2>/dev/null || \
    sed -i 's@//.*deb.debian.org@//mirrors.ustc.edu.cn@g' /etc/apt/sources.list 2>/dev/null || true

# 安装系统依赖和OpenJDK 17
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    openjdk-17-jdk \
    curl \
    wget \
    unzip \
    procps \
    netcat-openbsd \
    ca-certificates \
    tzdata \
    nodejs \
    npm && \
    ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    rm -rf /var/lib/apt/lists/*

# 创建fit用户
RUN groupadd -r -g 2000 fit && \
    useradd -r -u 2000 -g fit -d ${FIT_HOME} -s /bin/bash -c "FIT Framework" fit

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
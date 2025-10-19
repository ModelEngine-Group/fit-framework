ARG FIT_VERSION=3.5.3
FROM amazonlinux:2023

# 重新声明ARG以在FROM之后使用
ARG FIT_VERSION=3.5.3

LABEL maintainer="FIT Framework Team <support@fit-framework.org>"
LABEL description="FIT Framework Base Image - Amazon Linux 2023 (Cloud Optimized)"
LABEL org.opencontainers.image.source="https://github.com/ModelEngine-Group/fit-framework"
LABEL org.opencontainers.image.version="${FIT_VERSION}"
LABEL org.opencontainers.image.title="FIT Framework (Amazon Linux)"

# 设置FIT Framework环境变量
ENV FIT_VERSION=${FIT_VERSION}
ENV FIT_HOME=/opt/fit-framework
ENV FIT_JAVA_DIR=${FIT_HOME}/java
ENV JAVA_HOME=/usr/lib/jvm/java-17-amazon-corretto
ENV PATH=${FIT_JAVA_DIR}/bin:${JAVA_HOME}/bin:${PATH}
ENV JAVA_OPTS="-Xms256m -Xmx1024m -Djava.awt.headless=true"

# 配置yum和网络超时设置
RUN yum install -y --allowerasing --setopt=timeout=300 --setopt=retries=3 \
        java-17-amazon-corretto-devel \
        curl \
        wget \
        unzip \
        nc \
        shadow-utils \
        ca-certificates \
        && yum clean all

# 创建fit用户
RUN groupadd -r -g 2000 fit && \
    useradd -r -u 2000 -g fit -d ${FIT_HOME} -s /bin/bash -c "FIT Framework" fit

# 创建目录结构
RUN mkdir -p ${FIT_HOME} ${FIT_JAVA_DIR}/{bin,conf,lib,plugins,logs,data,dynamic-plugins} && \
    chown -R fit:fit ${FIT_HOME}

# 下载并安装FIT Framework
RUN cd /tmp && \
    wget -q "https://github.com/ModelEngine-Group/fit-framework/releases/download/v${FIT_VERSION}/${FIT_VERSION}.zip" && \
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
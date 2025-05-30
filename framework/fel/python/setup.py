# -- encoding: utf-8 --
# Copyright (c) 2024 Huawei Technologies Co., Ltd. All Rights Reserved.
# This file is a part of the ModelEngine Project.
# Licensed under the MIT License. See License.txt in the project root for license information.
# ======================================================================================================================
"""
功 能：用于打包工程为 wheel 文件的脚本。
"""
# 打包方式：在 fel/python 目录下执行 python setup.py sdist bdist_wheel
import setuptools

_FEL_FRAMEWORK_VERSION = "0.0.1.dev"

setuptools.setup(
    name="fel",
    version=_FEL_FRAMEWORK_VERSION,
    author="fit",
    url="https://gitlab.huawei.com/fitlab/fit",
    packages=setuptools.find_packages(
        exclude=["*.tests", "*.tests.*", "tests.*", "tests", "_test.*", "_test"]),
    classifiers=[
        "Programming Language :: Python :: 3",
        "License :: Huawei license",
        "Operating System :: OS Independent",
    ],
    install_requires=["langchain==0.2.6",
                      "llama_index==0.10.47",
                      "requests==2.31"],
    python_requires='==3.9.11'
)

/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {Drawer} from 'antd';
import {CodePlayground} from '@/components/common/code/CodePlayground.jsx';
import '../custom-antd.css';
import {useEffect} from 'react';
import PropTypes from 'prop-types';

const drawerStyles = {
    body: {
        paddingTop: 0, paddingLeft: 0, paddingRight: 0, paddingBottom: 0, display: "flex"
    }
};

const TOP_ALIGN = '60px';

/**
 * 代码抽屉组件.
 *
 * @param getContainer 获取容器dom.
 * @param width 宽度.
 * @param open 打开抽屉.
 * @param onClose 抽屉关闭时的回调.
 * @param onConfirm 确认时的回调.
 * @param languages 语言列表.
 * @param editorConfig 编辑器配置. {language, code, suggestions}.
 * @param executeFunc 执行代码的方法.
 *
 * @return {JSX.Element}
 * @constructor
 */
const CodeDrawer = ({
                               container,
                               width,
                               open,
                               onClose,
                               onConfirm,
                               languages = ["python"],
                               editorConfig = {language: "python", code: "", suggestions: []},
                               executeFunc
                           }) => {
    const rootStyle = {};
    container && (rootStyle.position = "absolute");

    const _onClose = () => {
        onClose && onClose();
    };

    const _onConfirm = (v) => {
        _onClose();
        onConfirm && onConfirm(v);
    };

    useEffect(() => {
        // 设置ant-drawer-content-wrapper的下移60px
        const jadeCodeDrawer = document.querySelector('.jade-code-drawer');
        if (jadeCodeDrawer) {
            const parentElement = jadeCodeDrawer.closest('.ant-drawer-content-wrapper');
            if (parentElement) {
                parentElement.style.top = TOP_ALIGN;
            }
        }
    }, []);

    return (<>
        <Drawer className={"jade-code-drawer"}
                rootStyle={{...rootStyle}}
                width={width}
                mask={false}
                title={null}
                closable={false}
                onClose={_onClose}
                open={open}
                getContainer={container ? container : false}
                style={{borderRadius: 4}}
                styles={drawerStyles}
                destroyOnClose={true}
        >
            <CodePlayground width={width}
                            languages={languages}
                            editorConfig={editorConfig}
                            executeFunc={executeFunc}
                            onClose={_onClose}
                            onConfirm={_onConfirm}/>
        </Drawer>
    </>);
};

CodeDrawer.propTypes = {
    container: PropTypes.object.isRequired,
    width: PropTypes.number,
    open: PropTypes.bool,
    onClose: PropTypes.func,
    onConfirm: PropTypes.func,
    languages: PropTypes.array,
    editorConfig: PropTypes.object,
    executeFunc: PropTypes.func,
};

export {CodeDrawer};
/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import ApiInvokeIcon from '../asserts/icon-api-invoke.svg?react';
import {baseToolNodeDrawer} from '@/components/base/baseToolNodeDrawer.jsx';

/**
 * 工具调用节点绘制器
 *
 * @override
 */
export const toolInvokeNodeDrawer = (shape, div, x, y) => {
    const self = baseToolNodeDrawer(shape, div, x, y);
    self.type = "toolInvokeNodeDrawer";

    /**
     * @override
     */
    self.getHeaderIcon = () => {
        return (<>
            <ApiInvokeIcon className="jade-node-custom-header-icon"/>
        </>);
    };

    // 暂时屏蔽: 不需要查看版本信息功能.
    // /**
    //  * @override
    //  */
    // const getToolMenus = self.getToolMenus;
    // self.getToolMenus = () => {
    //     const menus = getToolMenus.apply(self);
    //     const uniqueName = shape.flowMeta.jober.entity.uniqueName;
    //     if (uniqueName) {
    //         menus.push({
    //             key: '4',
    //             label: "查看版本信息",
    //             action: () => {},
    //             // 子菜单被打开时调用.
    //             onOpen: () => {
    //                 self.refreshVersionInfo && self.refreshVersionInfo();
    //             },
    //             children: [{
    //                 key: "4-1",
    //                 label: (<>
    //                     <VersionInfoLoader shape={shape} drawer={self}/>
    //                 </>)
    //             }]
    //         });
    //     }
    //     return menus;
    // };

    return self;
};

/**
 * 加载VersionInfo所需数据.
 *
 * @param shape 图形对象.
 * @param drawer 绘制器.
 * @return {JSX.Element} 组件.
 * @constructor
 */
// const VersionInfoLoader = ({shape, drawer}) => {
//     const [data, setData] = useState({});
//
//     /**
//      * 刷新版本信息.
//      */
//     drawer.refreshVersionInfo = () => {
//         shape.fetchVersionInfo((data) => setData(data));
//     };
//
//     useEffect(() => {
//         shape.fetchVersionInfo((data) => setData(data));
//     }, []);
//
//     return (<>
//         <VersionInfo versionInfo={data}/>
//     </>);
// };
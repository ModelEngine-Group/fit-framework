/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import React, {createContext, forwardRef, useContext, useEffect, useImperativeHandle, useReducer, useRef} from 'react';
import './contentStyle.css';
import {ConfigProvider, Form} from 'antd';
import {useUpdateEffect} from '@/components/common/UseUpdateEffect.jsx';
import {EVENT_TYPE} from '@fit-elsa/elsa-core';
import PropTypes from 'prop-types';
import {SYSTEM_ACTION} from '@/common/Consts.js';

const DataContext = createContext(null);
const ShapeContext = createContext(null);
const DispatchContext = createContext(null);
const FormContext = createContext(null);

/**
 * 默认根节点，作为其他所有组件的容器.
 *
 * @param shape 图形.
 * @param component 待加载组件.
 * @param shapeStatus 图形状态.
 * @return {JSX.Element}
 * @constructor
 */
export const DefaultRoot = forwardRef(function ({shape, component, shapeStatus, borderPadding}, ref) {
    const [data, dispatch] = useReducer(component.reducers, component.getJadeConfig());
    const id = "react-root-" + shape.id;
    const [form] = Form.useForm();
    const domRef = useRef();

    // 对外暴露方法.
    useImperativeHandle(ref, () => {
        return {
            getData: () => {
                return data;
            },
            dispatch: (action) => {
                dispatch(action);
            }
        }
    });

    /**
     * 校验当前节点的form输入是否合法.
     *
     * @return Promise 校验结果
     */
    shape.validateForm = () => {
        return form.validateFields();
    };

    // 相当于 componentDidMount
    useEffect(() => {
        shape.observe();
        shape.page.addEventListener(EVENT_TYPE.FOCUSED_SHAPES_CHANGE, onFocusedShapeChange);
        shape.page.triggerEvent({type: "shape_rendered", value: {id: shape.id}});
        return () => {
            shape.page.removeEventListener(EVENT_TYPE.FOCUSED_SHAPES_CHANGE, onFocusedShapeChange);
        };
    }, []);

    const onFocusedShapeChange = () => {
        if (!domRef.current) {
            return;
        }
        const focusedShapes = shape.page.getFocusedShapes();
        if (focusedShapes.includes(shape)) {
            domRef.current.style.pointerEvents = focusedShapes.length > 1 ? "none" : "auto";
        } else {
            domRef.current.style.pointerEvents = "auto";
        }
    };

    // 第一次进来不会触发，第一次发生变化时才触发.
    useUpdateEffect(() => {
        if (data.jadeNodeConfigChangeIgnored) {
            return;
        }
        if (shape.hasError) {
            shape.validateForm().then(shape.offError);
        }
        shape.graph.dirtied(null, {action: SYSTEM_ACTION.JADE_NODE_CONFIG_CHANGE, shape: shape.id});
    }, [data]);

    // 当前是评估页面，并且runnable为false时，才出现遮盖层
    return (<>
        {shape.enableMask && <div className='jade-cover-level' style={{
            margin: -borderPadding,
            width: `calc(100% + ${2 * borderPadding}px)`,
            height: `calc(100% + ${2 * borderPadding}px)`,
        }}/>}
        <div id={id} style={{display: 'block'}} ref={domRef}>
            <Form form={form}
                  name={`form-${shape.id}`}
                  layout='vertical' // 设置全局的垂直布局
                  className={'jade-form'}
            >
                <DispatchContext.Provider value={dispatch}>
                    {shape.drawer.getHeaderComponent(data, shapeStatus)}
                    <FormContext.Provider value={form}>
                        <ShapeContext.Provider value={shape}>
                            <DataContext.Provider value={data}>
                                <div
                                  className='react-node-content'
                                  style={{borderRadius: `${shape.borderRadius}px`}}>
                                    {component.getReactComponents(shapeStatus, data)}
                                </div>
                            </DataContext.Provider>
                        </ShapeContext.Provider>
                    </FormContext.Provider>
                    {shape.drawer.getFooterComponent()}
                </DispatchContext.Provider>
            </Form>
        </div>
    </>);
});

DefaultRoot.propTypes = {
    shape: PropTypes.object,
    component: PropTypes.object,
    shapeStatus: PropTypes.object
};

export function useDataContext() {
    return useContext(DataContext);
}

export function useShapeContext() {
    return useContext(ShapeContext);
}

export function useDispatch() {
    return useContext(DispatchContext);
}

export function useFormContext() {
    return useContext(FormContext);
}
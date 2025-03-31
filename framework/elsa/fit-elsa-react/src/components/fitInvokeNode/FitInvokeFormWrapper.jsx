/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {useDataContext} from "@/components/DefaultRoot.jsx";
import {InvokeInput} from "@/components/common/InvokeInput.jsx";
import {InvokeOutput} from "@/components/common/InvokeOutput.jsx";
import FitInvokeService from "@/components/fitInvokeNode/FitInvokeService.jsx";

/**
 * FIT调用表单Wrapper
 *
 * @returns {JSX.Element} FIT调用表单Wrapper的DOM
 */
export default function FitInvokeFormWrapper() {
    const data = useDataContext();
    const inputData = data && data.inputParams;

    return (<>
        <InvokeInput inputData={inputData}/>
        <FitInvokeService/>
        <InvokeOutput/>
    </>);
}
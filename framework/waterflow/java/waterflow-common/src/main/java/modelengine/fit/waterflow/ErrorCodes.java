/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.waterflow;

/**
 * 异常类型枚举类
 *
 * @author 陈镕希
 * @since 1.0
 */
public enum ErrorCodes {
    // /** ------------ Generic Exception. From 10000000 to 10000999 --------------------- */
    /**
     * 入参为空
     */
    INPUT_PARAM_IS_EMPTY(10000000, "Input param is empty, empty param is {0}."),

    /**
     * 枚举类转换异常
     */
    ENUM_CONVERT_FAILED(10000001, "Cannot convert enum {0} by name: {1}."),

    /**
     * 实体对象未找到
     */
    ENTITY_NOT_FOUND(10000002, "Cannot find entity {0} by id: {1}."),

    /**
     * 入参不合法
     */
    INPUT_PARAM_IS_INVALID(10000003, "Input param is invalid, invalid param is {0}."),

    /**
     * 不符合预期
     */
    UN_EXCEPTED_ERROR(10000006, "unexpected error:{0}"),

    /**
     * 分页查询时Offset范围不正确。
     */
    PAGINATION_OFFSET_INVALID(10000008, "The range of offset is incorrect."),
    /**
     * 分页查询时Limit范围不正确。
     */
    PAGINATION_LIMIT_INVALID(10000009, "The range of limit is incorrect."),

    /**
     * 类型转换失败。
     */
    TYPE_CONVERT_FAILED(10000011, "Cannot convert type."),

    /** ------------ FlowEngines Exception 10007000-10007999 --------------------- */
    /**
     * 流程节点转换不支持操作
     */
    FLOW_NODE_CREATE_ERROR(10007000, "Processor can not be null during create flowable node."),

    /**
     * 流程节点不支持执行操作
     */
    FLOW_NODE_OPERATOR_NOT_SUPPORT(10007001, "Flow node with id: {0}, type: {1}, for operator [{2}] not supported."),

    /**
     * 流程没有开始节点
     */
    FLOW_HAS_NO_START_NODE(10007002, "Flow definition with id: {0} has no start node."),

    /**
     * 流程任务不支持执行操作
     */
    FLOW_TASK_OPERATOR_NOT_SUPPORT(10007003, "Flow task with name: {0}, type: {1}, are not supported."),

    /**
     * 流程执行错误，没有手动执行任务
     */
    FLOW_ENGINE_INVALID_MANUAL_TASK(10007004, "Flow engine executor error for invalid manual task."),

    /**
     * 流程执行错误，非法节点Id
     */
    FLOW_ENGINE_INVALID_NODE_ID(10007005, "Flow engine executor error for invalid node id: {0}."),

    /**
     * 流程定义解析失败
     */
    FLOW_ENGINE_PARSER_NOT_SUPPORT(10007010, "Flow engine parser not support {0} operator."),

    /**
     * 流程启动失败
     */
    FLOW_START_ERROR(10007011, "Flow status is invalid"),
    FLOW_EXECUTE_FITABLE_TASK_FAILED(10007012,
            "execute jober failed, jober name: {0}, jober type: {1}, fitables: {2}, errors: {3}"),

    /**
     * 流程执行不支持发送事件
     */
    FLOW_SEND_EVENT_NOT_SUPPORT(100070013, "Flow send event are not supported."),

    /**
     * 流程引擎数据库不支持该操作
     */
    FLOW_ENGINE_DATABASE_NOT_SUPPORT(100070014, "Operation :{0} is not supported."),

    /**
     * 流程定义更新失败
     */
    FLOW_DEFINITION_UPDATE_NOT_SUPPORT(100070015, "Flow status :{0} update not supported."),

    /**
     * 通过eventMetaId查询to节点失败
     */
    FLOW_FIND_TO_NODE_BY_EVENT_FAILED(100070016, "Find to node by event metaId :{0} failed."),
    FLOW_GRAPH_NOT_FOUND(100070017, "Flow graph id: {0} version: {1} not found."),
    FLOW_MODIFY_PUBLISHED_GRAPH(100070018,
            "graph data with id: {0} version: {1} has been published, can not be modified"),
    FLOW_ID_NOT_MATCH(100070019, "Flow id {0} does not match id {1} in data."),
    FLOW_GRAPH_DATA_PARSE_FAILED(100070020, "Parse graph data failed."),
    FLOW_HANDLE_SMART_FORM_FAILED(100070021, "Failed to handle the smart form task."),
    FLOW_TERMINATE_FAILED(100070022,
            "Failed to terminate flows by trace id {0}, when the flow status is error, archived or terminate."),

    /**
     * ElsaFlowsGraphRepo不支持该操作
     */
    ELSA_FLOW_GRAPH_NOT_SUPPORT(100070023, "Operation :{0} is not supported."),

    /**
     * DbFlowsGraphRepo 不支持该操作
     */
    NOT_SUPPORT(100070024, "Operation :{0} is not supported."),

    /**
     * 流程已存在
     */
    FLOW_ALREADY_EXIST(100070025, "flow already exist, {0}."),

    /**
     * 流程回调函数执行fitables失败
     */
    FLOW_EXECUTE_CALLBACK_FITABLES_FAILED(10007026,
            "Failed to execute callback, callback name: {0}, callback type: {1}, fitables: {2}, errors: {3}"),

    /**
     * 流程引擎OhScript语法错误
     */
    FLOW_ENGINE_OHSCRIPT_GRAMMAR_ERROR(100070024, "OhScript grammar error. Source Code: {0}"),

    /**
     * 流程引擎条件规则变量未找到
     */
    FLOW_ENGINE_CONDITION_RULE_PARSE_ERROR(100070025, "Condition rule parse error. Condition Rule: {0}"),

    /**
     * 找不到对应流程节点
     */
    FLOW_NODE_NOT_FOUND(100070024, "Flow node id {0} not found, flow meta id {1}, version {2}."),

    /**
     * flow节点任务数达到最大值
     */
    FLOW_NODE_MAX_TASK(100070024, "Flow node id {0} tasks over the limit."),

    /**
     * 流程自动任务特定异常重试失败
     */
    FLOW_RETRY_JOBER_UPDATE_DATABASE_FAILED(10007024, "Failed to update the retry record for retryJober, toBatch: {0}"),

    /**
     * 异步jober执行失败
     */
    FLOW_EXECUTE_ASYNC_JOBER_FAILED(10007027,
            "execute async jober failed."),

    /**
     * 流程执行过程出现异常
     */
    FLOW_ENGINE_EXECUTOR_ERROR(10007500, "Flow engine executor errors "
            + "stream id: {0}, node id: {1}, name: {2}, exception: {3}, errors: {4}."),

    /**
     * 流程执行过程通过ohscript调用fitable出现异常
     */
    FLOW_OHSCRIPT_INVOKE_FITABLE_ERROR(10007501,
            "Error code: 10007501, Flow engine executor ohscript code error when invoke fitable."),

    /**
     * 流程定义删除失败
     */
    FLOW_DEFINITION_DELETE_ERROR(10007502, "Error code: 10007502, Flow definition delete error"),

    /**
     * 流程出现系统错误
     */
    FLOW_SYSTEM_ERROR(10007503, "SYSTEM ERROR"),

    /**
     * 流程调用过程出现网络错误
     */
    FLOW_NETWORK_ERROR(10007504, "Error code: 10007504, Network error when Invoke fitable"),

    /**
     * 流程执行过程中不支持处理该类型
     */
    CONTEXT_TYPE_NOT_SUPPORT(10007505, "Not support this type."),

    /**
     * 中间节点连接线不合法
     */
    INVALID_STATE_NODE_EVENT_SIZE(10007518, "State node event size must be 1, please check config"),

    /**
     * 节点对应的event个数不合法
     */
    INVALID_EVENT_SIZE(10007506, "Error code: 10007506, Invalid event size."),

    /**
     * 流程storeJober调用过程执行出错
     */
    FLOW_STORE_JOBER_INVOKE_ERROR(10007507, "Flow store jober invoke error, tool id:{0}."),

    /**
     * 流程httpJober调用过程执行出错
     */
    FLOW_HTTP_JOBER_INVOKE_ERROR(10007508, "Flow http jober invoke error."),

    /**
     * 流程genericableJober调用过程执行出错
     */
    FLOW_GENERICALBE_JOBER_INVOKE_ERROR(10007509, "Flow genericable jober invoke error."),

    /**
     * 流程generalJober调用过程执行出错
     */
    FLOW_GENERAL_JOBER_INVOKE_ERROR(100075010, "Flow general jober invoke error."),

    /**
     * 条件节点执行出错
     */
    CONDITION_NODE_EXEC_ERROR(10007511, "Condition node executor error."),

    /**
     * 流程图保存失败
     */
    FLOW_GRAPH_SAVE_ERROR(10007512, "Flow graph save error, flow id: {0}, version: {1}."),

    /**
     * 流程图升级失败
     */
    FLOW_GRAPH_UPGRADE_ERROR(10007513, "Flow graph upgrade error, flow id: {0}, version: {1}."),

    /**
     * 流程校验失败
     */
    FLOW_VALIDATE_ERROR(10007514, "Flow graph validate error, detail: {0}"),

    /**
     * 流程节点个数不合法
     */
    INVALID_FLOW_NODE_SIZE(10007515, "Node size must more than 3, please check config"),

    /**
     * 开始节点连接线不合法
     */
    INVALID_START_NODE_EVENT_SIZE(10007516, "Start node event size must be 1, please check config"),

    /**
     * 连接线配置不合法
     */
    INVALID_EVENT_CONFIG(10007517, "Event config is invalid, event id: {0}"),
    ;

    private final Integer errorCode;

    private final String message;

    ErrorCodes(Integer errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "err " + this.errorCode + ": " + this.message;
    }
}

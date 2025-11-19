import ast
import hashlib
from pathlib import Path

TYPE_MAP = {
    # 基础类型
    "int": "integer",
    "float": "number",
    "str": "string",
    "bool": "boolean",
    "bytes": "string",
    # 容器类型
    "dict": "object",
    "Dict": "object",
    "Union": "object",
    "list": "array",
    "List": "array",
    "tuple": "array",
    "Tuple": "array",
    "set": "array",
    "Set": "array",
    # 特殊类型
    "None": "null",
}

type_errors = []

class ClassInfo:
    """存储自定义类的信息"""
    def __init__(self, name:str):
        self.name = name
        self.fields: dict[str, ast.AST] = {} # 字段名 -> 类型注解
        self.bases: list[str] = [] # 基类名

    def add_field(self, field_name: str, annotation: ast.AST):
        """添加字段"""
        self.fields[field_name] = annotation

    def add_base(self, base_name: str):
        """添加基类"""
        self.bases.append(base_name)


def parse_type(annotation, class_map):
    """解析参数类型"""
    global type_errors

    if annotation is None:
        type_errors.append("缺少类型注解（必须显式指定参数类型）")
        return "invalid", None, True
    
    elif isinstance(annotation, ast.Name):
        if annotation.id in TYPE_MAP: # 基础类型
            return TYPE_MAP[annotation.id], None, True
        elif annotation.id in class_map: # 自定义类型
            class_info = class_map[annotation.id]
            properties = {}
            for field_name, field_annotation in class_info.fields.items():
                field_type, field_items, _ = parse_type(field_annotation, class_map)
                
                if field_type == "invalid":
                    type_errors.append(f"类 {annotation.id} 的字段 {field_name} 类型无效")
                    return "invalid", None, True
                
                field_schema = {"type": field_type}
                if field_type == "array":
                    field_schema["items"] = field_items if field_items else {}
                elif field_type == "object" and field_items:
                    if "properties" in field_items:
                        field_schema["properties"] = field_items["properties"]
                    if "anyOf" in field_items:
                        field_schema["anyOf"] = field_items["anyOf"]
                    if "additionalProperties" in field_items:
                        field_schema["additionalProperties"] = field_items["additionalProperties"]
                
                properties[field_name] = field_schema
            return "object", {"type":"object", "properties":properties}, True
        else: # 未知类型
            type_errors.append(f"不支持的类型: {annotation.id}")
            return "invalid", None, True
    
    elif isinstance(annotation, ast.Constant) and annotation.value is None: # None
        return "null", None, False

    elif isinstance(annotation, ast.Subscript) and isinstance(annotation.value, ast.Name): # 容器类型
        container = annotation.value.id

        # List[int] 
        if container in ("list", "List"):
            item_type, item_schema, _ = parse_type(annotation.slice, class_map)
            if item_type == "invalid":
                type_errors.append(f"不支持的列表元素类型: {annotation.slice}")
                return "invalid", None, True
            items = item_schema if item_schema else {"type": item_type}
            return "array", items, True

        # Dict[str, int] → object
        elif container in ("dict", "Dict"):
            if isinstance(annotation.slice, ast.Tuple) and len(annotation.slice.elts) == 2:
                key_annot, value_annot = annotation.slice.elts
                key_type, _, _ = parse_type(key_annot, class_map)
                if key_type != "string":
                    type_errors.append(f"Dict 的键类型必须是 string，实际是 {key_type}")
                    return "invalid", None, True
                value_type, value_schema, _ = parse_type(value_annot, class_map)
                items = value_schema if value_schema else {"type": value_type}
                return "object", {"additionalProperties": items}, True

        # Optional[int]
        elif container == "Optional":
            inner_type, inner_items, _ = parse_type(annotation.slice, class_map)

            if inner_type == "invalid":
                type_errors.append(f"不支持的Optional类型: {annotation.slice}")
                return "invalid", None, False
            return inner_type, inner_items, False
        
        # Union[str, int]
        elif container == "Union":
            if isinstance(annotation.slice, ast.Tuple):
                schemas = []
                for elt in annotation.slice.elts:
                    elt_type, elt_items, _ = parse_type(elt, class_map)
                    if elt_type == "invalid":
                        type_errors.append(f"不支持的 Union 元素类型: {ast.dump(elt)}")
                        return "invalid", None, True
                    schema = {"type": elt_type}
                    if elt_items:
                        schema.update(elt_items)
                    schemas.append(schema)
                return "object", {"anyOf": schemas}, True
            else:
                inner_type, inner_items, _ = parse_type(annotation.slice, class_map)
                if inner_type == "invalid":
                    type_errors.append(f"不支持的 Union 类型: {ast.dump(annotation.slice)}")
                    return "invalid", None, True
                schema = {"type": inner_type}
                if inner_items:
                    schema.update(inner_items)
                return "object", {"anyOf": [schema]}, True
        
        # Tuple[str]
        elif container in ("tuple", "Tuple"):
            if isinstance(annotation.slice, ast.Tuple):
                tuple_items = []
                for elt in annotation.slice.elts:
                    item_type, item_schema, _ = parse_type(elt, class_map)
                    if item_type == "invalid":
                        type_errors.append(f"不支持的元组元素类型: {ast.dump(elt)}")
                        return "invalid", None, True
                    tuple_items.append(item_schema if item_schema else {"type": item_type})
                # 返回固定长度 tuple 的 items 列表
                return "array", {"items": tuple_items}, True
            else:
                # 单元素 Tuple
                item_type, item_schema, _ = parse_type(annotation.slice, class_map)
                if item_type == "invalid":
                    type_errors.append(f"不支持的元组元素类型: {ast.dump(annotation.slice)}")
                    return "invalid", None, True
                return "array", {"items": item_schema if item_schema else {"type": item_type}}, True
        
        # Set[int]
        elif container in ("set", "Set"):
            item_type, item_schema, _ = parse_type(annotation.slice, class_map)
            if item_type == "invalid":
                type_errors.append(f"不支持的集合元素类型: {annotation.slice}")
                return "invalid", None, True
            items = item_schema if item_schema else {"type": item_type}
            return "array", items, True
        
        else:
            type_errors.append(f"不支持的容器类型: {container}")
            return "invalid", None, True
            
    type_errors.append(f"无法识别的类型: {ast.dump(annotation)}")
    return "invalid", None, True


def parse_parameters(args, class_map):
    """解析函数参数"""
    properties = {}
    order = []
    required = []

    for arg in args.args:
        arg_name = arg.arg
        order.append(arg_name)
        arg_type, items, is_required = parse_type(arg.annotation, class_map)
        # 定义参数
        prop_def = {
            "defaultValue": "",
            "description": f"参数 {arg_name}",
            "name": arg_name,
            "type": arg_type,
        }
        if arg_type == "array" and items:
            if "items" in items:
                prop_def["items"] = items["items"]
            else:
                arr_items = {"type": items.get("type", "object")}
                if "properties" in items:
                    arr_items["properties"] = items["properties"]
                if "anyOf" in items:
                    arr_items["anyOf"] = items["anyOf"]
                if "additionalProperties" in items:
                    arr_items["additionalProperties"] = items["additionalProperties"]
                prop_def["items"] = arr_items

        if arg_type == "object" and items:
            if "properties" in items:
                prop_def["properties"] = items["properties"]
            if "anyOf" in items:
                prop_def["anyOf"] = items["anyOf"]
            if "additionalProperties" in items:
                prop_def["additionalProperties"] = items["additionalProperties"]

        prop_def["examples"] = ""
        prop_def["required"] = is_required

        properties[arg_name] = prop_def
        if is_required:
            required.append(arg_name)
    return properties, order, required


def parse_return(annotation, custom_classes):
    """解析返回值类型"""
    if not annotation:
        return {"type": "string", "convertor": ""}

    return_type, items, _ = parse_type(annotation, custom_classes)
    if return_type == "array":
        ret = {"type": "array"}
        if items:
            if "items" in items:
                ret["items"] = items["items"]
            else:
                arr_items = {"type": items.get("type", "object")}
                if isinstance(items, dict) and "properties" in items:
                    arr_items["properties"] = items["properties"]
                if isinstance(items, dict) and "anyOf" in items:
                    arr_items["anyOf"] = items["anyOf"]
                if isinstance(items, dict) and "additionalProperties" in items:
                    arr_items["additionalProperties"] = items["additionalProperties"]
                ret["items"] = arr_items
        ret["convertor"] = ""
        return ret

    elif return_type == "object":
        ret = {"type": "object"}
        if items and isinstance(items, dict):
            if "properties" in items:
                ret["properties"] = items["properties"]
            if "anyOf" in items:
                ret["anyOf"] = items["anyOf"]
            if "additionalProperties" in items:
                ret["additionalProperties"] = items["additionalProperties"]
        ret["convertor"] = ""
        return ret

    else:
        return {"type": return_type, "convertor": ""}


def parse_python_file(file_path: Path):
    """解析 *.py 文件, 提取 definition / tool """
    with open(file_path, "r", encoding="utf-8") as f:
        source = f.read()
    tree = ast.parse(source)

    py_name = file_path.stem
    definitions = []
    tool_groups = []
    class_map: dict[str, ClassInfo] = {} # 类名, 类信息
    # 收集自定义类
    for node in tree.body:
        if isinstance(node, ast.ClassDef):
            class_info = ClassInfo(node.name)
            for base in node.bases:
                if isinstance(base, ast.Name):
                    class_info.add_base(base.id)
            for subnode in node.body:
                if isinstance(subnode, ast.FunctionDef) and subnode.name == "__init__":
                    for arg in subnode.args.args[1:]: # 跳过 self
                        arg_name = arg.arg
                        class_info.add_field(arg_name, arg.annotation)
            class_map[node.name] = class_info
    # 解析函数定义
    for node in tree.body:
        if isinstance(node, ast.FunctionDef):
            func_name = node.name

            # 获取描述
            description = f"执行 {func_name} 方法"
            if node.body and isinstance(node.body[0], ast.Expr):
                expr_value = node.body[0].value
                # 同时判断两种字符串节点类型
                if isinstance(expr_value, (ast.Str, ast.Constant)):
                    # 提取字符串内容
                    docstring = expr_value.s if isinstance(expr_value, ast.Str) else expr_value.value
                    if isinstance(docstring, str):  # 确保是字符串类型
                        # 按换行分割，过滤空行并取第一行
                        lines = [line.strip() for line in docstring.split("\n") if line.strip()]
                        if lines:  # 若有有效行，取第一行作为描述
                            description = lines[0]

            # 装饰器取 genericableId, fitableId
            genericable_id, fitable_id = "", ""
            for deco in node.decorator_list:
                if isinstance(deco, ast.Call) and getattr(deco.func, "id", "") == "fitable":
                    if len(deco.args) >= 2:
                        genericable_id = getattr(deco.args[0], "s", "")
                        fitable_id = getattr(deco.args[1], "s", "")

            if not (genericable_id and fitable_id):
                continue

            # 解析参数和返回值
            properties, order, required = parse_parameters(node.args, class_map)
            return_schema = parse_return(node.returns, class_map)

            # definition schema
            definition_schema = {
                "name": func_name,
                "description": description,
                "parameters": {
                    "type": "object",
                    "properties": properties,
                    "required": required,
                },
                "order": order,
                "return": return_schema,
            }
            definitions.append({"schema": definition_schema})

            # tool schema
            tool_schema = {
                "name": func_name,
                "description": description,
                "parameters": {
                    "type": "object",
                    "properties": {
                        k: {
                            "name": v["name"],
                            "type": v["type"],
                            **({"items": v["items"]} if "items" in v else {}),
                            **({"properties": v["properties"]} if "properties" in v else {}),
                            **({"anyOf": v["anyOf"]} if "anyOf" in v else {}),
                            **({"additionalProperties": v["additionalProperties"]} if "additionalProperties" in v else {}),
                            "required": False,  # 工具里参数默认非必填
                        }
                        for k, v in properties.items()
                    },
                    "required": [],
                },
                "order": order,
                "return": {
                    "name": "",
                    "description": f"{func_name} 函数的返回值",
                    "type": return_schema["type"],
                    **({"items": return_schema["items"]} if "items" in return_schema else {}),
                    **({"properties": return_schema["properties"]} if "properties" in return_schema else {}),
                    **({"anyOf": return_schema["anyOf"]} if "anyOf" in return_schema else {}),
                    **({"additionalProperties": return_schema["additionalProperties"]} if "additionalProperties" in return_schema else {}),
                    "convertor": "",
                    "examples": "",
                },
            }

            # tool
            tool = {
                "namespace": func_name,
                "schema": tool_schema,
                "runnables": {
                    "FIT": {"genericableId": genericable_id, "fitableId": fitable_id}
                },
                "extensions": {"tags": ["FIT"]},
                "definitionName": func_name,
            }

            # toolGroup
            tool_group = {
                "name": f"Impl-{func_name}",
                "summary": "",
                "description": "",
                "extensions": {},
                "definitionGroupName": py_name,
                "tools": [tool],
            }
            tool_groups.append(tool_group)

    definition_group = {
                "name": py_name,
                "summary": "",
                "description": "",
                "extensions": {},
                "definitions": definitions,
            }
    return definition_group, tool_groups


def calculate_checksum(file_path: Path) -> str:
    """计算文件的 sha256 哈希值"""
    h = hashlib.sha256()
    with open(file_path, "rb") as f:
        for chunk in iter(lambda: f.read(8192), b""):
            h.update(chunk)
    return h.hexdigest()

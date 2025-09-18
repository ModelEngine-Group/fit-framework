import ast
import hashlib
from pathlib import Path

TYPE_MAP = {
    "int": "integer",
    "float": "number",
    "str": "string",
    "bool": "boolean",
    "dict": "object",
    "list": "array",
    "tuple": "array",
    "set": "array",
}

def parse_type(annotation):
    """解析参数类型"""
    if isinstance(annotation, ast.Name):
        return TYPE_MAP.get(annotation.id, "string"), None, True  # True=必填

    elif isinstance(annotation, ast.Subscript):
        if isinstance(annotation.value, ast.Name):
            container = annotation.value.id

            # List[int] / list[str]
            if container in ("list", "List"):
                item_type, _, _ = parse_type(annotation.slice)
                return "array", {"type": item_type}, True

            # Dict[str, int] → object
            elif container in ("dict", "Dict"):
                return "object", None, True

            # Optional[int]
            elif container == "Optional":
                inner_type, inner_items, _ = parse_type(annotation.slice)
                return inner_type, inner_items, False

            # Union[str, int] → 简化为 string
            elif container == "Union":
                return "string", None, True

    return "string", None, True


def parse_parameters(args):
    """解析函数参数"""
    properties = {}
    order = []
    required = []

    for arg in args.args:
        arg_name = arg.arg
        order.append(arg_name)
        arg_type = "string"
        items = None
        is_required = True
        if arg.annotation:
            arg_type, items, is_required = parse_type(arg.annotation)
        # 定义参数
        prop_def = {
            "defaultValue": "",
            "description": f"参数 {arg_name}",
            "name": arg_name,
            "type": arg_type,
            **({"items": items} if items else {}),
            "examples": "",
            "required": is_required,
        }
        properties[arg_name] = prop_def
        if is_required:
            required.append(arg_name)
    return properties, order, required


def parse_return(annotation):
    """解析返回值类型"""
    if not annotation:
        return {"type": "string", "convertor": ""}

    return_type, items, _ = parse_type(annotation)
    ret = {
        "type": return_type,
        **({"items": items} if items else {}),
        "convertor": ""
    }
    return ret


def parse_python_file(file_path: Path):
    """解析 *.py 文件, 提取 definition / tool """
    with open(file_path, "r", encoding="utf-8") as f:
        source = f.read()
    tree = ast.parse(source)

    py_name = file_path.stem
    definitions = []
    tool_groups = []

    for node in tree.body:
        if isinstance(node, ast.FunctionDef):
            func_name = node.name
            # 默认描述
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

            # 解析参数和返回值
            properties, order, required = parse_parameters(node.args)
            return_schema = parse_return(node.returns)

            # 装饰器取 genericableId, fitableId
            genericable_id, fitable_id = "", ""
            for deco in node.decorator_list:
                if isinstance(deco, ast.Call) and getattr(deco.func, "id", "") == "fitable":
                    if len(deco.args) >= 2:
                        genericable_id = getattr(deco.args[0], "s", "")
                        fitable_id = getattr(deco.args[1], "s", "")

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
                            "required": False,  # 工具里参数默认非必填
                        }
                        for k, v in properties.items()
                    },
                    "required": [],
                },
                "order": order,
                "return": {
                    "name": "",
                    "description": description or f"{func_name} 的返回值",
                    "type": return_schema["type"],
                    **({"items": return_schema["items"]} if "items" in return_schema else {}),
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
            if genericable_id and fitable_id:
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

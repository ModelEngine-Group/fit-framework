## 简介

`data_injection`的作用：将函数返回值指定字段的值写入缓存并替换为索引；或将函数入参指定字段被替换为索引的值从缓存中读取并替换。`data_injection`通过json_path的方式指定入参或返回值中需要被替换的字段；并且被替换的字段只支持`str`和`bytes`类型。

## 效果演示

采用两个具有不同功能的`fitable`对于功能进行演示：

1. `user_plugin_file_read`：文件读取接口，功能为模拟文件读取，无输入参数，返回值类型为`List[Flowdata]`
2. `political_word_cleaner_plugin`：政治敏感词清洗接口，功能为清洗内容中的政治敏感词，具有一个类型为`List[Flowdata]`的入参，返回值类型为`List[Flowdata]`

<span style="color:red;">其中第一个`fitable`的输出将直接作为第二个`fitable`的输入。</span>

### 使用data_injection前

在未使用`data_injection`注解时，两个`fitable`的输出分别为：

```json
[
	{
		"businessData": {},
		"passData": {
			"data": "",
			"text": "共产党，火药配方",
			"meta": {
				"meta_key1": "meta_value1",
				"meta_key2": "meta_value2"
			}
		},
		"contextData": {}
	}
]
```

```json
[
	{
		"businessData": {},
		"passData": {
			"data": "",
			"text": "***，火药配方",
			"meta": {
				"meta_key1": "meta_value1",
				"meta_key2": "meta_value2"
			}
		},
		"contextData": {}
	}
]
```

### 使用data_injection后

在使用`data_injection`注解后，两个`fitable`分别添加注解`@data_injection(None, "[*].passData.text")`和`@data_injection({0: "[*].passData.text"}, None)`，两个`fitable`的输出分别为：

```json
[
	{
		"businessData": {},
		"passData": {
			"data": "",
			"text": "00000000@PyCharmHakuna",
			"meta": {
				"meta_key1": "meta_value1",
				"meta_key2": "meta_value2"
			}
		},
		"contextData": {}
	}
]
```

```json
[
	{
		"businessData": {},
		"passData": {
			"data": "",
			"text": "***，火药配方",
			"meta": {
				"meta_key1": "meta_value1",
				"meta_key2": "meta_value2"
			}
		},
		"contextData": {}
	}
]
```

其中第一个`fitable`的输入中每个元素的`passData`的`text`字段被替换为一个索引，而第一个`fitable`替换了字段后的输出结果作为第二个`fitable`的输入后仍然可以获得正确的结果。

## 使用方式

### 指定需要有字段被替换的入参或返回值

被`data_injection`装饰的函数可以包含多个入参和一个返回值，而`data_injection`装饰本身有两个入参，分别用于指定被注解函数入参和返回值的替换规则，第一个入参类型为`Dict[int, 替换规则]`其中`Dict`的`key`表示被装饰函数的第几个参数（从 0 开始计数），而`Dict`的`value`表示字段替换规则；第二个入参为`替换规则`，仅包括字段替换规则本身。

> `data_injection`的两个入参类型不同是因为被`data_injection`装饰的函数可能会有多个入参但至多有一个返回值。

如果你不希望对于任何入参的某个字段进行替换，那么`data_injection`中的第一个入参需要设定为`None`；相应的，如果不希望对于返回值的某个字段进行替换，那么`data_injection`中的第二个入参需要设定为`None`。

总结：`data_injection`可以指定被装饰函数的任意参数和返回值的替换规则，也可以指定不对于任何参数和返回值进行替换。

示例：

```python
# 为索引为 1 的入参 value2 指定替换规则 1；并为返回值指定替换规则 2
@fitable("genericable_id_of_your_fitable", "fitable_id_of_your_fitable")
@data_injection({1: 替换规则1}, 替换规则2)  
def your_fitable(value1: TypeOfValue1, value2: TypeOfValue2) -> TypeOfReturn:
    # your implement

# 为索引为 0 的入参 value1 指定替换规则 1；并为索引为 1 的入参 value2 指定替换规则 1；不对于返回值指定替换规则
@fitable("genericable_id_of_your_fitable", "fitable_id_of_your_fitable")
@data_injection({0: 替换规则1, 1: 替换规则2}, None)
def your_fitable(value1: TypeOfValue1, value2: TypeOfValue2) -> TypeOfReturn:
    # your implement

# 为返回值指定替换规则 2；不对于任何入参指定替换规则
@fitable("genericable_id_of_your_fitable", "fitable_id_of_your_fitable")
@data_injection(None, 替换规则1)  
def your_fitable(value1: TypeOfValue1, value2: TypeOfValue2) -> TypeOfReturn:
    # your implement

# 不对于任何入参和返回值指定替换规则，等效于不加 data_injection 注解
@fitable("genericable_id_of_your_fitable", "fitable_id_of_your_fitable")
@data_injection(None, None)  
def your_fitable(value1: TypeOfValue1, value2: TypeOfValue2) -> TypeOfReturn:
    # your implement
```

### 指定需要被替换的字段

`data_injection`的替换规则采用 json path 表示，其介绍见于[JSON Path 语法介绍和使用场景](https://developer.aliyun.com/article/1117317)，但是目前`data_injection`**仅支持以下语法**：

- `field_name`：表示某个字段
- `[*]`：表示列表中所有元素
- `*`：表示所有字段

示例：

```python
class Location:
    def __init__(self, country: str, province: str):
        self.country: str = country
        self.province: str = province

class Person:
    def __init__(self, name: str, location: Location, hobbies: List[str]):
        self.name: str = name
        self.location: Location = location
        self.hobbies: List[str] = hobbies


".name"              # name 字段
".location.country"  # location 字段的 country 字段
".location.*"        # location 字段的所有字段
".hobbies[*]"        # hobbies 字段的每个元素
```

`data_injection`所装饰的函数的每个参数或返回值都可以**指定一个或多个被替换的字段**。如果希望指定一个被替换的字段，替换规则的类型可以为`str`；如果希望指定多个被替换的字段，则替换规则类型为`List[str]`，即相当于**单个替换规则的集合**。

### 近端缓存功能的内存大小限制机制使用指导

```python
from fitframework.api.decorators import fit

@fit("com.huawei.fit.bigdata.cache.validate.capacity")
def _validate_capacity(_: int) -> None:
    pass

def some_function_need_validate(to_allocate_memory: int):
    _validate_capacity(to_allocate_memory)
   ....
```

注意`to_allocate_memory`值为输入文件的大小，单位为`byte`。泛服务`com.huawei.fit.bigdata.cache.validate.capacity`定义的逻辑为：判断输入文件是否会导致系统内存溢出，避免文件直接加在导致环境内存溢出。

## 综合示例

```python
@fitable(generic_id='genericable_id_of_your_fitable', fitable_id='fitable_id_of_your_fitable')
@data_injection(None, ["[*].passData.text", "[*].passData.data"]) # 不对于任何入参替换，替换返回值中的两个字段
def your_fitable(flow_data_list: List[FlowData]) -> List[FlowData]:
    # your implement


@fitable(generic_id='genericable_id_of_your_fitable', fitable_id='user_plugin_file_read')
@data_injection({0: ["[*].passData.text", "[*].passData.data"]}, None) # 不对于返回值进行替换，替换入参中的两个字段
def your_fitable(flow_data_list: List[FlowData]) -> List[FlowData]:
    # your implement

@fitable(generic_id='genericable_id_of_your_fitable', fitable_id='user_plugin_file_read')
@data_injection({0: "[*].passData.text"}, "[*].passData.text") # 分别替换入参和返回值中的一个字段
def your_fitable(flow_data_list: List[FlowData]) -> List[FlowData]:
    # your implement
```
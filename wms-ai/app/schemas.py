"""统一返回信封，对齐 Java com.wms.common.Result。"""
from typing import Any

from pydantic import BaseModel


class Result(BaseModel):
    code: int = 200
    msg: str = "成功"
    total: int = 0
    data: Any = None

    @staticmethod
    def suc(data: Any = None, total: int = 0) -> "Result":
        return Result(code=200, msg="成功", total=total, data=data)

    @staticmethod
    def fail(msg: str = "失败") -> "Result":
        return Result(code=400, msg=msg, total=0, data=None)

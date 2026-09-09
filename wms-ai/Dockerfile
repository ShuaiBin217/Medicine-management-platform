FROM python:3.11-slim

WORKDIR /app

# 先装依赖（利用缓存层）
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple

# 复制代码与知识库文档
COPY app/ ./app/
COPY main.py .
COPY knowledge-base/ ./knowledge-base/

EXPOSE 8091

ENV AI_PORT=8091 \
    KB_PATH=knowledge-base

CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8091"]

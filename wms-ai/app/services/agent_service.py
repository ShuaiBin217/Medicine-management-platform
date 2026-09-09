"""AI 服务：对齐 com.wms.ai.LangChain4jConfig + WmsAiService。

LangChain4j → LangChain Python 映射：
- OpenAiChatModel(DeepSeek)                → ChatOpenAI(api_key, base_url, model, temperature, max_tokens)
- @SystemMessage                           → ChatPromptTemplate 的 ("system", SYSTEM_PROMPT)
- MessageWindowChatMemory(maxMessages=20)  → MessagesPlaceholder("history") + 前端 history 截窗 20 条
- AiServices.builder(...).tools(wmsTools)  → llm.bind_tools + create_tool_calling_agent + AgentExecutor
"""
from langchain.agents import AgentExecutor, create_tool_calling_agent
from langchain_core.messages import AIMessage, HumanMessage
from langchain_core.prompts import ChatPromptTemplate, MessagesPlaceholder
from langchain_openai import ChatOpenAI

from app.config import Settings
from app.prompts.system import SYSTEM_PROMPT
from app.tools import all_tools


def build_agent_executor(settings: Settings) -> AgentExecutor:
    """构造 AgentExecutor，等价 LangChain4j 的 AiServices.builder(...).tools(wmsTools).build()。"""
    llm = ChatOpenAI(
        api_key=settings.deepseek_api_key,
        base_url=settings.deepseek_base_url,  # DeepSeek
        model=settings.deepseek_model,
        temperature=settings.deepseek_temperature,
        max_tokens=settings.deepseek_max_tokens,
    )
    prompt = ChatPromptTemplate.from_messages([
        ("system", SYSTEM_PROMPT),
        MessagesPlaceholder("history"),
        ("human", "{input}"),
        MessagesPlaceholder("agent_scratchpad"),
    ])
    agent = create_tool_calling_agent(llm, all_tools, prompt)
    return AgentExecutor(
        agent=agent,
        tools=all_tools,
        verbose=False,
        max_iterations=10,
        handle_parsing_errors=True,
    )


def build_history(history_list: list[dict], max_msgs: int = 20) -> list:
    """从前端 history 重建消息窗口，对齐 MessageWindowChatMemory(maxMessages=20)。

    修正 Java 旧版的设计缺陷：Java 用单一共享 MessageWindowChatMemory（所有用户共享记忆）。
    Python 改用前端传来的 history 重建上下文，每个会话独立。
    """
    msgs = []
    for h in (history_list or [])[-max_msgs:]:
        role = h.get("role")
        content = h.get("content")
        if not content:
            continue
        if role == "user":
            msgs.append(HumanMessage(content=content))
        else:
            msgs.append(AIMessage(content=content))
    return msgs


# —— 模块级单例 ——
_executor: AgentExecutor | None = None


def get_agent_executor() -> AgentExecutor:
    global _executor
    if _executor is None:
        from app.config import get_settings
        _executor = build_agent_executor(get_settings())
    return _executor


def reset_agent_executor() -> None:
    """供配置变更或测试时重建。"""
    global _executor
    _executor = None

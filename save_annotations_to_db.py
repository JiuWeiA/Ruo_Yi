import sqlite3
from datetime import datetime
from typing import List, Dict


def save_annotations_to_db(annotations: List[Dict], text_id: str, db_path: str = "complication.db"):
    """
    将标注结果保存到数据库中

    参数:
    - annotations: 来自 ComplicationAnnotator 的 validated_anns 结果
    - text_id: 当前文本编号（可由前端提供）
    - db_path: SQLite 数据库路径，默认当前目录下
    """
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()

    # 创建表（如果不存在）
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS complications (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            text_id TEXT,
            entity_type TEXT,
            entity_text TEXT,
            start INTEGER,
            end INTEGER,
            create_time TEXT
        )
    """)

    # 插入标注数据
    now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    for ann in annotations:
        cursor.execute("""
            INSERT INTO complications (text_id, entity_type, entity_text, start, end, create_time)
            VALUES (?, ?, ?, ?, ?, ?)
        """, (text_id, ann["type"], ann["text"], ann["start"], ann["end"], now))

    conn.commit()
    conn.close()

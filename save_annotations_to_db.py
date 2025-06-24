import sqlite3
from datetime import datetime
from typing import List, Dict


def save_annotations_to_db(annotations: List[Dict], text_id: str, db_path: str = "complication.db"):
    """
    ����ע������浽���ݿ���

    ����:
    - annotations: ���� ComplicationAnnotator �� validated_anns ���
    - text_id: ��ǰ�ı���ţ�����ǰ���ṩ��
    - db_path: SQLite ���ݿ�·����Ĭ�ϵ�ǰĿ¼��
    """
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()

    # ��������������ڣ�
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

    # �����ע����
    now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    for ann in annotations:
        cursor.execute("""
            INSERT INTO complications (text_id, entity_type, entity_text, start, end, create_time)
            VALUES (?, ?, ?, ?, ?, ?)
        """, (text_id, ann["type"], ann["text"], ann["start"], ann["end"], now))

    conn.commit()
    conn.close()

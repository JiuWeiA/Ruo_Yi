def get_complication_statistics(db_path: str = "complication.db") -> Dict[str, int]:
    """
    ͳ�Ƹ��ಢ��֢�ı�ע����������״ͼʹ��

    ����:
    {
        "pleural": 12,
        "infection": 7,
        "pneumothorax": 5
    }
    """
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()

    stats = {
        "pleural": 0,
        "infection": 0,
        "pneumothorax": 0
    }

    for entity_type in stats.keys():
        cursor.execute("SELECT COUNT(*) FROM complications WHERE entity_type = ?", (entity_type,))
        stats[entity_type] = cursor.fetchone()[0]

    conn.close()
    return stats

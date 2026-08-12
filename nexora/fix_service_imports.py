#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
为所有 ServiceImpl 添加 StringTools import
"""
import os

base_path = r"D:\dev\ai\k12\nexora\nexora-java\nexora-common\src\main\java\com\nexora\service\impl"

count = 0
for filename in os.listdir(base_path):
    if not filename.endswith('.java'):
        continue
    
    filepath = os.path.join(base_path, filename)
    
    with open(filepath, 'r', encoding='utf-8') as f:
        lines = f.readlines()
    
    # 检查是否已经包含 StringTools import
    has_string_tools = any('import com.nexora.utils.StringTools;' in line for line in lines)
    if has_string_tools:
        print("OK {} - Already has StringTools import".format(filename))
        continue
    
    # 查找最后一个 import 语句
    insert_idx = -1
    for i, line in enumerate(lines):
        if line.strip().startswith('import '):
            insert_idx = i
    
    if insert_idx >= 0:
        # 在最末个 import 后添加 StringTools import
        new_import_line = 'import com.nexora.utils.StringTools;\n'
        lines.insert(insert_idx + 1, new_import_line)
        
        with open(filepath, 'w', encoding='utf-8') as f:
            f.writelines(lines)
        
        print("OK {} - Added StringTools import".format(filename))
        count += 1

print("\n共处理 {} 个文件".format(count))
print("SUCCESS!")

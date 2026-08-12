#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
批量添加 DateUtil import 到 PO 类
"""
import os
import re

def fix_po_imports():
    """为每个 PO 类添加 DateUtil import"""
    base_path = r"D:\dev\ai\k12\nexora\nexora-java\nexora-common\src\main\java\com\nexora\entity\po"
    
    count = 0
    for filename in os.listdir(base_path):
        if not filename.endswith('.java'):
            continue
        
        filepath = os.path.join(base_path, filename)
        
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # 检查是否已经包含 DateUtil import
        if 'import com.nexora.utils.DateUtil;' in content:
            print("OK {} - Already has DateUtil import".format(filename))
            continue
        
        # 查找 DateTimePatternEnum import 行，在其后添加 DateUtil import
        old_pattern = r'(import\s+com\.nexora\.entity\.enums\.DateTimePatternEnum;\n)'
        new_import = 'import com.nexora.utils.DateUtil;\n'
        
        if re.search(old_pattern, content):
            new_content = re.sub(old_pattern, r'\1' + new_import, content)
            
            with open(filepath, 'w', encoding='utf-8') as f:
                f.write(new_content)
            
            print("OK {} - Added DateUtil import".format(filename))
            count += 1
        else:
            print("? {} - No DateTimePatternEnum import (skip)".format(filename))
    
    print(f"\n共处理 {count} 个文件")


def fix_service_imports():
    """为 ServiceImpl 添加 StringTools import"""
    base_path = r"D:\dev\ai\k12\nexora\nexora-java\nexora-common\src\main\java\com\nexora\service\impl"
    
    count = 0
    for filename in os.listdir(base_path):
        if not filename.endswith('.java'):
            continue
        
        filepath = os.path.join(base_path, filename)
        
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # 检查是否已经包含 StringTools import
        if 'import com.nexora.utils.StringTools;' in content:
            print("OK {} - Already has StringTools import".format(filename))
            continue
        
        # 在最后一个 import 语句后添加 StringTools import
        last_import_pattern = r'(import\s+.*?;)(\s*\n\s*//|\s*\n\s*/\*|\s*\npackage|\s*\n$$)'
        new_import = 'import com.nexora.utils.StringTools;\n'
        
        # 简单方法：找到 package 声明前的所有 import，在最末个 import 后添加
        lines = content.split('\n')
        import_lines = []
        insert_idx = 0
        
        for i, line in enumerate(lines):
            if line.strip().startswith('import '):
                import_lines.append((i, line))
            elif line.strip() and not line.strip().startswith('//') and not line.strip().startswith('*'):
                break
            insert_idx = i
        
        if import_lines:
            last_import_idx = import_lines[-1][0]
            lines.insert(last_import_idx + 1, new_import.strip())
            new_content = '\n'.join(lines)
            
            with open(filepath, 'w', encoding='utf-8') as f:
                f.write(new_content)
            
            print("OK {} - Added StringTools import".format(filename))
            count += 1
    
    print("\n共处理 {} 个文件".format(count))


if __name__ == '__main__':
    print("=" * 60)
    print("修复 PO 类中的 DateUtil import...")
    print("=" * 60)
    fix_po_imports()
    
    print("\n" + "=" * 60)
    print("修复 Service 类中的 StringTools import...")
    print("=" * 60)
    fix_service_imports()
    
    print("\n" + "=" * 60)
    print("✅ 所有修复已完成!")
    print("=" * 60)

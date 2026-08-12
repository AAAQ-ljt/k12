#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
测试登录 API
"""
import requests
import json

url = "http://localhost:6061/api/account/login"
data = {
    "username": "admin",
    "password": "123456"
}

print(f"POST {url}")
print(f"Data: {json.dumps(data, indent=2)}")
print()

try:
    # 方式 1: form-data (Spring @RequestParam)
    response = requests.post(url, data=data)
    print("=== Form Data ===")
    print(f"Status: {response.status_code}")
    print(f"Response: {response.text[:500]}")
    
except Exception as e:
    print(f"Error: {e}")

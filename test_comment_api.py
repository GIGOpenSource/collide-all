#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
CommentController接口测试脚本
测试评论相关的API接口功能
"""

import requests
import json
import time
from datetime import datetime

class CommentAPITester:
    def __init__(self, base_url="http://localhost:8080"):
        self.base_url = base_url
        self.session = requests.Session()
        self.session.headers.update({
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        })
        
    def print_separator(self, title):
        """打印分隔符"""
        print("\n" + "="*60)
        print(f" {title} ")
        print("="*60)
    
    def print_response(self, response, test_name):
        """打印响应结果"""
        print(f"\n【{test_name}】")
        print(f"状态码: {response.status_code}")
        print(f"响应时间: {response.elapsed.total_seconds():.3f}秒")
        
        try:
            result = response.json()
            print(f"响应数据: {json.dumps(result, ensure_ascii=False, indent=2)}")
        except:
            print(f"响应文本: {response.text}")
    
    def test_list_comments(self):
        """测试评论列表查询接口"""
        self.print_separator("测试评论列表查询接口")
        
        # 测试用例1: 查询所有评论
        print("\n1. 查询所有评论")
        response = self.session.get(f"{self.base_url}/api/v1/comments/list")
        self.print_response(response, "查询所有评论")
        
        # 测试用例2: 按内容ID查询评论
        print("\n2. 按内容ID查询评论")
        params = {
            'targetId': 101,
            'commentType': 'CONTENT',
            'currentPage': 1,
            'pageSize': 10
        }
        response = self.session.get(f"{self.base_url}/api/v1/comments/list", params=params)
        self.print_response(response, "按内容ID查询评论")
        
        # 测试用例3: 按用户ID查询评论
        print("\n3. 按用户ID查询评论")
        params = {
            'userId': 101,
            'currentPage': 1,
            'pageSize': 5
        }
        response = self.session.get(f"{self.base_url}/api/v1/comments/list", params=params)
        self.print_response(response, "按用户ID查询评论")
        
        # 测试用例4: 查询根评论
        print("\n4. 查询根评论")
        params = {
            'parentId': 0,
            'currentPage': 1,
            'pageSize': 10
        }
        response = self.session.get(f"{self.base_url}/api/v1/comments/list", params=params)
        self.print_response(response, "查询根评论")
        
        # 测试用例5: 查询回复评论
        print("\n5. 查询回复评论")
        params = {
            'parentId': 101,
            'currentPage': 1,
            'pageSize': 10
        }
        response = self.session.get(f"{self.base_url}/api/v1/comments/list", params=params)
        self.print_response(response, "查询回复评论")
        
        # 测试用例6: 关键词搜索
        print("\n6. 关键词搜索")
        params = {
            'keyword': '精彩',
            'currentPage': 1,
            'pageSize': 10
        }
        response = self.session.get(f"{self.base_url}/api/v1/comments/list", params=params)
        self.print_response(response, "关键词搜索")
    
    def test_create_comment(self):
        """测试创建评论接口"""
        self.print_separator("测试创建评论接口")
        
        # 测试用例1: 创建根评论
        print("\n1. 创建根评论")
        data = {
            "commentType": "CONTENT",
            "targetId": 101,
            "parentCommentId": 0,
            "content": "这是一条新创建的根评论，测试接口功能！",
            "userId": 101,
            "userNickname": "测试用户1",
            "userAvatar": "https://example.com/avatar1.jpg",
            "status": "NORMAL"
        }
        response = self.session.post(f"{self.base_url}/api/v1/comments/create", json=data)
        self.print_response(response, "创建根评论")
        
        # 保存新创建的评论ID用于后续测试
        if response.status_code == 200:
            result = response.json()
            if result.get('success') and result.get('data'):
                new_comment_id = result['data']['id']
                print(f"新创建的评论ID: {new_comment_id}")
                
                # 测试用例2: 创建回复评论
                print("\n2. 创建回复评论")
                reply_data = {
                    "commentType": "CONTENT",
                    "targetId": 101,
                    "parentCommentId": new_comment_id,
                    "content": "这是对新评论的回复！",
                    "userId": 102,
                    "userNickname": "测试用户2",
                    "userAvatar": "https://example.com/avatar2.jpg",
                    "replyToUserId": 101,
                    "replyToUserNickname": "测试用户1",
                    "replyToUserAvatar": "https://example.com/avatar1.jpg",
                    "status": "NORMAL"
                }
                response = self.session.post(f"{self.base_url}/api/v1/comments/create", json=reply_data)
                self.print_response(response, "创建回复评论")
        
        # 测试用例3: 创建动态评论
        print("\n3. 创建动态评论")
        dynamic_data = {
            "commentType": "DYNAMIC",
            "targetId": 101,
            "parentCommentId": 0,
            "content": "这是一条动态评论！",
            "userId": 103,
            "userNickname": "测试用户3",
            "userAvatar": "https://example.com/avatar3.jpg",
            "status": "NORMAL"
        }
        response = self.session.post(f"{self.base_url}/api/v1/comments/create", json=dynamic_data)
        self.print_response(response, "创建动态评论")
        
        # 测试用例4: 创建评论（验证参数）
        print("\n4. 创建评论（缺少必填参数）")
        invalid_data = {
            "commentType": "CONTENT",
            "targetId": 101,
            "content": "缺少用户ID的评论"
        }
        response = self.session.post(f"{self.base_url}/api/v1/comments/create", json=invalid_data)
        self.print_response(response, "创建评论（缺少必填参数）")
    
    def test_error_cases(self):
        """测试错误情况"""
        self.print_separator("测试错误情况")
        
        # 测试用例1: 无效的评论类型
        print("\n1. 无效的评论类型")
        data = {
            "commentType": "INVALID_TYPE",
            "targetId": 101,
            "parentCommentId": 0,
            "content": "测试无效评论类型",
            "userId": 101,
            "userNickname": "测试用户1",
            "userAvatar": "https://example.com/avatar1.jpg"
        }
        response = self.session.post(f"{self.base_url}/api/v1/comments/create", json=data)
        self.print_response(response, "无效的评论类型")
        
        # 测试用例2: 不存在的目标ID
        print("\n2. 不存在的目标ID")
        data = {
            "commentType": "CONTENT",
            "targetId": 99999,
            "parentCommentId": 0,
            "content": "测试不存在的目标ID",
            "userId": 101,
            "userNickname": "测试用户1",
            "userAvatar": "https://example.com/avatar1.jpg"
        }
        response = self.session.post(f"{self.base_url}/api/v1/comments/create", json=data)
        self.print_response(response, "不存在的目标ID")
        
        # 测试用例3: 空内容
        print("\n3. 空内容")
        data = {
            "commentType": "CONTENT",
            "targetId": 101,
            "parentCommentId": 0,
            "content": "",
            "userId": 101,
            "userNickname": "测试用户1",
            "userAvatar": "https://example.com/avatar1.jpg"
        }
        response = self.session.post(f"{self.base_url}/api/v1/comments/create", json=data)
        self.print_response(response, "空内容")
    
    def test_performance(self):
        """测试性能"""
        self.print_separator("测试性能")
        
        # 测试并发查询
        print("\n1. 并发查询测试")
        import threading
        
        def query_comments():
            response = self.session.get(f"{self.base_url}/api/v1/comments/list")
            return response.elapsed.total_seconds()
        
        threads = []
        results = []
        
        for i in range(5):
            thread = threading.Thread(target=lambda: results.append(query_comments()))
            threads.append(thread)
            thread.start()
        
        for thread in threads:
            thread.join()
        
        avg_time = sum(results) / len(results)
        print(f"并发查询平均响应时间: {avg_time:.3f}秒")
        print(f"最快响应时间: {min(results):.3f}秒")
        print(f"最慢响应时间: {max(results):.3f}秒")
    
    def run_all_tests(self):
        """运行所有测试"""
        print("开始测试CommentController接口...")
        print(f"测试时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
        print(f"测试地址: {self.base_url}")
        
        try:
            # 测试评论列表查询
            self.test_list_comments()
            
            # 测试创建评论
            self.test_create_comment()
            
            # 测试错误情况
            self.test_error_cases()
            
            # 测试性能
            self.test_performance()
            
        except Exception as e:
            print(f"\n测试过程中发生错误: {e}")
        
        print("\n" + "="*60)
        print(" 测试完成 ")
        print("="*60)

def main():
    """主函数"""
    # 可以修改base_url来测试不同的环境
    tester = CommentAPITester("http://localhost:8080")
    
    # 运行所有测试
    tester.run_all_tests()

if __name__ == "__main__":
    main()

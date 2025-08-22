#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
ContentController根据contentId查询详情API测试脚本
测试新增的内容详情查询接口
"""

import requests
import json

# 配置
BASE_URL = "http://localhost:8080"  # 根据实际部署情况修改
API_ENDPOINT = f"{BASE_URL}/api/v1/content/core"

def test_get_content_by_id():
    """测试根据ID查询内容详情"""
    print("=== 测试根据ID查询内容详情 ===")
    
    # 测试存在的内容ID（根据实际情况修改）
    content_id = 107
    
    url = f"{API_ENDPOINT}/{content_id}"
    
    response = requests.get(url)
    print(f"状态码: {response.status_code}")
    
    if response.status_code == 200:
        result = response.json()
        print(f"响应: {json.dumps(result, indent=2, ensure_ascii=False)}")
        
        if result.get("success"):
            print("✅ 内容详情查询成功")
            data = result.get("data", {})
            print(f"内容ID: {data.get('id')}")
            print(f"标题: {data.get('title')}")
            print(f"描述: {data.get('description', '')[:100]}...")
            print(f"内容类型: {data.get('contentType')}")
            print(f"作者: {data.get('authorNickname')} (ID: {data.get('authorId')})")
            print(f"分类: {data.get('categoryName')} (ID: {data.get('categoryId')})")
            print(f"状态: {data.get('status')}")
            print(f"审核状态: {data.get('reviewStatus')}")
            print(f"点赞数: {data.get('likeCount')}")
            print(f"查看数: {data.get('viewCount')}")
            print(f"评论数: {data.get('commentCount')}")
            print(f"收藏数: {data.get('favoriteCount')}")
            print(f"创建时间: {data.get('createTime')}")
            print(f"更新时间: {data.get('updateTime')}")
            
            # 检查可选字段
            if data.get('publishTime'):
                print(f"发布时间: {data.get('publishTime')}")
            if data.get('coverUrl'):
                print(f"封面URL: {data.get('coverUrl')}")
            if data.get('tags'):
                print(f"标签: {data.get('tags')}")
        else:
            print(f"❌ 内容详情查询失败: {result.get('message')}")
    else:
        print(f"❌ API调用失败: {response.text}")

def test_get_content_with_user_id():
    """测试带用户ID的内容详情查询"""
    print("\n=== 测试带用户ID的内容详情查询 ===")
    
    content_id = 107
    user_id = 1
    
    url = f"{API_ENDPOINT}/{content_id}"
    params = {"userId": user_id}
    
    response = requests.get(url, params=params)
    print(f"状态码: {response.status_code}")
    
    if response.status_code == 200:
        result = response.json()
        if result.get("success"):
            print("✅ 带用户ID的内容详情查询成功")
            data = result.get("data", {})
            print(f"内容ID: {data.get('id')}")
            print(f"标题: {data.get('title')}")
            print(f"用户互动状态:")
            print(f"  - 是否已点赞: {data.get('isLiked', 'null')}")
            print(f"  - 是否已收藏: {data.get('isFavorited', 'null')}")
            print(f"  - 是否已关注作者: {data.get('isFollowed', 'null')}")
        else:
            print(f"❌ 带用户ID的内容详情查询失败: {result.get('message')}")
    else:
        print(f"❌ API调用失败: {response.text}")

def test_get_content_include_offline():
    """测试包含下线内容的查询"""
    print("\n=== 测试包含下线内容的查询 ===")
    
    content_id = 107
    
    url = f"{API_ENDPOINT}/{content_id}"
    params = {"includeOffline": "true"}
    
    response = requests.get(url, params=params)
    print(f"状态码: {response.status_code}")
    
    if response.status_code == 200:
        result = response.json()
        if result.get("success"):
            print("✅ 包含下线内容的查询成功")
            data = result.get("data", {})
            print(f"内容ID: {data.get('id')}")
            print(f"标题: {data.get('title')}")
            print(f"状态: {data.get('status')}")
        else:
            print(f"❌ 包含下线内容的查询失败: {result.get('message')}")
    else:
        print(f"❌ API调用失败: {response.text}")

def test_get_nonexistent_content():
    """测试查询不存在的内容"""
    print("\n=== 测试查询不存在的内容 ===")
    
    content_id = 999999  # 不太可能存在的ID
    
    url = f"{API_ENDPOINT}/{content_id}"
    
    response = requests.get(url)
    print(f"状态码: {response.status_code}")
    
    if response.status_code == 200:
        result = response.json()
        if not result.get("success"):
            print("✅ 正确返回了内容不存在的错误")
            print(f"错误信息: {result.get('message')}")
        else:
            print("❌ 应该返回错误但却成功了")
    else:
        print(f"❌ API调用失败: {response.text}")

def test_get_content_with_invalid_id():
    """测试无效的内容ID"""
    print("\n=== 测试无效的内容ID ===")
    
    invalid_id = "abc"  # 非数字ID
    
    url = f"{API_ENDPOINT}/{invalid_id}"
    
    response = requests.get(url)
    print(f"状态码: {response.status_code}")
    
    if response.status_code == 400:
        print("✅ 正确返回了400错误（无效参数）")
        print(f"错误信息: {response.text}")
    else:
        print(f"⚠️  状态码不是400: {response.text}")

def test_comprehensive_scenario():
    """测试综合场景"""
    print("\n=== 测试综合场景 ===")
    
    # 多个可能存在的内容ID
    test_content_ids = [107, 108, 109, 110, 111]
    
    for content_id in test_content_ids:
        print(f"\n--- 测试内容ID: {content_id} ---")
        url = f"{API_ENDPOINT}/{content_id}"
        
        try:
            response = requests.get(url, timeout=5)
            
            if response.status_code == 200:
                result = response.json()
                if result.get("success"):
                    data = result.get("data", {})
                    print(f"✅ ID {content_id}: {data.get('title', 'No Title')} "
                          f"({data.get('contentType')}) - {data.get('status')}")
                else:
                    print(f"❌ ID {content_id}: {result.get('message')}")
            else:
                print(f"❌ ID {content_id}: HTTP {response.status_code}")
        except requests.exceptions.Timeout:
            print(f"⏰ ID {content_id}: 请求超时")
        except Exception as e:
            print(f"❌ ID {content_id}: 请求异常 - {e}")

if __name__ == "__main__":
    print("ContentController内容详情查询API测试开始")
    print("=" * 60)
    
    try:
        # 执行各种测试
        test_get_content_by_id()
        test_get_content_with_user_id()
        test_get_content_include_offline()
        test_get_nonexistent_content()
        test_get_content_with_invalid_id()
        test_comprehensive_scenario()
        
        print("\n" + "=" * 60)
        print("🎉 所有测试完成！")
        print("\nAPI接口功能说明:")
        print("1. 根据内容ID查询详情: GET /api/v1/content/core/{contentId}")
        print("2. 支持可选参数:")
        print("   - userId: 用于查询用户互动状态（点赞、收藏、关注）")
        print("   - includeOffline: 是否包含下线内容")
        print("3. 返回完整的内容信息，包括:")
        print("   - 基本信息（标题、描述、类型等）")
        print("   - 作者信息（ID、昵称、头像）")
        print("   - 分类信息（ID、名称）")
        print("   - 状态信息（发布状态、审核状态）")
        print("   - 统计信息（点赞数、查看数、评论数等）")
        print("   - 时间信息（创建、更新、发布时间）")
        print("   - 互动状态（如果提供userId）")
        
    except requests.exceptions.ConnectionError:
        print("❌ 无法连接到服务器，请确保应用正在运行")
        print(f"尝试连接的地址: {BASE_URL}")
    except Exception as e:
        print(f"❌ 测试过程中出现错误: {e}")

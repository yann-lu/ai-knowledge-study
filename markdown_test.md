# Markdown格式测试

## 数字列表测试
1. 存储用户的详细信息，如用户的个人资料、偏好设置等。
2. key是机构的id，hashKey是权限组id，value是每个权限组下的权限点
3. 多种维度信息的存储和读取(商品id、商品名称、商品描述、商品库存、商品好评)

## 破折号列表测试
- Hash
  - 存储用户的详细信息，如用户的个人资料、偏好设置等。
  - key是机构的id，hashKey是权限组id，value是每个权限组下的权限点
  - 多种维度信息的存储和读取(商品id、商品名称、商品描述、商品库存、商品好评)
  - 购物车(以用户id为key，商品id为field，商品数量为value)
  - 对象存储(一般采用string + json存储，对象中某些频繁变化的属性抽出来用hash存储)
  - redisson的分布式锁 hset lock 线程id count(代表锁重入的次数)

## 表格测试
| 数据结构 | 使用场景 | 特点 |
|---------|---------|------|
| Hash | 存储用户详细信息 | key-value结构 |
| List | 消息队列 | 有序列表 |
| Set | 去重 | 无序不重复 |

## 任务列表测试
- [ ] 未完成任务
- [x] 已完成任务
- [ ] 另一个未完成任务

## 删除线测试
~~这是被删除的内容~~

## 代码块测试
```java
public class Test {
    public static void main(String[] args) {
        System.out.println("Hello World");
    }
}
```

## 内联代码测试
这是一个 `内联代码` 示例。

## 自动链接测试
https://www.example.com

## 引用测试
> 这是一个引用块
> 可以包含多行内容
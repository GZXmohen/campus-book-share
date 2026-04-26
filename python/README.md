# Campus Book Share - Recommendation Service

基于 TF-IDF + 余弦相似度 的相似图书推荐服务

## 环境要求

- Python 3.8+

## 安装依赖

```bash
cd python
pip install -r requirements.txt
```

## 运行测试

```bash
cd python
python test_recommendation.py
```

## 启动 API 服务

```bash
cd python
python app.py
```

服务运行在 http://localhost:5000

## API 接口

### 获取相似图书
```
GET /api/recommend/similar/<book_id>?top_k=5

Response:
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "book_id": 3,
      "similarity": 0.8542,
      "title": "Python数据分析",
      "author": "王五",
      "cover_image": "",
      "sale_price": 0,
      "rent_price": 0
    }
  ]
}
```

### 关键词搜索
```
GET /api/recommend/search?keywords=Python编程&top_k=10
```

### 刷新索引
```
POST /api/recommend/refresh
```

### 健康检查
```
GET /health
```

## 集成到 Go 后端

在 Go 后端的 PostController 中添加推荐接口：

```go
// GetSimilarBooks 获取相似图书
func GetSimilarBooks(c *gin.Context) {
    bookId := c.Param("id")
    resp, err := http.Get("http://localhost:5000/api/recommend/similar/" + bookId)
    if err != nil {
        c.JSON(500, gin.H{"error": err.Error()})
        return
    }
    defer resp.Body.Close()
    
    var result map[string]interface{}
    json.NewDecoder(resp.Body).Decode(&result)
    c.JSON(200, result)
}
```

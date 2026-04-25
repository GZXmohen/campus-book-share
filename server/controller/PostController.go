package controller

import (
	"bookshare/common"
	"bookshare/model"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
)

func CreatePost(ctx *gin.Context) {
	var requestPost model.Post
	// 1. 数据绑定
	if err := ctx.ShouldBindJSON(&requestPost); err != nil {
		ctx.JSON(http.StatusBadRequest, gin.H{"code": 400, "msg": "数据格式错误"})
		return
	}

	// 2. 获取当前登录用户 (从中间件里取)
	user, exists := ctx.Get("user")
	if !exists {
		ctx.JSON(http.StatusUnauthorized, gin.H{"code": 401, "msg": "请先登录"})
		return
	}
	// 类型断言，把 interface{} 转回 model.User
	currentUser := user.(model.User)

	// 3. 补全信息
	requestPost.UserId = currentUser.ID

	// 4. 存入数据库
	if err := common.GetDB().Create(&requestPost).Error; err != nil {
		ctx.JSON(http.StatusInternalServerError, gin.H{"code": 500, "msg": "发布失败"})
		return
	}

	ctx.JSON(http.StatusOK, gin.H{"code": 200, "msg": "发布成功", "data": requestPost})
}

// GetPostList 获取帖子列表 (支持分页)
func GetPostList(ctx *gin.Context) {
	db := common.GetDB()

	page := ctx.DefaultQuery("page", "1")
	keyword := ctx.DefaultQuery("keyword", "")
	pageSize := 10

	pageInt, _ := strconv.Atoi(page)
	offset := (pageInt - 1) * pageSize

	var posts []model.Post
	query := db.Preload("User").Order("created_at desc")

	if keyword != "" {
		query = query.Where("title LIKE ? OR author LIKE ?", "%"+keyword+"%", "%"+keyword+"%")
	}

	query.Offset(offset).Limit(pageSize).Find(&posts)

	for i := range posts {
		posts[i].User.Password = ""
	}

	ctx.JSON(http.StatusOK, gin.H{
		"code": 200,
		"data": posts,
		"msg":  "获取成功",
	})
}

// GetPostDetail 获取单个帖子详情
func GetPostDetail(ctx *gin.Context) {
	db := common.GetDB()

	// 从 URL 参数获取 postId
	postId := ctx.Param("id")
	print("GetPostDetail: postId = ", postId, "\n")

	var post model.Post
	// 查询并预加载发布者信息和评论信息
	if err := db.Preload("User").Preload("Comments.User").First(&post, postId).Error; err != nil {
		print("GetPostDetail: error = ", err.Error(), "\n")
		ctx.JSON(http.StatusNotFound, gin.H{"code": 404, "msg": "帖子不存在"})
		return
	}
	// 防止泄露密码
	post.User.Password = ""
	// 清理评论中的用户密码
	for i := range post.Comments {
		post.Comments[i].User.Password = ""
	}

	ctx.JSON(http.StatusOK, gin.H{
		"code": 200,
		"data": post,
		"msg":  "获取成功",
	})
}
func GetMyPosts(ctx *gin.Context) {
	user, exists := ctx.Get("user")
	if !exists {
		ctx.JSON(http.StatusUnauthorized, gin.H{"code": 401, "msg": "请先登录"})
		return
	}
	currentUser := user.(model.User)

	db := common.GetDB()
	var posts []model.Post
	db.Where("user_id = ?", currentUser.ID).Preload("User").Order("created_at desc").Find(&posts)

	for i := range posts {
		posts[i].User.Password = ""
	}

	ctx.JSON(http.StatusOK, gin.H{"code": 200, "data": posts, "msg": "获取成功"})
}
func UpdatePost(ctx *gin.Context) {
	postId := ctx.Param("id")
	user, exists := ctx.Get("user")
	if !exists {
		ctx.JSON(http.StatusUnauthorized, gin.H{"code": 401, "msg": "请先登录"})
		return
	}
	currentUser := user.(model.User)

	var updateData model.Post
	if err := ctx.ShouldBindJSON(&updateData); err != nil {
		ctx.JSON(http.StatusBadRequest, gin.H{"code": 400, "msg": "数据格式错误"})
		return
	}

	db := common.GetDB()
	var post model.Post
	if err := db.First(&post, postId).Error; err != nil {
		ctx.JSON(http.StatusNotFound, gin.H{"code": 404, "msg": "帖子不存在"})
		return
	}

	if post.UserId != currentUser.ID {
		ctx.JSON(http.StatusForbidden, gin.H{"code": 403, "msg": "无权编辑"})
		return
	}

	post.Title = updateData.Title
	post.Author = updateData.Author
	post.Description = updateData.Description
	post.CoverImage = updateData.CoverImage
	post.IsSell = updateData.IsSell
	post.SalePrice = updateData.SalePrice
	post.IsRent = updateData.IsRent
	post.RentPrice = updateData.RentPrice
	post.ContactWX = updateData.ContactWX
	post.ContactQQ = updateData.ContactQQ

	db.Save(&post)
	ctx.JSON(http.StatusOK, gin.H{"code": 200, "data": post, "msg": "更新成功"})
}

func DeletePost(ctx *gin.Context) {
	postId := ctx.Param("id")
	user, exists := ctx.Get("user")
	if !exists {
		ctx.JSON(http.StatusUnauthorized, gin.H{"code": 401, "msg": "请先登录"})
		return
	}
	currentUser := user.(model.User)

	db := common.GetDB()
	var post model.Post
	if err := db.First(&post, postId).Error; err != nil {
		ctx.JSON(http.StatusNotFound, gin.H{"code": 404, "msg": "帖子不存在"})
		return
	}

	if post.UserId != currentUser.ID {
		ctx.JSON(http.StatusForbidden, gin.H{"code": 403, "msg": "无权删除"})
		return
	}

	db.Delete(&post)
	ctx.JSON(http.StatusOK, gin.H{"code": 200, "msg": "删除成功"})
}

// GetPostComments 获取帖子评论
func GetPostComments(ctx *gin.Context) {
	db := common.GetDB()

	// 从 URL 参数获取 postId
	postId := ctx.Param("id")
	print("GetPostComments: postId = ", postId, "\n")

	var comments []model.Comment
	// 查询并预加载评论者信息
	if err := db.Where("post_id = ?", postId).Preload("User").Order("created_at desc").Find(&comments).Error; err != nil {
		print("GetPostComments: error = ", err.Error(), "\n")
		ctx.JSON(http.StatusInternalServerError, gin.H{"code": 500, "msg": "获取评论失败"})
		return
	}

	print("GetPostComments: found ", len(comments), " comments\n")

	// 清理评论中的用户密码
	for i := range comments {
		if comments[i].User.ID != 0 {
			comments[i].User.Password = ""
		}
	}

	ctx.JSON(http.StatusOK, gin.H{
		"code": 200,
		"data": comments,
		"msg":  "获取成功",
	})
}

// CreateComment 创建评论
func CreateComment(ctx *gin.Context) {
	var requestComment struct {
		PostId  uint   `json:"post_id" binding:"required"`
		Content string `json:"content" binding:"required"`
	}

	// 1. 数据绑定
	if err := ctx.ShouldBindJSON(&requestComment); err != nil {
		ctx.JSON(http.StatusBadRequest, gin.H{"code": 400, "msg": "数据格式错误"})
		return
	}

	// 2. 获取当前登录用户 (从中间件里取)
	user, exists := ctx.Get("user")
	if !exists {
		ctx.JSON(http.StatusUnauthorized, gin.H{"code": 401, "msg": "请先登录"})
		return
	}
	// 类型断言，把 interface{} 转回 model.User
	currentUser := user.(model.User)

	// 3. 检查帖子是否存在
	db := common.GetDB()
	var post model.Post
	if err := db.First(&post, requestComment.PostId).Error; err != nil {
		ctx.JSON(http.StatusNotFound, gin.H{"code": 404, "msg": "帖子不存在"})
		return
	}

	// 4. 创建评论
	comment := model.Comment{
		PostId:  requestComment.PostId,
		UserId:  currentUser.ID,
		Content: requestComment.Content,
	}

	if err := db.Create(&comment).Error; err != nil {
		ctx.JSON(http.StatusInternalServerError, gin.H{"code": 500, "msg": "评论失败"})
		return
	}

	// 5. 为帖子作者创建通知
	if post.UserId != currentUser.ID {
		notification := model.Notification{
			UserId:  post.UserId,
			Type:    1, // 1-评论通知
			Content: currentUser.Username + " 评论了你的帖子",
			PostId:  &post.ID,
			IsRead:  false,
		}
		db.Create(&notification)
	}

	// 6. 加载评论者信息
	db.Preload("User").First(&comment, comment.ID)
	comment.User.Password = ""

	ctx.JSON(http.StatusOK, gin.H{"code": 200, "msg": "评论成功", "data": comment})
}

// DeleteComment 删除评论
func DeleteComment(ctx *gin.Context) {
	commentId := ctx.Param("id")
	user, exists := ctx.Get("user")
	if !exists {
		ctx.JSON(http.StatusUnauthorized, gin.H{"code": 401, "msg": "请先登录"})
		return
	}
	currentUser := user.(model.User)

	db := common.GetDB()
	var comment model.Comment

	// 打印日志，查看传递的commentId
	print("DeleteComment: commentId = ", commentId, "\n")

	// 直接使用commentId查询
	if err := db.First(&comment, commentId).Error; err != nil {
		print("DeleteComment: error = ", err.Error(), "\n")
		ctx.JSON(http.StatusNotFound, gin.H{"code": 404, "msg": "评论不存在"})
		return
	}

	print("DeleteComment: found comment with ID = ", comment.ID, " UserId = ", comment.UserId, " CurrentUserId = ", currentUser.ID, "\n")

	if comment.UserId != currentUser.ID {
		ctx.JSON(http.StatusForbidden, gin.H{"code": 403, "msg": "无权删除"})
		return
	}

	db.Delete(&comment)
	ctx.JSON(http.StatusOK, gin.H{"code": 200, "msg": "删除成功"})
}

const PythonRecommenderURL = "http://localhost:5000"

type SimilarBook struct {
	BookID     uint    `json:"book_id"`
	Similarity float64 `json:"similarity"`
	Title      string  `json:"title"`
	Author     string  `json:"author"`
	CoverImage string  `json:"cover_image"`
	SalePrice  float64 `json:"sale_price"`
	RentPrice  float64 `json:"rent_price"`
}

type RecommendResponse struct {
	Code    int           `json:"code"`
	Message string        `json:"message"`
	Data    []SimilarBook `json:"data"`
}

func GetSimilarBooks(ctx *gin.Context) {
	postId := ctx.Param("id")
	topK := ctx.DefaultQuery("top_k", "5")

	url := fmt.Sprintf("%s/api/recommend/similar/%s?top_k=%s", PythonRecommenderURL, postId, topK)

	resp, err := http.Get(url)
	if err != nil {
		ctx.JSON(http.StatusServiceUnavailable, gin.H{
			"code": 503,
			"msg":  "推荐服务不可用",
			"data": []SimilarBook{},
		})
		return
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		ctx.JSON(http.StatusInternalServerError, gin.H{
			"code": 500,
			"msg":  "读取推荐结果失败",
			"data": []SimilarBook{},
		})
		return
	}

	var recommendResp RecommendResponse
	if err := json.Unmarshal(body, &recommendResp); err != nil {
		ctx.JSON(http.StatusInternalServerError, gin.H{
			"code": 500,
			"msg":  "解析推荐结果失败",
			"data": []SimilarBook{},
		})
		return
	}

	ctx.JSON(http.StatusOK, gin.H{
		"code": 200,
		"msg":  "获取成功",
		"data": recommendResp.Data,
	})
}

func RefreshRecommendIndex(ctx *gin.Context) {
	url := fmt.Sprintf("%s/api/recommend/refresh", PythonRecommenderURL)

	resp, err := http.Post(url, "application/json", nil)
	if err != nil {
		ctx.JSON(http.StatusServiceUnavailable, gin.H{
			"code": 503,
			"msg":  "推荐服务不可用",
		})
		return
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		ctx.JSON(http.StatusInternalServerError, gin.H{
			"code": 500,
			"msg":  "刷新索引失败",
		})
		return
	}

	ctx.Data(http.StatusOK, "application/json", body)
}

func SearchBooks(ctx *gin.Context) {
	keywords := ctx.Query("keywords")
	topK := ctx.DefaultQuery("top_k", "10")

	if keywords == "" {
		ctx.JSON(http.StatusBadRequest, gin.H{
			"code": 400,
			"msg":  "keywords参数不能为空",
			"data": []SimilarBook{},
		})
		return
	}

	url := fmt.Sprintf("%s/api/recommend/search?keywords=%s&top_k=%s", PythonRecommenderURL, keywords, topK)

	resp, err := http.Get(url)
	if err != nil {
		ctx.JSON(http.StatusServiceUnavailable, gin.H{
			"code": 503,
			"msg":  "推荐服务不可用",
			"data": []SimilarBook{},
		})
		return
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		ctx.JSON(http.StatusInternalServerError, gin.H{
			"code": 500,
			"msg":  "搜索失败",
			"data": []SimilarBook{},
		})
		return
	}

	var recommendResp struct {
		Code    int                      `json:"code"`
		Message string                   `json:"message"`
		Data    []map[string]interface{} `json:"data"`
	}
	if err := json.Unmarshal(body, &recommendResp); err != nil {
		ctx.JSON(http.StatusInternalServerError, gin.H{
			"code": 500,
			"msg":  "解析搜索结果失败",
			"data": []SimilarBook{},
		})
		return
	}

	ctx.JSON(http.StatusOK, gin.H{
		"code": 200,
		"msg":  "获取成功",
		"data": recommendResp.Data,
	})
}

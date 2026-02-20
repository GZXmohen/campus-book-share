package controller

import (
	"bookshare/common"
	"bookshare/model"
	"github.com/gin-gonic/gin"
	"net/http"
	"strconv"
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

	var post model.Post
	// 查询并预加载发布者信息
	if err := db.Preload("User").First(&post, postId).Error; err != nil {
		ctx.JSON(http.StatusNotFound, gin.H{"code": 404, "msg": "帖子不存在"})
		return
	}
	// 防止泄露密码
	post.User.Password = ""

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

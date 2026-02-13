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

	// 获取分页参数 (默认第1页，每页10条)
	page := ctx.DefaultQuery("page", "1")
	pageSize := 10

	// 计算偏移量
	pageInt, _ := strconv.Atoi(page)
	offset := (pageInt - 1) * pageSize

	var posts []model.Post
	// Preload("User") 会自动填充 User 字段，但在 User model 里要注意隐私，别把密码查出来了
	// Order("created_at desc") 让最新的帖子排在最前面
	db.Preload("User").Order("created_at desc").Offset(offset).Limit(pageSize).Find(&posts)

	// 手动清除密码，防止泄露！
	// 因为 posts 是一个切片，我们遍历它，把每个 User 的 Password 设为空字符串
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

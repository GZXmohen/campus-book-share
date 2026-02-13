package controller

import (
	"bookshare/common"
	"bookshare/model"
	"github.com/gin-gonic/gin"
	"net/http"
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

package controller

import (
	"bookshare/common"
	"bookshare/model"
	"github.com/gin-gonic/gin"
	"golang.org/x/crypto/bcrypt"
	"gorm.io/gorm"
	"net/http"
)

// Register 注册接口
func Register(ctx *gin.Context) {
	DB := common.GetDB()

	// 1. 获取参数
	var requestUser model.User
	// ShouldBindJSON 会自动要把前端传来的 JSON 映射到结构体
	if err := ctx.ShouldBindJSON(&requestUser); err != nil {
		ctx.JSON(http.StatusBadRequest, gin.H{"code": 400, "msg": "参数格式错误"})
		return
	}

	// 2. 数据验证 (这里简单验证，实际可以更复杂)
	if len(requestUser.Username) == 0 || len(requestUser.Password) < 6 {
		ctx.JSON(http.StatusUnprocessableEntity, gin.H{"code": 422, "msg": "用户名不能为空，密码不能少于6位"})
		return
	}

	// 3. 判断手机号/用户名是否存在
	if isUserExist(DB, requestUser.Username) {
		ctx.JSON(http.StatusUnprocessableEntity, gin.H{"code": 422, "msg": "用户已存在"})
		return
	}

	// 4. 创建用户 (密码加密)
	hashedPassword, err := bcrypt.GenerateFromPassword([]byte(requestUser.Password), bcrypt.DefaultCost)
	if err != nil {
		ctx.JSON(http.StatusInternalServerError, gin.H{"code": 500, "msg": "加密错误"})
		return
	}
	newUser := model.User{
		Username:  requestUser.Username,
		Password:  string(hashedPassword),
		ContactWX: requestUser.ContactWX, // 注册时顺便带上微信号
	}
	DB.Create(&newUser)

	// 5. 返回结果
	ctx.JSON(http.StatusOK, gin.H{"code": 200, "msg": "注册成功"})
}

// Login 登录接口
func Login(ctx *gin.Context) {
	DB := common.GetDB()

	// 1. 获取参数
	var requestUser model.User
	if err := ctx.ShouldBindJSON(&requestUser); err != nil {
		ctx.JSON(http.StatusBadRequest, gin.H{"code": 400, "msg": "参数格式错误"})
		return
	}

	// 2. 判断用户是否存在
	var user model.User
	DB.Where("username = ?", requestUser.Username).First(&user)
	if user.ID == 0 {
		ctx.JSON(http.StatusUnprocessableEntity, gin.H{"code": 422, "msg": "用户不存在"})
		return
	}

	// 3. 判断密码是否正确
	if err := bcrypt.CompareHashAndPassword([]byte(user.Password), []byte(requestUser.Password)); err != nil {
		ctx.JSON(http.StatusBadRequest, gin.H{"code": 400, "msg": "密码错误"})
		return
	}

	// 4. 发放 Token
	token, err := common.ReleaseToken(user)
	if err != nil {
		ctx.JSON(http.StatusInternalServerError, gin.H{"code": 500, "msg": "系统异常，Token生成失败"})
		return
	}

	// 5. 返回 Token 给前端
	ctx.JSON(http.StatusOK, gin.H{
		"code": 200,
		"data": gin.H{"token": token},
		"msg":  "登录成功",
	})
}

// 辅助函数：检查用户是否存在
func isUserExist(db *gorm.DB, username string) bool {
	var user model.User
	db.Where("username = ?", username).First(&user)
	return user.ID != 0
}

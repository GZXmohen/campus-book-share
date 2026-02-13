package middleware

import (
	"bookshare/common"
	"bookshare/model"
	"github.com/gin-gonic/gin"
	"net/http"
	"strings"
)

func AuthMiddleware() gin.HandlerFunc {
	return func(ctx *gin.Context) {
		// 1. 获取 Authorization header
		// 前端传过来的格式通常是: Bearer <token>
		tokenString := ctx.GetHeader("Authorization")

		// 2. 验证格式
		if tokenString == "" || !strings.HasPrefix(tokenString, "Bearer ") {
			ctx.JSON(http.StatusUnauthorized, gin.H{"code": 401, "msg": "权限不足"})
			ctx.Abort() // 阻止请求继续
			return
		}

		// 提取 token 部分 (去掉 "Bearer " 前缀)
		tokenString = tokenString[7:]

		// 3. 解析 Token
		token, claims, err := common.ParseToken(tokenString)
		if err != nil || !token.Valid {
			ctx.JSON(http.StatusUnauthorized, gin.H{"code": 401, "msg": "Token无效或已过期"})
			ctx.Abort()
			return
		}

		// 4. 获取用户信息，存入上下文 (Context)
		var user model.User
		common.GetDB().First(&user, claims.UserId)

		if user.ID == 0 {
			ctx.JSON(http.StatusUnauthorized, gin.H{"code": 401, "msg": "用户不存在"})
			ctx.Abort()
			return
		}

		// *** 关键点 ***
		// 把用户对象存在 gin 的上下文里，后续的 Controller 可以直接取出来用
		ctx.Set("user", user)

		ctx.Next() // 放行，进入下一个环节
	}
}

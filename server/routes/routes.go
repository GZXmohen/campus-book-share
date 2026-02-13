package routes

import (
	"bookshare/controller"
	"github.com/gin-gonic/gin"
)

func CollectRoute(r *gin.Engine) *gin.Engine {
	// 所有 API 都加个 /api 前缀，规范化
	apiGroup := r.Group("/api")
	{
		// 用户路由
		userGroup := apiGroup.Group("/auth")
		{
			userGroup.POST("/register", controller.Register)
			userGroup.POST("/login", controller.Login)
		}

		// 以后可以在这里加帖子路由...
	}

	return r
}

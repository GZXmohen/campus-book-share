package routes

import (
	"bookshare/controller"
	"bookshare/middleware"
	"github.com/gin-gonic/gin"
)

func CollectRoute(r *gin.Engine) *gin.Engine {
	r.Use(gin.Recovery(), gin.Logger()) // 添加默认中间件
	// 所有 API 都加个 /api 前缀，规范化
	apiGroup := r.Group("/api")
	{
		// --- 公开路由 (无需登录) ---
		userGroup := apiGroup.Group("/auth")
		{
			userGroup.POST("/register", controller.Register)
			userGroup.POST("/login", controller.Login)
		}
		// 获取列表：GET /api/posts?page=1
		apiGroup.GET("/posts", controller.GetPostList)
		// 获取详情：GET /api/posts/1
		apiGroup.GET("/posts/:id", controller.GetPostDetail)

		// --- 受保护路由 (需要登录) ---
		// 使用 AuthMiddleware 中间件保护这些接口
		postGroup := apiGroup.Group("/post")
		postGroup.Use(middleware.AuthMiddleware())
		{
			postGroup.POST("/create", controller.CreatePost)
		}
	}

	return r
}

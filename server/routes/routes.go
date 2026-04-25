package routes

import (
	"bookshare/controller"
	"bookshare/middleware"
	"github.com/gin-gonic/gin"
)

func CollectRoute(r *gin.Engine) *gin.Engine {
	r.Use(gin.Recovery(), gin.Logger())
	apiGroup := r.Group("/api")
	{
		userGroup := apiGroup.Group("/auth")
		{
			userGroup.POST("/register", controller.Register)
			userGroup.POST("/login", controller.Login)
			userGroup.POST("/admin/login", controller.AdminLogin)
		}
		apiGroup.GET("/posts", controller.GetPostList)
		apiGroup.GET("/posts/:id", controller.GetPostDetail)
		apiGroup.GET("/posts/:id/comments", controller.GetPostComments)
		apiGroup.GET("/posts/:id/similar", controller.GetSimilarBooks)
		apiGroup.GET("/posts/search", controller.SearchBooks)
		apiGroup.POST("/posts/refresh-recommend", controller.RefreshRecommendIndex)

		commentGroup := apiGroup.Group("/comment")
		commentGroup.Use(middleware.AuthMiddleware())
		{
			commentGroup.POST("/create", controller.CreateComment)
			commentGroup.DELETE("/:id", controller.DeleteComment)
		}

		postGroup := apiGroup.Group("/post")
		postGroup.Use(middleware.AuthMiddleware())
		{
			postGroup.POST("/create", controller.CreatePost)
			postGroup.GET("/my", controller.GetMyPosts)
			postGroup.PUT("/:id", controller.UpdatePost)
			postGroup.DELETE("/:id", controller.DeletePost)
		}

		userInfoGroup := apiGroup.Group("/user")
		userInfoGroup.Use(middleware.AuthMiddleware())
		{
			userInfoGroup.GET("/info", controller.GetUserInfo)
			userInfoGroup.PUT("/info", controller.UpdateUserInfo)
			userInfoGroup.POST("/change-password", controller.ChangePassword)
			userInfoGroup.GET("/notifications", controller.GetNotifications)
			userInfoGroup.PUT("/notifications/:id", controller.MarkNotificationAsRead)
		}

		apiGroup.POST("/upload", middleware.AuthMiddleware(), controller.UploadImage)

		adminGroup := apiGroup.Group("/admin")
		adminGroup.Use(middleware.AuthMiddleware())
		{
			adminGroup.GET("/stats/dashboard", controller.AdminDashboard)
			adminGroup.GET("/stats/post-trend", controller.AdminGetPostTrend)
			adminGroup.GET("/stats/trade-distribution", controller.AdminGetTradeDistribution)
			adminGroup.GET("/users", controller.AdminGetUsers)
			adminGroup.DELETE("/users/:id", controller.AdminDeleteUser)
			adminGroup.GET("/posts", controller.AdminGetPosts)
			adminGroup.DELETE("/posts/:id", controller.AdminDeletePost)
			adminGroup.GET("/comments", controller.AdminGetComments)
			adminGroup.DELETE("/comments/:id", controller.AdminDeleteComment)
			adminGroup.GET("/notifications", controller.AdminGetNotifications)
			adminGroup.DELETE("/notifications/:id", controller.AdminDeleteNotification)
		}
	}

	return r
}

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
		}
		apiGroup.GET("/posts", controller.GetPostList)
		apiGroup.GET("/posts/:id", controller.GetPostDetail)

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
		}

		apiGroup.POST("/upload", middleware.AuthMiddleware(), controller.UploadImage)
	}

	return r
}

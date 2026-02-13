package main

import (
	"bookshare/common"
	"github.com/gin-gonic/gin"
)

func main() {
	// 1. 初始化数据库
	common.InitDB()

	// 2. 初始化 Gin 引擎
	r := gin.Default()

	// 3. 简单的测试路由
	r.GET("/ping", func(c *gin.Context) {
		c.JSON(200, gin.H{
			"message": "pong",
			"status":  "后端服务已启动",
		})
	})

	// 4. 启动服务在 8080 端口
	r.Run(":8080")
}

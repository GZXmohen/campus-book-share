package main

import (
	"bookshare/common"
	"bookshare/routes"
	"github.com/gin-gonic/gin"
)

func main() {
	// 1. 初始化数据库
	common.InitDB()

	// 2. 初始化 Gin 引擎
	r := gin.Default()

	// 3. 加载路由 (调用 routes.go 里的函数)
	r = routes.CollectRoute(r)

	// 4. 启动服务在 8080 端口
	r.Run(":8080")
}

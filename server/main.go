package main

import (
	"bookshare/common"
	"bookshare/routes"
	"github.com/gin-gonic/gin"
)

func main() {
	common.InitDB()
	r := gin.Default()

	r.Static("/uploads", "./uploads")

	r = routes.CollectRoute(r)
	r.Run(":8080")
}

package controller

import (
	"github.com/gin-gonic/gin"
	"net/http"
	"path/filepath"
	"time"
)

func UploadImage(ctx *gin.Context) {
	file, err := ctx.FormFile("image")
	if err != nil {
		ctx.JSON(http.StatusBadRequest, gin.H{"code": 400, "msg": "请选择文件"})
		return
	}

	filename := time.Now().Format("20060102150405") + "_" + file.Filename
	dst := filepath.Join("uploads", filename)

	if err := ctx.SaveUploadedFile(file, dst); err != nil {
		ctx.JSON(http.StatusInternalServerError, gin.H{"code": 500, "msg": "上传失败"})
		return
	}

	url := "/uploads/" + filename
	ctx.JSON(http.StatusOK, gin.H{"code": 200, "data": gin.H{"url": url}, "msg": "上传成功"})
}

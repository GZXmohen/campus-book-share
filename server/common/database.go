package common

import (
	"bookshare/model"
	"fmt"
	"gorm.io/driver/mysql"
	"gorm.io/gorm"
)

var DB *gorm.DB

func InitDB() *gorm.DB {
	dsn := "root:zhangxun4436@tcp(127.0.0.1:3306)/campus_book_db?charset=utf8mb4&parseTime=True&loc=Local"

	db, err := gorm.Open(mysql.Open(dsn), &gorm.Config{})
	if err != nil {
		panic("failed to connect database, error: " + err.Error())
	}

	// 自动迁移模式：GORM 会自动创建表！
	// 如果改了 struct 字段，重启后表结构也会自动更新
	db.AutoMigrate(&model.User{}, &model.Post{})

	DB = db
	fmt.Println("数据库连接成功，表结构已迁移！")
	return db
}

// 获取DB实例
func GetDB() *gorm.DB {
	return DB
}

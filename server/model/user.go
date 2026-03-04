package model

import "gorm.io/gorm"

type User struct {
	gorm.Model
	Username  string `gorm:"type:varchar(20);not null;unique" json:"username"`
	Password  string `gorm:"size:255;not null" json:"password"` // 登录时需要获取密码
	StudentId string `gorm:"type:varchar(20)" json:"student_id"`
	Avatar    string `gorm:"type:varchar(255)" json:"avatar"`
	ContactWX string `gorm:"type:varchar(50)" json:"contact_wx"`
	ID        uint   `json:"id"` // 显式定义 ID 字段，确保正确序列化
}

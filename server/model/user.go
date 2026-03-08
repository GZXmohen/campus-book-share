package model

import (
	"encoding/json"

	"gorm.io/gorm"
)

type User struct {
	gorm.Model
	Username  string `gorm:"type:varchar(20);not null;unique" json:"username"`
	Password  string `gorm:"size:255;not null" json:"password"` // 登录时需要获取密码
	StudentId string `gorm:"type:varchar(20)" json:"student_id"`
	Avatar    string `gorm:"type:varchar(255)" json:"avatar"`
	ContactWX string `gorm:"type:varchar(50)" json:"contact_wx"`
}

// 重写JSON序列化，将ID转换为id
func (u User) MarshalJSON() ([]byte, error) {
	type Alias User
	return json.Marshal(&struct {
		ID uint `json:"id"`
		*Alias
	}{
		ID:    u.ID,
		Alias: (*Alias)(&u),
	})
}

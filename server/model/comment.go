package model

import (
	"encoding/json"

	"gorm.io/gorm"
)

type Comment struct {
	gorm.Model
	// 关联帖子 (外键)
	PostId uint `json:"post_id"`
	Post   Post `gorm:"foreignKey:PostId" json:"post,omitempty"`

	// 关联用户 (外键)
	UserId uint `json:"user_id"`
	User   User `gorm:"foreignKey:UserId" json:"user"`

	// 评论内容
	Content string `gorm:"type:text;not null" json:"content"`
}

// 重写JSON序列化，将ID转换为id
func (c Comment) MarshalJSON() ([]byte, error) {
	return json.Marshal(&struct {
		ID        uint   `json:"id"`
		PostID    uint   `json:"post_id"`
		UserID    uint   `json:"user_id"`
		User      User   `json:"user"`
		Content   string `json:"content"`
		CreatedAt string `json:"created_at"`
	}{
		ID:        c.ID,
		PostID:    c.PostId,
		UserID:    c.UserId,
		User:      c.User,
		Content:   c.Content,
		CreatedAt: c.CreatedAt.Format("2006-01-02 15:04:05"),
	})
}

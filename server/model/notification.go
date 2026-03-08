package model

import (
	"encoding/json"

	"gorm.io/gorm"
)

type Notification struct {
	gorm.Model
	// 关联用户 (外键)
	UserId uint `json:"user_id"`
	User   User `gorm:"foreignKey:UserId" json:"user"`

	// 通知类型 (例如：1-评论通知，2-点赞通知，3-系统通知等)
	Type int `json:"type"`

	// 通知内容
	Content string `gorm:"type:text;not null" json:"content"`

	// 关联的帖子 (可选)
	PostId *uint `json:"post_id"`
	Post   *Post `gorm:"foreignKey:PostId" json:"post,omitempty"`

	// 是否已读
	IsRead bool `json:"is_read" default:"false"`
}

// 重写JSON序列化，将ID转换为id
func (n Notification) MarshalJSON() ([]byte, error) {
	return json.Marshal(&struct {
		ID        uint   `json:"id"`
		UserID    uint   `json:"user_id"`
		User      User   `json:"user"`
		Type      int    `json:"type"`
		Content   string `json:"content"`
		PostID    *uint  `json:"post_id"`
		Post      *Post  `json:"post,omitempty"`
		IsRead    bool   `json:"is_read"`
		CreatedAt string `json:"created_at"`
	}{
		ID:        n.ID,
		UserID:    n.UserId,
		User:      n.User,
		Type:      n.Type,
		Content:   n.Content,
		PostID:    n.PostId,
		Post:      n.Post,
		IsRead:    n.IsRead,
		CreatedAt: n.CreatedAt.Format("2006-01-02 15:04:05"),
	})
}

package model

import (
	"gorm.io/gorm"
)

type UserBehavior struct {
	gorm.Model
	UserId     uint `json:"user_id" gorm:"index"`
	PostId     uint `json:"post_id" gorm:"index"`
	BehaviorType int `json:"behavior_type"` // 1-浏览 2-评论 3-收藏 4-发布
}

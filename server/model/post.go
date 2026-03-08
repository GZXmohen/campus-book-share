package model

import (
	"encoding/json"

	"gorm.io/gorm"
)

type Post struct {
	gorm.Model
	// 关联用户 (外键)
	UserId uint `json:"user_id"`
	User   User `gorm:"foreignKey:UserId" json:"user"`

	// 书籍基础信息
	Title       string `gorm:"type:varchar(50);not null" json:"title"`
	Author      string `gorm:"type:varchar(50);not null" json:"author"`
	Description string `gorm:"type:text" json:"description"`         // 比如：九成新，校本部面交
	CoverImage  string `gorm:"type:varchar(255)" json:"cover_image"` // 封面图 URL

	// 交易模式 (核心逻辑)
	IsSell    bool    `json:"is_sell"`    // 是否出售
	SalePrice float64 `json:"sale_price"` // 售价
	IsRent    bool    `json:"is_rent"`    // 是否出租
	RentPrice float64 `json:"rent_price"` // 租金 (例如：2元/次)

	// 联系方式 (冗余字段，方便直接展示)
	ContactWX string `gorm:"type:varchar(50);not null" json:"contact_wx"`
	ContactQQ string `gorm:"type:varchar(50)" json:"contact_qq"`

	// 关联评论
	Comments []Comment `gorm:"foreignKey:PostId" json:"comments"`
}

// 重写JSON序列化
func (p Post) MarshalJSON() ([]byte, error) {
	return json.Marshal(&struct {
		ID          uint      `json:"ID"`
		UserID      uint      `json:"user_id"`
		User        User      `json:"user"`
		Title       string    `json:"title"`
		Author      string    `json:"author"`
		Description string    `json:"description"`
		CoverImage  string    `json:"cover_image"`
		IsSell      bool      `json:"is_sell"`
		SalePrice   float64   `json:"sale_price"`
		IsRent      bool      `json:"is_rent"`
		RentPrice   float64   `json:"rent_price"`
		ContactWX   string    `json:"contact_wx"`
		ContactQQ   string    `json:"contact_qq"`
		Comments    []Comment `json:"comments"`
		CreatedAt   string    `json:"created_at"`
	}{
		ID:          p.ID,
		UserID:      p.UserId,
		User:        p.User,
		Title:       p.Title,
		Author:      p.Author,
		Description: p.Description,
		CoverImage:  p.CoverImage,
		IsSell:      p.IsSell,
		SalePrice:   p.SalePrice,
		IsRent:      p.IsRent,
		RentPrice:   p.RentPrice,
		ContactWX:   p.ContactWX,
		ContactQQ:   p.ContactQQ,
		Comments:    p.Comments,
		CreatedAt:   p.CreatedAt.Format("2006-01-02 15:04:05"),
	})
}

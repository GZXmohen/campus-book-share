package model

import "gorm.io/gorm"

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
}

package common

import (
	"bookshare/model"
	"github.com/golang-jwt/jwt/v5"
	"time"
)

// 定义加密密钥 (毕设可以随便写，实际项目要保密)
var jwtKey = []byte("campus_book_share_2026_secret_key")

type Claims struct {
	UserId uint
	jwt.RegisteredClaims
}

// ReleaseToken 生成 Token
func ReleaseToken(user model.User) (string, error) {
	// Token 有效期 7 天
	expirationTime := time.Now().Add(7 * 24 * time.Hour)
	claims := &Claims{
		UserId: user.ID,
		RegisteredClaims: jwt.RegisteredClaims{
			ExpiresAt: jwt.NewNumericDate(expirationTime),
			IssuedAt:  jwt.NewNumericDate(time.Now()),
			Issuer:    "bookshare",
			Subject:   "user_token",
		},
	}
	// 创建 Token 对象：指定加密算法 + 传入 Claims
	// jwt.SigningMethodHS256：常用的对称加密算法（HS256），用上面的 jwtKey 加密
	token := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)
	tokenString, err := token.SignedString(jwtKey)
	return tokenString, err
}

// ParseToken 解析 Token
func ParseToken(tokenString string) (*jwt.Token, *Claims, error) {
	claims := &Claims{}
	token, err := jwt.ParseWithClaims(tokenString, claims, func(token *jwt.Token) (interface{}, error) {
		return jwtKey, nil
	})
	return token, claims, err
}

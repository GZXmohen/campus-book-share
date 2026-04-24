package controller

import (
	"bookshare/common"
	"bookshare/model"
	"encoding/json"
	"net/http"
	"strconv"
	"time"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

type AdminStats struct {
	UserCount         int `json:"userCount"`
	PostCount         int `json:"postCount"`
	CommentCount      int `json:"commentCount"`
	NotificationCount int `json:"notificationCount"`
}

func AdminLogin(ctx *gin.Context) {
	var requestUser model.User
	if err := ctx.ShouldBindJSON(&requestUser); err != nil {
		ctx.JSON(http.StatusBadRequest, gin.H{"code": 400, "msg": "参数格式错误"})
		return
	}

	if requestUser.Username != "admin" || requestUser.Password != "admin123" {
		ctx.JSON(http.StatusUnauthorized, gin.H{"code": 401, "msg": "账号或密码错误"})
		return
	}

	token, err := common.ReleaseToken(model.User{Username: "admin", Model: gorm.Model{ID: 1}})
	if err != nil {
		ctx.JSON(http.StatusInternalServerError, gin.H{"code": 500, "msg": "Token生成失败"})
		return
	}

	ctx.JSON(http.StatusOK, gin.H{
		"code": 200,
		"data": gin.H{"token": token, "user": gin.H{"id": 1, "username": "admin", "role": "admin"}},
		"msg":  "登录成功",
	})
}

func AdminDashboard(ctx *gin.Context) {
	db := common.GetDB()

	var userCount int64
	var postCount int64
	var commentCount int64
	var notificationCount int64

	db.Model(&model.User{}).Count(&userCount)
	db.Model(&model.Post{}).Count(&postCount)
	db.Model(&model.Comment{}).Count(&commentCount)
	db.Model(&model.Notification{}).Count(&notificationCount)

	ctx.JSON(http.StatusOK, gin.H{
		"code": 200,
		"data": AdminStats{
			UserCount:         int(userCount),
			PostCount:         int(postCount),
			CommentCount:      int(commentCount),
			NotificationCount: int(notificationCount),
		},
		"msg": "获取成功",
	})
}

func AdminGetPostTrend(ctx *gin.Context) {
	db := common.GetDB()

	type MonthlyCount struct {
		Month string
		Count int
		Sell  int
		Rent  int
	}

	now := time.Now()
	monthNames := []string{"1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月"}

	var results []MonthlyCount
	for i := 5; i >= 0; i-- {
		targetMonth := now.AddDate(0, -i, 0)
		year := targetMonth.Year()
		month := int(targetMonth.Month())

		startDate := time.Date(year, time.Month(month), 1, 0, 0, 0, 0, time.Local)
		endDate := startDate.AddDate(0, 1, 0)

		var posts []model.Post
		db.Where("created_at >= ? AND created_at < ?", startDate, endDate).Find(&posts)

		sell := 0
		rent := 0
		for _, p := range posts {
			if p.IsSell {
				sell++
			}
			if p.IsRent {
				rent++
			}
		}

		results = append(results, MonthlyCount{
			Month: monthNames[month-1],
			Count: len(posts),
			Sell:  sell,
			Rent:  rent,
		})
	}

	ctx.JSON(http.StatusOK, gin.H{
		"code": 200,
		"data": results,
		"msg":  "获取成功",
	})
}

func AdminGetTradeDistribution(ctx *gin.Context) {
	db := common.GetDB()

	var sellCount int64
	var rentCount int64
	var bothCount int64

	db.Model(&model.Post{}).Where("is_sell = ? AND is_rent = ?", true, false).Count(&sellCount)
	db.Model(&model.Post{}).Where("is_sell = ? AND is_rent = ?", false, true).Count(&rentCount)
	db.Model(&model.Post{}).Where("is_sell = ? AND is_rent = ?", true, true).Count(&bothCount)

	ctx.JSON(http.StatusOK, gin.H{
		"code": 200,
		"data": gin.H{
			"sell":      sellCount,
			"rent":      rentCount,
			"sell_rent": bothCount,
		},
		"msg": "获取成功",
	})
}

func AdminGetUsers(ctx *gin.Context) {
	db := common.GetDB()

	page, _ := strconv.Atoi(ctx.DefaultQuery("page", "1"))
	pageSize, _ := strconv.Atoi(ctx.DefaultQuery("pageSize", "10"))
	username := ctx.Query("username")
	studentId := ctx.Query("studentId")

	offset := (page - 1) * pageSize

	var users []model.User
	query := db.Order("created_at desc")

	if username != "" {
		query = query.Where("username LIKE ?", "%"+username+"%")
	}
	if studentId != "" {
		query = query.Where("student_id LIKE ?", "%"+studentId+"%")
	}

	query.Offset(offset).Limit(pageSize).Find(&users)

	var total int64
	db.Model(&model.User{}).Count(&total)

	for i := range users {
		users[i].Password = ""
	}

	var usersJSON []map[string]interface{}
	for _, u := range users {
		userMap := map[string]interface{}{
			"id":         u.ID,
			"username":   u.Username,
			"created_at": u.CreatedAt.Format("2006-01-02 15:04:05"),
		}
		usersJSON = append(usersJSON, userMap)
	}

	ctx.JSON(http.StatusOK, gin.H{
		"code": 200,
		"data": gin.H{
			"list":  usersJSON,
			"total": total,
			"page":  page,
		},
		"msg": "获取成功",
	})
}

func AdminDeleteUser(ctx *gin.Context) {
	userId := ctx.Param("id")

	db := common.GetDB()

	if err := db.Delete(&model.User{}, userId).Error; err != nil {
		ctx.JSON(http.StatusInternalServerError, gin.H{"code": 500, "msg": "删除失败"})
		return
	}

	ctx.JSON(http.StatusOK, gin.H{"code": 200, "msg": "删除成功"})
}

func AdminGetPosts(ctx *gin.Context) {
	db := common.GetDB()

	page, _ := strconv.Atoi(ctx.DefaultQuery("page", "1"))
	pageSize, _ := strconv.Atoi(ctx.DefaultQuery("pageSize", "10"))
	title := ctx.Query("title")
	author := ctx.Query("author")

	offset := (page - 1) * pageSize

	var posts []model.Post
	query := db.Preload("User").Preload("Comments").Order("created_at desc")

	if title != "" {
		query = query.Where("title LIKE ?", "%"+title+"%")
	}
	if author != "" {
		query = query.Where("author LIKE ?", "%"+author+"%")
	}

	query.Offset(offset).Limit(pageSize).Find(&posts)

	var total int64
	db.Model(&model.Post{}).Count(&total)

	for i := range posts {
		posts[i].User.Password = ""
		for j := range posts[i].Comments {
			posts[i].Comments[j].User.Password = ""
		}
	}

	var postsJSON []map[string]interface{}
	for _, p := range posts {
		postMap := map[string]interface{}{
			"id":             p.ID,
			"title":          p.Title,
			"author":         p.Author,
			"contact_wx":     p.ContactWX,
			"is_sell":        p.IsSell,
			"is_rent":        p.IsRent,
			"created_at":     p.CreatedAt.Format("2006-01-02 15:04:05"),
			"comments":       p.Comments,
			"comments_count": len(p.Comments),
		}
		postsJSON = append(postsJSON, postMap)
	}

	ctx.JSON(http.StatusOK, gin.H{
		"code": 200,
		"data": gin.H{
			"list":  postsJSON,
			"total": total,
			"page":  page,
		},
		"msg": "获取成功",
	})
}

func AdminDeletePost(ctx *gin.Context) {
	postId := ctx.Param("id")

	db := common.GetDB()

	if err := db.Delete(&model.Post{}, postId).Error; err != nil {
		ctx.JSON(http.StatusInternalServerError, gin.H{"code": 500, "msg": "删除失败"})
		return
	}

	ctx.JSON(http.StatusOK, gin.H{"code": 200, "msg": "删除成功"})
}

func AdminGetComments(ctx *gin.Context) {
	db := common.GetDB()

	page, _ := strconv.Atoi(ctx.DefaultQuery("page", "1"))
	pageSize, _ := strconv.Atoi(ctx.DefaultQuery("pageSize", "10"))
	content := ctx.Query("content")

	offset := (page - 1) * pageSize

	var comments []model.Comment
	query := db.Preload("User").Preload("Post").Order("created_at desc")

	if content != "" {
		query = query.Where("content LIKE ?", "%"+content+"%")
	}

	query.Offset(offset).Limit(pageSize).Find(&comments)

	var total int64
	db.Model(&model.Comment{}).Count(&total)

	for i := range comments {
		comments[i].User.Password = ""
	}

	var commentsJSON []map[string]interface{}
	for _, c := range comments {
		var postTitle string
		if c.Post.ID != 0 {
			postTitle = c.Post.Title
		}
		commentMap := map[string]interface{}{
			"id":         c.ID,
			"content":    c.Content,
			"post_id":    c.PostId,
			"post_title": postTitle,
			"user": map[string]interface{}{
				"username": c.User.Username,
			},
			"created_at": c.CreatedAt.Format("2006-01-02 15:04:05"),
		}
		commentsJSON = append(commentsJSON, commentMap)
	}

	ctx.JSON(http.StatusOK, gin.H{
		"code": 200,
		"data": gin.H{
			"list":  commentsJSON,
			"total": total,
			"page":  page,
		},
		"msg": "获取成功",
	})
}

func AdminDeleteComment(ctx *gin.Context) {
	commentId := ctx.Param("id")

	db := common.GetDB()

	if err := db.Delete(&model.Comment{}, commentId).Error; err != nil {
		ctx.JSON(http.StatusInternalServerError, gin.H{"code": 500, "msg": "删除失败"})
		return
	}

	ctx.JSON(http.StatusOK, gin.H{"code": 200, "msg": "删除成功"})
}

func AdminGetNotifications(ctx *gin.Context) {
	db := common.GetDB()

	page, _ := strconv.Atoi(ctx.DefaultQuery("page", "1"))
	pageSize, _ := strconv.Atoi(ctx.DefaultQuery("pageSize", "10"))

	offset := (page - 1) * pageSize

	var notifications []model.Notification
	db.Preload("User").Preload("Post").Order("created_at desc").Offset(offset).Limit(pageSize).Find(&notifications)

	var total int64
	db.Model(&model.Notification{}).Count(&total)

	var notificationsJSON []map[string]interface{}
	for _, n := range notifications {
		data, _ := json.Marshal(n)
		var notificationMap map[string]interface{}
		json.Unmarshal(data, &notificationMap)
		notificationsJSON = append(notificationsJSON, notificationMap)
	}

	ctx.JSON(http.StatusOK, gin.H{
		"code": 200,
		"data": gin.H{
			"list":  notificationsJSON,
			"total": total,
			"page":  page,
		},
		"msg": "获取成功",
	})
}

func AdminDeleteNotification(ctx *gin.Context) {
	notificationId := ctx.Param("id")

	db := common.GetDB()

	if err := db.Delete(&model.Notification{}, notificationId).Error; err != nil {
		ctx.JSON(http.StatusInternalServerError, gin.H{"code": 500, "msg": "删除失败"})
		return
	}

	ctx.JSON(http.StatusOK, gin.H{"code": 200, "msg": "删除成功"})
}

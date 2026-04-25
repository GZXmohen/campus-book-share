from recommendation.tfidf import recommender


TEST_BOOKS = [
    {
        'id': 1,
        'title': 'Python编程 从入门到实践',
        'author': '张三',
        'description': 'Python入门书籍，适合初学者，包含大量实例'
    },
    {
        'id': 2,
        'title': 'Java编程思想',
        'author': '李四',
        'description': 'Java经典教材，深入讲解Java语言特性'
    },
    {
        'id': 3,
        'title': 'Python数据分析',
        'author': '王五',
        'description': '使用Python进行数据分析和可视化的实战指南'
    },
    {
        'id': 4,
        'title': '数据结构与算法',
        'author': '赵六',
        'description': '计算机基础课程，讲解常用数据结构和算法'
    },
    {
        'id': 5,
        'title': '机器学习实战',
        'author': '孙七',
        'description': 'Python机器学习入门书籍，包含sklearn用法'
    },
    {
        'id': 6,
        'title': 'Go语言并发编程',
        'author': '周八',
        'description': '深入讲解Go语言goroutine和channel'
    },
    {
        'id': 7,
        'title': '算法导论',
        'author': '赵六',
        'description': '经典算法教材，涵盖各种高级算法设计'
    },
    {
        'id': 8,
        'title': 'Django Web开发',
        'author': '吴九',
        'description': 'Python Web框架Django的实战教程'
    }
]


def test_recommendation():
    print('=' * 50)
    print('TF-IDF 相似图书推荐测试')
    print('=' * 50)

    recommender.build_index(TEST_BOOKS)
    print(f'\n已加载 {len(TEST_BOOKS)} 本图书到索引\n')

    print('-' * 50)
    print('测试1: Python书籍推荐')
    print('-' * 50)
    similar = recommender.get_similar(1, top_k=3)
    for item in similar:
        print(f"  相似度: {item['similarity']:.4f} | {item['title']} - {item['author']}")

    print('\n' + '-' * 50)
    print('测试2: 数据结构书籍推荐')
    print('-' * 50)
    similar = recommender.get_similar(4, top_k=3)
    for item in similar:
        print(f"  相似度: {item['similarity']:.4f} | {item['title']} - {item['author']}")

    print('\n' + '-' * 50)
    print('测试3: 关键词搜索 "Python 编程"')
    print('-' * 50)
    results = recommender.search_by_keywords('Python 编程', top_k=5)
    for item in results:
        print(f"  相似度: {item['similarity']:.4f} | {item['title']} - {item['author']}")

    print('\n' + '-' * 50)
    print('测试4: 关键词搜索 "算法"')
    print('-' * 50)
    results = recommender.search_by_keywords('算法', top_k=5)
    for item in results:
        print(f"  相似度: {item['similarity']:.4f} | {item['title']} - {item['author']}")

    print('\n' + '=' * 50)
    print('测试完成!')
    print('=' * 50)


if __name__ == '__main__':
    test_recommendation()

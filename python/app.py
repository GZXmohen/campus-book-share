from flask import Flask, request, jsonify
from flask_cors import CORS
from recommendation.tfidf import recommender
import requests
import json


app = Flask(__name__)
CORS(app)

GO_SERVER_URL = 'http://localhost:8080'


TEST_BOOKS = [
    {'id': 1, 'title': 'Python编程 从入门到实践', 'author': '张三', 'description': 'Python入门书籍，适合初学者，包含大量实例'},
    {'id': 2, 'title': 'Java编程思想', 'author': '李四', 'description': 'Java经典教材，深入讲解Java语言特性'},
    {'id': 3, 'title': 'Python数据分析', 'author': '王五', 'description': '使用Python进行数据分析和可视化的实战指南'},
    {'id': 4, 'title': '数据结构与算法', 'author': '赵六', 'description': '计算机基础课程，讲解常用数据结构和算法'},
    {'id': 5, 'title': '机器学习实战', 'author': '孙七', 'description': 'Python机器学习入门书籍，包含sklearn用法'},
    {'id': 6, 'title': 'Go语言并发编程', 'author': '周八', 'description': '深入讲解Go语言goroutine和channel'},
    {'id': 7, 'title': '算法导论', 'author': '赵六', 'description': '经典算法教材，涵盖各种高级算法设计'},
    {'id': 8, 'title': 'Django Web开发', 'author': '吴九', 'description': 'Python Web框架Django的实战教程'},
]


def fetch_books():
    try:
        resp = requests.get(f'{GO_SERVER_URL}/api/posts', timeout=5)
        if resp.status_code == 200:
            data = resp.json()
            books = data.get('data', [])
            if isinstance(books, list):
                return books
            return books.get('list', [])
    except Exception as e:
        print(f'Fetch books failed: {e}')
    return []


@app.route('/api/recommend/similar/<book_id>', methods=['GET'])
def get_similar_books(book_id):
    top_k = request.args.get('top_k', 5, type=int)
    print(f'[DEBUG] get_similar called with book_id={book_id} (type: {type(book_id)})')
    print(f'[DEBUG] book_ids in index: {recommender.book_ids}')
    similar = recommender.get_similar(int(book_id), top_k=top_k)
    print(f'[DEBUG] similar result: {similar}')
    return jsonify({
        'code': 200,
        'message': 'success',
        'data': similar
    })


@app.route('/api/recommend/search', methods=['GET'])
def search_books():
    keywords = request.args.get('keywords', '')
    top_k = request.args.get('top_k', 10, type=int)

    if not keywords:
        return jsonify({
            'code': 400,
            'message': 'keywords is required',
            'data': []
        })

    results = recommender.search_by_keywords(keywords, top_k=top_k)
    return jsonify({
        'code': 200,
        'message': 'success',
        'data': results
    })


@app.route('/api/recommend/refresh', methods=['POST'])
def refresh_index():
    books = fetch_books()
    if not books:
        return jsonify({
            'code': 500,
            'message': 'Failed to fetch books from Go server',
            'data': None
        })

    recommender.build_index(books)
    return jsonify({
        'code': 200,
        'message': f'Indexed {len(books)} books successfully',
        'data': {'count': len(books)}
    })


@app.route('/api/recommend/stats', methods=['GET'])
def get_stats():
    return jsonify({
        'code': 200,
        'message': 'success',
        'data': {
            'total_books': len(recommender.books),
            'vector_shape': recommender.book_vectors.shape if recommender.book_vectors is not None else None
        }
    })


@app.route('/health', methods=['GET'])
def health_check():
    return jsonify({'status': 'ok'})


@app.route('/api/recommend/load-test', methods=['POST'])
def load_test_data():
    recommender.build_index(TEST_BOOKS)
    print(f'[DEBUG] Loaded {len(TEST_BOOKS)} test books')
    print(f'[DEBUG] book_ids: {recommender.book_ids}')
    print(f'[DEBUG] book_vectors shape: {recommender.book_vectors.shape if recommender.book_vectors is not None else None}')
    return jsonify({
        'code': 200,
        'message': f'Loaded {len(TEST_BOOKS)} test books',
        'data': {'count': len(TEST_BOOKS)}
    })


if __name__ == '__main__':
    books = fetch_books()
    if books:
        recommender.build_index(books)
        print(f'Loaded {len(books)} books from Go server')
    else:
        print('Warning: No books from Go server, use /api/recommend/load-test to load test data')

    app.run(host='0.0.0.0', port=5000, debug=True)

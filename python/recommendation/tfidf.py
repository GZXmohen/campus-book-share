import jieba
import numpy as np
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
import re


STOPWORDS = set([
    '的', '了', '是', '在', '和', '与', '及', '或', '之', '于',
    '为', '有', '我', '你', '他', '她', '它', '们', '这', '那',
    '个', '一', '不', '就', '也', '都', '要', '会', '能', '可',
    '以', '到', '说', '很', '着', '过', '被', '把', '给', '让',
    '从', '向', '往', '比', '但', '却', '而且', '因为', '所以',
    '如果', '虽然', '只是', '或者', '还是', '九成新', '全新',
    '便宜', '出售', '出租', '价格', '可以', '想', '联系'
])


class BookRecommender:
    def __init__(self):
        self.vectorizer = TfidfVectorizer(
            token_pattern=r'(?u)\b\w+\b',
            max_features=5000
        )
        self.book_vectors = None
        self.books = []
        self.book_ids = []

    def _get_id(self, book):
        book_id = book.get('id') or book.get('ID')
        return int(book_id) if book_id else None

    def _tokenize(self, text):
        if not text:
            return []
        text = re.sub(r'[^\w\s]', ' ', str(text))
        words = jieba.cut(text)
        return [w.strip() for w in words if w.strip() and w.strip() not in STOPWORDS and len(w.strip()) > 1]

    def _create_document(self, book):
        parts = [
            book.get('title', ''),
            book.get('author', ''),
            book.get('description', '')
        ]
        text = ' '.join(parts)
        tokens = self._tokenize(text)
        return ' '.join(tokens)

    def build_index(self, books):
        self.books = books
        self.book_ids = [self._get_id(book) for book in books]

        documents = [self._create_document(book) for book in books]

        if not any(doc.strip() for doc in documents):
            self.book_vectors = None
            return self

        self.vectorizer.fit(documents)
        self.book_vectors = self.vectorizer.transform(documents)
        return self

    def get_similar(self, book_id, top_k=5):
        if self.book_vectors is None:
            return []

        try:
            idx = self.book_ids.index(book_id)
        except ValueError:
            return []

        target_vector = self.book_vectors[idx].reshape(1, -1)
        similarities = cosine_similarity(target_vector, self.book_vectors)[0]

        similar_indices = similarities.argsort()[::-1]

        results = []
        for i in similar_indices:
            if i == idx:
                continue
            if len(results) >= top_k:
                break
            if similarities[i] > 0:
                results.append({
                    'book_id': self.book_ids[i],
                    'similarity': float(round(similarities[i], 4)),
                    'title': self.books[i].get('title', ''),
                    'author': self.books[i].get('author', ''),
                    'cover_image': self.books[i].get('cover_image', ''),
                    'sale_price': self.books[i].get('sale_price', 0),
                    'rent_price': self.books[i].get('rent_price', 0)
                })

        return results

    def search_by_keywords(self, keywords, top_k=10):
        if not keywords or self.book_vectors is None:
            return []

        tokens = self._tokenize(keywords)
        if not tokens:
            return []

        doc = ' '.join(tokens)
        query_vector = self.vectorizer.transform([doc])
        similarities = cosine_similarity(query_vector, self.book_vectors)[0]

        indices = similarities.argsort()[::-1]

        results = []
        for i in indices:
            if len(results) >= top_k:
                break
            if similarities[i] > 0:
                results.append({
                    'book_id': self.book_ids[i],
                    'similarity': float(round(similarities[i], 4)),
                    'title': self.books[i].get('title', ''),
                    'author': self.books[i].get('author', '')
                })

        return results


recommender = BookRecommender()

data class Book(val bookId: String, val title: String, val content: String, val username: String, val rating: Int, val authorUid: String, var isFavorited: Boolean = false)

public class Rating {

    public int userId;
    public int movieId;
    public int ratingValue;
    public int timestamp;

    public Rating(int userId, int movieId, int ratingValue) {
        this.userId = userId;
        this.movieId = movieId;
        this.ratingValue = ratingValue;
    }
}

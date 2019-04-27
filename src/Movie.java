import java.util.ArrayList;
import java.util.List;

public class Movie {

    public int id;
    public String title;

    // list ordered by users. all movies have the same order related to users
    public List<Rating> ratings;

    public Movie(int id, String title) {
        this.id = id;
        this.title = title;
        this.ratings = new ArrayList<Rating>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Movie movie = (Movie) o;
        return id == movie.id;
    }
}

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class User {

    public int id;
    public List<Map.Entry<Movie, Double>> recommendations;

    public User(int id) {
        this.id = id;
        this.recommendations = new ArrayList<Map.Entry<Movie, Double>>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id;
    }
}

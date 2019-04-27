import java.io.*;
import java.util.*;

public class Main {

    private static List<Movie> movies = new ArrayList<Movie>();
    private static List<Rating> ratings = new ArrayList<Rating>();
    private static List<User> users = new ArrayList<User>();

    private static double[][] similarities;

    private static FileOutputStream writer;

    public static void main(String[] args) {
        Main main = new Main();
        main.readMoviesFile();
        System.out.println("Movie's file has been uploaded");
        main.readRatingsFile();
        System.out.println("Rating's file has been uploaded");
        main.createSimilarityTable();
        System.out.println("Similarity table has been created");
        main.openOutputFile();
        main.calculateRatingsToUsers();
        System.out.println("Finished creating recommendations!");
        System.out.println("Check the 'output.txt' file for the results. ");
        main.closeOutputFile();
    }

    private void readMoviesFile(){

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("resources/file/movies.dat").getFile());
        //File file = new File(classLoader.getResource("resources/file/movies_small_example.dat").getFile());
        try  {
            Scanner scanner = new Scanner(new BufferedReader(new FileReader(file)));

            while (scanner.hasNextLine()) {

                String line = scanner.nextLine();

                String[] lineSplitted = line.split("\\|");

                movies.add(new Movie(Integer.parseInt(lineSplitted[0]), lineSplitted[1]));
            }

            scanner.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readRatingsFile(){

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("resources/file/ratings.dat").getFile());
        //File file = new File(classLoader.getResource("resources/file/ratings_small_example.dat").getFile());
        try  {
            Scanner scanner = new Scanner(new BufferedReader(new FileReader(file)));

            while (scanner.hasNextLine()) {

                String line = scanner.nextLine();
                String[] lineSplitted = line.split("\\s+");

                Rating rating = new Rating(Integer.parseInt(lineSplitted[0]), Integer.parseInt(lineSplitted[1]),
                        Integer.parseInt(lineSplitted[2]));
                ratings.add(rating);

                if(!users.contains(new User(Integer.parseInt(lineSplitted[0])))) {
                    users.add(new User(Integer.parseInt(lineSplitted[0])));
                }

                movies.get(rating.movieId-1).ratings.add(rating);
            }

            Collections.sort(users, new Comparator<User>() {
                @Override
                public int compare(User t0, User t1) {
                    return t0.id - t1.id;
                }
            });

            scanner.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createSimilarityTable() {

        for (Movie movie : movies) {
            completeRatingsArrayForMovie(movie);
            Collections.sort(movie.ratings, new Comparator<Rating>() {
                @Override
                public int compare(Rating t0, Rating t1) {
                    return t0.userId - t1.userId;
                }
            });
        }

        similarities = new double[movies.size()][movies.size()];

        for(int i = 0; i < movies.size(); i++) {
            for(int j = i; j < movies.size(); j++) {

                double s = similarity(movies.get(i), movies.get(j));

                similarities[i][j] = s;
                similarities[j][i] = s;
            }
        }
    }

    private void completeRatingsArrayForMovie(Movie movie) {
        for (User user : users) {

            boolean hadRatingForMovie = false;

            for (Rating rating : movie.ratings) {

                if (user.id == rating.userId) {
                    hadRatingForMovie = true;
                    break;
                }
            }

            if (!hadRatingForMovie) {
                movie.ratings.add(new Rating(user.id, movie.id, 0));
            }
        }
    }

    private double similarity(Movie movie1, Movie movie2) {

        if (movie1.equals(movie2)) {
            return 1;
        }

        double similarity = 0;

        double numerator = 0;

        double ratingsMovie1Base = 0;
        double ratingsMovie2Base = 0;


        for (int i = 0; i < movie1.ratings.size(); i++) {
            numerator += movie1.ratings.get(i).ratingValue * movie2.ratings.get(i).ratingValue;

            ratingsMovie1Base += Math.pow(movie1.ratings.get(i).ratingValue, 2);
            ratingsMovie2Base += Math.pow(movie2.ratings.get(i).ratingValue, 2);
        }

        double denominator = Math.sqrt(ratingsMovie1Base) * Math.sqrt(ratingsMovie2Base);

        similarity = numerator / denominator;

        return similarity;
    }

    private void calculateRatingsToUsers(){
        List<Movie> recommendedMovies = new ArrayList<Movie>();
        List<Movie> notRecommendMovies;

        for (User user : users) {

            notRecommendMovies = new ArrayList<Movie>(movies);

            for (Movie movie : movies) {

                Rating rating = movie.ratings.get(user.id-1);

                if (rating.ratingValue == 0) { // if the user didn't rated this movie and it to recommendation list
                    user.recommendations.add(new AbstractMap.SimpleEntry<Movie, Double>(movie, 0.0)); // add a pair <Movie, RatingValue> to the user recommendations
                    recommendedMovies.add(movie);
                }
            }

            notRecommendMovies.removeAll(recommendedMovies);

            // predict rating for each of the recommendations
            for (Map.Entry<Movie, Double> recommendedMovie : user.recommendations) {
                double ratingValue = predictRating(user, recommendedMovie.getKey(), notRecommendMovies);
                recommendedMovie.setValue(ratingValue);
            }

            System.out.print("Calculating each user's ratings: ");
            Collections.sort(user.recommendations, new Comparator<Map.Entry<Movie, Double>>() {
            			@Override
                        public int compare(Map.Entry<Movie, Double> t0, Map.Entry<Movie, Double> t1) {
                        	if (t0.getValue().isNaN() && t1.getValue().isNaN()) return 0;
                        	if (t0.getValue().isNaN()) return 1;
                        	if (t1.getValue().isNaN()) return -1;
                        	return Double.compare(t0.getValue(), t1.getValue()) * -1;
                        }
                    });

            writeToOutputFile(user, user.recommendations.subList(0,user.recommendations.size() >= 5 ? 5 : user.recommendations.size()));


            //clear unnecessary data
            user.recommendations.clear();
            recommendedMovies.clear();
            notRecommendMovies.clear();
        }
    }

    private double predictRating(User user, Movie movie, List<Movie> notRecommendMovies) {

        double rating;

        double sumOfRatingTimesSimilarity = 0;
        double sumOfSimilarities = 0;

        for (Movie m : notRecommendMovies) {

            double s = similarities[m.id - 1][movie.id - 1];

            int idx = users.indexOf(user); // users global array has same index as movies users' ratings
            sumOfRatingTimesSimilarity += m.ratings.get(idx).ratingValue * s;
            sumOfSimilarities += s;
        }

        rating = sumOfRatingTimesSimilarity / sumOfSimilarities;
        return rating;
    }

    private void openOutputFile() {
        try {
            writer = new FileOutputStream(new File("output.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeOutputFile() {
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeToOutputFile(User user, List<Map.Entry<Movie, Double>> top5) {

        String line = "user ID: " + user.id + " top 5 recommendations: ";


        for(int i = 0; i < top5.size(); i++) {
            Map.Entry<Movie, Double> recommendation = top5.get(i);
            line += recommendation.getKey().title + "::" + recommendation.getValue();

            if(i < 4) {
                line += " | ";
            }
        }
        line += "\n";

        System.out.println(user.id);

        try {
            writer.write(line.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

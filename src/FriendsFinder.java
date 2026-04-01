import java.util.*;
import java.awt.Point;

public class FriendsFinder {
    /**
     * Returns the authors' names.
     * @return  The names of the authors of this file.
     */
    public static String getAuthors() {
        return "Kamil Reyes and Matt Greenblatt";
    }

    public static List<Point> nearestFriends(List<Point> points) {
        List<Point> result = new List<>();
        result.add(points.get(0));
        result.add(points.get(1));
        float minDistance = distance(points.get(0), points.get(1));
        for (int i = 0; i < points.size() - 1; i++) {
            for (int j = i + 1; j < points.size(); j++) {
                float distance = distance(points.get(i), points.get(j));
                if (distance < minDistance) {
                    minDistance = distance;
                    result.clear();
                    result.add(points.get(i));
                    result.add(points.get(j));
                }
            }
        }
    }

    public static float distance(Point p1, Point p2) {
        float d = Math.sqrt(Math.pow(p1.getX()-p2.getX()) + Math.pow(p1.getY()-p2.getY()));
        return d;
    }
}

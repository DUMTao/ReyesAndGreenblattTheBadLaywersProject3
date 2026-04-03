import java.util.*;
import java.awt.Point;
import java.util.concurrent.*;

public class Speed {
    /**
     * Returns the authors' names.
     * @return  The names of the authors of this file.
     */
    public static String getAuthors() {
        return "Kamil Reyes and Matt Greenblatt";
    }

    //1. Failed 200,000 68/100
    public static List<Point> nearestFriends(List<Point> points){
        //Creating the lists for the result of nearest friends and the points
        List<Point> nearestFriends = new ArrayList<>();
        List<Point> pointsX = new ArrayList<>(points);
        List<Point> pointsY = new ArrayList<>(points);

        //Base case: If there's no points or there's only 2 then the distance is the min by default
        if (points == null || points.size() < 2){
            return nearestFriends;
        }

        //Sort the points by comparing their distances
        pointsX.sort(compareX);
        pointsY.sort(compareY);

        //Store the recursive result into the worker thread and save for later
        WorkerResult result = closestPairPoints(pointsX, pointsY);

        //Returns the min distance pair of points
        nearestFriends.add(result.p1);
        nearestFriends.add(result.p2);
        return nearestFriends;

    }

    /*
    These comparators are for comparing the split distances, first sort my x and then my y
     */
    static Comparator<Point> compareX = new Comparator<Point>() {
        public int compare(Point x1, Point x2) {
            return Double.compare(x1.getX(), x2.getX());
        }
    };

    static Comparator<Point> compareY = new Comparator<Point>() {
        public int compare(Point y1, Point y2) {
            return Double.compare(y1.getY(), y2.getY());
        }
    };

    //Regular distance formula
    public static double distance(Point p1, Point p2) {
        double d = Math.sqrt(Math.pow((p1.getX()-p2.getX()), 2) + Math.pow((p1.getY()-p2.getY()), 2));
        return d;
    }

    //Thread worker class
    static class WorkerResult {
        double distance;
        Point p1;
        Point p2;

        WorkerResult(double distance, Point p1, Point p2) {
            this.distance = distance;
            this.p1 = p1;
            this.p2 = p2;
        }
    }

    private static WorkerResult closestPairPoints(List<Point> pointsX, List<Point> pointsY){
        List<Point> leftYPoints = new ArrayList<>();
        List<Point> rightYPoints = new ArrayList<>();
        List<Point> distanceStrip = new ArrayList<>();
        int n = pointsX.size(); //Get how many points are within the x list

        //If there's less than three nodes just brute force
        if (n <= 3){
            return bruteF(pointsX);
        }

        //Since there's more than 3 nodes, get the midpoint of the "graph" by the half of pointsX
        int mid = n / 2;
        Point midP = pointsX.get(mid);

        //Separate and Create the lists of the x points on the left and right sides based on the midpoint
        List<Point> leftXPoints = pointsX.subList(0,  mid);
        List<Point> rightXPoints = pointsX.subList(mid, n);

        //Iterate through the points to populate the left and right x points
        for (Point point : pointsY){
            if (point.getX() <= midP.getX()){
                leftYPoints.add(point);
            }
            else {
                rightYPoints.add(point);
            }
        }

        //Yay recursion! Then get the shortest distance of the closest pairs
        WorkerResult left = closestPairPoints(leftXPoints, leftYPoints);
        WorkerResult right = closestPairPoints(rightXPoints, rightYPoints);

        WorkerResult bestDistance = left.distance < right.distance ? left : right;
        double delta = bestDistance.distance;

        //Build distance strip
        for (Point point : pointsY){
            if (Math.abs(point.getX() - midP.getX()) < delta){
                distanceStrip.add(point);
            }
        }
        WorkerResult stripResult = stripBest(distanceStrip, delta);
        return stripResult.distance < bestDistance.distance ? stripResult : bestDistance;

    }

    private static WorkerResult bruteF(List<Point> points) {
        double minDis = Double.MAX_VALUE;
        Point p1 = null;
        Point p2 = null;

        for (int i = 0; i < points.size(); i++) {
            for (int j = i + 1; j < points.size(); j++) {
                double d = distance(points.get(i), points.get(j));
                if (d < minDis) {
                    minDis = d;
                    p1 = points.get(i);
                    p2 = points.get(j);
                }
            }
        }
        return new WorkerResult(minDis, p1, p2);
    }

    private static WorkerResult stripBest(List<Point> strip, double delta) {
        double minDis = delta;
        Point p1 = null;
        Point p2 = null;
        strip.sort(compareY);

        for (int i = 0; i < strip.size(); i++) {
            for (int j = i + 1; j < strip.size() && (strip.get(j).getY() - strip.get(i).getY()) < minDis; j++) {
                double dis = distance(strip.get(i), strip.get(j));
                if (dis < minDis) {
                    minDis = dis;
                    p1 = strip.get(i);
                    p2 = strip.get(j);
                }
            }
        }

        if (p1 == null) {
            return new WorkerResult(delta, null, null);
        }
        return new WorkerResult(minDis, p1, p2);
    }

}


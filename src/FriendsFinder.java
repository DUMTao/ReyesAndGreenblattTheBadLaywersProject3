import java.util.*;
import java.awt.Point;
import java.util.concurrent.*;

public class FriendsFinder {
    /**
     * Returns the authors' names.
     * @return  The names of the authors of this file.
     */
    public static String getAuthors() {
        return "Kamil Reyes and Matt Greenblatt";
    }

    //1. Failed 200,000 68/100
    public static List<Point> nearestFriends(List<Point> points){
        List<Point> nearestFriends = new ArrayList<>();
        List<Point> pointsX = new ArrayList<>(points);
        List<Point> pointsY = new ArrayList<>(points);

        if (points == null || points.size() < 2){
            return nearestFriends;
        }

        pointsX.sort(compareX);
        pointsY.sort(compareY);

        WorkerResult result = closestPairPoints(pointsX, pointsY);

        nearestFriends.add(result.p1);
        nearestFriends.add(result.p2);
        return nearestFriends;

    }

    static Comparator<Point> compareX = new Comparator<Point>() {
        public int compare(Point x1, Point x2) {
            return Double.compare(x1.getX(), x2.getY());
        }
    };

    static Comparator<Point> compareY = new Comparator<Point>() {
        public int compare(Point y1, Point y2) {
            return Double.compare(y1.getX(), y2.getY());
        }
    };

    public static double distance(Point p1, Point p2) {
        double d = Math.sqrt(Math.pow((p1.getX()-p2.getX()), 2) + Math.pow((p1.getY()-p2.getY()), 2));
        return d;
    }

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
        //Lists for the y points and the Strip
        List<Point> leftYPoints = new ArrayList<>();
        List<Point> rightYPoints = new ArrayList<>();
        List<Point> distanceStrip = new ArrayList<>();
        int n = pointsX.size();

        //If there's less than three nodes just brute force
        if (n <= 3){
            return bruteF(pointsX);
        }

        int mid = n / 2;
        Point midP = pointsX.get(mid);

        List<Point> leftXPoints = pointsX.subList(0,  mid);
        List<Point> rightXPoints = pointsX.subList(mid, n);
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
        WorkerResult right = closestPairPoints(leftXPoints, leftYPoints);

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

    /* Failed @ 200,000 68/100
    List<Point> nearestFriends = new ArrayList<>();
        List<Future<WorkerResult>> futures = new ArrayList<>();
        //result.add(points.get(0));
        //result.add(points.get(1));
        if (points == null || points.size() < 2){
            return nearestFriends;
        }

        int n = points.size();
        int numThreads = 16;
        int chunkSize = (n + numThreads - 1) / numThreads;

        double minDistance = distance(points.get(0), points.get(1));
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        for (int t = 0; t < numThreads; t++){
            int start = t * chunkSize;
            int end = Math.min(start + chunkSize, n - 1);

            Future<WorkerResult> future = executor.submit(() -> {
                    double localMin = Double.MAX_VALUE;
                    Point best1 = null;
                    Point best2 = null;

                    for (int i = start; i < end; i++){
                        for (int j = i + 1; j < n; j++){
                            double d = distance(points.get(i), points.get(j));

                            if (d < localMin){
                                localMin = d;
                                best1 = points.get(i);
                                best2 = points.get(j);
                            }
                        }
                    }

                    return new WorkerResult(localMin, best1, best2);
            });

            futures.add(future);
        }


        double minDist = Double.MAX_VALUE;
        Point final1  = null;
        Point final2 = null;

        for (Future<WorkerResult> future : futures){
            WorkerResult r = null; //Waits if necessary
            try {
                r = future.get();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }

            if (r.distance < minDist){
                 minDist = r.distance;
                 final1 = r.p1;
                 final2 = r.p2;
            }
        }

        executor.shutdown();

        nearestFriends.add(final1);
        nearestFriends.add(final2);
        return nearestFriends;
     */

    //Brute Force version that failed/timed out at size 40,000 62/100
    /*
    public static List<Point> nearestFriends(List<Point> points) {
        List<Point> result = new ArrayList<>();
        result.add(points.get(0));
        result.add(points.get(1));
        double minDistance = distance(points.get(0), points.get(1));
        for (int i = 0; i < points.size() - 1; i++) {
            for (int j = i + 1; j < points.size(); j++) {
                double distance = distance(points.get(i), points.get(j));
                if (distance < minDistance) {
                    minDistance = distance;
                    result.clear();
                    result.add(points.get(i));
                    result.add(points.get(j));
                }
            }
        }

        return result;
    }
     */

}

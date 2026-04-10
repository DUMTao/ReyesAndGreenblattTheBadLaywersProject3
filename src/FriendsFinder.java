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


    private static final int PARALLEL_LIMIT = 8000;

    //1. Failed 4 Million 78/100
    public static List<Point> nearestFriends(List<Point> points){
        //Creating the lists for the result of nearest friends and the points
        List<Point> nearestFriends = new ArrayList<>(2);
        Point[] pointsX = points.toArray(new Point[0]);
        Point[] pointsY = points.toArray(new Point[0]);

        //Base case: If there's no points or there's only 2 then the distance is the min by default
        if (points == null || points.size() < 2){
            return nearestFriends;
        }

        //Sort the points by comparing their distances
        Arrays.sort(pointsX, Comparator.comparingDouble(p -> p.x));
        Arrays.sort(pointsY, Comparator.comparingDouble(p -> p.y));

        //Create the pool for the threads
        //ForkJoinPool pool = new ForkJoinPool();
        //Store the recursive result from the pool into the worker thread and save for later
        WorkerResult result = ForkJoinPool.commonPool().invoke(new ClosestPairTask(pointsX, pointsY, 0, pointsX.length, 0, pointsY.length));

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
        double dx = p1.x - p2.x;
        double dy = p1.y - p2.y;

        double d = dx * dx + dy * dy;
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

    static class ClosestPairTask extends RecursiveTask<WorkerResult>{
        //Create a list to sort the points by X and Y for the subproblem
        Point[] pointsX;
        Point[] pointsY;
        int low, high, yLow, yHigh;

        //Constructor for information storing
        ClosestPairTask(Point[] pointsX, Point[] pointsY, int low, int high, int yLow, int yHigh){
            this.pointsX = pointsX;
            this.pointsY = pointsY;
            this.low = low;
            this.high = high;
            this.yLow = yLow;
            this.yHigh = yHigh;
        }

        //Call the ForkJoinPool to handle the parallelization of the recursion
        @Override
        protected WorkerResult compute(){
            int n = high - low;
            int li = 0, ri = 0;

            if (n <= 3){
                return bruteF(pointsX, low, high);
            }

            int mid = low + n / 2;
            double midX = pointsX[mid].x;

            Point[] leftYPoints = new Point[n];
            Point[] rightYPoints = new Point[n];

            for (int i = yLow; i < yHigh; i++){
                Point p = pointsY[i];
                if (p.x < midX || (p.x == midX && p.y <= pointsX[mid].y)) {
                    leftYPoints[li++] = p;
                } else {
                    rightYPoints[ri++] = p;
                }
            }

            Point[] leftY = Arrays.copyOf(leftYPoints, li);
            Point[] rightY = Arrays.copyOf(rightYPoints, ri);

            ClosestPairTask leftTask = new ClosestPairTask(pointsX, leftY, low, mid, 0, li);
            ClosestPairTask rightTask = new ClosestPairTask(pointsX, rightY, mid, high, 0, ri);
            WorkerResult left, right;

            if (n > PARALLEL_LIMIT){
                leftTask.fork();
                right = rightTask.compute();
                left = leftTask.join();
            }
            else {
                left = leftTask.compute();
                right = rightTask.compute();
            }

            WorkerResult best = left.distance < right.distance ? left : right;
            double delta = best.distance;

            Point[] strip = new Point[yHigh - yLow];
            int si = 0;
            for (int i = yLow; i < yHigh; i++){
                Point p = pointsY[i];
                double dx = p.x - midX;
                if (dx * dx < delta){
                    strip[si++] = p;
                }
            }
            WorkerResult stripBest = stripBest(strip, si, delta);

            if (stripBest.p1 != null && stripBest.distance < best.distance){
                return stripBest;
            }
            return best;
        }
    }
    /*
        This is the Brute Force method for when the sub halfs have 3 or less points to compute
        Not paralellized because it doesn't need to for a small sample size so it works perfectly for a base case of DnC recursion
     */
    private static WorkerResult bruteF(Point[] points, int low, int high) {
        //Create and Store the smallest distance found and both closest pair points
        double minDis = Double.MAX_VALUE;
        Point p1 = null;
        Point p2 = null;

        //Compare every pair of points in the sub half
        for (int i = low; i < high; i++) {
            for (int j = i + 1; j < high; j++) {
                double dx = points[i].x - points[j].x;
                double dy = points[i].y - points[j].y;

                //Get the distance between the points
                double d = dx * dx + dy * dy;
                //Update the minimum if found pair is closer than last
                if (d < minDis) {
                    minDis = d;
                    p1 = points[i];
                    p2 = points[j];
                }
            }
        }
        //Return the closest pair found in the sub half
        return new WorkerResult(minDis, p1, p2);
    }

    /*
        Searches the strip region around the sub-division line to faint any pair of points
        that might be closer than previously found.
        The only has the points that their x from the midpoint is less than delta and then sorted by Y to search the distances of 7 neighbors at max.
     */
    private static WorkerResult stripBest(Point[] strip, int n, double bestSq) {
        double best = bestSq; //Store the best distance from the left and right halves
        Point p1 = null;
        Point p2 = null;

        //For each point found in the strip, compare with the y distance less than the stored minimum to reduce comparisons
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n && j <= i + 7; j++) {
                //Store the distance between the two points
                double dy = strip[j].y - strip[i].y;
                if (dy * dy >= best) break;

                double dx = strip[j].x - strip[i].x;
                double dis = dx * dx + dy * dy;
                //Update this minimum if the strip pair is closer than the minimum
                if (dis < best) {
                    best = dis;
                    p1 = strip[i];
                    p2 = strip[j];
                }
            }
        }

        //Else, return the new closest pair
        return  new WorkerResult(best, p1, p2);
    }

    //End of Friends Finder
}
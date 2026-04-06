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


    //1. Failed 4 Million 78/100
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

        //Create the pool for the threads
        ForkJoinPool pool = new ForkJoinPool();
        //Store the recursive result from the pool into the worker thread and save for later
        WorkerResult result = pool.invoke(new ClosestPairTask(pointsX, pointsY));

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

        //Create hashset for the points at X for faster return of information and build the Y points life
        Set<Point> leftSet = new HashSet<>(leftXPoints);
        for (Point point : pointsY){
            if (leftSet.contains(point)){
                leftYPoints.add(point);
            }
            else {
                rightYPoints.add(point);
            }
        }

        //Yay recursion! Then get the shortest distance of the closest pairs
        ClosestPairTask leftTask = new ClosestPairTask(leftXPoints, leftYPoints);
        ClosestPairTask rightTask = new ClosestPairTask(rightXPoints, rightYPoints);

        leftTask.fork(); //Schedules to run the left half asynchronous "run if there's an available one"
        WorkerResult right = rightTask.compute(); //Compute right half on the current thread in parallel
        WorkerResult left = leftTask.join(); //Waits for left half to finish to store the result

        //Calculates the best distance found on either respective side
        WorkerResult bestDistance = left.distance < right.distance ? left : right;
        double delta = bestDistance.distance;

        //Build distance strip
        for (Point point : pointsY){
            if (Math.abs(point.getX() - midP.getX()) < delta){
                distanceStrip.add(point);
            }
        }

        //Stores the best strip result from the sub halfs and returns the result if there's a distance smaller than the best distance
        WorkerResult stripResult = stripBest(distanceStrip, delta);
        return stripResult.distance < bestDistance.distance ? stripResult : bestDistance;

    }

    static class ClosestPairTask extends RecursiveTask<WorkerResult>{
        //Create a list to sort the points by X and Y for the subproblem
        List<Point> pointsX;
        List<Point> pointsY;

        //Constructor for information storing
        ClosestPairTask(List<Point> pointsX, List<Point> pointsY){
            this.pointsX = pointsX;
            this.pointsY = pointsY;
        }

        //Call the ForkJoinPool to handle the parallelization of the recursion
        @Override
        protected WorkerResult compute(){
            return closestPairPoints(pointsX, pointsY);
        }
    }
    /*
        This is the Brute Force method for when the sub halfs have 3 or less points to compute
        Not paralellized because it doesn't need to for a small sample size so it works perfectly for a base case of DnC recursion
     */
    private static WorkerResult bruteF(List<Point> points) {
        //Create and Store the smallest distance found and both closest pair points
        double minDis = Double.MAX_VALUE;
        Point p1 = null;
        Point p2 = null;

        //Compare every pair of points in the sub half
        for (int i = 0; i < points.size(); i++) {
            for (int j = i + 1; j < points.size(); j++) {
                //Get the distance between the points
                double d = distance(points.get(i), points.get(j));
                //Update the minimum if found pair is closer than last
                if (d < minDis) {
                    minDis = d;
                    p1 = points.get(i);
                    p2 = points.get(j);
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
    private static WorkerResult stripBest(List<Point> strip, double delta) {
        double minDis = delta; //Store the best distance from the left and right halves
        Point p1 = null;
        Point p2 = null;
        strip.sort(compareY);

        //For each point found in the strip, compare with the y distance less than the stored minimum to reduce comparisons
        for (int i = 0; i < strip.size(); i++) {
            for (int j = i + 1; j < strip.size() && (strip.get(j).getY() - strip.get(i).getY()) < minDis; j++) {
                //Store the distance between the two points
                double dis = distance(strip.get(i), strip.get(j));
                //Update this minimum if the strip pair is closer than the minimum
                if (dis < minDis) {
                    minDis = dis;
                    p1 = strip.get(i);
                    p2 = strip.get(j);
                }
            }
        }

        //If there was no pair found, return the original delta
        if (p1 == null){
            return new WorkerResult(delta, null, null);
        }

        //Else, return the new closest pair
        return  new WorkerResult(minDis, p1, p2);
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

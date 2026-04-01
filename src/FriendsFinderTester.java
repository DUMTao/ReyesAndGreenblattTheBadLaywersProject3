import java.util.Random;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.lang.*;
import java.util.concurrent.Callable;
import java.util.Arrays;
import java.awt.Point;
import java.util.*;

/**
 * Code to test submitted solutions for the sorting project.
 *
 * @author Kyle Burke
 */
public class FriendsFinderTester extends SpeedGrader.CodeTester {

    private static final int MAX_SCORE = 100;

    private static final double MULTIPLIER_TO_TIME_OUT = 5.0;

    private static final int[] SIZES = new int[]{2, 3, 5, 10, 20, 30, 50, 75, 100, 200, 300, 400, 500, 600, 700, 800, 900, 1000, 2000, 3000, 4000, 5000, 6000, 7000, 8000, 9000, 10000, 20000, 25000, 30000, 35000, 40000, 50000, 100000, 200000, 400000, 800000, 1000000, 2000000, 4000000, 4500000, 5000000, 5250000, 5500000, 5750000, 6000000, 6250000, 6500000, 6750000, 7000000};

    public FriendsFinderTester() {
        //no state
    }

    public String getCodeAuthors() {
        return FriendsFinder.getAuthors();
    }

    /**
     * Runs the tests and prints feedback if asserts are enabled.
     */
    public Long call() {

        String message;
        boolean test;


        int n = this.problemSize;

        //System.out.println("Testing FriendsFinder on an array of size " + n + ".");
        Random random = new Random();

        List<Point> friends = new ArrayList<Point>(); //locations of people
        while (friends.size() < n) {
            int x = random.nextInt();
            int y = random.nextInt();
            Point p = new Point(x, y);
            friends.add(p);
        }



        //test the function
        long startTime = getSystemMillis();
        List<Point> studentFriends = FriendsFinder.nearestFriends(friends);
        long stopTime = getSystemMillis();

        test = studentFriends.size() <= 2;
        message = "Your code returned a list with too many (" + studentFriends.size() + ") points!";
        assert test : message;

        test = studentFriends.size() >= 2;
        message = "Your code didn't return enough (" + studentFriends.size() + ") points!";
        assert test : message;

        Point studentFriendA = studentFriends.get(0);
        Point studentFriendB = studentFriends.get(1);
        double studentDistance = studentFriendA.distance(studentFriendB);

        test = friends.contains(studentFriendA);
        message = "You returned a friend location (first one) that wasn't in the original list.";
        assert test : message;

        test = friends.contains(studentFriendB);
        message = "You returned a friend location (second one) that wasn't in the original list.";
        assert test : message;


        if (n < 10000) {

            boolean correct = checkClosest(friends, studentFriendA, studentFriendB);

            test = correct;
            message = "You found an incorrect smallest distance.  Your best friends are: " + studentFriendA + " and " + studentFriendB + ".";
            assert test : message;
        } else {
            System.out.println("I didn't check the correctness at this level.");
        }

        long time = stopTime-startTime;
        return time;

    }

    private boolean checkClosest(List<Point> friends, Point friendA, Point friendB) {
        double foundDist = Math.sqrt(((friendA.getX() - friendB.getX()) * (friendA.getX() - friendB.getX())) + ((friendB.getY() - friendA.getY()) * (friendB.getY() - friendA.getY())));
        double minDist = foundDist + 1;
        for (int i = 0; i < friends.size(); i++) {
            for (int j = 0; j < friends.size(); j++) {
                double dist = Math.sqrt(((friends.get(i).getX() - friends.get(j).getX()) * (friends.get(i).getX() - friends.get(j).getX())) + ((friends.get(j).getY() - friends.get(i).getY()) * (friends.get(j).getY() - friends.get(i).getY())));
                if (dist < minDist && i > j) {
                    minDist = dist;
                }
            }
        }
        return minDist == foundDist && friends.contains(friendA) && friends.contains(friendB);
    }


    public static void main(String[] args) throws Exception {
        SpeedGrader grader = new SpeedGrader();
        SpeedGrader.CodeTester tester = new FriendsFinderTester();
        grader.runTests(tester, MULTIPLIER_TO_TIME_OUT, MAX_SCORE, SIZES);
    }
}
package com.sdsmdg.harjot.materialshadows.utilities;

import java.util.Arrays;
import java.util.Stack;

public class GrahamScan {
    private Stack<Point2D> hull = new Stack<Point2D>();

    public GrahamScan(Point2D[] pts) {

        // defensive copy
        int N = pts.length;
        Point2D[] points = new Point2D[N];
        System.arraycopy(pts, 0, points, 0, N);
        Arrays.sort(points);

        Arrays.sort(points, 1, N, points[0].POLAR_ORDER);

        hull.push(points[0]); // p[0] is first extreme point
        int k1;
        for (k1 = 1; k1 < N; k1++)
            if (!points[0].equals(points[k1]))
                break;
        if (k1 == N)
            return; // all points equal

        int k2;
        for (k2 = k1 + 1; k2 < N; k2++)
            if (Point2D.ccw(points[0], points[k1], points[k2]) != 0)
                break;
        hull.push(points[k2 - 1]); // points[k2-1] is second extreme point

        for (int i = k2; i < N; i++) {
            Point2D top = hull.pop();
            while (Point2D.ccw(hull.peek(), top, points[i]) <= 0)
            {
                top = hull.pop();
            }
            hull.push(top);
            hull.push(points[i]);
        }

        assert isConvex();
    }

    public Iterable<Point2D> hull() {
        Stack<Point2D> s = new Stack<Point2D>();
        for (Point2D p : hull)
            s.push(p);
        return s;
    }

    private boolean isConvex() {
        int N = hull.size();
        if (N <= 2) {
            return true;
        }

        Point2D[] points = new Point2D[N];
        int n = 0;
        for (Point2D p : hull()){
            points[n++] = p;
        }

        for (int i = 0; i < N; i++){
            if (Point2D.ccw(points[i], points[(i + 1) % N], points[(i + 2) % N]) <= 0){
                return false;
            }
        }
        return true;
    }

}

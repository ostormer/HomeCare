package genetic_alg;

import java.awt.Color;
import java.awt.Graphics;
import java.util.LinkedList;

import javax.swing.JComponent;

public class RouteDisplayComponent extends JComponent {

    private static class Line{
        final int x1; 
        final int y1;
        final int x2;
        final int y2;   
        final Color color;

        public Line(int x1, int y1, int x2, int y2, Color color) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.color = color;
        }               
    }
    private static class Point{
        final int x; 
        final int y;
        final Color color;

        public Point(int x, int y, Color color) {
            this.x = x;
            this.y = y;
            this.color = color;
        }               
    }
    
    private Problem problem;
    private final LinkedList<Line> lines = new LinkedList<Line>();
    private final LinkedList<Point> points = new LinkedList<Point>();
    
    public RouteDisplayComponent(Problem problem) {
        super();
        this.problem = problem;
    }

    public void drawLinesFromSolution(Solution solution) {
        
    }

    public void addAllPoints() {
        for (Patient patient : this.problem.getPatients()) {
            addPoint(patient.getxCoord(), patient.getyCoord(), new Color(0, 0, 1));
        }
        repaint();
    }
    
    public void addLine(int x1, int x2, int x3, int x4, Color color) {
        lines.add(new Line(x1,x2,x3,x4, color));        
        // repaint();
    }

    public void clearLines() {
        lines.clear();
        // repaint();
    }
    
    public void addPoint(int x, int y, Color color) {
        points.add(new Point(x, y, color));
        // repaint();
    }
    
    public void clearPoints() {
        points.clear();
        // repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (Line line : lines) {
            g.setColor(line.color);
            g.drawLine(line.x1, line.y1, line.x2, line.y2);
        }
        for (Point point : points) {
            g.setColor(point.color);
            g.drawOval(point.x - 1, point.y - 1, 2, 2);
        }
        
    }
    


}

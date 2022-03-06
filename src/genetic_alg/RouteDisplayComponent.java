package genetic_alg;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

import javax.swing.JComponent;

public class RouteDisplayComponent extends JComponent {

    private static final long serialVersionUID = 1L;
    public static final int DRAW_FACTOR = 10;

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
        this.addAllPoints();
    }

    public void displaySolution(Solution solution) {
        Random rand = new Random();
        for (ArrayList<Patient> plan : solution.getNursePlans()) {
            if (plan.size() == 0) {
                continue;
            }
            // Select random color for nurse
            final float hue = rand.nextFloat();
            // Saturation between 0.1 and 0.3
            final float saturation = (rand.nextInt(2000) + 1000) / 10000f;
            final float luminance = 0.9f;
            final Color color = Color.getHSBColor(hue, saturation, luminance);
            // Draw lines
            addLine(problem.getxCoordDepot(),
                    problem.getyCoordDepot(),
                    plan.get(0).getxCoord(),
                    plan.get(0).getyCoord(),
                    color); // From depot
            for (int i=0; i<plan.size()-1; i++) {
                addLine(plan.get(i).getxCoord(),
                        plan.get(i).getyCoord(),
                        plan.get(i+1).getxCoord(),
                        plan.get(i+1).getyCoord(),
                        color); // Between patients
            }
            addLine(plan.get(plan.size()-1).getxCoord(),
                    plan.get(plan.size()-1).getyCoord(),
                    problem.getxCoordDepot(),
                    problem.getyCoordDepot(),
                    color); // To depot
        }
        repaint();
    }

    public void addAllPoints() {
        for (Patient patient : this.problem.getPatients()) {
            addPoint(patient.getxCoord(), patient.getyCoord(), new Color(0, 0, 255));
        }
        // Add depot
        addPoint(this.problem.getxCoordDepot(), this.problem.getyCoordDepot(), new Color(255, 0, 0));
        repaint();
    }
    
    public void addLine(int x1, int x2, int x3, int x4, Color color) {
        lines.add(new Line((x1 + 1) * DRAW_FACTOR,
                (x2 + 1) * DRAW_FACTOR,
                (x3 + 1) * DRAW_FACTOR,
                (x4 + 1) * DRAW_FACTOR,
                color));        
        // repaint();
    }
    
    public void addPoint(int x, int y, Color color) {
        points.add(new Point((x + 1) * DRAW_FACTOR, (y + 1) * DRAW_FACTOR, color));
        // repaint();
    }
    
    public void clearLines() {
        lines.clear();
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
            g.drawOval(point.x-DRAW_FACTOR/2, point.y-DRAW_FACTOR/2, DRAW_FACTOR, DRAW_FACTOR);
        }
        
    }
    


}

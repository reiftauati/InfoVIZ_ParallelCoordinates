package parallel_coords1;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.awt.geom.*;

import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;


public class MainPanel extends JPanel implements MouseListener, MouseMotionListener {

	boolean dirty;
	int w,h;
	List<Axis> axes;
	List<HyrumPolyline> lines;
	int numEntities;
	private int leftMargin = 40;
	private Rectangle box;
	private Point corner;

	public MainPanel() {
		super();
		dirty = false;
		axes = null;
		lines = new ArrayList<HyrumPolyline>();
		box = new Rectangle(-10, -10, 0, 0);
		corner = new Point();
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	public void setAxes(List<Axis> ax, int count) {
		axes = ax;
		dirty = true;
		numEntities = count;
		repaint();
	}

	private void prerender(Graphics g) {
		w = getWidth();
		h = getHeight();
		FontMetrics f = g.getFontMetrics();

		if (axes != null) {
			int xbuffer = (w-leftMargin*2) / (axes.size()-1);
			int ybuffer = 20;
			int x = leftMargin;
			lines.clear();
			for (Axis ax : axes) {
				Line2D dim = new Line2D.Float(x,ybuffer,x,h-ybuffer);
				ax.setDimensions(dim);
				x += xbuffer;
			}
			for (int i=0; i<numEntities; ++i) {
				//				System.out.println(axes.get(0).toString()
				//						+ "real value: " + axes.get(0).getPrintedValue(i)
				//						+ "; normalized value: " + axes.get(0).getNormalizedValue(i));
				HyrumPolyline line = new HyrumPolyline();
				for (int j=0; j<axes.size(); ++j) {
					line.addPoint(axes.get(j).getPixelPosition(i));
				}
				lines.add(line);
			}
		}
	}

	@Override
	public void paintComponent(Graphics g1) {
		if (w != getWidth() || h != getHeight() || dirty) {
			prerender(g1);
			dirty = false;
		}

		Graphics2D g = (Graphics2D)g1;

		//	draw blank background
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, getWidth(), getHeight());

		g.setColor(Color.MAGENTA);
		for (HyrumPolyline p : lines) {
			p.draw(g);
		}
		if (axes != null) {
			for (Axis x : axes) {
				x.draw(g);
			}
		}
		g.draw(box);
	}

	public void resetLines(){
		for(HyrumPolyline pl: lines){
			pl.highlighted = false;
			pl.faded = false;
		}
		repaint();
	}

	@Override
	public void mouseDragged(MouseEvent m) {
		int x = m.getX();
		int y = m.getY();
		box.setFrameFromDiagonal(corner.x, corner.y, x, y);
		Point mouseAt = new Point(x,y);
		double min = 1;
		//outerloop:
		
			for(HyrumPolyline pl: lines){
				pl.highlighted = false;
					for(int i=0;i<pl.points.size();i++){
						if(i<pl.getNumPoints()-1 && pl.faded == false){
							Point2D first = pl.points.get(i);
							Point2D second = pl.points.get(i+1);
							//System.out.println("distance is " + distance);
							if (box.intersectsLine(first.getX(), first.getY(), second.getX(), second.getY())){// && pl.points.get(i) == first && pl.points.get(i+1) == second){
								pl.highlighted = true;
								repaint();
							} 
						}
				}
			}
		//repaint();
		
	}

	@Override
	public void mouseMoved(MouseEvent m) {
		//do your tooltip stuff here
		int x = m.getX();
		int y = m.getY();
		Point mouseAt = new Point(x,y);
		double min = 999999;
		int minIndex = 0;
		//outerloop:
			for(int i=0;i<lines.size();i++){
				//pl.highlighted = false;
				for(int j=0;j<lines.get(i).points.size();j++){
					if(j<lines.get(i).getNumPoints()-1 && lines.get(i).faded == false){
						for(HyrumPolyline pl2: lines){ pl2.highlighted = false; }
						Point2D first = lines.get(i).points.get(j);
						Point2D second = lines.get(i).points.get(j+1);
						double distance = Line2D.ptSegDistSq(first.getX(), first.getY(), second.getX(), second.getY(), mouseAt.getX(), mouseAt.getY());
							if (distance < min && distance < lines.size()){	
								min = distance;
								minIndex = i;
								//i = pl.points.size();
								break; //outerloop;
							} else { lines.get(i).highlighted = false;  }
						}
					}
					lines.get(minIndex).highlighted = true;
					String tooltip = "";
						for(int k = 0; k<axes.size(); k++){
							tooltip += axes.get(k).getPrintedValue(minIndex)+" ";
							//System.out.println(tooltip);
							
						}
					setToolTipText(tooltip);
				}
		
		
		repaint();
	}

	/**

	 * 
	 * @param arg0
	 */
	
	@Override
	public void mouseClicked(MouseEvent arg0) {}

	@Override
	public void mouseEntered(MouseEvent arg0) {}

	@Override
	public void mouseExited(MouseEvent arg0) {}

	@Override
	public void mousePressed(MouseEvent m) {
		corner.x = m.getX();
		corner.y = m.getY();
		box.setLocation(corner.x, corner.y);
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		for(HyrumPolyline pl: lines){
			if(pl.highlighted == false){
				pl.faded = true;                   
			}
		repaint();
		}
	}

}

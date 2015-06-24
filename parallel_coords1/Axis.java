package parallel_coords1;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class Axis {
	private String columnName;
	private String columnType;
	List<Object> values;
	List<Double> normalizedValues;
	private Line2D line;
	List<String> yLabels;
	private enum DataType {
		TEXT,
		NUMERIC
	}
	private DataType dataType;

	public Axis(String n, String t) {
		columnName = n;
		columnType = t;
		values = new ArrayList<Object>();
		yLabels = new ArrayList<String>();
		dataType = DataType.NUMERIC;
	}

	public void setDimensions(Line2D coords) {
		line = coords;
	}

	public void draw(Graphics2D g) {
		g.setColor(Color.BLACK);
		g.draw(line);
		int textWidth = g.getFontMetrics().stringWidth(columnName);
		g.drawString(columnName, (int)(line.getX1()-textWidth/2), (int)(line.getY2()+g.getFontMetrics().getHeight()));

		g.setColor(Color.RED);
		if (dataType == DataType.NUMERIC) {
			double labelSpacing = (line.getY2() - line.getY1()) / (yLabels.size()-1);
			for (int i=0; i<yLabels.size(); ++i) {
				g.drawString(yLabels.get(i), (int)line.getX1()+2, (int)(line.getY2()-i*labelSpacing));
			}
		} else {
			double labelSpacing = (line.getY2() - line.getY1()) / (yLabels.size()+1);
			for (int i=0; i<yLabels.size(); ++i) {
				g.drawString(yLabels.get(i), (int)line.getX1()+2, (int)(line.getY2()-(i+1)*labelSpacing));
			}

		}


	}

	public void extractData(ResultSet rs) throws SQLException {
		if (columnType.equalsIgnoreCase("char")
				|| columnType.equalsIgnoreCase("varchar")) {
			values.add(rs.getString(columnName));
			dataType = DataType.TEXT;
		}
		if (columnType.equalsIgnoreCase("smallint")
				|| columnType.equalsIgnoreCase("double")
				|| columnType.equalsIgnoreCase("int")) {
			values.add(rs.getDouble(columnName));			
		}
	}

	public void debug() {
		for (int i=0; i<values.size(); ++i) {
			System.out.println(columnName + " row#" + i + " = " + values.get(i));
		}

	}

	public void normalize() {
		normalizedValues = new ArrayList<Double>();
		Set<String> tmp = new TreeSet<String>();
		Map<String, Double> map = new HashMap<String, Double>();
		for (Object v : values) {
			//set all values to fractions between zero and 1
			if (dataType == DataType.NUMERIC) {
				normalizedValues.add((Double)v);
			} else {
				tmp.add((String)v);
			}
		}
		//if they're strings, do this...
		if (dataType == DataType.TEXT) {
			double delta = 1.0 / (tmp.size()+1);
			double y = delta;
			for (String s : tmp) {
				map.put(s, y);
				y += delta;
				yLabels.add(s);
			}
			for (Object v : values) {
				normalizedValues.add(map.get((String)v));
			}
		} else {
			//if they're numbers, do this...
			double max = Collections.max(normalizedValues);
			double min = Collections.min(normalizedValues);
			final int numYAxisLabels = 5;
			double delta = (max-min)/(numYAxisLabels-1);
			for (int i=0; i<numYAxisLabels-1; ++i) {
				yLabels.add(String.format("%.2f", min+i*delta));
			}
			yLabels.add(String.format("%.2f", max));

			//okay, now normalize
			for (int i=0; i<values.size(); ++i) {
				double d = normalizedValues.get(i);
				d = (d-min) / (max-min);
				normalizedValues.set(i, d);
			}
		}
	}

	public double getNormalizedValue(int i) {
		return normalizedValues.get(i);
	}

	public Point2D getPixelPosition(int i) {
		double y = line.getY2()-((line.getY2()-line.getY1())*normalizedValues.get(i));
		return new Point2D.Double(line.getX1(), y);
	}

	public String getPrintedValue(int i) {
		return columnName + ": " + values.get(i).toString() + " ";
	}

	@Override
	public String toString() {
		return columnName;
	}
}

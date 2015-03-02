/*
 * #%L
 * Cell Counter plugin for ImageJ.
 * %%
 * Copyright (C) 2007 - 2015 Kurt De Vos and Board of Regents of the
 * University of Wisconsin-Madison.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

// Created on December 13, 2005, 8:40 AM

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ListIterator;
import java.util.Vector;

/**
 * TODO
 *
 * @author Kurt De Vos
 */
public class CellCntrMarkerVector extends Vector<CellCntrMarker> {

	private int type;
	private Color color;

	/** Creates a new instance of MarkerVector */
	public CellCntrMarkerVector(final int type) {
		super();
		this.type = type;
		color = createColor(type);
	}

	public void addMarker(final CellCntrMarker marker) {
		add(marker);
	}

	public CellCntrMarker getMarker(final int n) {
		return get(n);
	}

	public int getVectorIndex(final CellCntrMarker marker) {
		return indexOf(marker);
	}

	public void removeMarker(final int n) {
		remove(n);
	}

	public void removeLastMarker() {
		super.removeElementAt(size() - 1);
	}

	private Color createColor(final int typeID) {
		switch (typeID) {
			case (1):
				return Color.blue;
			case (2):
				return Color.cyan;
			case (3):
				return Color.green;
			case (4):
				return Color.magenta;
			case (5):
				return Color.orange;
			case (6):
				return Color.pink;
			case (7):
				return Color.red;
			case (8):
				return Color.yellow;
			default:
				Color c =
					new Color((int) (255 * Math.random()), (int) (255 * Math.random()),
						(int) (255 * Math.random()));
				while (c.equals(Color.blue) | c.equals(Color.cyan) |
					c.equals(Color.green) | c.equals(Color.magenta) |
					c.equals(Color.orange) | c.equals(Color.pink) | c.equals(Color.red) |
					c.equals(Color.yellow))
				{
					c =
						new Color((int) (255 * Math.random()), (int) (255 * Math.random()),
							(int) (255 * Math.random()));
				}
				return c;
		}
	}

	private boolean isCloser(final CellCntrMarker m1, final CellCntrMarker m2,
		final Point p)
	{
		final Point2D p1 = new Point2D.Double(m1.getX(), m1.getY());
		final Point2D p2 = new Point2D.Double(m1.getX(), m2.getY());
		System.out.println("px = " + p.x + " py = " + p.y);
		System.out.println(Math.abs(p1.distance(p)) + " < " +
			Math.abs(p2.distance(p)));
		return (Math.abs(p1.distance(p)) < Math.abs(p2.distance(p)));
	}

	public CellCntrMarker getMarkerFromPosition(final Point p,
		final int sliceIndex)
	{
		final Vector<CellCntrMarker> v = new Vector<CellCntrMarker>();
		final ListIterator<CellCntrMarker> it = this.listIterator();
		while (it.hasNext()) {
			final CellCntrMarker m = it.next();
			if (m.getZ() == sliceIndex) {
				v.add(m);
			}
		}
		CellCntrMarker currentsmallest = v.get(0);
		for (int i = 1; i < v.size(); i++) {
			final CellCntrMarker m2 = v.get(i);
			final Point p1 =
				new Point(currentsmallest.getX(), currentsmallest.getY());
			final Point p2 = new Point(m2.getX(), m2.getY());
			final boolean closer =
				Math.abs(p1.distance(p)) > Math.abs(p2.distance(p));
			if (closer) {
				currentsmallest = m2;
			}
		}

		return currentsmallest;
	}

	public int getType() {
		return type;
	}

	public void setType(final int type) {
		this.type = type;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(final Color color) {
		this.color = color;
	}

}

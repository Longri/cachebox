/*
 * Copyright 2010, 2011, 2012, 2013 mapsforge.org
 * Copyright 2014 Ludwig M Brinckmann
 * Copyright 2016 devemux86
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.mapsforge.map.layer;

import org.mapsforge.map.model.DisplayModel;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.RandomAccess;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A thread-safe {@link Layer} list which does not allow {@code null} elements.
 */
public class Layers implements Iterable<Layer>, RandomAccess {

    private final DisplayModel displayModel;
    private final List<Layer> layersList;
    private final Redrawer redrawer;
    Layers(Redrawer redrawer, DisplayModel displayModel) {
        this.redrawer = redrawer;
        this.displayModel = displayModel;

        this.layersList = new CopyOnWriteArrayList<Layer>();
    }

    private static void checkIsNull(Collection<Layer> layers) {
        if (layers == null) {
            throw new IllegalArgumentException("layers must not be null");
        }

        for (Layer layer : layers) {
            checkIsNull(layer);
        }
    }

    private static void checkIsNull(Layer layer) {
        if (layer == null) {
            throw new IllegalArgumentException("layer must not be null");
        }
    }

    /**
     * Note: By default a redraw will take place afterwards.
     *
     * @see List#add(int, Object)
     */
    public synchronized void add(int index, Layer layer) {
        add(index, layer, true);
    }

    /**
     * @see List#add(int, Object)
     */
    public synchronized void add(int index, Layer layer, boolean redraw) {
        checkIsNull(layer);
        layer.setDisplayModel(this.displayModel);
        this.layersList.add(index, layer);
        layer.assign(this.redrawer);
        if (redraw) {
            this.redrawer.redrawLayers();
        }
    }

    /**
     * Note: By default a redraw will take place afterwards.
     *
     * @see List#add(Object)
     */
    public synchronized void add(Layer layer) {
        add(layer, true);
    }

    /**
     * @see List#add(Object)
     */
    public synchronized void add(Layer layer, boolean redraw) {
        checkIsNull(layer);
        layer.setDisplayModel(this.displayModel);

        this.layersList.add(layer);
        layer.assign(this.redrawer);
        if (redraw) {
            this.redrawer.redrawLayers();
        }
    }

    /**
     * Note: By default a redraw will take place afterwards.
     *
     * @see List#addAll(Collection)
     */
    public synchronized void addAll(Collection<Layer> layers) {
        addAll(layers, true);
    }

    /**
     * @see List#addAll(Collection)
     */
    public synchronized void addAll(Collection<Layer> layers, boolean redraw) {
        checkIsNull(layers);
        for (Layer layer : layers) {
            layer.setDisplayModel(this.displayModel);
        }
        this.layersList.addAll(layers);
        for (Layer layer : layers) {
            layer.assign(this.redrawer);
        }
        if (redraw) {
            this.redrawer.redrawLayers();
        }
    }

    /**
     * Note: By default a redraw will take place afterwards.
     *
     * @see List#addAll(int, Collection)
     */
    public synchronized void addAll(int index, Collection<Layer> layers) {
        addAll(index, layers, true);
    }

    /**
     * @see List#addAll(int, Collection)
     */
    public synchronized void addAll(int index, Collection<Layer> layers, boolean redraw) {
        checkIsNull(layers);
        this.layersList.addAll(index, layers);
        for (Layer layer : layers) {
            layer.setDisplayModel(this.displayModel);
            layer.assign(this.redrawer);
        }
        if (redraw) {
            this.redrawer.redrawLayers();
        }
    }

    /**
     * Note: By default a redraw will take place afterwards.
     *
     * @see List#clear()
     */
    public synchronized void clear() {
        clear(true);
    }

    /**
     * @see List#clear()
     */
    public synchronized void clear(boolean redraw) {
        for (Layer layer : this.layersList) {
            layer.unassign();
        }
        this.layersList.clear();
        if (redraw) {
            this.redrawer.redrawLayers();
        }
    }

    /**
     * @see List#contains(Object)
     */
    public synchronized boolean contains(Layer layer) {
        checkIsNull(layer);
        return this.layersList.contains(layer);
    }

    /**
     * @see List#get(int)
     */
    public synchronized Layer get(int index) {
        return this.layersList.get(index);
    }

    /**
     * @see List#indexOf(Object)
     */
    public synchronized int indexOf(Layer layer) {
        checkIsNull(layer);
        return this.layersList.indexOf(layer);
    }

    /**
     * @see List#isEmpty()
     */
    public synchronized boolean isEmpty() {
        return this.layersList.isEmpty();
    }

    /**
     * @see List#iterator()
     */
    @Override
    public synchronized Iterator<Layer> iterator() {
        return this.layersList.iterator();
    }

    /**
     * Note: By default a redraw will take place afterwards.
     *
     * @see List#remove(int)
     */
    public synchronized Layer remove(int index) {
        return remove(index, true);
    }

    /**
     * @see List#remove(int)
     */
    public synchronized Layer remove(int index, boolean redraw) {
        Layer layer = this.layersList.remove(index);
        layer.unassign();
        if (redraw) {
            this.redrawer.redrawLayers();
        }
        return layer;
    }

    /**
     * Note: By default a redraw will take place afterwards.
     *
     * @see List#remove(Object)
     */
    public synchronized boolean remove(Layer layer) {
        return remove(layer, true);
    }

    /**
     * @see List#remove(Object)
     */
    public synchronized boolean remove(Layer layer, boolean redraw) {
        checkIsNull(layer);
        if (this.layersList.remove(layer)) {
            layer.unassign();
            if (redraw) {
                this.redrawer.redrawLayers();
            }
            return true;
        }
        return false;
    }

    /**
     * @see List#size()
     */
    public synchronized int size() {
        return this.layersList.size();
    }
}

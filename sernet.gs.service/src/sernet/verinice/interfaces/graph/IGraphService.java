/*******************************************************************************
 * Copyright (c) 2013 Daniel Murygin.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.interfaces.graph;

import java.util.List;

/**
 * A service to load and analyze the element network of verinice with JGraphT.
 * 
 * JGraphT is a free Java graph library that provides mathematical graph-theory
 * objects and algorithms.
 * 
 * Call one of the filter methods to configure which elements are loaded by the
 * service: setScopeId(Integer scopeId) setTypeIds(String[] typeIds)
 * setRelationIds(String[] relationIds)
 * IGraphElementLoader.setElementFilter(IElementFilter elementFilter)
 * 
 * You have to call "create()" to initialize the service. After creation get the
 * result by calling getGraph()
 * 
 * @see http://jgrapht.org/
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public interface IGraphService {

    /**
     * Initializes and creates the VeriniceGraph.
     */
    VeriniceGraph create(List<? extends IGraphElementLoader> loaderList);

    /**
     * Initializes and creates the VeriniceGraph.
     * 
     * @param loadLinks
     *            Disables the loading of links if {@code false} regardless of
     *            the relation ids.
     */
    VeriniceGraph create(List<? extends IGraphElementLoader> loaderList, boolean loadLinks);

    /**
     * Initializes and creates the VeriniceGraph. The loading of links will be
     * enabled.
     */
    VeriniceGraph create(List<? extends IGraphElementLoader> loaderList, String[] relationIds);

    /**
     * Initializes and creates the VeriniceGraph.
     * 
     * @param loadLinks
     *            Disables the loading of links if {@code false} regardless of
     *            the relation ids.
     * @param loaderList
     *            One or more IGraphElementLoader that loads elements which are
     *            part of the graph
     */
    VeriniceGraph create(List<? extends IGraphElementLoader> loaderList, String[] relationIds,
            boolean loadLinks);

    /**
     * Initializes and creates a directed verinice graph. The loading of links
     * will be enabled.
     * 
     * @return
     */

    VeriniceGraph createDirectedGraph(List<? extends IGraphElementLoader> loaderList);

    /**
     * Initializes and creates a directed verinice graph..
     * 
     * @param loadLinks
     *            Disables the loading of links if {@code false} regardless of
     *            the relation ids.
     */
    VeriniceGraph createDirectedGraph(List<? extends IGraphElementLoader> loaderList,
            boolean loadLinks);

    /**
     * Initializes and creates a directed verinice graph..
     * 
     * @param loadLinks
     *            Disables the loading of links if {@code false} regardless of
     *            the relation ids.
     * @param loaderList
     *            One or more IGraphElementLoader that loads elements which are
     *            part of the graph
     */
    VeriniceGraph createDirectedGraph(List<? extends IGraphElementLoader> loaderList,
            String[] relationIds, boolean loadLinks);

}

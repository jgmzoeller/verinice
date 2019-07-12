/*******************************************************************************
 * Copyright (c) 2009  Daniel Murygin <dm[at]sernet[dot]de>,
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public
 * License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *      Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp.tree;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import sernet.gs.service.StringUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.views.CnAImageProvider;
import sernet.gs.ui.rcp.main.common.model.CnATreeElementLabelGenerator;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Label provider for ISO 27000 model elements.
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class TreeLabelProvider extends LabelProvider {

    private static final Logger LOG = Logger.getLogger(TreeLabelProvider.class);
    private static final int MAX_TEXT_WIDTH = 80;

    @Override
    public Image getImage(Object obj) {
        Image image = ImageCache.getInstance().getImage(ImageCache.UNKNOWN);
        try {
            if (!(obj instanceof CnATreeElement)) {
                return image;
            } else {
                return CnAImageProvider.getImage((CnATreeElement) obj);
            }
        } catch (Exception e) {
            LOG.error("Error while getting image for tree item.", e);
            return image;
        }
    }

    @Override
    public String getText(Object obj) {
        String text = "unknown";
        if (!(obj instanceof CnATreeElement)) {
            return text;
        }
        try {
            CnATreeElement element = (CnATreeElement) obj;
            String title = CnATreeElementLabelGenerator.getElementTitle(element);
            text = StringUtil.truncate(title, MAX_TEXT_WIDTH);
            if (LOG.isDebugEnabled()) {
                text = text + " (db: " + element.getDbId() + ", uu: " + element.getUuid()
                        + ", scope: " + element.getScopeId() + ", ext: " + element.getExtId() + ")";
            }
        } catch (Exception e) {
            LOG.error("Error while getting label for tree item.", e);
        }
        return text;
    }

}
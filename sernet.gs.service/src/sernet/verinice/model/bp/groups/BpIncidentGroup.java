/*******************************************************************************
 * Copyright (c) 2018 Jochen Kemnade <jk[at]sernet[dot]de>.
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
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 *
 ******************************************************************************/

package sernet.verinice.model.bp.groups;

import java.util.Collection;

import sernet.hui.common.connect.ITaggableElement;
import sernet.verinice.model.bp.IBpGroup;
import sernet.verinice.model.bp.elements.BpIncident;
import sernet.verinice.model.bsi.TagHelper;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Group;

@SuppressWarnings("serial")
public class BpIncidentGroup extends Group<BpIncident> implements IBpGroup, ITaggableElement {

    public static final String TYPE_ID = "bp_incident_group"; //$NON-NLS-1$
    public static final String TYPE_ID_HIBERNATE = "bp_incidentgroup"; //$NON-NLS-1$

    public static final String PROP_NAME = "bp_incident_group_name"; //$NON-NLS-1$
    public static final String PROP_TAG = "bp_incident_group_tag"; //$NON-NLS-1$

    public static final String[] CHILD_TYPES = new String[] { BpIncident.TYPE_ID };

    public BpIncidentGroup() {
        super();
    }

    public BpIncidentGroup(CnATreeElement parent) {
        super(parent);
        init();
    }

    /*
     * @see sernet.gs.ui.rcp.main.common.model.CnATreeElement#getTypeId()
     */
    @Override
    public String getTypeId() {
        return TYPE_ID;
    }

    @Override
    public String getTitle() {
        return getEntity().getPropertyValue(PROP_NAME);
    }

    @Override
    public void setTitel(String name) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_NAME), name);
    }

    /*
     * @see sernet.verinice.iso27k.model.Group#getChildTypes()
     */
    @Override
    public String[] getChildTypes() {
        return CHILD_TYPES;
    }

    @Override
    public Collection<String> getTags() {
        return TagHelper.getTags(getEntity().getPropertyValue(PROP_TAG));
    }
}

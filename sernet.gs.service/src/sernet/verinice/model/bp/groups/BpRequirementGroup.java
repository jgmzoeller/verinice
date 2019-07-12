/*******************************************************************************
 * Copyright (c) 2017 Sebastian Hagedorn.
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
 *     Sebastian Hagedorn sh[at]sernet.de - initial API and implementation
 ******************************************************************************/
package sernet.verinice.model.bp.groups;

import java.util.Collection;
import java.util.Date;

import sernet.hui.common.connect.IIdentifiableElement;
import sernet.hui.common.connect.ITaggableElement;
import sernet.verinice.model.bp.IBpGroup;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bsi.TagHelper;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Group;

/**
 * 
 * @author Sebastian Hagedorn sh[at]sernet.de
 */
public class BpRequirementGroup extends Group<BpRequirement>
        implements IBpGroup, IIdentifiableElement, ITaggableElement {

    private static final long serialVersionUID = 7752776589962581996L;

    public static final String TYPE_ID = "bp_requirement_group";
    public static final String PROP_NAME = "bp_requirement_group_name"; //$NON-NLS-1$
    private static final String PROP_OBJECTBROWSER_DESC = "bp_requirement_group_objectbrowser_content"; //$NON-NLS-1$
    private static final String PROP_ID = "bp_requirement_group_id"; //$NON-NLS-1$
    public static final String PROP_TAG = "bp_requirement_group_tag"; //$NON-NLS-1$
    private static final String PROP_LAST_CHANGE = "bp_requirement_group_last_change"; //$NON-NLS-1$

    private static final String PROP_IMPLEMENTATION_ORDER = "bp_requirement_group_impl_seq"; //$NON-NLS-1$

    public static final String[] CHILD_TYPES = new String[] { BpRequirement.TYPE_ID,
            BpRequirementGroup.TYPE_ID };

    protected BpRequirementGroup() {
    }

    public BpRequirementGroup(CnATreeElement parent) {
        super(parent);
        init();
    }

    @Override
    public String getTitle() {
        return getEntity().getPropertyValue(PROP_NAME);
    }

    @Override
    public void setTitel(String name) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_NAME), name);
    }

    @Override
    public String getTypeId() {
        return TYPE_ID;
    }

    @Override
    public String[] getChildTypes() {
        return CHILD_TYPES;
    }

    public String getObjectBrowserDescription() {
        return getEntity().getPropertyValue(PROP_OBJECTBROWSER_DESC);
    }

    public void setObjectBrowserDescription(String description) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_OBJECTBROWSER_DESC),
                description);
    }

    @Override
    public String getIdentifier() {
        return getEntity().getPropertyValue(PROP_ID);
    }

    public void setIdentifier(String id) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_ID), id);
    }

    public Date getLastChange() {
        return getEntity().getDate(PROP_LAST_CHANGE);
    }

    public void setLastChange(Date date) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_LAST_CHANGE),
                String.valueOf(date.getTime()));
    }

    public String getImplementationOrder() {
        return getEntity().getPropertyValue(PROP_IMPLEMENTATION_ORDER);
    }

    public void setImplementationOrder(String implementationSequence) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_IMPLEMENTATION_ORDER),
                implementationSequence);
    }

    @Override
    public String getFullTitle() {
        return joinPrefixAndTitle(getIdentifier(), getTitle());
    }
    
    @Override
    public Collection<String> getTags() {
        return TagHelper.getTags(getEntity().getPropertyValue(PROP_TAG));
    }

}

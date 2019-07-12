/*******************************************************************************
 * Copyright (c) 2009 Daniel Murygin <dm[at]sernet[dot]de>.
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
 *     Daniel <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.model.iso27k;

import java.util.Collection;

import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.IAbbreviatedElement;
import sernet.hui.common.connect.ITaggableElement;
import sernet.verinice.interfaces.IReevaluator;
import sernet.verinice.model.bsi.TagHelper;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.ILinkChangeListener;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@SuppressWarnings("serial")
public class Process extends CnATreeElement
        implements IISO27kElement, IAbbreviatedElement, ITaggableElement {

	public static final String TYPE_ID = "process"; //$NON-NLS-1$
	public static final String PROP_ABBR = "process_abbr"; //$NON-NLS-1$
	public static final String PROP_NAME = "process_name"; //$NON-NLS-1$
	public static final String PROP_TAG = "process_tag"; //$NON-NLS-1$
    public static final String PROP_USER = "process_user"; //$NON-NLS-1$
    public static final String PROP_VALUE_USER_1 = "process_user_1"; //$NON-NLS-1$
    public static final String PROP_VALUE_USER_2 = "process_user_2"; //$NON-NLS-1$
    public static final String PROP_VALUE_USER_3 = "process_user_3"; //$NON-NLS-1$
	public static final String PROCESS_VALUE_CONFIDENTIALITY = "process_value_confidentiality"; //$NON-NLS-1$
	public static final String PROCESS_VALUE_INTEGRITY = "process_value_integrity"; //$NON-NLS-1$
	public static final String PROCESS_VALUE_AVAILABILITY = "process_value_availability"; //$NON-NLS-1$
	public static final String REL_PROCESS_ASSET = "rel_process_asset"; //$NON-NLS-1$
	
    private final IReevaluator protectionRequirementsProvider = new ProtectionRequirementsValueAdapter(this);
    private final ILinkChangeListener linkChangeListener = new MaximumProtectionRequirementsValueListener(this);

    @Override
    public ILinkChangeListener getLinkChangeListener() {
        return linkChangeListener;
    }

    @Override
    public IReevaluator getProtectionRequirementsProvider() {
        return protectionRequirementsProvider;
    }

	/**
	 * Creates an empty asset
	 */
	public Process() {
		super();
		setEntity(new Entity(TYPE_ID));
        getEntity().initDefaultValues(getTypeFactory());
	}
	
	public Process(CnATreeElement parent) {
		super(parent);
		setEntity(new Entity(TYPE_ID));
		getEntity().initDefaultValues(getTypeFactory());
        // sets the localized title via HUITypeFactory from message bundle
        setTitel(getTypeFactory().getMessage(TYPE_ID));
    }
	
	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.common.model.CnATreeElement#getTypeId()
	 */
	@Override
	public String getTypeId() {
		return TYPE_ID;
	}
	
	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.common.model.CnATreeElement#getTitel()
	 */
	@Override
	public String getTitle() {
		return getEntity().getSimpleValue(PROP_NAME);
	}
	
	@Override
    public void setTitel(String name) {
		getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_NAME), name);
	}
	
	@Override
    public String getAbbreviation() {
		return getEntity().getSimpleValue(PROP_ABBR);
	}
	
	public void setAbbreviation(String abbreviation) {
		getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_ABBR), abbreviation);
	}
	
	@Override
    public Collection<String> getTags() {
		return TagHelper.getTags(getEntity().getSimpleValue(PROP_TAG));
	}

}

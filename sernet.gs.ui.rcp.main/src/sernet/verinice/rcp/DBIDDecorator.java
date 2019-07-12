/*******************************************************************************
 * Copyright (c) 2012 Sebastian Hagedorn <sh@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Sebastian Hagedorn <sh@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp;

import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Audit;

/**
 *
 */
public class DBIDDecorator extends LabelProvider implements ILightweightLabelDecorator {

    /*
     * @see
     * org.eclipse.jface.viewers.ILightweightLabelDecorator#decorate(java.lang.
     * Object, org.eclipse.jface.viewers.IDecoration)
     */
    @Override
    public void decorate(Object o, IDecoration decoration) {
        if (!(o instanceof CnATreeElement)) {
            return;
        }
        CnATreeElement elmt = (CnATreeElement) o;
        if ((elmt.isScope() || elmt instanceof Audit) && Activator.getDefault()
                .getPluginPreferences().getBoolean(PreferenceConstants.SHOW_DBID_DECORATOR)) {
            decoration.addSuffix(new StringBuilder().append(" <").append((elmt.getDbId()))
                    .append(">").toString());
        }
    }

}

/*******************************************************************************
 * Copyright (c) 2017 Daniel Murygin <dm{a}sernet{dot}de>.
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
 *     Daniel Murygin <dm{a}sernet{dot}de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.bp.rcp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.dnd.TransferData;

import sernet.gs.ui.rcp.main.bsi.dnd.transfer.BaseProtectionModelingTransfer;
import sernet.gs.ui.rcp.main.bsi.dnd.transfer.VeriniceElementTransfer;
import sernet.hui.common.VeriniceContext;
import sernet.springclient.RightsServiceClient;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.RightEnabledUserInteraction;
import sernet.verinice.iso27k.rcp.action.DropPerformer;
import sernet.verinice.iso27k.rcp.action.MetaDropAdapter;
import sernet.verinice.model.bp.elements.Application;
import sernet.verinice.model.bp.elements.BusinessProcess;
import sernet.verinice.model.bp.elements.Device;
import sernet.verinice.model.bp.elements.IcsSystem;
import sernet.verinice.model.bp.elements.ItSystem;
import sernet.verinice.model.bp.elements.Network;
import sernet.verinice.model.bp.elements.Room;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.rcp.catalog.CatalogDragListener;

/**
 * This drop performer class starts the modeling process
 * of IT base protection after one or more modules
 * are dragged from sernet.verinice.rcp.catalog.CatalogView 
 * and dropped on an element in BaseProtectionView.
 *
 * @see CatalogDragListener
 * @see MetaDropAdapter
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public class BbModelingDropPerformer implements DropPerformer, RightEnabledUserInteraction {

    private static final Logger log = Logger.getLogger(BbModelingDropPerformer.class);
    private static final List<String> supportedDropTypeIds;
    static {
        supportedDropTypeIds = new ArrayList<>(8);
        supportedDropTypeIds.add(Application.TYPE_ID);
        supportedDropTypeIds.add(BusinessProcess.TYPE_ID);
        supportedDropTypeIds.add(Device.TYPE_ID);
        supportedDropTypeIds.add(IcsSystem.TYPE_ID);
        supportedDropTypeIds.add(ItSystem.TYPE_ID);
        supportedDropTypeIds.add(Network.TYPE_ID);
        supportedDropTypeIds.add(Room.TYPE_ID);
    }
    
    private boolean isActive = false;
    private CnATreeElement targetElement = null; 

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.iso27k.rcp.action.DropPerformer#performDrop(java.lang.
     * Object, java.lang.Object, org.eclipse.jface.viewers.Viewer)
     */
    @Override
    public boolean performDrop(Object data, Object target, Viewer viewer) {
        List<CnATreeElement> draggedModules = getDraggedElements(data);
        if (log.isDebugEnabled()) {
            logParameter(draggedModules, targetElement);
        }  
        modelModulesAndElement(draggedModules,targetElement);
        return true;
    }
    

    private void modelModulesAndElement(List<CnATreeElement> draggedModules,
            CnATreeElement element) {
        // TODO Auto-generated method stub      
    }

    private List<CnATreeElement> getDraggedElements(Object data) {
        List<CnATreeElement> elementList = null;
        if(data instanceof Object[]) {
            elementList = new LinkedList<>();
            for (Object o : (Object[])data) {
                if(o instanceof CnATreeElement) {
                    elementList.add((CnATreeElement) o);
                }
            }
        } else {
            elementList = Collections.emptyList();
        }
        return elementList;
    }

    /* 
     * @see
     * sernet.verinice.iso27k.rcp.action.DropPerformer#validateDrop(java.lang.
     * Object, int, org.eclipse.swt.dnd.TransferData)
     */
    @Override
    public boolean validateDrop(Object rawTarget, int operation, TransferData transferData) {
        if (!checkRights()) {
            log.debug("ChechRights() failed, return false");
            isActive = false;
        } else {
            if (!getTransfer().isSupportedType(transferData)) {
                log.debug("Unsupported type of TransferData");
                this.targetElement = null;
            } else {
                this.targetElement = getTargetElement(rawTarget);
            }
            isActive = isTargetElement();
        }
        return isActive;   
    }

    /*
     * @see sernet.verinice.iso27k.rcp.action.DropPerformer#isActive()
     */
    @Override
    public boolean isActive() {
        return isActive;
    }
    
    /*
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#checkRights()
     */
    @Override
    public boolean checkRights() {
        RightsServiceClient service = (RightsServiceClient) VeriniceContext.get(VeriniceContext.RIGHTS_SERVICE);
        return service.isEnabled(getRightID());
    }

    /*
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#getRightID()
     */
    @Override
    public String getRightID() {
        return ActionRightIDs.BASEPROTECTIONMODELING;
    }

    /*
     * @see
     * sernet.verinice.interfaces.RightEnabledUserInteraction#setRightID(java.
     * lang.String)
     */
    @Override
    public void setRightID(String rightID) {
        // nothing to do
    }
    
    private CnATreeElement getTargetElement(Object target) {
        if (log.isDebugEnabled()) {
            log.debug("Target: " + target);
        }
        CnATreeElement element = null;
        if (target instanceof CnATreeElement) {
            element = (CnATreeElement) target;
            if(!supportedDropTypeIds.contains(element.getTypeId())) {
                if (log.isDebugEnabled()) {
                    log.debug("Unsupported type of target element: " + element.getTypeId());
                }
                element = null;
            }
        } 
        return element;
    }

    protected boolean isTargetElement() {
        return this.targetElement!=null;
    }

    protected VeriniceElementTransfer getTransfer() {
        return BaseProtectionModelingTransfer.getInstance();
    }
    
    private void logParameter(List<CnATreeElement> draggedElements, CnATreeElement targetElementParam) {
        log.debug("Module(s):");
        for (CnATreeElement module : draggedElements) {
            log.debug(module);
        }
        log.debug("is/are modeled with: " + targetElementParam + "...");
    }

}

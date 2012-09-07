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
package sernet.gs.ui.rcp.main.service.crudcommands;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.model.bsi.Attachment;
import sernet.verinice.service.commands.LoadAttachments;

/**
 *
 */
public class LoadReportISAAttachmentImageCount extends GenericCommand {
    
    private static final Logger LOG = Logger.getLogger(LoadReportISAAttachmentImageCount.class);
    
    private Integer rootElmt;
    
    private List<String> results;
    
    public static final String[] COLUMNS = new String[] { 
        "imageNr"
    };
    
    private static final String[] IMAGEMIMETYPES = new String[]{
        "jpg",
        "png"
    };

    public LoadReportISAAttachmentImageCount(){
        // do nothing
    }
    
    public LoadReportISAAttachmentImageCount(Integer root){
        this.rootElmt = root;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        results = new ArrayList<String>(0);
        LoadAttachments attachmentLoader = new LoadAttachments(rootElmt);
        int count = 0;
        try {
            attachmentLoader = ServiceFactory.lookupCommandService().executeCommand(attachmentLoader);
            for(Attachment attachment : attachmentLoader.getAttachmentList()){
                    if(isSupportedMIMEType(attachment.getMimeType())){
                        results.add(String.valueOf(count));
                        count++;
                    }
            }
        } catch (CommandException e){
            LOG.error("Error while executing command", e);
        }
    }
    
    public void setRoot(Integer root){
        this.rootElmt = root;
    }
    
    private boolean isSupportedMIMEType(String mimetype){
        for(String s : IMAGEMIMETYPES){
            if(s.equalsIgnoreCase(mimetype)){
               return true; 
            }
        }
        return false;
    }
    
    public List<String> getResult(){
        return results;
    }

}

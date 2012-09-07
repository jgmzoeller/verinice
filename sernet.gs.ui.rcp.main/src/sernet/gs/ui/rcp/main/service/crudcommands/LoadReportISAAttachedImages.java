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
import sernet.verinice.service.commands.LoadAttachmentFile;
import sernet.verinice.service.commands.LoadAttachments;

/**
 *
 */
public class LoadReportISAAttachedImages extends GenericCommand {
    
    private static final Logger LOG = Logger.getLogger(LoadReportISAAttachedImages.class);
    
    private Integer rootElmt;
    
    private List<byte[]> results;
    
    private static final String[] IMAGEMIMETYPES = new String[]{
        "jpg",
        "png"
    };
    
    public static final String[] COLUMNS = new String[] { 
        "imageData"
    };
    
    public LoadReportISAAttachedImages(){
        // do nothing
    }
    
    public LoadReportISAAttachedImages(Integer root){
        this.rootElmt = root;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        results = new ArrayList<byte[]>(0);
        LoadAttachments attachmentLoader = new LoadAttachments(rootElmt);
        try {
            attachmentLoader = ServiceFactory.lookupCommandService().executeCommand(attachmentLoader);
            for(Attachment attachment : attachmentLoader.getAttachmentList()){
                if(isSupportedMIMEType(attachment.getMimeType())){
                    LoadAttachmentFile fileLoader = new LoadAttachmentFile(attachment.getDbId());
                    fileLoader = ServiceFactory.lookupCommandService().executeCommand(fileLoader);
                    results.add(fileLoader.getAttachmentFile().getFileData());
                }
            }
        } catch (CommandException e) {
            LOG.error("Error while loading attachments", e);
        }
        
    }
    
    public List<byte[]> getResult(){
        return results;
    }
    
    public List<List<String>> getDummyResult(){
        List<List<String>> result = new ArrayList<List<String>>();
        for(int i = 0; i < results.size(); i++){
            ArrayList<String> nList = new ArrayList<String>(0);
            nList.add(String.valueOf(i));
            result.add(nList);
        }
        return result;
    }
    
    public void setRootElmt(int root){
        this.rootElmt = new Integer(root);
    }
    
    public byte[] getResult(int resultNr){
        if(results.size() >= resultNr + 1){
            return results.get(resultNr);
        }
        else return new byte[0];
    }
    
    private boolean isSupportedMIMEType(String mimetype){
        for(String s : IMAGEMIMETYPES){
            if(s.equalsIgnoreCase(mimetype)){
               return true; 
            }
        }
        return false;
    }

}

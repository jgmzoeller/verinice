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
package sernet.verinice.oda.driver.preferences;


import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.jface.util.PropertyChangeEvent;

import java.io.File;
import java.util.logging.Level;

import sernet.verinice.oda.driver.Activator;

/**
 *
 */
public class ReportPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    private BooleanFieldEditor reportLoggingEditor;
    private ComboFieldEditor logLvlFieldEditor;
    private StringFieldEditor logFileNameEditor;
    
    private String[][] logLvlValues = new String[][]{
            new String[]{Messages.getString("ReportLogLevel.0"), Level.INFO.toString()},
            new String[]{Messages.getString("ReportLogLevel.1"), Level.WARNING.toString()},
            new String[]{Messages.getString("ReportLogLevel.2"), Level.FINEST.toString()},
            new String[]{Messages.getString("ReportLogLevel.3"), Level.SEVERE.toString()},
            new String[]{Messages.getString("ReportLogLevel.4"), Level.ALL.toString()}
    };
            
    
    public ReportPreferencePage(){
        super(GRID);
        setDescription(Messages.getString("ReportPreferencePage.0")); //$NON-NLS-1$
        
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    @Override
    public void init(IWorkbench arg0) {
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
     */
    @Override
    protected void createFieldEditors() {
        
        reportLoggingEditor = new BooleanFieldEditor(PreferenceConstants.REPORT_LOGGING_ENABLED, Messages.getString("ReportPreferencePage.1"), getFieldEditorParent());
        addField(reportLoggingEditor);
        
        logLvlFieldEditor = new ComboFieldEditor(PreferenceConstants.REPORT_LOGGING_LVL, Messages.getString("ReportPreferencePage.2"), logLvlValues, getFieldEditorParent());
        addField(logLvlFieldEditor);
        
        logFileNameEditor = new StringFieldEditor(PreferenceConstants.REPORT_LOG_FILE, Messages.getString("ReportPreferencePage.3"), getFieldEditorParent());
        addField(logFileNameEditor);
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        super.propertyChange(event);
        if (event.getProperty().equals(FieldEditor.VALUE)) {
            checkState();
        }
    }

    @Override
    protected void checkState() {
        super.checkState();
        if (!isValid()) {
            return;
        }

    }
    
}